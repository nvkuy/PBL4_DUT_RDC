import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetailHistory extends JFrame implements ActionListener {
    private JLabel lb1;
    private JPanel pn;
    private JTable table;
    private JScrollPane scrollPane;
    private String date,  comp, compstate;
    private int state;
    private List<List<String>> apps = new ArrayList<>();
    private List<List<String>> finalList = new ArrayList<>();
    private List<String> notAllowApps = new ArrayList<>();
    private JButton btnBack;
    private ClientAdmin client = new ClientAdmin();
    public DetailHistory(String s, String date, int state, String comp, String compstate)  {
        super(s);
        this.comp = comp;
        this.date = date;
        this.state = state;
        this.compstate = compstate;
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
    public void GetData() throws Exception{

        String option1 = "/AppHistory";
        client.writeMes(option1);
        client.writeMes(comp);
        int n1 = Integer.parseInt(client.readMes());
        apps = new ArrayList<>();
        for(int i = 0; i < n1; i++){
            String appName = client.readMes();
            String timeID = client.readMes();
            if(timeID.equals(date)){
                apps.add(Arrays.asList(appName, timeID));
            }
        }

        String option2 = "/NotAllowApp";
        client.writeMes(option2);
        int n = Integer.parseInt(client.readMes());
        for(int i = 0;i < n;i++){
            String appName = client.readMes();
            notAllowApps.add(appName);
        }
        if(state == 1){
            for(int i = 0;i < apps.size();i++){
                for(int j = 0;j < notAllowApps.size();j++){
                    if(apps.get(i).get(0).equals(notAllowApps.get(j))){
                        finalList.add(Arrays.asList(apps.get(i).get(0),apps.get(i).get(1)));
                        break;
                    }
                }

            }
        }
        else{
            for(int i = 0;i<apps.size();i++){
                finalList.add(Arrays.asList(apps.get(i).get(0),apps.get(i).get(1)));
            }
        }

        GUI();
    }
    public void GUI() {
        setDefaultCloseOperation(3);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        setSize(1000, 600);

        pn = new JPanel(null);
        pn.setSize(1000, 600);
        pn.setBounds(0, 0, 1000, 600);
        pn.setBackground(Color.BLACK);

        lb1 = new JLabel("COMPUTER APP DETAIL HISTORY");
        lb1.setForeground(Color.WHITE);
        lb1.setFont(new Font("Arial", Font.BOLD, 20));

        String[] columnNames = {"Time", "Log App"};

        Object[][] data = new Object[finalList.size()][];
        for(int i = 0;i< finalList.size();i++){
            List<String> row = new ArrayList<>();
            row.add(finalList.get(i).get(1));
            row.add(finalList.get(i).get(0));
            data[i] = row.toArray();
        }
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 14));

        TableCellRenderer cellRenderer = new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value.toString());
                label.setFont(new Font("Arial", Font.PLAIN, 14));
                label.setPreferredSize(new Dimension(0, 100));
                label.setVerticalAlignment(SwingConstants.TOP);
                return label;
            }
        };

        table.setDefaultRenderer(Object.class, cellRenderer);
        btnBack=new JButton("BACK");
        btnBack.setFont(new Font("Arial",Font.BOLD,16));
        btnBack.setBackground(Color.white);
        btnBack.setForeground(Color.black);
        btnBack.addActionListener(this);
        scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 350));
        btnBack.setBounds(770,500,200,60);

        table.setFillsViewportHeight(true);

        scrollPane.setBounds(50, 120, 800, 350);
        lb1.setBounds(50, 50, 400, 50);

        pn.add(lb1);
        pn.add(scrollPane);
        pn.add(btnBack);

        add(pn);

        setVisible(true);
    }
    public void windowClosing(WindowEvent we) {
        dispose();
        System.exit(0);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==btnBack){
            new AppHistory("App history", comp, compstate);
            dispose();
        }
    }
}
