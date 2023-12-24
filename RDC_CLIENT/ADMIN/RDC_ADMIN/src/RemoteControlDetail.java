import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RemoteControlDetail extends JFrame implements ActionListener {
    private JLabel lb1;
    private JButton btnBack;
    private JPanel pn;
    private String comp ="";
    private String state = "";
    private ClientAdmin client = new ClientAdmin();
    private String key;
    private String targetIP;
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
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        setSize(1000,750);

        lb1=new JLabel("COMPUTER REMOTE CONTROLLER");
        lb1.setForeground(Color.WHITE);
        lb1.setFont(new Font("Arial", Font.BOLD, 16));


        btnBack=new JButton("BACK");
        btnBack.setFont(new Font("Arial",Font.BOLD,12));
        btnBack.setBackground(Color.white);
        btnBack.setForeground(Color.black);

        pn=new JPanel(null);
        pn.setSize(1000,750);
        pn.setBounds(0,0,1000,750);
        pn.setBackground(Color.BLACK);

        controlSignalQueue = new ConcurrentLinkedQueue<>();

        screen = new ScreenDisplayer();

        remoteControlHandler = new RemoteControlHandler(key, targetIP, this, controlSignalQueue);
        Thread thread = new Thread(remoteControlHandler);
        thread.start();

        lb1.setBounds(50,10,400, 30);
        btnBack.setBounds(900,10,100,30);
        btnBack.addActionListener(this);
        pn.add(lb1);
        pn.add(btnBack);
        pn.add(screen);

        add(pn);
        setVisible(true);

    }

    public class ScreenDisplayer extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
        private BufferedImage screenFrame;
        public static final int SCREEN_WIDTH = 900;
        public static final int SCREEN_HEIGHT = 600;

        public ScreenDisplayer() {
            setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
            setBounds(50,50,SCREEN_WIDTH,SCREEN_HEIGHT);
            addKeyListener(this);
            addMouseListener(this);
            addMouseMotionListener(this);
            setFocusable(true);
            requestFocus();
        }

        @Override
        public void paint(Graphics g) {

            if (screenFrame != null){
//                screenFrame = Util.resizeImage(screenFrame, SCREEN_WIDTH, SCREEN_HEIGHT);
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

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mouseDragged(MouseEvent e) {

        }

        @Override
        public void mouseMoved(MouseEvent e) {
            controlSignalQueue.add("M M " + e.getX() + " " + e.getY());
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
