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

			isRunning = true;
			isAdmin = server.compDataHelper.isAdminComp(compID);

			System.out.println(compID + " connected!");
			if (isAdmin)
				new AdminMesLoopHandler().start();
			else
				new EmployeeMesLoopHandler().start();
			
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

                switch (option) {
                    case "/AppHistory" -> {

                        String targetCompID = readMes();
                        List<List<String>> apps = server.compDataHelper.readAppHistory(targetCompID);

                        writeMes(String.valueOf(apps.size()));
                        for (var app_time : apps) {
                            writeMes(app_time.get(0));
                            writeMes(app_time.get(1));
                        }

                    }
                    case "/NotAllowApp" -> {
                        List<String> notAllowApps = server.compDataHelper.readNotAllowApps();
                        writeMes(notAllowApps.size() + "");
                        for (String app : notAllowApps)
                            writeMes(app);
                    }
                    case "/CompInfo" -> {
                        option = readMes();
                        switch (option) {
                            case "/Read" -> {
                                String targetCompID = readMes();
                                Map<String, String> info = server.compDataHelper.readEmployeeCompInfo(targetCompID);
                                writeMes(info.get("CompID"));
                                writeMes(info.get("EmployeeID"));
                                writeMes(info.get("EmployeeName"));
                                writeMes(info.get("Mail"));
                                writeCompressMes(info.get("EmployeeImage"));
                            }
                            case "/AddOrInsert" -> {
                            }
                            //..
                            case "/Delete" -> {
                            }
                            //..
                            default -> {
                            }
                            // more feature..
                        }
                    }
                    case "/AllCompID" -> {
                        List<String> allCompID = server.compDataHelper.readEmployeeCompIDs();
                        writeMes(allCompID.size() + "");
                        for (String ID : allCompID)
                            writeMes(ID);
                    }
                    case "/OnlineList" -> {
                        writeMes(server.employees.keySet().size() + "");
                        for (String compID : server.employees.keySet())
                            writeMes(compID);
                    }
                    case "/RemoteControl" -> {

                        String targetCompID = readMes();
                        Integer width = Integer.valueOf(readMes());
                        Integer height = Integer.valueOf(readMes());
                        AES rdcAES = new AES();
                        ConnectionHandler connectionHandler = server.employees.get(targetCompID);
                        writeMes(rdcAES.getKeyStr());
                        writeMes(connectionHandler.ip);
                        connectionHandler.writeMes("/RemoteControl");
                        connectionHandler.writeMes(rdcAES.getKeyStr());
                        connectionHandler.writeMes(ip);
                        connectionHandler.writeMes(String.valueOf(width));
                        connectionHandler.writeMes(String.valueOf(height));

                    }
                    default -> {
                    }
                    // more feature..
                }

			}

		}

	}

}
