import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Notification  extends JFrame implements ActionListener {
    private String comp = "";
    private String state = "";
    private ClientAdmin client;
    private Integer notAllowNum = 0;
    private JLabel lb1;
    private JPanel pn;
    private JTextArea textArea;
    private JButton btnWarnning, btnInfor, btnReset, btnBack;


    public Notification(ClientAdmin client, String name, String state){
        super("Notification");
        this.client = client;
        this.comp = name;
        this.state = state;
        GetData();
        GUI();
    }
    public void GetData(){
        try{
            String option2 = "/NotAllowApp";
            List<String> notAllowApps = new ArrayList<>();
            client.writeMes(option2);
            int n0 = Integer.parseInt(client.readMes());
            for (int i = 0; i < n0; i++) {
                String appName = client.readMes();
                notAllowApps.add(appName);
            }
            String option1 = "/AppHistory";
            client.writeMes(option1);
            client.writeMes(comp);
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
            notAllowNum = count;

        } catch(Exception e){

        }
    }
    public void GUI(){
        setDefaultCloseOperation(3);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        setSize(1500, 900);

        lb1 = new JLabel(new String("SEND NOTIFICATION TO EMPLOYEE"));
        lb1.setForeground(Color.BLACK);
        lb1.setFont(new Font("Arial", Font.BOLD, 32));

        textArea = new JTextArea();
        textArea.setColumns(40);
        textArea.setRows(20);
        textArea.setFont(new Font("Arial", Font.PLAIN, 24));

        textArea.setText("Your computer is using "+ notAllowNum +" invalid processes");

        Border border = BorderFactory.createLineBorder(Color.BLUE, 10);
        textArea.setBorder(border);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(1400, 450));

        btnWarnning = new JButton("Send warning");
        btnWarnning.setFont(new Font("Arial", Font.BOLD, 25));
        btnWarnning.setBackground(Color.blue);
        btnWarnning.setForeground(Color.white);

        btnInfor = new JButton("Send message");
        btnInfor.setFont(new Font("Arial", Font.BOLD, 25));
        btnInfor.setBackground(Color.blue);
        btnInfor.setForeground(Color.white);

        btnReset = new JButton("Reset");
        btnReset.setFont(new Font("Arial", Font.BOLD, 33));
        btnReset.setBackground(Color.blue);
        btnReset.setForeground(Color.white);

        btnBack = new JButton("Back");
        btnBack.setFont(new Font("Arial", Font.BOLD, 33));
        btnBack.setBackground(Color.blue);
        btnBack.setForeground(Color.white);

        pn = new JPanel(null);
        pn.setSize(1500, 900);
        pn.setBounds(0, 0, 1500, 900);
        pn.setBackground(Color.WHITE);

        lb1.setBounds(50, 100, 800, 75);
        textArea.setBounds(50,200,1400,450);

        btnWarnning.setBounds(50, 700, 300, 100);
        btnInfor.setBounds(400, 700, 300, 100);
        btnReset.setBounds(750, 700, 300, 100);
        btnBack.setBounds(1100, 700, 300, 100);

        btnWarnning.addActionListener(this);
        btnInfor.addActionListener(this);
        btnReset.addActionListener(this);
        btnBack.addActionListener(this);

        pn.add(lb1);
        pn.add(textArea);
        pn.add(btnWarnning);
        pn.add(btnInfor);
        pn.add(btnReset);
        pn.add(btnBack);

        add(pn);
        show();


    }
    public void windowClosing(WindowEvent we) {
        dispose();
        System.exit(0);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == btnReset){
            textArea.setText("");
        }
        if(e.getSource() == btnBack){
            try {
                new DetailComputer(client, comp, state);
            } catch (Exception ex) {
                client.Shutdown();
            }
            dispose();
        }
        if(e.getSource() == btnWarnning){
            String option1 = "/SendNotification";
            try {
                client.writeMes(option1);
                client.writeMes(comp);
                client.writeMes("CAUTION");
                String message = textArea.getText();
                client.writeMes(message);
                textArea.setText("");
            } catch (Exception ex) {
                client.Shutdown();
            }
        }
        if(e.getSource() == btnInfor){
            String option1 = "/SendNotification";
            try {
                client.writeMes(option1);
                client.writeMes(comp);
                client.writeMes("INFO");
                String message = textArea.getText();
                client.writeMes(message);
                textArea.setText("");
            } catch (Exception ex) {
                client.Shutdown();
            }
        }
    }
}
