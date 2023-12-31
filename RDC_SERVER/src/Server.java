import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/RDC";
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "";
    private static final Integer PORT = 6996;
    private static final String KEY_PATH = "Key.txt";

    Connection conn;
    CompDataHelper compDataHelper;
    ServerSocket server;
    Map<String, ConnectionHandler> admins, employees;
    volatile Boolean isRunning = false;
    RSA rsa;

    public static void main(String[] args) {

        Server server = new Server();
		try {

			server.Init();
			server.Start();

		} catch (Exception e) {
//			e.printStackTrace();
			System.out.println("Server init fail!");
            server.Shutdown();
		}

    }

    public void Init() throws Exception {

        System.out.println("Server initing..");

        server = new ServerSocket(PORT);

        rsa = new RSA(KEY_PATH);
        // rsa.printKeys();

        conn = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
        compDataHelper = new CompDataHelper(conn);
        admins = new HashMap<>();
        employees = new HashMap<>();

    }

    public void Start() {

        isRunning = true;
        System.out.println("Server started!");

        try {

            while (isRunning) {

                Socket socket = server.accept();
                Thread handler = new Thread(new ConnectionHandler(this, socket));
                handler.start();

            }

        } catch (Exception e) {
            // e.printStackTrace();
        }

    }

    public void Shutdown() {

        System.out.println("Shutdown server..");
        isRunning = false;

        try {

            conn.close();
            server.close();
            System.out.println("Server down!");

        } catch (Exception ignored) { }

    }

}
