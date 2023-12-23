import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class RemoteControlHandler implements Runnable {

    private final AES aes;
    private final String targetIP;
    private static final int SCREEN_PORT = 6969;
    private static final int COMMAND_PORT = 8888;
    private static final int PACKET_SIZE = 1 << 15;
    private static final long MAX_DELAY = 1000;
    private ImageQueue frameQueue;
    private DatagramSocket adminUDPSocket;
    private Socket adminTCPSocket;
    private InetAddress inetAddress;
    private RemoteControlDetail mRemoteControl;

    private int paintFramePerSecond = 0;
    private long sumDelay = 0;
    private int packetCnt = 0;
    private static final boolean BENCHMARK = true;
    private boolean isRunning = true;
    private ControlSignalQueue controlSignalQueue;

    /*

    image packet structure:

    - first 8 bytes: timeID
    - next 2 bytes: partID (0 if it is header)
    - if packet is header:
        + next 2 bytes: number of parts which image was divided
        + other bytes: IV
    - else: image part data

     */

    public RemoteControlHandler(String key, String ip, RemoteControlDetail mRemoteControl, ControlSignalQueue controlSignalQueue) {
        this.aes = new AES(key);
        this.targetIP = ip;
        this.mRemoteControl = mRemoteControl;
        this.controlSignalQueue = controlSignalQueue;
    }

    @Override
    public void run() {

        try {

            inetAddress = InetAddress.getByName(targetIP);

            adminTCPSocket = new Socket(targetIP, COMMAND_PORT);

            adminUDPSocket = new DatagramSocket(SCREEN_PORT);

            System.out.println("RDC: " + inetAddress.getHostAddress());

            frameQueue = new ImageQueue(MAX_DELAY);

            Thread controlSignalHandler = new Thread(new ControlSignalHandler());
            controlSignalHandler.start();

            Thread screenReceiver = new Thread(new ScreenReceiver());
            screenReceiver.start();

            Thread screenRender = new Thread(new ScreenRender());
            screenRender.start();

            if (BENCHMARK) {
                Thread benchmarkFPS = new Thread(new BenchmarkFPS());
                benchmarkFPS.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class ControlSignalHandler implements Runnable {
        private BufferedReader inp;
        private PrintWriter out;

        @Override
        public void run() {

            try {

                isRunning = true;
                inp = new BufferedReader(new InputStreamReader(adminTCPSocket.getInputStream()));
                out = new PrintWriter(adminTCPSocket.getOutputStream(), true);

                syncTime();

                while (isRunning) {

                    String signal = controlSignalQueue.getNext();
                    if (signal != null) writeMes(signal);

                }

            } catch (Exception e) {
                shutdown();
            }

        }

        private void syncTime() throws Exception {
            readMes();
            writeMes(String.valueOf(System.currentTimeMillis()));
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

    }

    public void shutdown() {
        isRunning = false;
        try {
            adminTCPSocket.close();
            adminUDPSocket.close();
        } catch (IOException e) {
        }
    }

    private class BenchmarkFPS implements Runnable {

        @Override
        public void run() {

            while (isRunning) {
                try {
                    Thread.sleep(1000);
                    if (packetCnt > 0) {
                        System.out.println("FPS: " + paintFramePerSecond
                                + " - AVG DELAY: " + (sumDelay / packetCnt));
                    }
                    paintFramePerSecond = 0;
                    sumDelay = 0;
                    packetCnt = 0;
                } catch (Exception e) {
                }
            }

        }

    }

    private class ScreenRender implements Runnable {

        @Override
        public void run() {

            while (isRunning) {

                try {

                    Thread.sleep(2);

                    BufferedImage img = frameQueue.getNextImage(aes);
                    if (img == null) continue;

//                    long t1 = System.currentTimeMillis();
                    mRemoteControl.screen.display(img);
//                    long t2 = System.currentTimeMillis();
//                    System.out.println("Draw time: " + (t2 - t1));

                    if (BENCHMARK)
                        paintFramePerSecond++;

                    Thread.sleep(2);

                } catch (Exception e) {
//                    e.printStackTrace();
                }

            }

        }
    }

    private class ScreenReceiver implements Runnable {

        @Override
        public void run() {

            while (isRunning) {

                try {

                    byte[] receiveData = new byte[PACKET_SIZE];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    adminUDPSocket.receive(receivePacket);
                    if (!receivePacket.getAddress().getHostAddress().equals(targetIP)) continue;

                    Thread packetDataProcessor = new Thread(new PacketDataProcessor(receivePacket.getData(), receivePacket.getLength()));
                    packetDataProcessor.start();

                } catch (Exception e) {
//                    e.printStackTrace();
                }


            }

        }

        private class PacketDataProcessor implements Runnable {

            private final byte[] rawData;
            private final int length;

            public PacketDataProcessor(byte[] rawData, int length) {
                this.rawData = rawData;
                this.length = length;
            }

            @Override
            public void run() {

                try {

                    if (BENCHMARK) {
                        sumDelay += System.currentTimeMillis() - Util.bytesToLong(Arrays.copyOfRange(rawData, 0, 8));
                        packetCnt++;
                    }
                    frameQueue.push(Arrays.copyOfRange(rawData, 0, length));

                } catch (Exception e) {
//                    e.printStackTrace();
                }

            }

        }

    }

}
