import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Server {
	
	private static final String DB_URL = "jdbc:mysql://127.0.0.2:3306/rdc";
	private static final String USER_NAME = "root";
	private static final String PASSWORD = "";
	private static final Integer PORT = 6996;
	private static final String KEY_PATH = "Key.txt";
	
	Connection conn;
	ServerSocket server;
	Set<String> adminIP, notAllowApp;
	Map<String, AdminHandler> admin;
	Map<String, EmployeeHandler> employee;
	volatile Boolean isRunning = false;
	RSA rsa;

	public static void main(String[] args) {
		
		Server server = new Server();
		server.Init();
		server.Start();
		
	}
	
	public void Init() {
		try {
			
			System.out.println("Server starting..");
			
			server = new ServerSocket(PORT);
			
			rsa = new RSA(KEY_PATH);
			// rsa.printKeys();
			
			conn = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
			adminIP = new TreeSet<>();
			notAllowApp = new TreeSet<>();
			admin = new TreeMap<>();
			employee = new TreeMap<>();
			
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM _ADMIN_COMP");
			while (rs.next()) {
				String ip = rs.getString("IPComp");
				adminIP.add(ip);
				// System.out.println(ip);
			}
			
			rs = stmt.executeQuery("SELECT * FROM _NOT_ALLOW_APP");
			while (rs.next()) {
				String app = rs.getString("AppName");
				notAllowApp.add(app);
				// System.out.println(app);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Init server failed!");
		}
	}
	
	public void Start() {
		
		isRunning = true;
		System.out.println("Server started!");
		
		try {
			
			while (isRunning) {
				
				Socket socket = server.accept();
				Thread handler = null;
				if (adminIP.contains(socket.getInetAddress().toString()))
					handler = new Thread(new AdminHandler(this, socket));
				else
					handler = new Thread(new EmployeeHandler(this, socket));
				handler.start();
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void Shutdown() {
		
		System.out.println("Shutdown server..");
		isRunning = false;
		
		try {
			
			conn.close();
			server.close();
			System.out.println("Server down!");
			
		} catch (Exception e) {}
		
	}
	
}
