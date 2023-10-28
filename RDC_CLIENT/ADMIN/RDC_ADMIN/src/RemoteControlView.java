import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RemoteControlView extends JFrame implements ActionListener {
    private JLabel lb1,lb2,lb3;
    private JPanel pn;
    private ArrayList<JButton> btnList1, btnList2;
    private ImageIcon btnSearch,btnLogout;

    private List<String> onlineComps = new ArrayList<>();
    private List<List<String>> apps = new ArrayList<>();

    public RemoteControlView(String s, List<String> onlineComps, List<List<String>> apps)  {
        super(s);
        for(int i = 0;i<onlineComps.size();i++){
            this.onlineComps.add(onlineComps.get(i));
        }
        for (int i = 0; i < apps.size(); i++) {
            List<String> app = apps.get(i);
            this.apps.add(Arrays.asList(app.get(0), app.get(1)));
        }
        GUI();

    }

    public void GUI(){
        setDefaultCloseOperation(3);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        setSize(1000,600);

        lb1=new JLabel("DESKTOP REMOTE CONTROL");
        lb1.setForeground(Color.WHITE);
        lb1.setFont(new Font("Arial", Font.BOLD, 20));
        lb2=new JLabel("Online now (Can remote control): ");
        lb2.setForeground(Color.WHITE);
        lb2.setFont(new Font("Arial",Font.PLAIN,16));
        lb3=new JLabel("Not online (Can read last online data): ");
        lb3.setForeground(Color.WHITE);
        lb3.setFont(new Font("Arial",Font.PLAIN,16));

        Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/search.jpg"));

        Image newImage = icon.getScaledInstance(70, 70, Image.SCALE_SMOOTH);
        btnSearch = new ImageIcon(newImage);
        icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/logout.jpg"));
        newImage = icon.getScaledInstance(70, 70, Image.SCALE_SMOOTH);
        btnLogout = new ImageIcon(newImage);

        btnList1=new ArrayList<JButton>(10);
        btnList2=new ArrayList<JButton>(10);
        pn=new JPanel(null);
        pn.setSize(1000,600);
        pn.setBounds(0,0,1000,600);
        pn.setBackground(Color.BLACK);
        for(int i=0;i<onlineComps.size();i++){
            btnList1.add(new JButton(onlineComps.get(i)));
            btnList2.add(new JButton("NOT "+ i));
        }
        for(int i=0;i<onlineComps.size();i++){
            btnList1.get(i).setBounds(50+80*i,230,70,70);
            btnList1.get(i).setBackground(Color.GREEN);

            btnList2.get(i).setBounds(50+80*i,430,70,70);
            btnList2.get(i).setBackground(Color.YELLOW);

            btnList1.get(i).addActionListener(this);
            btnList2.get(i).addActionListener(this);
            pn.add(btnList1.get(i));
            pn.add(btnList2.get(i));
        }

        if (btnSearch.getIconWidth() == -1) {
            System.out.println("Error loading image: search.jpg");
        }
        if (btnLogout.getIconWidth() == -1) {
            System.out.println("Error loading image: logout.jpg");
        }

        lb1.setBounds(100,50,400,50);
        lb2.setBounds(100,150,400,50);
        lb3.setBounds(100,350,400,50);
        JLabel lbx=new JLabel();
        lbx.setIcon(btnSearch);
        JLabel lby=new JLabel();
        lby.setIcon(btnLogout);

        lbx.setBounds(900,50,70,70);
        lby.setBounds(900,480,70,70);
        pn.add(lb1);
        pn.add(lb2);
        pn.add(lb3);
        pn.add(lbx);
        pn.add(lby);

        add(pn);

        show();


    }

    public void windowClosing(WindowEvent we) {
        dispose();
        System.exit(0);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==btnList1.get(0)){
            DetailComputer detail = new DetailComputer("Detail Computer", apps, btnList1.get(0).getText());

        }
    }
}
