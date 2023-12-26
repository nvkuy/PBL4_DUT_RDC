import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.security.PublicKey;
import java.security.spec.ECField;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
        List<String> listData = new ArrayList<String>();
        int num = 0;
        @Override
        public void run() {

            int check = 1;

            while (isRunning) {
                try {
                    LocalDateTime date = LocalDateTime.now();

                    if(check == 1){
                        GetData();
                        Thread.sleep(120000);
                    }
                    if (date.getMinute() == 1) check = 1;
                    if ((date.getMinute() == 58 || date.getMinute() == 59) && (check == 1)) {
                        data = (num + System.lineSeparator()) + data;
                        String[] lines = data.split(System.lineSeparator());
                        writeMes("/AppHistory");
                        for (String line : lines) {
                            writeMes(line);
                        }
                        data = "";
                        num = 0;
                        listData.clear();
                        check = 0;
                    }
                } catch (Exception e) {

                }
            }

        }
        public void GetData() throws Exception{
            String machineName = InetAddress.getLocalHost().getHostName();
            Process[] list = null;
            if (machineName == null || machineName.isEmpty())
                list = ProcessHandle.allProcesses().toArray(Process[]::new);
            else
                list = ProcessHandle.allProcesses().filter(p -> p.info().command().isPresent() && p.info().command().get().contains(machineName)).toArray(Process[]::new);
            for (Process p : list) {
                int a = 0;
                for (int i = 0; i < listData.size(); i++) {
                    if (p.info().command().isPresent() && p.info().command().get().equals(listData.get(i))) {
                        a = 1;
                        break;
                    }
                }
                if (a == 0) {
                    data += (p.info().command().orElse("") + System.lineSeparator());
                    num++;
                    listData.add(p.info().command().orElse(""));
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