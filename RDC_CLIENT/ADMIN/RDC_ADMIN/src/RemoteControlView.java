import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class RemoteControlView extends JFrame implements ActionListener {
    private JLabel lb1, lb2, lb3;
    private JPanel pn, pnList1, pnList2;
    private ArrayList<JButton> btnList1, btnList2;


    private List<String> onlineComps;
    private List<String> offlineComps;
    private List<Integer> onlineNotAllow;
    private List<Integer> offlineNotAllow;
    private List<String> notAllowApps;
    private Thread dataThread;

    private ClientAdmin client;
    private Boolean isRunning = true;

    public RemoteControlView(ClientAdmin client) {
        super("Remote control");
        this.client = client;
        GUI();
        dataThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    while (isRunning) {
                        GetData();
                        GUI2();
                        Thread.sleep(5000);
                    }
                } catch (Exception p) {
                    p.printStackTrace();
                }

            }

            public void isStop() {
                isRunning = false;
            }
        });
        dataThread.start();


    }

    public void GetData() {
        try {
            String option2 = "/NotAllowApp";
            notAllowApps = new ArrayList<>();
            client.writeMes(option2);
            int n0 = Integer.parseInt(client.readMes());
            for (int i = 0; i < n0; i++) {
                String appName = client.readMes();
                notAllowApps.add(appName);
            }
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
            for (int i = 0; i < n1; i++) {
                String comp = client.readMes();
                allComps.add(comp);
            }

            for (int i = 0; i < n1; i++) {
                int check = 0;
                for (int j = 0; j < n; j++) {
                    if (allComps.get(i).equals(onlineComps.get(j))) {
                        check = 1;
                        break;
                    }
                }
                if (check == 0) {
                    offlineComps.add(allComps.get(i));
                }
            }
            GetData1();
        } catch (Exception e) {

        }
    }
    public void GetData1() throws Exception{
        onlineNotAllow = new ArrayList<>();
        offlineNotAllow = new ArrayList<>();
        for(int i = 0;i<onlineComps.size();i++){
            String option1 = "/AppHistory";
            client.writeMes(option1);
            client.writeMes(onlineComps.get(i));
            int n1 = Integer.parseInt(client.readMes());
            List<List<String>> apps = new ArrayList<>();
            for (int j = 0; j < n1; j++) {
                String appName = client.readMes();
                String timeID = client.readMes();
                apps.add(Arrays.asList(appName, timeID));

            }
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
            Date currentDate = new Date();
            String formattedDate = sdf.format(currentDate);
            int count = 0;
            for(int j = 0;j < n1;j++){
                if(formattedDate.equals(apps.get(j).get(1))){
                    for(int k = 0;k<notAllowApps.size();k++){
                        if(apps.get(j).get(0).equals(notAllowApps.get(k))){
                            count++;
                            break;
                        }
                    }
                }
            }
            onlineNotAllow.add(count);
        }
        for(int i = 0;i<offlineComps.size();i++){
            String option1 = "/AppHistory";
            client.writeMes(option1);
            client.writeMes(offlineComps.get(i));
            int n1 = Integer.parseInt(client.readMes());
            List<List<String>> apps = new ArrayList<>();
            for (int j = 0; j < n1; j++) {
                String appName = client.readMes();
                String timeID = client.readMes();
                apps.add(Arrays.asList(appName, timeID));

            }
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
            Date currentDate = new Date();
            String formattedDate = sdf.format(currentDate);
            int count = 0;
            for(int j = 0;j < n1;j++){
                if(formattedDate.equals(apps.get(j).get(1))){
                    for(int k = 0;k<notAllowApps.size();k++){
                        if(apps.get(j).get(0).equals(notAllowApps.get(k))){
                            count++;
                            break;
                        }
                    }
                }
            }
            offlineNotAllow.add(count);
        }
    }

    public void GUI() {
        setDefaultCloseOperation(3);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        setSize(2000, 1200);

        lb1 = new JLabel("DESKTOP REMOTE CONTROL");
        lb1.setForeground(Color.BLACK);
        lb1.setFont(new Font("Arial", Font.BOLD, 40));
        lb2 = new JLabel("Online now (Can remote control): ");
        lb2.setForeground(Color.BLACK);
        lb2.setFont(new Font("Arial", Font.PLAIN, 32));
        lb3 = new JLabel("Not online (Can read last online data): ");
        lb3.setForeground(Color.BLACK);
        lb3.setFont(new Font("Arial", Font.PLAIN, 32));

        pnList1 = new JPanel(null);
        pnList1.setSize(1800, 140);
        pnList1.setBounds(50, 460, 1800, 140);
        pnList1.setBackground(Color.WHITE);
        pnList2 = new JPanel(null);
        pnList2.setSize(1800, 140);
        pnList2.setBounds(50, 860, 1800, 140);
        pnList2.setBackground(Color.WHITE);


        pn = new JPanel(null);
        pn.setSize(2000, 1200);
        pn.setBounds(0, 0, 2000, 1200);
        pn.setBackground(Color.WHITE);


        lb1.setBounds(100, 100, 800, 100);
        lb2.setBounds(100, 300, 800, 100);
        lb3.setBounds(100, 700, 800, 100);

        pn.add(lb1);
        pn.add(lb2);
        pn.add(lb3);
        pn.add(pnList1);
        pn.add(pnList2);
        add(pn);

        setVisible(true);


    }

    public void GUI2() {
        pnList1.removeAll();
        pnList2.removeAll();


        btnList1 = new ArrayList<>(10);
        btnList2 = new ArrayList<>(10);
        Font customFont = new Font("Arial", Font.PLAIN, 24);

        for (int i = 0; i < onlineComps.size(); i++) {
            JButton btn = new JButton("<html><center><b>"+onlineComps.get(i) + "<br>" + onlineNotAllow.get(i) +"</center></html>");
            btn.setFont(customFont);
            btnList1.add(btn);
            btn.setBounds(200 * i, 0, 180, 140);
            btn.setBackground(Color.GREEN);
            if(onlineNotAllow.get(i) > 0) {
                btn.setBackground(Color.RED);
                btn.setForeground(Color.WHITE);
            }
            btn.addActionListener(this);
            pnList1.add(btnList1.get(i));
        }

        for (int i = 0; i < offlineComps.size(); i++) {
            JButton btn = new JButton("<html><center><b>"+offlineComps.get(i) + "<br>" + offlineNotAllow.get(i) +"</center></html>");
            btn.setFont(customFont);
            btnList2.add(btn);
            btn.setBounds(200 * i, 0, 180, 140);
            btn.setBackground(Color.YELLOW);
            if(offlineNotAllow.get(i) > 0) {
                btn.setBackground(Color.RED);
                btn.setForeground(Color.WHITE);
            }
            btn.addActionListener(this);
            pnList2.add(btnList2.get(i));
        }
        pnList1.revalidate();
        pnList2.revalidate();
        pnList1.repaint();
        pnList2.repaint();

    }

    public void paint(Graphics g) {
        super.paint(g);
    }


    public void windowClosing(WindowEvent we) {
        dispose();
        System.exit(0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < onlineComps.size(); i++) {
            if (e.getSource() == btnList1.get(i)) {
                try {
                    new DetailComputer(client, onlineComps.get(i), "ONLINE");
                } catch (Exception ex) {
                    client.Shutdown();
                }
                dispose();
                isRunning = false;
            }
        }
        for (int i = 0; i < offlineComps.size(); i++) {
            if (e.getSource() == btnList2.get(i)) {
                try {
                    new DetailComputer(client, offlineComps.get(i), "OFFLINE");
                } catch (Exception ex) {
                    client.Shutdown();
                }
                dispose();
                isRunning = false;
            }
        }

    }
}


