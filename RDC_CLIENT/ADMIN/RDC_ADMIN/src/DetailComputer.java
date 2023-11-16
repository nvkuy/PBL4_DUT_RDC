
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;



public class DetailComputer extends JFrame implements ActionListener {
    private JLabel lb1,lb2,lb3,lb4,lb5,lb6;
    private JButton btnRemote, btnHistory,btnBack;
    private JPanel pn;
    private String comp ="";
    private String state = "";
    private ClientAdmin client = new ClientAdmin();
    private EmployeeComputer computer = new EmployeeComputer();

    public DetailComputer(String s, String name, String state)  {
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
        client.writeMes("/CompInfo");
        client.writeMes("/Read");
        client.writeMes(comp);
        computer.setCompID(client.readMes());
        computer.setEmployeeID(client.readMes());
        computer.setEmployeeName(client.readMes());
        computer.setMail(client.readMes());
        computer.setCompress(client.readCompressMes());
        System.out.println(computer.getCompress());
        GUI();
    }
    public void GUI() throws Exception{
        setDefaultCloseOperation(3);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        setSize(1000,600);

        lb1=new JLabel(new String("COMPUTER " + computer.getCompID()+" ("+state+")"));
        lb1.setForeground(Color.WHITE);
        lb1.setFont(new Font("Arial", Font.BOLD, 20));

        lb2=new JLabel("Employee ID: "+computer.getEmployeeID());
        lb2.setForeground(Color.WHITE);
        lb2.setFont(new Font("Arial",Font.PLAIN,16));

        lb3=new JLabel("Name: "+computer.getEmployeeName());
        lb3.setForeground(Color.WHITE);
        lb3.setFont(new Font("Arial",Font.PLAIN,16));

        lb4=new JLabel("Mail: "+computer.getMail());
        lb4.setForeground(Color.WHITE);
        lb4.setFont(new Font("Arial",Font.PLAIN,16));

        lb5=new JLabel("Computer ID: "+ computer.getCompID());
        lb5.setForeground(Color.WHITE);
        lb5.setFont(new Font("Arial",Font.PLAIN,16));

        btnRemote=new JButton("REMOTE CONTROL");
        btnRemote.setFont(new Font("Arial",Font.BOLD,16));
        btnRemote.setBackground(Color.white);
        btnRemote.setForeground(Color.black);

        btnHistory=new JButton("VIEW APP HISTORY");
        btnHistory.setFont(new Font("Arial",Font.BOLD,16));
        btnHistory.setBackground(Color.white);
        btnHistory.setForeground(Color.black);

        btnBack=new JButton("CHOOSE ANOTHER COMPUTER");
        btnBack.setFont(new Font("Arial",Font.BOLD,16));
        btnBack.setBackground(Color.white);
        btnBack.setForeground(Color.black);

        pn=new JPanel(null);
        pn.setSize(1000,600);
        pn.setBounds(0,0,1000,600);
        pn.setBackground(Color.BLACK);

        lb1.setBounds(50,50,400, 50);
        lb2.setBounds(50, 120, 400, 30);
        lb3.setBounds(50,150,400,30);
        lb4.setBounds(50,180,400,30);
        lb5.setBounds(50,250,400,30);


        btnBack.setBounds(230,500,300,60);
        btnRemote.setBounds(550,500,200,60);
        btnHistory.setBounds(770,500,200,60);
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

        if(!computer.getCompress().equals("")){
            byte[] decodedBytes = AES.decode(computer.getCompress());
            ImageIcon imageIcon = new ImageIcon(decodedBytes);
            Image image = imageIcon.getImage();
            Image resizedImage = image.getScaledInstance(400, 300, Image.SCALE_SMOOTH);
            ImageIcon resizedImageIcon = new ImageIcon(resizedImage);
            lb6 = new JLabel(resizedImageIcon);
            lb6.setBounds(550,150,400,300);
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
        if(e.getSource()==btnHistory){
            new AppHistory("AppHistory",comp,state);
            dispose();
        }
        if(e.getSource()==btnBack){
            new RemoteControlView("Remote control");
            dispose();
        }
        if(e.getSource()==btnRemote){
            if(state.equals("ONLINE")){
                new RemoteControlDetail("Computer remote", comp, state);
                dispose();
            } else{
                JOptionPane.showMessageDialog(this, "Hiện tại máy tính đang chưa hoạt động", "Message",1);
            }
        }
    }
}
