import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    protected BufferedReader inp;
    protected PrintWriter out;
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

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        writeMes(String.valueOf(screenSize.width));
        writeMes(String.valueOf(screenSize.height));


        isRunning = true;

        Thread serverCmdHandler = new Thread(new ServerCmdHandler());
        serverCmdHandler.start();



        Thread appHistoryLogger = new Thread(new AppHistoryLog());
        appHistoryLogger.start();


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

        if (mes == null || mes.equals(""))
            mes = " ";

        byte[] IV = aes.generateIV();
        String IVStr = Util.byteToStr(IV);
        out.println(Gzip.compress(IVStr));
        String compressMes = Gzip.compress(mes);
        out.println(aes.encrypt(compressMes, IV));

    }

    private class AppHistoryLog implements Runnable {

        String data = "";
        int num = 0;
        @Override
        public void run() {
            while (isRunning) {
                try {
                    String machineName = System.getenv("COMPUTERNAME");
                    ProcessBuilder processBuilder;

                    try {
                        if (machineName == null || machineName.isEmpty()) {
                            processBuilder = new ProcessBuilder("tasklist");
                        } else {
                            processBuilder = new ProcessBuilder("tasklist", "/S", machineName);
                        }

                        Process process = processBuilder.start();

                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                String processName = line.split("\\s+")[0];
                                if (processName.isEmpty()) {
                                    continue;
                                }

                                if (processName.endsWith(".exe")) {
                                    processName = processName.substring(0, processName.length() - 4);
                                }
                                data += (processName + System.lineSeparator());
                                num++;


                            }
                        }
                    } catch (Exception e){

                    }

                    data = (num + System.lineSeparator()) + data;
                    String[] lines = data.split(System.lineSeparator());
                    writeMes("/AppHistory");
                    for (String line : lines) {
                        writeMes(line);
                    }
                    data = "";
                    num = 0;

                    Thread.sleep(120000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

    }

    private class ServerCmdHandler implements Runnable {

        @Override
        public void run() {

            while (isRunning) {

                try {
                    String option = readMes();
                    if (option.equals("/RemoteControl")) {

                        String key = readMes();
                        String targetIP = readMes();
                        Integer targetScreenWidth = Integer.valueOf(readMes());
                        Integer targetScreenHeight = Integer.valueOf(readMes());

                        Thread remoteControlHandler = new Thread(new RemoteControlHandler(key, targetIP, targetScreenWidth, targetScreenHeight));
                        remoteControlHandler.start();

//                        JOptionPane.showMessageDialog(null, "Your computer are being controlled!", "Message",JOptionPane.INFORMATION_MESSAGE);

                    } else {
                        // ..
                    }
                } catch (Exception e) {
                    Shutdown();
//                    e.printStackTrace();
                }

            }

        }
    }

}