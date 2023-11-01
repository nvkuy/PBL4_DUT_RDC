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
    private String date,state,  comp;
    private List<List<String>> apps = new ArrayList<>();
    private ClientAdmin client = new ClientAdmin();
    public DetailHistory(String s, String date, String state, String comp)  {
        super(s);
        this.comp = comp;
        this.date = date;
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

        Object[][] data = new Object[apps.size()][];
        for(int i = 0;i< apps.size();i++){
            List<String> row = new ArrayList<>();
            row.add(apps.get(i).get(1));
            row.add(apps.get(i).get(0));
            data[i] = row.toArray();
        }
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 16));

        TableCellRenderer cellRenderer = new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value.toString());
                label.setFont(new Font("Arial", Font.PLAIN, 16));
                label.setPreferredSize(new Dimension(0, 100));
                label.setVerticalAlignment(SwingConstants.TOP);
                return label;
            }
        };

        table.setDefaultRenderer(Object.class, cellRenderer);

        scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 400));

        table.setFillsViewportHeight(true);

        scrollPane.setBounds(50, 120, 800, 400);
        lb1.setBounds(50, 50, 400, 50);

        pn.add(lb1);
        pn.add(scrollPane);

        add(pn);

        setVisible(true);
    }
    public void windowClosing(WindowEvent we) {
        dispose();
        System.exit(0);
    }
    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
