import java.net.Socket;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EmployeeHandler extends ConnectionHandler {

	public EmployeeHandler(Server server, Socket socket) {
		super(server, socket);
	}

	@Override
	public void run() {
		
		try {
			
			super.run();

			// System.out.println("Employee: " + ip);
			
			while (isRunning) {
				
				String option = readMes();
				
				if (option.equals("/AppHistory")) {

					int n = Integer.valueOf(readMes());
					List<String> apps = new ArrayList<>();
					for (int i = 0; i < n; i++)
						apps.add(readMes());
					server.compDataHelper.insertAppHistory(compID, apps);
					
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
