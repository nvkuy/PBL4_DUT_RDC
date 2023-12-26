import java.io.*;
import java.net.Socket;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnectionHandler implements Runnable {

	protected Server server;
	protected Socket socket;
	protected BufferedReader inp;
	protected PrintWriter out;
	protected String compID;
	volatile protected Boolean isRunning = false;
	protected boolean isAdmin;
	protected String ip;
	protected AES aes;
	protected int viewport_width;
	protected int viewport_height;
	
	public ConnectionHandler(Server server, Socket socket) {
		this.server = server;
		this.socket = socket;
	}
	
	@Override
	public void run() {

		try {

			inp = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			ip = socket.getInetAddress().getHostAddress();
			
			compID = inp.readLine();
			// System.out.println(ip);

			verify();
			// System.out.println(compID + " verified!");

			viewport_width = Integer.parseInt(readMes());
			viewport_height = Integer.parseInt(readMes());

			isRunning = true;
			isAdmin = server.compDataHelper.isAdminComp(compID);

			System.out.println(compID + " connected!");
			if (isAdmin) {
				server.admins.put(compID, this);
				new AdminMesLoopHandler().start();
			} else {
				server.employees.put(compID, this);
				new EmployeeMesLoopHandler().start();
			}
			
		} catch (Exception e) {
			// e.printStackTrace();
			close();
		}
		
	}
	
	public void verify() throws Exception {
		
		PublicKey clientPK = RSA.getPublicKeyFromStr(server.compDataHelper.getPublicKeyStr(compID));
		
		// client verify server
		String crypMes = inp.readLine();
		String testMes = server.rsa.decrypt(crypMes);
		String signMes = server.rsa.sign(testMes);
		out.println(signMes);
		
		// server verify client
		testMes = String.valueOf((long)(Math.random() * 1e18));
		crypMes = RSA.encrypt(testMes, clientPK);
		out.println(crypMes);
		signMes = inp.readLine();
		if (!RSA.verify(testMes, signMes, clientPK))
			throw new Exception("Can't verify exception!");
		
		// generate share key
		aes = new AES();
		String key = aes.getKeyStr();
		String crypKey = RSA.encrypt(key, clientPK);
		out.println(crypKey);

	}
	
	public String readMes() throws Exception {

		String IVStr = inp.readLine();
		String crypMes = inp.readLine();
		byte[] IV = Util.strToByte(IVStr);
		return aes.decrypt(crypMes, IV);
		
	}
	
	public void writeMes(String mes) throws Exception {

		if (mes == null || mes.isEmpty())
			mes = " ";

		byte[] IV = aes.generateIV();
		String crypMes = aes.encrypt(mes, IV);
		String IVStr = Util.byteToStr(IV);
		out.println(IVStr);
		out.println(crypMes);
		
	}

	public String readCompressMes() throws Exception {

		String IVStr = Gzip.decompress(inp.readLine());
		byte[] IV = Util.strToByte(IVStr);
		String compressMes = aes.decrypt(inp.readLine(), IV);
		return Gzip.decompress(compressMes);

	}

	public void writeCompressMes(String mes) throws Exception {

		if (mes == null || mes.isEmpty())
			mes = " ";

		byte[] IV = aes.generateIV();
		String IVStr = Util.byteToStr(IV);
		out.println(Gzip.compress(IVStr));
		String compressMes = Gzip.compress(mes);
		out.println(aes.encrypt(compressMes, IV));

	}
	
	public void close() {

		System.out.println(compID + " disconnected!");
		isRunning = false;
		if (isAdmin)
			server.admins.remove(compID);
		else
			server.employees.remove(compID);
		
	}

	private class EmployeeMesLoopHandler {

		public void start() throws Exception {

			while (isRunning) {

				String option = readMes();

				if (option.equals("/AppHistory")) {

					int n = Integer.parseInt(readMes());
					List<String> apps = new ArrayList<>();
					for (int i = 0; i < n; i++)
						apps.add(readMes());
					server.compDataHelper.insertAppHistory(compID, apps);

				} else {
					// more feature..
				}

			}

		}

	}

	private class AdminMesLoopHandler {

		public void start() throws Exception {

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
						Map<String, String> info = server.compDataHelper.readEmployeeCompInfo(targetCompID);
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

					List<String> allCompID = server.compDataHelper.readEmployeeCompIDs();

					writeMes(allCompID.size() + "");
					for (String ID : allCompID)
						writeMes(ID);

				} else if (option.equals("/OnlineList")) {

					writeMes(server.employees.keySet().size() + "");
					for (String compID : server.employees.keySet())
						writeMes(compID);

				} else if (option.equals("/RemoteControl")) {

					String targetCompID = readMes();
					AES rdcAES = new AES();
					ConnectionHandler connectionHandler = server.employees.get(targetCompID);

					int employeeScreenWidth = connectionHandler.viewport_width;
					int employeeScreenHeight = connectionHandler.viewport_height;
					double scaleDown = 1.0f;
					scaleDown = Math.max((double)(employeeScreenWidth) / viewport_width, scaleDown);
					scaleDown = Math.max((double)(employeeScreenHeight) / viewport_height, scaleDown);
					int w1 = (int) (scaleDown * employeeScreenWidth);
					int h1 = (int) (scaleDown * employeeScreenHeight);
					double scaleUp = 1e9;
					scaleUp = Math.min((double)(viewport_width) / w1, scaleUp);
					scaleUp = Math.min((double)(viewport_height) / h1, scaleUp);
					int w2 = (int) (scaleUp * w1);
					int h2 = (int) (scaleUp * h1);

					writeMes(rdcAES.getKeyStr());
					writeMes(connectionHandler.ip);
					writeMes(String.valueOf(w2));
					writeMes(String.valueOf(h2));

					connectionHandler.writeMes("/RemoteControl");
					connectionHandler.writeMes(rdcAES.getKeyStr());
					connectionHandler.writeMes(ip);
					connectionHandler.writeMes(String.valueOf(w2));
					connectionHandler.writeMes(String.valueOf(h2));

				} else {
					// more feature..
				}

			}

		}

	}

}
