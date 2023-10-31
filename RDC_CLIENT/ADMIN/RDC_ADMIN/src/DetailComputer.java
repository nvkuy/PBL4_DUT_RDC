import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetailComputer extends JFrame implements ActionListener {
    private JLabel lb1,lb2,lb3,lb4,lb5,lb6,lb7;
    private JButton btnRemote, btnHistory;
    private JPanel pn;
    private String comp ="";
    private String state = "";
    private List<List<String>> apps = new ArrayList<>();

    public DetailComputer(String s, List<List<String>> apps, String name)  {
        super(s);
        for (int i = 0; i < apps.size(); i++) {
            List<String> app = apps.get(i);
            this.apps.add(Arrays.asList(app.get(0), app.get(1)));
        }
        comp=name;
        state="ONLINE";
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

        lb5=new JLabel("Computer ID: "+comp);
        lb5.setForeground(Color.WHITE);
        lb5.setFont(new Font("Arial",Font.PLAIN,16));

        lb6=new JLabel("IP ADDRESS: ");
        lb6.setForeground(Color.WHITE);
        lb6.setFont(new Font("Arial",Font.PLAIN,16));

        lb7=new JLabel("CPU: |RAM: |DISK: ");
        lb7.setForeground(Color.WHITE);
        lb7.setFont(new Font("Arial",Font.PLAIN,16));

        btnRemote=new JButton("REMOTE CONTROL");
        btnRemote.setFont(new Font("Arial",Font.BOLD,16));
        btnRemote.setBackground(Color.white);
        btnRemote.setForeground(Color.black);

        btnHistory=new JButton("VIEW APP HISTORY");
        btnHistory.setFont(new Font("Arial",Font.BOLD,16));
        btnHistory.setBackground(Color.white);
        btnHistory.setForeground(Color.black);

        lb1.setBounds(50,50,400, 50);
        lb2.setBounds(50, 120, 400, 30);
        lb3.setBounds(50,150,400,30);
        lb4.setBounds(50,180,400,30);
        lb5.setBounds(50,250,400,30);
        lb6.setBounds(50,280,400,30);
        lb7.setBounds(50,310,400,30);

        btnRemote.setBounds(550,500,200,60);
        btnHistory.setBounds(770,500,200,60);
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
        pn.add(lb7);
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
            AppHistory history = new AppHistory("AppHistory", apps);
        }
    }
}
