import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.net.Socket;
import java.security.PublicKey;
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientAdmin {

    private static final String SERVER_INFO_PATH = "ServerInfo.txt";
    private static final String COMP_ID_PATH = "CompID.txt";
    private static final String KEY_PATH = "Key.txt";

    String serverIP;
    Integer serverPort;
    PublicKey serverPublicKey;
    String compID;
    RSA rsa;
    AES aes;
    DataInputStream inp;
    DataOutputStream out;
    volatile Boolean isRunning = false;

    public static void main(String[] args) {

        ClientAdmin client = new ClientAdmin();
        try {

            client.Init();
            client.Connect();
            client.Interact();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error!");
            client.Shutdown();
        }

    }

    public void Init() throws Exception {

        BufferedReader bf = new BufferedReader(new FileReader(COMP_ID_PATH));
        compID = bf.readLine();
        bf.close();

        rsa = new RSA(KEY_PATH);

        bf = new BufferedReader(new FileReader(SERVER_INFO_PATH));
        serverIP = bf.readLine();
        serverPort = Integer.valueOf(bf.readLine());
        String publicKeyStr = bf.readLine();
        serverPublicKey = RSA.getPublicKeyFromStr(publicKeyStr);
        bf.close();

        Socket socket = new Socket(serverIP, serverPort);
        inp = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        System.out.println("Inited client");

    }

    public void Connect() throws Exception {

        out.writeUTF(compID);

        // client verify server
        String testMes = String.valueOf((long)(Math.random() * 1e18));
        String crypMes = RSA.encrypt(testMes, serverPublicKey);
        out.writeUTF(crypMes);
        String signMes = inp.readUTF();
        if (!RSA.verify(testMes, signMes, serverPublicKey))
            throw new Exception();

        // server verify client
        crypMes = inp.readUTF();
        testMes = rsa.decrypt(crypMes);
        signMes = rsa.sign(testMes);
        out.writeUTF(signMes);

        // get share key
        String crypKey = inp.readUTF();
        String key = rsa.decrypt(crypKey);
        aes = new AES(key);

        System.out.println("Connected to server!");

    }

    public void Interact() throws Exception {

//        isRunning = true;
//        while (isRunning) {
//
//            // handler func..
//
//        }

        RemoteControlView view = new RemoteControlView("Remote control");

    }


    public void Shutdown() {

        isRunning = false;
        System.out.println("Disconnect to server..");

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

}