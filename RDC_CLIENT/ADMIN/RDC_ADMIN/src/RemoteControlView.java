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
    private JPanel pn,pnList1,pnList2;
    private ArrayList<JButton> btnList1, btnList2;


    private List<String> onlineComps;
    private List<String> offlineComps;
    private Thread dataThread;

    private ClientAdmin client = new ClientAdmin();
    private Boolean isRunning = true;

    public RemoteControlView(String s)  {
        super(s);
        try {

            client.Init();
            client.Connect();
            GUI();
//            GetData();
//            GUI2();
            dataThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try{

                        while(isRunning){
                            GetData();
                            GUI2();
                            Thread.sleep(5000);
                        }
                    } catch(Exception p){
                        p.printStackTrace();
                    }

                }
                public void isStop(){
                    isRunning = false;
                }
            });
            dataThread.start();


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
            offlineComps = new ArrayList<>();
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



        pn=new JPanel(null);
        pn.setSize(1000,600);
        pn.setBounds(0,0,1000,600);
        pn.setBackground(Color.BLACK);


        lb1.setBounds(100,50,400,50);
        lb2.setBounds(100,150,400,50);
        lb3.setBounds(100,350,400,50);

        pn.add(lb1);
        pn.add(lb2);
        pn.add(lb3);

        add(pn);

        setVisible(true);


    }
    public void GUI2() {
        if(isPanelInContainer(pnList1)){
            for(int i = 0;i < btnList1.size();i++){
                remove(btnList1.get(i));
            }
            remove(pnList1);
        }
        if(isPanelInContainer(pnList2)){
            for(int i = 0;i < btnList1.size();i++){
                remove(btnList2.get(i));
            }
            remove(pnList2);
        }

        btnList1 = new ArrayList<>(10);
        btnList2 = new ArrayList<>(10);
        pnList1 = new JPanel(null);
        pnList1.setSize(900,70);
        pnList1.setBounds(50, 230, 900, 70);
        pnList1.setBackground(Color.BLUE);
        pnList2 = new JPanel(null);
        pnList2.setSize(900,70);
        pnList2.setBounds(50, 430, 900, 70);
        pnList2.setBackground(Color.GREEN);

        for (int i = 0; i < onlineComps.size(); i++) {
            JButton btn = new JButton(onlineComps.get(i));
            btnList1.add(btn);
            btn.setBounds(100 * i, 0, 90, 70);
            btn.setBackground(Color.GREEN);
            btn.addActionListener(this);
            pnList1.add(btnList1.get(i));
        }

        for (int i = 0; i < offlineComps.size(); i++) {
            JButton btn = new JButton(offlineComps.get(i));
            btnList2.add(btn);
            btn.setBounds(100 * i, 0, 90, 70);
            btn.setBackground(Color.YELLOW);
            btn.addActionListener(this);
            pnList2.add(btnList2.get(i));
        }
        add(pnList1);
        add(pnList2);
        revalidate();
        repaint();
        setVisible(true);
    }
    private boolean isPanelInContainer(JPanel panel) {
        Component[] components = getContentPane().getComponents();
        for (Component component : components) {
            if (component.equals(panel)) {
                return true;
            }
        }
        return false;
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
                isRunning = false;
            }
        }
        for(int i = 0;i<offlineComps.size();i++){
            if(e.getSource()==btnList2.get(i)){
                new DetailComputer("Detail Computer", btnList2.get(i).getText(),"OFFLINE");
                dispose();
                isRunning = false;
            }
        }

    }
}

