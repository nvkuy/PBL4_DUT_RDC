import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RemoteControlDetail extends JFrame implements ActionListener {
    private JLabel lb1;
    private JButton btnBack;
    private JPanel pn;
    public String comp = "";
    public String state = "";
    private ClientAdmin client;
    private String key;
    private String targetIP;
    public ScreenDisplayer screen;
    private BlockingQueue<String> controlSignalQueue;
    private RemoteControlHandler remoteControlHandler;

    private int viewport_width;
    private int viewport_height;

    public RemoteControlDetail(ClientAdmin client, String name, String state) throws Exception {
        super("Remote control detail");
        this.comp = name;
        this.state = state;
        this.client = client;

        GetData();
        GUI();
    }

    public void GetData() throws Exception {
        client.writeMes("/RemoteControl");
        client.writeMes(comp);
        key = client.readMes();
        targetIP = client.readMes();

        viewport_width = Integer.parseInt(client.readMes());
        viewport_height = Integer.parseInt(client.readMes());
    }

    public void GUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        setSize(1000, 750);

        lb1 = new JLabel("COMPUTER REMOTE CONTROLLER");
        lb1.setForeground(Color.WHITE);
        lb1.setFont(new Font("Arial", Font.BOLD, 16));


        btnBack = new JButton("BACK");
        btnBack.setFont(new Font("Arial", Font.BOLD, 12));
        btnBack.setBackground(Color.white);
        btnBack.setForeground(Color.black);

        pn = new JPanel(null);
        pn.setSize(1000, 750);
        pn.setBounds(0, 0, 1000, 750);
        pn.setBackground(Color.BLACK);

        controlSignalQueue = new LinkedBlockingQueue<>();

        remoteControlHandler = new RemoteControlHandler(key, targetIP, this, controlSignalQueue);
        Thread thread = new Thread(remoteControlHandler);
        thread.start();

        lb1.setBounds(50, 10, 400, 30);
        btnBack.setBounds(900, 10, 100, 30);
        btnBack.addActionListener(this);
        screen = new ScreenDisplayer();
        pn.add(lb1);
        pn.add(btnBack);
        pn.add(screen);

        add(pn);
        setVisible(true);

        screen.addKeyListener(screen);
        screen.addMouseListener(screen);
        screen.addMouseMotionListener(screen);
        screen.setFocusable(true);
        screen.setRequestFocusEnabled(true);
        screen.requestFocus();

    }

    public class ScreenDisplayer extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
        private BufferedImage screenFrame;

        public static final int MAX_WIDTH = 900;
        public static final int MAX_HEIGHT = 600;

        public ScreenDisplayer() {
            setSize(viewport_width, viewport_height);
            setBounds(50, 50, viewport_width, viewport_height);
        }

        @Override
        public void paint(Graphics g) {

            if (screenFrame != null) {
                if (screenFrame.getWidth() < viewport_width)
                    screenFrame = Util.resizeImage(screenFrame, viewport_width, viewport_height);
//                System.out.println(screenFrame.getWidth());
//                System.out.println(screenFrame.getHeight());
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
            try {
                controlSignalQueue.put("K P " + e.getKeyCode());
            } catch (Exception ignored) {
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            try {
                controlSignalQueue.put("K R " + e.getKeyCode());
            } catch (Exception ignored) {
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            try {
                controlSignalQueue.put("M P " + e.getButton());
            } catch (Exception ignored) {
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            try {
                controlSignalQueue.put("M R " + e.getButton());
            } catch (Exception ignored) {
            }
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
            try {
                controlSignalQueue.put("M M " + e.getX() + " " + e.getY());
            } catch (Exception ignored) {
            }
        }
    }

    public void windowClosing(WindowEvent we) {
        dispose();
        System.exit(0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnBack) {
            try {
                new DetailComputer(client, comp, state);
            } catch (Exception ex) {
                client.Shutdown();
            }
            remoteControlHandler.shutdown();
            dispose();
        }

    }
}
