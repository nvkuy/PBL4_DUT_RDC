import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppHistory extends JFrame implements ActionListener {
    private JLabel lb1;
    private JPanel pn;
    private JTable table;
    private JButton btnBack;
    private JScrollPane scrollPane;
    private List<List<String>> apps = new ArrayList<>();
    private List<List<Object>> data = new ArrayList<>();
    private List<String> notAllowApps = new ArrayList<>();
    private ClientAdmin client;
    private String comp, state;

    public AppHistory(ClientAdmin client, String comp, String state) throws Exception {
        super("App history");
        this.comp = comp;
        this.state = state;
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
            apps.add(Arrays.asList(appName, timeID));

        }
        String option2 = "/NotAllowApp";
        client.writeMes(option2);
        int n = Integer.parseInt(client.readMes());
        for (int i = 0; i < n; i++) {
            String appName = client.readMes();
            notAllowApps.add(appName);
//            System.out.println(i + appName);
        }

        for (int i = 0; i < apps.size(); i++) {
            int check = 0;

            for (List<Object> row : data) {
                String date = String.valueOf(row.get(0));
                if (date.equals(apps.get(i).get(1))) {
                    int count = (int) row.get(2) + 1;
                    row.set(2, count);
                    check = 1;
                    for (int j = 0; j < notAllowApps.size(); j++) {
                        if (apps.get(i).get(0).equals(notAllowApps.get(j))) {
                            row.set(1, (int) row.get(1) + 1);
                        }
                    }
                    break;
                }
            }
            if (check == 0) {
                int notAllow = 0;
                List<Object> row = new ArrayList<>();
                row.add(apps.get(i).get(1));
                for (int j = 0; j < notAllowApps.size(); j++) {
                    if (apps.get(i).get(0).equals(notAllowApps.get(j))) {
                        notAllow = 1;
                        break;
                    }
                }
                row.add(notAllow);
                row.add(1);
                data.add(row);
            }
        }
        GUI();

    }

    public void GUI() throws Exception {
        setDefaultCloseOperation(3);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        setSize(2000, 1200);

        pn = new JPanel(null);
        pn.setSize(2000, 1200);
        pn.setBounds(0, 0, 2000, 1200);
        pn.setBackground(Color.WHITE);

        lb1 = new JLabel("COMPUTER APP HISTORY FOR " + comp);
        lb1.setForeground(Color.BLACK);
        lb1.setFont(new Font("Arial", Font.BOLD, 40));
        String[] columnNames = {"Time", "Not Allow", "All"};
        Object[][] data001 = new Object[data.size()][];
        for (int i = 0; i < data.size(); i++) {
            List<Object> row = data.get(i);
            data001[i] = row.toArray();
        }

        DefaultTableModel model = new DefaultTableModel(data001, columnNames);
        table = new JTable(model);
        table.setLayout(new BorderLayout()); // Set layout
        table.getTableHeader().setResizingAllowed(true); // Allow column resizing

        table.setFont(new Font("Arial", Font.PLAIN, 30));  // Set font size


        TableCellRenderer cellRenderer = new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value.toString());
                label.setFont(new Font("Arial", Font.PLAIN, 30));
                Dimension preferredSize = new Dimension(600, 200);
                label.setPreferredSize(preferredSize);
                label.setMinimumSize(preferredSize);
                label.setVerticalAlignment(SwingConstants.TOP);
                return label;
            }
        };

        table.setDefaultRenderer(Object.class, cellRenderer);
        table.setRowHeight(50);
        table.setFillsViewportHeight(true);

        scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(1600, 700));

        table.setFillsViewportHeight(true);
        btnBack = new JButton("BACK");
        btnBack.setFont(new Font("Arial", Font.BOLD, 32));
        btnBack.setBackground(Color.BLUE);
        btnBack.setForeground(Color.white);
        btnBack.addActionListener(this);
        scrollPane.setBounds(50, 240, 1600, 700);
        btnBack.setBounds(1550, 1000, 400, 120);
        lb1.setBounds(50, 100, 1200, 100);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                int selectedRow = table.getSelectedRow();
                int selectedColumn = table.getSelectedColumn();

                if (selectedColumn > 0) {
                    String selectedDate = (String) table.getValueAt(selectedRow, 0);
                    String selectedColumnName = table.getColumnName(selectedColumn);

                    try {
                        new DetailHistory(client, selectedDate, selectedColumn, comp, state);
                    } catch (Exception ex) {
                        client.Shutdown();
                    }
                    dispose();
                }
            }
        });
        table.revalidate(); // Refresh the table to apply changes
        table.repaint();
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
                new DetailComputer(client, comp, state);
            } catch (Exception ex) {
                client.Shutdown();
            }
            dispose();
        }
    }
}
