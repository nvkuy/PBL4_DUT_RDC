import java.io.*;
import java.net.Socket;
import java.security.PublicKey;
import java.security.spec.ECField;
import java.time.LocalDateTime;
import java.util.Scanner;

public class ClientEmployee {

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

        ClientEmployee client = new ClientEmployee();
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

        int check = 1;
        isRunning = true;
        while (isRunning) {

            LocalDateTime date = LocalDateTime.now();
            if(date.getMinute() == 3) check = 1;
            // Chuyển đổi mili giây sang thông tin ngày giờ hiện tại
            if((date.getMinute() == 0 || date.getMinute() == 1) && (check==1)){
                String datetime = String.format("%s_%s_%s_%s", date.getMonthValue(), date.getDayOfMonth(), date.getYear(), (date.getHour()-1));
                String file = "D:\\Windowservice\\ServiceTestLog\\ServiceTestLog\\bin\\Debug\\Logs\\ServiceLog_"+datetime+".txt";
                Scanner scanner = new Scanner(new File(file));
                writeMes("/AppHistory");
                while (scanner.hasNextLine()) {
                    String data= scanner.nextLine();
                    writeMes(data);
                }
                scanner.close();
                check = 0;
            }
        }

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

    public String readCompressMes() throws Exception {

        String IVStr = Gzip.decompress(inp.readUTF());
        String crypMes = Gzip.decompress(inp.readUTF());
        byte[] IV = AES.getIVFromStr(IVStr);
        return aes.decrypt(crypMes, IV);

    }

    public void writeCompressMes(String mes) throws Exception {

        if (mes == null || mes.equals(""))
            mes = " ";

        byte[] IV = aes.generateIV();
        String crypMes = aes.encrypt(mes, IV);
        String IVStr = AES.getIVStr(IV);
        out.writeUTF(Gzip.compress(IVStr));
        out.writeUTF(Gzip.compress(crypMes));

    }

}