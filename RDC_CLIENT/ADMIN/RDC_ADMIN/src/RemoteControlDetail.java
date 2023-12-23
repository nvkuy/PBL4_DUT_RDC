import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RemoteControlDetail extends JFrame implements ActionListener {
    private JLabel lb1;
    private JButton btnBack;
    private JPanel pn;
    private String comp ="";
    private String state = "";
    private ClientAdmin client = new ClientAdmin();
    private String key = "";
    private String targetIP = "";
    public ScreenDisplayer screen;
    private Queue<String> controlSignalQueue;
    private RemoteControlHandler remoteControlHandler;

    public RemoteControlDetail(String s, String name, String state)  {
        super(s);
        this.comp=name;
        this.state = state;
        try {
            client.Init();
            client.Connect();
            GetData();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error!");
            client.Shutdown();
        }
    }
    public void GetData() throws Exception {
        client.writeMes("/RemoteControl");
        client.writeMes(comp);
        client.writeMes(String.valueOf(ScreenDisplayer.SCREEN_WIDTH));
        client.writeMes(String.valueOf(ScreenDisplayer.SCREEN_HEIGHT));
        key = client.readMes();
        targetIP = client.readMes();

        GUI();
    }
    public void GUI() {
        setDefaultCloseOperation(3);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        setSize(1500,1000);

        lb1=new JLabel("COMPUTER REMOTE CONTROLLER");
        lb1.setForeground(Color.WHITE);
        lb1.setFont(new Font("Arial", Font.BOLD, 20));


        btnBack=new JButton("BACK");
        btnBack.setFont(new Font("Arial",Font.BOLD,16));
        btnBack.setBackground(Color.white);
        btnBack.setForeground(Color.black);

        pn=new JPanel(null);
        pn.setSize(1500,1000);
        pn.setBounds(0,0,1500,1000);
        pn.setBackground(Color.BLACK);

        controlSignalQueue = new ArrayDeque<>();

        screen = new ScreenDisplayer();
        remoteControlHandler = new RemoteControlHandler(key, targetIP, this, controlSignalQueue);
        Thread thread = new Thread(remoteControlHandler);
        thread.start();

        lb1.setBounds(50,50,400, 50);
        btnBack.setBounds(1250,50,200,60);
        btnBack.addActionListener(this);
        pn.add(lb1);
        pn.add(btnBack);
        pn.add(screen);

        add(pn);
        show();


    }

    public class ScreenDisplayer extends JPanel implements KeyListener, MouseListener {
        private BufferedImage screenFrame;
        public static final int SCREEN_WIDTH = 1200;
        public static final int SCREEN_HEIGHT = 750;

        public ScreenDisplayer() {
            setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
            setBounds(100,120,SCREEN_WIDTH,SCREEN_HEIGHT);
            addKeyListener(this);
            addMouseListener(this);
        }

        @Override
        public void paint(Graphics g) {

            if (screenFrame != null){
                screenFrame = Util.resizeImage(screenFrame, SCREEN_WIDTH, SCREEN_HEIGHT);
                g.drawImage(screenFrame, 0, 0, null);
            }


        }

        public void display(BufferedImage screenFrame) {
            this.screenFrame = screenFrame;
            repaint();
        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            controlSignalQueue.add("K P " + e.getKeyCode());
        }

        @Override
        public void keyReleased(KeyEvent e) {
            controlSignalQueue.add("K R " + e.getKeyCode());
        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            controlSignalQueue.add("M P " + e.getButton());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            controlSignalQueue.add("M R " + e.getButton());
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            controlSignalQueue.add("M M " + e.getX() + " " + e.getY());
        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    public void windowClosing(WindowEvent we) {
        dispose();
        System.exit(0);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==btnBack){
            new DetailComputer("Detail computer", comp, state);
            remoteControlHandler.shutdown();
            dispose();
        }

    }
}
