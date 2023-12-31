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
    private String date, comp, compstate;
    private int state;
    private List<List<String>> apps = new ArrayList<>();
    private List<List<String>> finalList = new ArrayList<>();
    private List<String> notAllowApps = new ArrayList<>();
    private JButton btnBack;
    private ClientAdmin client;

    public DetailHistory(ClientAdmin client, String date, int state, String comp, String compstate) throws Exception {
        super("Detail history");
        this.comp = comp;
        this.date = date;
        this.state = state;
        this.compstate = compstate;
        this.client = client;
        GetData();
    }

    public void GetData() throws Exception {

        String option1 = "/AppHistory";
        client.writeMes(option1);
        client.writeMes(comp);
        int n1 = Integer.parseInt(client.readMes());
        apps = new ArrayList<>();
        for (int i = 0; i < n1; i++) {
            String appName = client.readMes();
            String timeID = client.readMes();
            if (timeID.equals(date)) {
                apps.add(Arrays.asList(appName, timeID));
            }
        }

        String option2 = "/NotAllowApp";
        client.writeMes(option2);
        int n = Integer.parseInt(client.readMes());
        for (int i = 0; i < n; i++) {
            String appName = client.readMes();
            notAllowApps.add(appName);
        }
        if (state == 1) {
            for (int i = 0; i < apps.size(); i++) {
                for (int j = 0; j < notAllowApps.size(); j++) {
                    if (apps.get(i).get(0).equals(notAllowApps.get(j))) {
                        finalList.add(Arrays.asList(apps.get(i).get(0), apps.get(i).get(1)));
                        break;
                    }
                }

            }
        } else {
            for (int i = 0; i < apps.size(); i++) {
                finalList.add(Arrays.asList(apps.get(i).get(0), apps.get(i).get(1)));
            }
        }

        GUI();
    }

    public void GUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        setSize(1500, 900);

        pn = new JPanel(null);
        pn.setSize(1500, 900);
        pn.setBounds(0, 0, 1500, 900);
        pn.setBackground(Color.WHITE);

        lb1 = new JLabel("COMPUTER APP DETAIL HISTORY");
        lb1.setForeground(Color.BLACK);
        lb1.setFont(new Font("Arial", Font.BOLD, 32));

        String[] columnNames = {"Time", "Log App"};

        Object[][] data = new Object[finalList.size()][];
        for (int i = 0; i < finalList.size(); i++) {
            List<String> row = new ArrayList<>();
            row.add(finalList.get(i).get(1));
            row.add(finalList.get(i).get(0));
            data[i] = row.toArray();
        }
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 30));

        TableCellRenderer cellRenderer = new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value.toString());
                label.setFont(new Font("Arial", Font.PLAIN, 25));
                Dimension preferredSize = new Dimension(400, 150);
                label.setPreferredSize(preferredSize);
                label.setMinimumSize(preferredSize);
                label.setVerticalAlignment(SwingConstants.TOP);
                return label;
            }
        };

        table.setDefaultRenderer(Object.class, cellRenderer);
        table.setRowHeight(50);
        table.setFillsViewportHeight(true);

        btnBack = new JButton("BACK");
        btnBack.setFont(new Font("Arial", Font.BOLD, 24));
        btnBack.setBackground(Color.BLUE);
        btnBack.setForeground(Color.WHITE);
        btnBack.addActionListener(this);
        scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(1200, 500));
        btnBack.setBounds(1270, 700, 200, 100);

        table.setFillsViewportHeight(true);

        scrollPane.setBounds(50, 150, 1200, 500);
        lb1.setBounds(50, 50, 1200, 70);

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
        if (e.getSource() == btnBack) {
            try {
                new AppHistory(client, comp, compstate);
            } catch (Exception ex) {
                client.Shutdown();
            }
            dispose();
        }
    }
}
