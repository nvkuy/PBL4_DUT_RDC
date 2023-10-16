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
	volatile protected Boolean verified = false;
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
			
			isRunning = true;
			inp = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			ip = socket.getInetAddress().toString();
			
			compID = inp.readUTF();
			verify();
			
			if (verified) {
				if (server.adminIPs.contains(ip))
					server.admins.put(compID, (AdminHandler) this);
				else
					server.employees.put(compID, (EmployeeHandler) this);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			Shutdown();
		}
		
	}
	
	public void verify() throws Exception {
		
		PublicKey clientPK = null;
		String getKeySQL = "SELECT * FROM _COMP WHERE CompID = ?";
		PreparedStatement preparedStmt = server.conn.prepareStatement(getKeySQL);
		preparedStmt.setString(1, compID);
		ResultSet rs = preparedStmt.executeQuery();
		while (rs.next()) clientPK = RSA.getPublicKeyFromStr(rs.getString("PublicKey"));
		
		// client verify server
		String testMes = inp.readUTF();
		String signMes = server.rsa.sign(testMes);
		out.writeUTF(signMes + "");
		
		// server verify client
		testMes = String.valueOf((long)(Math.random() * 1e18));
		out.writeUTF(testMes + "");
		signMes = inp.readUTF();
		if (!server.rsa.verify(testMes, signMes, clientPK))
			return;
		
		// generate share key
		verified = true;
		aes = new AES();
		String key = aes.getKeyStr();
		String IV = aes.getIVStr();
		String crypKey = RSA.encrypt(key, clientPK);
		String crypIV = RSA.encrypt(IV, clientPK);
		out.writeUTF(crypKey + "");
		out.writeUTF(crypIV + "");
		
	}
	
	public String readMes() throws Exception {
		
		String crypMes = inp.readUTF();
		return aes.decrypt(crypMes);
		
	}
	
	public void writeMes(String mes) throws Exception {
		
		String crypMes = aes.encrypt(mes);
		out.writeUTF(crypMes + "");
		
	}
	
	public void Shutdown() {
		
		isRunning = false;
		if (server.adminIPs.contains(ip))
			server.admins.remove(ip);
		else
			server.employees.remove(ip);
		
	}

}
