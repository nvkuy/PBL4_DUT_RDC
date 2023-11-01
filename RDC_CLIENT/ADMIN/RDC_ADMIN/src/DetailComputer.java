import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetailComputer extends JFrame implements ActionListener {
    private JLabel lb1,lb2,lb3,lb4,lb5,lb6;
    private JButton btnRemote, btnHistory,btnBack;
    private JPanel pn;
    private String comp ="";
    private String state = "";
    private ClientAdmin client = new ClientAdmin();

    public DetailComputer(String s, String name, String state)  {
        super(s);
        this.comp=name;
        try {
            client.Init();
            client.Connect();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error!");
            client.Shutdown();
        }

        this.state = state;
        GUI();


    }
    public void GUI(){
        setDefaultCloseOperation(3);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        setSize(1000,600);

        lb1=new JLabel(new String("COMPUTER " + comp+" ("+state+")"));
        lb1.setForeground(Color.WHITE);
        lb1.setFont(new Font("Arial", Font.BOLD, 20));

        lb2=new JLabel("Employee ID: ");
        lb2.setForeground(Color.WHITE);
        lb2.setFont(new Font("Arial",Font.PLAIN,16));

        lb3=new JLabel("Name: ");
        lb3.setForeground(Color.WHITE);
        lb3.setFont(new Font("Arial",Font.PLAIN,16));

        lb4=new JLabel("Mail: ");
        lb4.setForeground(Color.WHITE);
        lb4.setFont(new Font("Arial",Font.PLAIN,16));

        lb5=new JLabel("Computer ID: "+ comp);
        lb5.setForeground(Color.WHITE);
        lb5.setFont(new Font("Arial",Font.PLAIN,16));

        lb6=new JLabel("IP ADDRESS: ");
        lb6.setForeground(Color.WHITE);
        lb6.setFont(new Font("Arial",Font.PLAIN,16));

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

        lb1.setBounds(50,50,400, 50);
        lb2.setBounds(50, 120, 400, 30);
        lb3.setBounds(50,150,400,30);
        lb4.setBounds(50,180,400,30);
        lb5.setBounds(50,250,400,30);
        lb6.setBounds(50,280,400,30);

        btnBack.setBounds(330,500,200,60);
        btnRemote.setBounds(550,500,200,60);
        btnHistory.setBounds(770,500,200,60);
        btnBack.addActionListener(this);
        btnRemote.addActionListener(this);
        btnHistory.addActionListener(this);
        pn=new JPanel(null);
        pn.setSize(1000,600);
        pn.setBounds(0,0,1000,600);
        pn.setBackground(Color.BLACK);
        pn.add(lb1);
        pn.add(lb2);
        pn.add(lb3);
        pn.add(lb4);
        pn.add(lb5);
        pn.add(lb6);
        pn.add(btnBack);
        pn.add(btnRemote);
        pn.add(btnHistory);

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

        }
    }
}
