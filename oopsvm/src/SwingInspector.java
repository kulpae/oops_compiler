import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;


class SwingInspector implements Inspector {

    private boolean stepByStep;
    private Semaphore stepLock;
    private SwingController controller;
    private SwingView view;
    private int offset;
    private int[] memory;

    public SwingInspector(boolean stepByStep){
        controller = new SwingController(this);
        view = new SwingView(controller, stepByStep);
        setStepByStep(stepByStep);
        stepLock = new Semaphore(1);
        nextStep();
    }

    public void close(){
        view.dispose();
        //wenn es bisher Schritt fuer Schritt ablief, 
        //so lasse das Programm nun ungestoert durchlaufen
        setStepByStep(false);
        if(!canStep()){
            //  Schritt-Blockade aufheben
            releaseStep();
        }
    }

    @Override
    public void readCode(String fileName){
        InputStreamReader reader=null;
        try {
            FileInputStream stream = new FileInputStream(fileName);
            reader = new InputStreamReader(stream);
            int c;
            StringBuffer buffer = new StringBuffer();
            SwingModel.LineModel lines = new SwingModel.LineModel();
            String label = null;
            int datSize = 0;
            while((c=reader.read())!=-1){
                if(c == '\n'){
                    SwingModel.Line line = SwingModel.parseCodeLine(buffer.toString());

                    if(!(line instanceof SwingModel.DatInstruction) && datSize > 0){
                      int[] mempart = new int[datSize];
                      System.arraycopy(this.memory, offset, mempart, 0, mempart.length);
                      view.addDataTable(label, mempart, offset);
                      offset += datSize;
                      datSize = 0;
                      label = null;
                    }

                    if(line instanceof SwingModel.DatInstruction){
                      SwingModel.DatInstruction dat = (SwingModel.DatInstruction) line;
                      if(label != null){
                        datSize += dat.size;
                      }
                    } else if(line instanceof SwingModel.Label){
                      label = line.tokens[0];
                    } else {
                      if(line instanceof SwingModel.Instruction){
                        offset += 3;
                      }
                      label = null;
                    }
                    lines.add(line);
                    buffer = new StringBuffer();
                } else {
                    buffer.append((char)c);
                }
            }
            view.setCode(lines);
        } catch(IOException e){
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            }catch(Exception e){}
        }

    }

    @Override
    public void sendRegister(final int index, final int value){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                view.setRegister(index, value);
            }
        });
    }

    @Override
    public void setupMemory(final int[] memory){
      this.memory = memory;
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                int[] prog = new int[memory.length];

                System.arraycopy(memory, 0, prog, 0, prog.length);
                // System.arraycopy(memory, stackPtr, stack, 0, stack.length);
                // System.arraycopy(memory, heapPtr, heap, 0, heap.length);

                view.setProgram(prog, 0);
                // view.setStack(stack, stackPtr);
                // view.setHeap(heap, heapPtr);
            }
        });
    }

    @Override
    public void setupRegisters(final int[] registers){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                view.setRegisters(registers);
            }
        });
    }

    @Override
    public void sendMemory(final int index, final int value){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                view.setMemory(index, value);
            }
        });
    }

    @Override
    public void sendException(Exception e){

    }

    public boolean canStep(){
        return stepLock.availablePermits() != 0;
    }

    public void releaseStep(){
        stepLock.release();
    }

    @Override
    public void nextStep(){
        try {
            if(stepByStep)stepLock.acquire();
        } catch (InterruptedException e){
            System.err.println(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void setStepByStep(boolean val){
        stepByStep = val;
    }

}
