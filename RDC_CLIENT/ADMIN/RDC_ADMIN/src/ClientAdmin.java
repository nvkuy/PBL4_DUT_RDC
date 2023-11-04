import java.io.*;
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
    protected BufferedReader inp;
    protected PrintWriter out;
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
        inp = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        System.out.println("Inited client");

    }

    public void Connect() throws Exception {

        out.println(compID);

        // client verify server
        String testMes = String.valueOf((long)(Math.random() * 1e18));
        String crypMes = RSA.encrypt(testMes, serverPublicKey);
        out.println(crypMes);
        String signMes = inp.readLine();
        if (!RSA.verify(testMes, signMes, serverPublicKey))
            throw new Exception();

        // server verify client
        crypMes = inp.readLine();
        testMes = rsa.decrypt(crypMes);
        signMes = rsa.sign(testMes);
        out.println(signMes);

        // get share key
        String crypKey = inp.readLine();
        String key = rsa.decrypt(crypKey);
        aes = new AES(key);

        System.out.println("Connected to server!");

    }

    public void Interact() throws Exception {

//        writeMes("/CompInfo");
//        writeMes("/Read");
//        writeMes("employee1");
//        String t1 = readMes();
//        String t2 = readMes();
//        String t3 = readMes();
//        String t4 = readMes();
//        String t5 = readCompressMes();
//        System.out.println(t5);

        new RemoteControlView("Remote control");

    }


    public void Shutdown() {

        isRunning = false;
        System.out.println("Disconnect to server..");

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
        out.println(IVStr);
        out.println(crypMes);

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
        out.println(Gzip.compress(IVStr));
        String compressMes = Gzip.compress(mes);
        out.println(aes.encrypt(compressMes, IV));

    }

}