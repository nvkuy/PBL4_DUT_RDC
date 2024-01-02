
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;


public class DetailComputer extends JFrame implements ActionListener {
    private JLabel lb1, lb2, lb3, lb4, lb5, lb6;
    private JButton btnRemote, btnHistory, btnBack, btnNoti;
    private JPanel pn;
    private String comp = "";
    private String state = "";
    private ClientAdmin client;
    private EmployeeComputer computer;

    public DetailComputer(ClientAdmin client, String name, String state) throws Exception {
        super("Detail computer");
        computer = new EmployeeComputer();
        this.comp = name;
        this.state = state;
        this.client = client;
        GetData();
    }

    public void GetData() throws Exception {
        client.writeMes("/CompInfo");
        client.writeMes("/Read");
        client.writeMes(comp);
        computer.setCompID(client.readMes());
        computer.setEmployeeID(client.readMes());
        computer.setEmployeeName(client.readMes());
        computer.setMail(client.readMes());
        computer.setCompress(client.readCompressMes());

        GUI();
    }

    public void GUI() throws Exception {
        setDefaultCloseOperation(3);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        setSize(1500, 900);

        lb1 = new JLabel(new String("COMPUTER " + computer.getCompID() + " (" + state + ")"));
        lb1.setForeground(Color.BLACK);
        lb1.setFont(new Font("Arial", Font.BOLD, 30));

        lb2 = new JLabel("Employee ID: " + computer.getEmployeeID());
        lb2.setForeground(Color.BLACK);
        lb2.setFont(new Font("Arial", Font.PLAIN, 24));

        lb3 = new JLabel("Name: " + computer.getEmployeeName());
        lb3.setForeground(Color.BLACK);
        lb3.setFont(new Font("Arial", Font.PLAIN, 24));

        lb4 = new JLabel("Mail: " + computer.getMail());
        lb4.setForeground(Color.BLACK);
        lb4.setFont(new Font("Arial", Font.PLAIN, 24));

        lb5 = new JLabel("Computer ID: " + computer.getCompID());
        lb5.setForeground(Color.BLACK);
        lb5.setFont(new Font("Arial", Font.PLAIN, 24));

        btnRemote = new JButton("REMOTE CONTROL");
        btnRemote.setFont(new Font("Arial", Font.BOLD, 24));
        btnRemote.setBackground(Color.blue);
        btnRemote.setForeground(Color.white);

        btnHistory = new JButton("VIEW APP HISTORY");
        btnHistory.setFont(new Font("Arial", Font.BOLD, 24));
        btnHistory.setBackground(Color.blue);
        btnHistory.setForeground(Color.white);

        btnBack = new JButton("CHOOSE ANOTHER COMPUTER");
        btnBack.setFont(new Font("Arial", Font.BOLD, 20));
        btnBack.setBackground(Color.blue);
        btnBack.setForeground(Color.white);

        btnNoti = new JButton("SEND NOTIFICATION");
        btnNoti.setFont(new Font("Arial", Font.BOLD, 24));
        btnNoti.setBackground(Color.blue);
        btnNoti.setForeground(Color.white);

        pn = new JPanel(null);
        pn.setSize(1500, 900);
        pn.setBounds(0, 0, 1500, 900);
        pn.setBackground(Color.WHITE);

        lb1.setBounds(50, 100, 600, 75);
        lb2.setBounds(50, 200, 600, 45);
        lb3.setBounds(50, 270, 600, 45);
        lb4.setBounds(50, 330, 600, 45);
        lb5.setBounds(50, 390, 800, 45);

        btnNoti.setBounds(50, 700, 300, 120);
        btnBack.setBounds(400, 700, 300, 120);
        btnRemote.setBounds(750, 700, 300, 120);
        btnHistory.setBounds(1100, 700, 300, 120);

        btnNoti.addActionListener(this);
        btnBack.addActionListener(this);
        btnRemote.addActionListener(this);
        btnHistory.addActionListener(this);

        pn.add(lb1);
        pn.add(lb2);
        pn.add(lb3);
        pn.add(lb4);
        pn.add(lb5);

        pn.add(btnBack);
        pn.add(btnRemote);
        pn.add(btnHistory);
        pn.add(btnNoti);

        if (!computer.getCompress().equals("")) {
            byte[] decodedBytes = Util.strToByte(computer.getCompress());
            ImageIcon imageIcon = new ImageIcon(decodedBytes);
            Image image = imageIcon.getImage();
            Image resizedImage = image.getScaledInstance(600, 450, Image.SCALE_SMOOTH);
            ImageIcon resizedImageIcon = new ImageIcon(resizedImage);
            lb6 = new JLabel(resizedImageIcon);
            lb6.setBounds(800, 200, 600, 450);
            pn.add(lb6);
        }

        add(pn);
        show();


    }

    public void windowClosing(WindowEvent we) {
        dispose();
        System.exit(0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnHistory) {
            try {
                new AppHistory(client, comp, state);
            } catch (Exception ex) {
                client.Shutdown();
            }
            dispose();
        }
        if (e.getSource() == btnBack) {
            new RemoteControlView(client);
            dispose();
        }
        if (e.getSource() == btnRemote) {
            if (state.equals("ONLINE")) {
                try {
                    new RemoteControlDetail(client, comp, state);
                } catch (Exception ex) {
                    client.Shutdown();
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Employee computer not online!", "Message", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        if (e.getSource() == btnNoti) {
            if (state.equals("ONLINE")) {
                try {
                    new Notification(client, comp, state);
                } catch (Exception ex) {
                    client.Shutdown();
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Employee computer not online!", "Message", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
