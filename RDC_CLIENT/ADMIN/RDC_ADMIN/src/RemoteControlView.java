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


    private List<String> onlineComps = new ArrayList<>();
    private List<String> offlineComps = new ArrayList<>();

    private ClientAdmin client = new ClientAdmin();

    public RemoteControlView(String s)  {
        super(s);
        try {

            client.Init();
            client.Connect();
            GetData();
            GUI();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error!");
            client.Shutdown();
        }



    }
    public void GetData(){
        try{
            String option = "/OnlineList";
            client.writeMes(option);
            int n = Integer.parseInt(client.readMes());

            onlineComps = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                String onlineComp = client.readMes();
                onlineComps.add(onlineComp);
            }

            String option1 = "/AllCompID";
            client.writeMes(option1);
            int n1 = Integer.parseInt(client.readMes());
            List<String> allComps = new ArrayList<>();
            for(int i = 0;i < n1;i++){
                String comp = client.readMes();
                allComps.add(comp);
            }

            for(int i = 0;i < n1;i++){
                int check = 0;
                for(int j = 0; j < n;j++){
                    if(allComps.get(i).equals(onlineComps.get(j))){
                        check = 1;
                        break;
                    }
                }
                if(check == 0){
                    offlineComps.add(allComps.get(i));
                }
            }
        } catch(Exception e){

        }
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


        btnList1=new ArrayList<JButton>(10);
        btnList2=new ArrayList<JButton>(10);
        pn=new JPanel(null);
        pn.setSize(1000,600);
        pn.setBounds(0,0,1000,600);
        pn.setBackground(Color.BLACK);
        for(int i=0;i<onlineComps.size();i++){
            btnList1.add(new JButton(onlineComps.get(i)));

        }
        for(int i = 0;i<offlineComps.size();i++){
            btnList2.add(new JButton(offlineComps.get(i)));
        }
        for(int i=0;i<onlineComps.size();i++){
            btnList1.get(i).setBounds(50+100*i,230,90,70);
            btnList1.get(i).setBackground(Color.GREEN);
            btnList1.get(i).addActionListener(this);
            pn.add(btnList1.get(i));
        }
        for(int i = 0;i<offlineComps.size();i++){
            btnList2.get(i).setBounds(50+100*i,430,90,70);
            btnList2.get(i).setBackground(Color.YELLOW);
            btnList2.get(i).addActionListener(this);
            pn.add(btnList2.get(i));
        }

        lb1.setBounds(100,50,400,50);
        lb2.setBounds(100,150,400,50);
        lb3.setBounds(100,350,400,50);

        pn.add(lb1);
        pn.add(lb2);
        pn.add(lb3);

        add(pn);

        show();


    }

    public void windowClosing(WindowEvent we) {
        dispose();
        System.exit(0);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        for(int i = 0;i<onlineComps.size();i++){
            if(e.getSource()==btnList1.get(i)){
                new DetailComputer("Detail Computer", btnList1.get(i).getText(),"ONLINE");
                dispose();
            }
        }
        for(int i = 0;i<offlineComps.size();i++){
            if(e.getSource()==btnList2.get(i)){
                new DetailComputer("Detail Computer", btnList2.get(i).getText(),"OFFLINE");
                dispose();
            }
        }

    }
}
