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
    private JScrollPane scrollPane;
    private List<List<String>> apps = new ArrayList<>();
    private List<List<Object>> data = new ArrayList<>();
    private ClientAdmin client = new ClientAdmin();
    private String comp;
    public AppHistory(String s, String comp)  {
        super(s);
        this.comp = comp;
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
            apps.add(Arrays.asList(appName, timeID));
            System.out.println(i + appName);
        }
        for(int i = 0;i<apps.size();i++){
            int check = 0;
            for(List<Object> row: data){
                String date = String.valueOf(row.get(0));
                if(date.equals(apps.get(i).get(1))){
                    int count = (int) row.get(2) + 1;
                    row.set(2, count);
                    check = 1;
                    break;
                }
            }
            if(check == 0){
                List<Object> row = new ArrayList<>();
                row.add(apps.get(i).get(1));
                row.add(0);
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
        setSize(1000, 600);

        pn = new JPanel(null);
        pn.setSize(1000, 600);
        pn.setBounds(0, 0, 1000, 600);
        pn.setBackground(Color.BLACK);

        lb1 = new JLabel("COMPUTER APP HISTORY FOR " + comp);
        lb1.setForeground(Color.WHITE);
        lb1.setFont(new Font("Arial", Font.BOLD, 20));
        String[] columnNames = {"Time", "Not Allow", "All"};
        Object[][] data001 = new Object[data.size()][];
        for (int i = 0; i < data.size(); i++) {
            List<Object> row = data.get(i);
            data001[i] = row.toArray();
        }

        DefaultTableModel model = new DefaultTableModel(data001,columnNames);
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

        // Thêm ListSelectionListener để xử lý sự kiện khi người dùng chọn ô
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;  // Đảm bảo chỉ xử lý sự kiện sau khi người dùng hoàn tất lựa chọn
                }
                int selectedRow = table.getSelectedRow();
                int selectedColumn = table.getSelectedColumn();

                // Loại trừ cột đầu tiên (cột ngày)
                if (selectedColumn > 0) {
                    String selectedDate = (String) table.getValueAt(selectedRow, 0);
                    String selectedColumnName = table.getColumnName(selectedColumn);

                    DetailHistory detailHistory =new DetailHistory("Detail History", selectedDate, selectedColumnName, comp);
                }
            }
        });

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
