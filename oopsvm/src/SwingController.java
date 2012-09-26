import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

class SwingController implements ActionListener {

    private SwingInspector app;
    public SwingController(SwingInspector app){
        this.app = app;
    }

    public void actionPerformed(ActionEvent e){
        if(e.getActionCommand().equals("close")){
            app.close();
        } else if(e.getActionCommand().equals("step") && !app.canStep()) {
            app.releaseStep();
        } else if(e.getActionCommand().equals("run")) {
            app.setStepByStep(false);
            if(!app.canStep()) app.releaseStep();
        }
    }
}
