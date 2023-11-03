import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
					List<List<String>> apps = server.compDataHelper.readAppHistory(targetCompID);
					
					writeMes(String.valueOf(apps.size()));
					for (var app_time : apps) {
						writeMes(app_time.get(0));
						writeMes(app_time.get(1));
					}

				} else if (option.equals("/NotAllowApp")) {

					List<String> notAllowApps = server.compDataHelper.readNotAllowApps();

					writeMes(notAllowApps.size() + "");
					for (String app : notAllowApps)
						writeMes(app);

				} else if (option.equals("/CompInfo")) {
					
					option = readMes();
					if (option.equals("/Read")) {

						String targetCompID = readMes();
						Map<String, String> info = server.compDataHelper.readCompInfo(targetCompID);
						writeMes(info.get("CompID"));
						writeMes(info.get("EmployeeID"));
						writeMes(info.get("EmployeeName"));
						writeMes(info.get("Mail"));
						writeCompressMes(info.get("EmployeeImage"));

					} else if (option.equals("/AddOrInsert")) {
						//..
					} else if (option.equals("/Delete")) {
						//..
					} else {
						// more feature..
					}
					
				} else if (option.equals("/AllCompID")) {

					List<String> allCompID = server.compDataHelper.readAllCompID();

					writeMes(allCompID.size() + "");
					for (String ID : allCompID)
						writeMes(ID);

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
