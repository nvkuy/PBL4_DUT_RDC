import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ConnectionHandler implements Runnable {

	protected Server server;
	protected Socket socket;
	protected DataInputStream inp;
	protected DataOutputStream out;
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

			inp = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			ip = socket.getInetAddress().getHostAddress();
			
			compID = inp.readUTF();
			// System.out.println(ip);
			// System.out.println(compID + " connected!");

			verify();
			// System.out.println(compID + " verified!");

			isRunning = true;
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
		String crypMes = inp.readUTF();
		String testMes = server.rsa.decrypt(crypMes);
		String signMes = server.rsa.sign(testMes);
		out.writeUTF(signMes);
		
		// server verify client
		testMes = String.valueOf((long)(Math.random() * 1e18));
		crypMes = RSA.encrypt(testMes, clientPK);
		out.writeUTF(crypMes);
		signMes = inp.readUTF();
		if (!RSA.verify(testMes, signMes, clientPK))
			throw new Exception("Can't verify exception!");
		
		// generate share key
		aes = new AES();
		String key = aes.getKeyStr();
		String crypKey = RSA.encrypt(key, clientPK);
		out.writeUTF(crypKey);

	}
	
	public String readMes() throws Exception {

		String IVStr = inp.readUTF();
		String crypMes = inp.readUTF();
		byte[] IV = AES.getIVFromStr(IVStr);
		return aes.decrypt(crypMes, IV);
		
	}
	
	public void writeMes(String mes) throws Exception {

		byte[] IV = aes.generateIV();
		String crypMes = aes.encrypt(mes, IV);
		String IVStr = AES.getIVStr(IV);
		out.writeUTF(IVStr);
		out.writeUTF(crypMes);
		
	}
	
	public void close() {
		
		isRunning = false;
		if (server.adminIPs.contains(ip))
			server.admins.remove(ip);
		else
			server.employees.remove(ip);
		
	}

}
