import java.io.File;
import java.nio.file.Files;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class CompDataHelper {
    private final Connection conn;

    public CompDataHelper(Connection connection) {
        this.conn = connection;
    }

    public boolean isAdminComp(String compID) throws Exception {
        String checkAdminCompIDSQL = "SELECT CompID FROM _ADMIN_COMP WHERE CompID = ?";
        PreparedStatement preparedStmt = conn.prepareStatement(checkAdminCompIDSQL);
        preparedStmt.setString(1, compID);
        ResultSet rs = preparedStmt.executeQuery();
        return rs.next();
    }

    public String getPublicKeyStr(String compID) throws Exception {
        String getKeySQL = "SELECT * FROM _COMP WHERE CompID = ?";
        PreparedStatement preparedStmt = conn.prepareStatement(getKeySQL);
        preparedStmt.setString(1, compID);
        ResultSet rs = preparedStmt.executeQuery();
        while (rs.next()) return rs.getString("PublicKey");
        return null;
    }

    public void insertAppHistory(String compID, List<String> apps) throws Exception {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        String curDate = formatter.format(date);
        String insertAppHistorySQL = "INSERT INTO _EMPLOYEE_APP_HISTORY VALUES (?, ?, ?)";
        PreparedStatement preparedStmt = conn.prepareStatement(insertAppHistorySQL);
        preparedStmt.setString(1, compID);
        preparedStmt.setString(2, curDate);
        for (String app : apps) {
            preparedStmt.setString(3, app);
            try {
                preparedStmt.executeUpdate();
            } catch (Exception e) {}
        }
    }

    public List<List<String>> readAppHistory(String compID) throws Exception {
        String readAppHistorySQL = "SELECT * FROM _EMPLOYEE_APP_HISTORY WHERE CompID = ?";
        PreparedStatement preparedStmt = conn.prepareStatement(readAppHistorySQL);
        preparedStmt.setString(1, compID);
        ResultSet rs = preparedStmt.executeQuery();
        List<List<String>> apps = new ArrayList<>();
        while (rs.next()) {
            String timeID = rs.getString("TimeID");
            String appName = rs.getString("AppName");
            apps.add(Arrays.asList(appName, timeID));
        }
        return apps;
    }

    public List<String> readNotAllowApps() throws Exception {
        List<String> notAllowApps = new ArrayList<>();
        String readNotAllowAppSQL = "SELECT * FROM _NOT_ALLOW_APP";
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(readNotAllowAppSQL);
        while (rs.next()) {
            String app = rs.getString("AppName");
            notAllowApps.add(app);
            // System.out.println(app);
        }
        return notAllowApps;
    }

    public List<String> readEmployeeCompIDs() throws Exception {
        String readAllCompIDSQL = "SELECT CompID FROM _COMP WHERE NOT EXISTS " +
                "(SELECT CompID FROM _ADMIN_COMP WHERE CompID = _COMP.CompID)";
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(readAllCompIDSQL);
        List<String> allCompID = new ArrayList<>();
        while (rs.next()) {
            String compID = rs.getString("CompID");
            allCompID.add(compID);
        }
        return allCompID;
    }

    public Map<String, String> readEmployeeCompInfo(String compID) throws Exception {
        String readCompInfoSQL = "SELECT * FROM _EMPLOYEE_COMP WHERE CompID = ?";
        PreparedStatement preparedStmt = conn.prepareStatement(readCompInfoSQL);
        preparedStmt.setString(1, compID);
        ResultSet rs = preparedStmt.executeQuery();
        Map<String, String> compInfo = new HashMap<>();
        while (rs.next()) {
            compInfo.put("CompID", rs.getString("CompID"));
            compInfo.put("EmployeeID", rs.getString("EmployeeID"));
            compInfo.put("EmployeeName", rs.getString("EmployeeName"));
            compInfo.put("Mail", rs.getString("Mail"));
            String img_file = rs.getString("EmployeeImage");
            File fi = new File("images/" + img_file);
            byte[] img = Files.readAllBytes(fi.toPath());
            compInfo.put("EmployeeImage", Util.byteToStr(img));
        }
        return compInfo;
    }

}
