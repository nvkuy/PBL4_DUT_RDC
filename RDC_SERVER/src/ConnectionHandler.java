import java.io.*;
import java.net.Socket;
import java.security.PublicKey;

public class ConnectionHandler implements Runnable {

	protected Server server;
	protected Socket socket;
	protected BufferedReader inp;
	protected PrintWriter out;
	protected String compID;
	volatile protected Boolean isRunning = false;
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
			System.out.println(compID + " connected!");
			if (server.adminIPs.contains(ip))
				server.admins.put(compID, (AdminHandler) this);
			else
				server.employees.put(compID, (EmployeeHandler) this);
			
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
		out.write(signMes);
		
		// server verify client
		testMes = String.valueOf((long)(Math.random() * 1e18));
		crypMes = RSA.encrypt(testMes, clientPK);
		out.write(crypMes);
		signMes = inp.readLine();
		if (!RSA.verify(testMes, signMes, clientPK))
			throw new Exception("Can't verify exception!");
		
		// generate share key
		aes = new AES();
		String key = aes.getKeyStr();
		String crypKey = RSA.encrypt(key, clientPK);
		out.write(crypKey);

	}
	
	public String readMes() throws Exception {

		String IVStr = inp.readLine();
		String crypMes = inp.readLine();
		byte[] IV = AES.getIVFromStr(IVStr);
		return aes.decrypt(crypMes, IV);
		
	}
	
	public void writeMes(String mes) throws Exception {

		if (mes == null || mes.equals(""))
			mes = " ";

		byte[] IV = aes.generateIV();
		String crypMes = aes.encrypt(mes, IV);
		String IVStr = AES.getIVStr(IV);
		out.write(IVStr);
		out.write(crypMes);
		
	}

	public String readCompressMes() throws Exception {

		String IVStr = Gzip.decompress(inp.readLine());
		byte[] IV = AES.getIVFromStr(IVStr);
		String compressMes = aes.decrypt(inp.readLine(), IV);
		return Gzip.decompress(compressMes);

	}

	public void writeCompressMes(String mes) throws Exception {

		if (mes == null || mes.equals(""))
			mes = " ";

		byte[] IV = aes.generateIV();
		String IVStr = AES.getIVStr(IV);
		out.write(Gzip.compress(IVStr));
		String compressMes = Gzip.compress(mes);
		out.write(aes.encrypt(compressMes, IV));

	}
	
	public void close() {

		System.out.println(compID + " disconnected!");
		isRunning = false;
		if (server.adminIPs.contains(ip))
			server.admins.remove(compID);
		else
			server.employees.remove(compID);
		
	}

}
