import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

class SwingView extends JFrame {

    private SwingController controller;
    private RegisterTable registerTbl;
    private WordTable progTbl;
    private JLabel helpPanel;
    private JPanel widgetPane;
    private CodeTextPane codeViewer;
    private LinkedList<SwingView.WordTable> dataPools;
    private static Color[] regColors = new Color[]{Color.orange, new Color(255,240,245),
      Color.yellow, new Color(255,211,155), Color.lightGray, Color.pink, Color.green,
      new Color(135,206,250), new Color(250,128,114)};

    public SwingView(SwingController controller, boolean expand){
        super("OOPSVM Inspector");
        this.controller = controller;
        dataPools = new LinkedList<SwingView.WordTable>();
        build(expand);
        setVisible(true);
    }

    private void build(boolean expand){
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        c.gridwidth=4;
        c.gridheight=4;
        c.gridx=c.gridy=0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = c.weighty = 1;
        JTabbedPane jtp = new JTabbedPane();
        progTbl = new WordTable();
        codeViewer = new CodeTextPane(this);
        JScrollPane cvSP = new JScrollPane(codeViewer);
        cvSP.getViewport().setBackground(Color.white);
        jtp.addTab("Code", cvSP);
        jtp.addTab("Program", new JScrollPane(progTbl));
        add(jtp, c);

        c.gridwidth=2;
        c.gridheight=1;
        c.gridx=4;
        c.gridy=4;
        c.weightx=0;
        c.weighty=0;
        c.fill=GridBagConstraints.HORIZONTAL;
        JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        if(expand){
            JButton stepBtn = new JButton("Schritt weiter");
            stepBtn.setActionCommand("step");
            stepBtn.addActionListener(controller);
            btnPane.add(stepBtn);

            JButton runBtn = new JButton("Durchlaufen");
            runBtn.setActionCommand("run");
            runBtn.addActionListener(controller);
            btnPane.add(runBtn);
        }

        JButton closeBtn = new JButton("Schließen");
        closeBtn.setActionCommand("close");
        closeBtn.addActionListener(controller);
        btnPane.add(closeBtn);


        add(btnPane, c);

        c.gridx=0;
        c.gridy=4;
        c.gridwidth=4;
        helpPanel = new JLabel();
        add(helpPanel, c);

        c.gridx=4;
        c.gridy=0;
        c.gridwidth=2;
        c.gridheight=1;
        c.fill=GridBagConstraints.BOTH;
        registerTbl = new SwingView.RegisterTable();
        JScrollPane registerTblSP = new JScrollPane(registerTbl);
        registerTblSP.setMinimumSize(new Dimension(300,40));
        add(registerTblSP, c);


        c.gridx=4;
        c.gridy=1;
        c.gridwidth=2;
        c.gridheight=3;
        c.fill=GridBagConstraints.BOTH;
        // stackTbl = new SwingView.WordTable();
        // heapTbl = new SwingView.WordTable();
        // JScrollPane stackTblSP = new JScrollPane(stackTbl);
        // JScrollPane heapTblSP = new JScrollPane(heapTbl);
        // JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, stackTblSP, heapTblSP);
        // jsp.setResizeWeight(0.5);
        widgetPane = new JPanel(new BorderLayout());
        add(new JScrollPane(widgetPane), c);

        setPreferredSize(new Dimension(800, 400));
        pack();
        setMinimumSize(new Dimension(400, 200));
    }

    public void setProgram(int[] prog, int offset){
        progTbl.setModel(new SwingModel.WordModel(prog, offset, 3));
        progTbl.getColumnModel().getColumn(1).setCellRenderer(new InstructionRenderer());
    }

    public void setCode(SwingModel.LineModel lineModel){
        codeViewer.setModel(lineModel);
    }

    public String progWord(int line, int col){
        line /= 3;
        col++;
        SwingModel.WordModel model = progTbl.getModel();
        if(line < 0 || line >= model.getRowCount() || col < 0 || col >= model.getColumnCount()){
            return "";
        } else {
            return String.valueOf(model.getValueAt(line, col));
        }
    }

    public void addDataTable(String name, int mem[], int offset){
      if(name == null || name.isEmpty()){
        name = "Memory "+offset;
      }

      SwingView.WordTable tbl = new SwingView.WordTable();
      tbl.setModel(new SwingModel.WordModel(mem, offset, 1, name));
      dataPools.add(tbl);
      addWidget(tbl);
    }

    private void addWidget(SwingView.WordTable tbl){
      JScrollPane tblS = new JScrollPane(tbl);
      int height = widgetPane.getHeight()/dataPools.size();
      int width = 50;
      if(widgetPane.getComponentCount() > 0){
        Component c = widgetPane.getComponent(0);
        widgetPane.removeAll();
        JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, c, tblS);
        // jsp.setResizeWeight(0.5);
        widgetPane.add(jsp, BorderLayout.CENTER);
      }else{
        widgetPane.add(tblS, BorderLayout.CENTER);
      }
      Container con = widgetPane;
      Component c;
      while(con.getComponentCount()>0){
        c = con.getComponent(0);
        if(c instanceof JScrollPane){
          c.setPreferredSize(new Dimension(width, height));
          break;
        } else if(c instanceof JSplitPane){
          Component c1 = ((JSplitPane)c).getTopComponent();
          Component c2 = ((JSplitPane)c).getBottomComponent();
          c1.setPreferredSize(new Dimension(width, height));
          c2.setPreferredSize(new Dimension(width, height));
          con = (Container)c1;
        } else {
          break;
        }
      }
      widgetPane.revalidate();
    }

    // public void setStack(int[] stack, int offset){
    //     stackTbl.setModel(new SwingModel.WordModel(stack, offset, 1, "Stack"));
    // }

    // public void setHeap(int[] heap, int offset){
    //     heapTbl.setModel(new SwingModel.WordModel(heap, offset, 1, "Heap"));
    // }

    public void setMemory(int idx, int value){
        progTbl.getModel().setMemory(idx, value);
        for(SwingView.WordTable t: dataPools){
          if(t.getModel().setMemory(idx, value)){
            break;
          }
        }
    }

    public void showHelp(int instr, int p1, int p2){
        String helpMsg = SwingModel.HelpDB.query(instr, p1, p2);
        helpPanel.setText("<html>"+helpMsg+"</html>");
    }

    public void setRegisters(int[] registers){
        registerTbl.getModel().setRegisters(registers);
    }

    public void setRegister(int idx, int value){
        registerTbl.getModel().setRegister(idx, value);
        if(idx == 0){
            progTbl.getModel().setMark(value, Color.lightGray);
            progTbl.scrollToRow(value);
            codeViewer.highlightLine(value);
            codeViewer.scrollToRow(value);
            int pos = progTbl.getModel().g2lRow(value);
            if(pos < progTbl.getModel().getRowCount()){
                int instr = (Integer)progTbl.getModel().getValueAt(pos, 1);
                int p1 = (Integer)progTbl.getModel().getValueAt(pos, 2);
                int p2 = (Integer)progTbl.getModel().getValueAt(pos, 3);
                showHelp(instr, p1, p2);
            }
        }
        for(SwingView.WordTable t: dataPools){
          if(t.getModel().setMark(value, regColors[idx])){
            break;
          }
        }
    }

    static class RegisterTable extends JTable {

        public RegisterTable(){
            super(new SwingModel.RegisterModel());
            setColumnSelectionAllowed(true);
            setRowSelectionAllowed(false);
        }

        public void setRegister(int index, int value){
            getModel().setRegister(index, value);
            setColumnSelectionInterval(index, index);
        }

        public SwingModel.RegisterModel getModel(){
            return (SwingModel.RegisterModel)super.getModel();
        }

        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column){
            Component c = super.prepareRenderer(renderer, row, column);
            Color color = (regColors.length > column)?regColors[column]:null;
            c.setBackground((color==null)?getBackground():color);
            return c;
        }
    }

    static class WordTable extends JTable {

        public WordTable(){
            super(new SwingModel.WordModel(new int[]{}, 0, 1));
            setRowSelectionAllowed(true);
            setColumnSelectionAllowed(false);
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            getTableHeader().setReorderingAllowed(false);
            getTableHeader().setResizingAllowed(false);
        }

        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column){
            Component c = super.prepareRenderer(renderer, row, column);
            Color color = getModel().getRowColor(row);
            c.setBackground((color==null)?getBackground():color);
            return c;
        }

        public void scrollToRow(int row){
            row = getModel().g2lRow(row);
            scrollRectToVisible(getCellRect(row, 0, true));
        }

        public SwingModel.WordModel getModel(){
            return (SwingModel.WordModel)super.getModel();
        }
    }

    static class InstructionRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            try{
                int instr = (Integer) value;
                setText(Assembler.instructions[instr]);
            }catch(Exception e){
                //bypass default renderer
            }
            return this;
        }
    }

    static class CodeTextPane extends JPanel implements Scrollable {
        static class Fragment {
            Color color;
            String info;
            TextLayout layout;
            float x, y, w, h;
        }

        static class Row {
            Fragment[] fragments;
            float x;
            float y;
            float height;
            float width;
            int lineNumber;
            TextLayout layout;
        }

        static final Color darkGreen = new Color(0,128,0);
        static final Color darkRed = new Color(128,0,0);
        static final Color[][] colors = {
            {Color.black},
            {darkRed, Color.blue, Color.blue, darkGreen},
            {darkGreen},
            {Color.magenta, darkGreen}
        };
        private SwingModel.LineModel model;
        private float tokenSpace = 10f;
        private float lineSpace = 5f;
        private ArrayList<Row> rows;
        private SwingView view;
        private int hiLine = -1;

        public CodeTextPane(SwingView view){
            super();
            this.view = view;
            setOpaque(true);
            setBackground(Color.white);
            ToolTipManager.sharedInstance().registerComponent(this);
            setFont(new Font("Courier", Font.PLAIN, 12));
        }

        public void setModel(SwingModel.LineModel model){
            this.model = model;
            rebuildCache();
            revalidate();
            repaint();
        }

        private void rebuildCache(){
            ArrayList<Row> rows = new ArrayList<Row>(); 
            Graphics2D g2 = (Graphics2D)getGraphics();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            FontRenderContext frc = g2.getFontRenderContext();
            float y = 20f;
            float maxW = 0f;
            int lineNumber =  0;
            for(SwingModel.Line line: model){
                lineNumber += line.lineCount;
            }
            float lineWidth = (float) new TextLayout(""+lineNumber, getFont(), frc).getBounds().getWidth();
            lineNumber = 0;
            for(SwingModel.Line line: model){
                Fragment[] fl = new Fragment[model.getTokenCount(line)];
                Row row = new Row();
                row.fragments = fl;

                float x = 10f + lineWidth;
                if(line instanceof SwingModel.Instruction){
                    row.lineNumber = lineNumber;
                    row.layout = new TextLayout(""+row.lineNumber, getFont(), frc);
                    row.x = x - (float)row.layout.getBounds().getWidth();
                } else {
                    row.lineNumber = -10;
                    row.x = x;
                }
                lineNumber += line.lineCount;
                rows.add(row); 

                x += tokenSpace;
                row.y = y;
                float maxH = 0f;
                for(int tok = 0; tok < model.getTokenCount(line); tok++){
                    fl[tok] = new Fragment();
                    String text = model.getText(line, tok);
                    if(text == null || text.isEmpty())text = "?";
                    fl[tok].layout = new TextLayout(text, getFont(), frc);
                    fl[tok].color = colors[line.type][Math.min(tok, colors[line.type].length-1)];
                    if(line instanceof SwingModel.Instruction && tok < 3){
                        fl[tok].info = view.progWord(row.lineNumber, tok);
                    }
                    fl[tok].x = x;
                    fl[tok].y = y;
                    fl[tok].w = (float)fl[tok].layout.getBounds().getWidth() + tokenSpace;
                    fl[tok].h = (float)fl[tok].layout.getBounds().getHeight() + lineSpace;
                    x += fl[tok].w;
                    maxH = Math.max(maxH, fl[tok].h);
                }
                row.width = x;
                row.height = maxH;
                maxW = Math.max(maxW, row.width);
                y += row.height;
            }
            setPreferredSize(new Dimension((int)maxW, (int)y));
            this.rows = rows;
        }

        public void highlightLine(int line){
            this.hiLine = line;
            repaint();
        }

        @Override
        public String getToolTipText(MouseEvent e){
            int x = e.getX();
            int y = e.getY();
            float rowy = 0;
            for(Row row: rows){
                rowy = row.y;
                if(rowy > y) {
                    for(Fragment f: row.fragments){
                        if(f.x+f.w > x){
                            return f.info;
                        }
                    }
                    return super.getToolTipText(e);
                };
            }
            return super.getToolTipText(e);
        }

        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            if(model == null || rows == null) return;

            Graphics2D g2 = (Graphics2D)g;

            for(Row row: rows){
                if(row.layout != null){
                    if(hiLine == row.lineNumber){
                        g2.setColor(SwingView.regColors[0]);
                        float h = row.layout.getAscent() + 1;
                        g2.fillRect(0, (int)(row.y - h), getWidth(), (int)row.height);
                    }
                    g.setColor(Color.black);
                    row.layout.draw(g2, row.x, row.y);
                }
                for(Fragment f: row.fragments){
                    g2.setColor(f.color);
                    f.layout.draw(g2, f.x, f.y);
                }
            }

        }

        public void scrollToRow(int targetRow){
            Rectangle r = new Rectangle(0,0,0,0);
            for(Row row: rows){
                if(row.lineNumber == targetRow){
                    r.y = (int)(row.y - (row.layout.getAscent() + 1));
                    r.width = (int)row.width;
                    r.height = (int)row.height;
                }
            }
            scrollRectToVisible(r);
        }

        public Dimension getPreferredScrollableViewportSize(){
            return getPreferredSize();
        }

        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 10;
        }

        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 20;
        }
    }
}
