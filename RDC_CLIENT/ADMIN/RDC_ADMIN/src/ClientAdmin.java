import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PublicKey;

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

        System.setProperty("sun.java2d.uiScale", "1");

        ClientAdmin client = new ClientAdmin();
        try {

            client.Init();
            client.Connect();
            client.Interact();

        } catch (Exception e) {
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
        String testMes = String.valueOf((long) (Math.random() * 1e18));
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

        writeMes(String.valueOf(RemoteControlDetail.ScreenDisplayer.MAX_WIDTH));
        writeMes(String.valueOf(RemoteControlDetail.ScreenDisplayer.MAX_HEIGHT));

//        System.out.println((int)(RemoteControlDetail.ScreenDisplayer.MAX_WIDTH / scaleRatio));
//        System.out.println((int)(RemoteControlDetail.ScreenDisplayer.MAX_HEIGHT / scaleRatio));

        new RemoteControlView(this);

    }


    public void Shutdown() {

        isRunning = false;
        System.out.println("Disconnect to server..");

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

}