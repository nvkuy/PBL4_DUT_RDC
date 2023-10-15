import java.net.Socket;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EmployeeHandler extends ConnectionHandler {

	public EmployeeHandler(Server server, Socket socket) {
		super(server, socket);
	}

	@Override
	public void run() {
		
		try {
			
			super.run();
			
			while (isRunning && verified) {
				
				String option = readMes();
				
				if (option.equals("/AppHistory")) {
					
					int n = Integer.valueOf(readMes());
					Date date = new Date();
					SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
					String curDate = formatter.format(date);
					String insertAppHistorySQL = "INSERT INTO _COMP_APP_HISTORY VALUES (?, ?, ?)";
					PreparedStatement preparedStmt = server.conn.prepareStatement(insertAppHistorySQL);
					preparedStmt.setString(1, compID);
					preparedStmt.setString(2, curDate);
					
					for (int i = 0; i < n; i++) {
						String app = readMes();
						preparedStmt.setString(3, app);
						preparedStmt.executeUpdate();
					}
					
				} else {
					// more feature..
				}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	
	
}
