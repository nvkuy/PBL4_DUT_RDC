import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminHandler extends ConnectionHandler {

	public AdminHandler(Server server, Socket socket) {
		super(server, socket);
	}

	@Override
	public void run() {

		try {

			super.run();

			// System.out.println("Admin: " + ip);

			while (isRunning) {

				String option = readMes();

				if (option.equals("/AppHistory")) {

					String targetCompID = readMes();
					String readAppHistorySQL = "SELECT * FROM _COMP_APP_HISTORY WHERE CompID = ?";
					PreparedStatement preparedStmt = server.conn.prepareStatement(readAppHistorySQL);
					preparedStmt.setString(1, targetCompID);
					ResultSet rs = preparedStmt.executeQuery();
					List<List<String>> apps = new ArrayList<>();
					
					while (rs.next()) {
						
						String timeID = rs.getString("TimeID");
						String appName = rs.getString("AppName");
						apps.add(Arrays.asList(appName, timeID));
						
					}
					
					writeMes(String.valueOf(apps.size()));
					for (var app_time : apps) {
						writeMes(app_time.get(0));
						writeMes(app_time.get(1));
					}

				} else if (option.equals("/NewCompInfo")) {
					
					//..
					
				} else if (option.equals("/DelCompInfo")) {
					
					//..
					
				} else if (option.equals("/EditCompInfo")) {
					
					//..
					
				} else if (option.equals("/ReadCompInfo")) {
					
					//..
					
				} else if (option.equals("/OnlineList")) {

					writeMes(server.employees.keySet().size() + "");
					for (String compID : server.employees.keySet())
						writeMes(compID);

				} else if (option.equals("/RemoteControl")) {
					
					//..
					
				} else {
					// more feature..
				}

			}

		} catch (Exception e) {
			// e.printStackTrace();
			close();
		}

	}

}
