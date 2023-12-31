import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RemoteControlHandler implements Runnable {

    private static final int SCREEN_PORT = 6969;
    private static final int COMMAND_PORT = 8888;
    private static final int DATA_SIZE = 1 << 14; // TODO: Will try 1 << 16
    private static final int FPS = 24;
    private static final int SLEEP_BETWEEN_FRAME = 1000 / FPS;
    private static final float IMAGE_QUALITY = 0.2f;
    private final int TARGET_SCREEN_WIDTH;
    private final int TARGET_SCREEN_HEIGHT;

    private final AES aes;
    private final String targetIP;
    private DatagramSocket employeeUDPSocket;
    private InetAddress inetAddress;
    private Rectangle area;

    private int sendFPS = 0;
    private long packetSent = 0;
    private long sumPacketSize = 0;
    private static final boolean BENCHMARK = true;
    private long timeDiff = 0;
    private Socket employeeTCPSocket;
    private boolean isRunning = true;
    private static final int SYNC_ROUND = 15;

    /*

    image packet structure:

    - first 8 bytes: timeID
    - next 2 bytes: partID (0 if it is header)
    - if packet is header:
        + next 2 bytes: number of parts which image was divided
        + other bytes: IV
    - else: image part data

     */

    public RemoteControlHandler(String key, String ip, Integer targetScreenWidth, Integer targetScreenHeight) {
        this.aes = new AES(key);
        this.targetIP = ip;
        this.TARGET_SCREEN_WIDTH = targetScreenWidth;
        this.TARGET_SCREEN_HEIGHT = targetScreenHeight;
    }

    @Override
    public void run() {

        try {

            inetAddress = InetAddress.getByName(targetIP);

            ServerSocket socket = new ServerSocket(COMMAND_PORT);
            while (true) {
                employeeTCPSocket = socket.accept();
                if (employeeTCPSocket.getInetAddress().getHostAddress().equals(inetAddress.getHostAddress())) {
                    Thread controlSignalHandler = new Thread(new ControlSignalHandler());
                    controlSignalHandler.start();
                    break;
                }
            }

            employeeUDPSocket = new DatagramSocket(SCREEN_PORT);

            System.out.println("RDC: " + inetAddress.getHostAddress());

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            area = new Rectangle(0, 0, (int) screenSize.getWidth(), (int) screenSize.getHeight());

            Thread screenHandler = new Thread(new ScreenShareHandler());
            screenHandler.start();

            if (BENCHMARK) {
                Thread benchmarkFPS = new Thread(new BenchmarkFPS());
                benchmarkFPS.start();
            }

        } catch (Exception e) {
//            e.printStackTrace();
        }

    }

    private class ControlSignalHandler implements Runnable {

        private BufferedReader inp;
        private PrintWriter out;
        private BlockingQueue<String> mouseSignals;
        private BlockingQueue<String> keySignals;

        @Override
        public void run() {

            try {

                isRunning = true;
                inp = new BufferedReader(new InputStreamReader(employeeTCPSocket.getInputStream()));
                out = new PrintWriter(employeeTCPSocket.getOutputStream(), true);

                syncTime();

                mouseSignals = new LinkedBlockingQueue<>();
                keySignals = new LinkedBlockingQueue<>();

                Thread mouseSignalHandler = new Thread(new MouseSignalHandler());
                mouseSignalHandler.start();

                Thread keySignalHandler = new Thread(new KeySignalHandler());
                keySignalHandler.start();

                while (isRunning) {

                    String signal = readMes();

                    if (signal.charAt(0) == 'M') mouseSignals.put(signal);
                    else keySignals.put(signal);

                }

            } catch (Exception e) {
                shutdown();
            }

        }

        private class MouseSignalHandler implements Runnable {

            private Robot robot;

            @Override
            public void run() {

                try {

                    robot = new Robot();

                    while (isRunning) {

                        String mouseSignal = mouseSignals.take();
                        String[] signal = mouseSignal.split(" ");

                        if (signal[1].equals("M")) { // Move

                            int x = Integer.parseInt(signal[2]);
                            int y = Integer.parseInt(signal[3]);
                            int x_real = (x * area.width) / TARGET_SCREEN_WIDTH;
                            int y_real = (y * area.height) / TARGET_SCREEN_HEIGHT;
                            robot.mouseMove(x_real, y_real);

                        } else if (signal[1].equals("P")) { // Press

                            int btn = Integer.parseInt(signal[2]);
                            robot.mousePress(InputEvent.getMaskForButton(btn));

                        } else if (signal[1].equals("R")) { // Release

                            int btn = Integer.parseInt(signal[2]);
                            robot.mouseRelease(InputEvent.getMaskForButton(btn));

                        } else {
                            //..
                        }

                    }

                } catch (Exception ignored) {
                }

            }
        }

        private class KeySignalHandler implements Runnable {

            private Robot robot;

            @Override
            public void run() {

                try {

                    robot = new Robot();

                    while (isRunning) {

                        String keySignal = keySignals.take();
                        String[] signal = keySignal.split(" ");

                        if (signal[1].equals("P")) { // Press

                            int keyCode = Integer.parseInt(signal[2]);
                            try { // skip utf-8 character..
                                robot.keyPress(keyCode);
                                if (keyCode == KeyEvent.VK_WINDOWS)
                                    robot.keyRelease(keyCode);
                            } catch (Exception ignored) {
                            }

                        } else if (signal[1].equals("R")) { // Release

                            int keyCode = Integer.parseInt(signal[2]);
                            try { // skip utf-8 character..
                                robot.keyRelease(keyCode);
                            } catch (Exception ignored) {
                            }

                        } else {
                            //..
                        }

                    }

                } catch (Exception ignored) {
                }

            }
        }

        private void syncTime() throws Exception {

            long sumTimeDiff = 0;
            for (int i = 0; i < SYNC_ROUND; i++) {
                long time1 = System.currentTimeMillis();
                writeMes(String.valueOf(time1));
                long time2 = Long.parseLong(readMes());
                long time3 = System.currentTimeMillis();
                long travelTime = (time3 - time1) / 2;
                sumTimeDiff += (time2 - travelTime - time1);
            }
            timeDiff = sumTimeDiff / SYNC_ROUND;

            System.out.println("TimeDiff: " + timeDiff);

        }

        public String readMes() throws Exception {

            String IVStr = inp.readLine();
            String crypMes = inp.readLine();
            byte[] IV = Util.strToByte(IVStr);
            return aes.decrypt(crypMes, IV);

        }

        public void writeMes(String mes) throws Exception {

            if (mes == null || mes.isEmpty()) mes = " ";

            byte[] IV = aes.generateIV();
            String crypMes = aes.encrypt(mes, IV);
            String IVStr = Util.byteToStr(IV);
            out.println(IVStr);
            out.println(crypMes);

        }

    }

    public void shutdown() {
        System.out.println("Shutdown remote control..");
        isRunning = false;
        try {
            employeeTCPSocket.close();
            employeeUDPSocket.close();
        } catch (Exception ignored) {
        }
    }

    private class BenchmarkFPS implements Runnable {

        @Override
        public void run() {

            while (isRunning) {
                try {
                    Thread.sleep(1000);
                    if (packetSent > 0)
                        System.out.println("SendFramePerSec: " + sendFPS + " - SendPerSec: " + sumPacketSize + " (bytes)" + " - AvgPacketSize: " + (sumPacketSize / packetSent) + " (bytes)");
                    sumPacketSize = packetSent = sendFPS = 0;
                } catch (Exception e) {

                }
            }

        }

    }

    private class ScreenShareHandler implements Runnable {

        @Override
        public void run() {

            while (isRunning) {

                try {

                    Thread.sleep(SLEEP_BETWEEN_FRAME);

                    Thread screenSender = new Thread(new ScreenSender());
                    screenSender.start();

                } catch (Exception e) {
//                    e.printStackTrace();
                }

            }

        }

        private class ScreenSender implements Runnable {

            @Override
            public void run() {

                try {

                    BufferedImage image = new Robot().createScreenCapture(area);
                    byte[] curTimeID = Util.longToBytes(System.currentTimeMillis() + timeDiff, 8);

                    if (image.getWidth() > TARGET_SCREEN_WIDTH)
                        image = Util.resizeImage(image, TARGET_SCREEN_WIDTH, TARGET_SCREEN_HEIGHT);
                    byte[] data = Util.compressImgToByte(image, IMAGE_QUALITY);

                    byte[] IV = aes.generateIV();
                    byte[] cryptImg = aes.encrypt(data, IV);

                    int numOfPart = (cryptImg.length + DATA_SIZE - 1) / DATA_SIZE;
//                    System.out.println("Packet: " + numOfPart + " " + data.length);

                    byte[] header = Util.concat(curTimeID, Util.longToBytes(0, 2), Util.longToBytes(numOfPart, 2), IV);
                    sendImagePart(header);

                    for (int id = 1; id <= numOfPart; id++) {
                        int start = (id - 1) * DATA_SIZE;
                        int end = Math.min(cryptImg.length, start + DATA_SIZE);
                        byte[] part = Arrays.copyOfRange(cryptImg, start, end);
                        byte[] packetData = Util.concat(curTimeID, Util.longToBytes(id, 2), part);
                        // TODO: Implement thread pool later..
                        sendImagePart(packetData);
                    }

                    if (BENCHMARK) sendFPS++;

                } catch (Exception e) {
//                    e.printStackTrace();
                }

            }

            private void sendImagePart(byte[] data) throws Exception {

                if (BENCHMARK) {
                    sumPacketSize += data.length;
                    packetSent++;
                }

                Thread imagePartSender = new Thread(new ImagePartSender(data));
                imagePartSender.start();

            }

            private class ImagePartSender implements Runnable {

                private final byte[] data;

                public ImagePartSender(byte[] data) {
//                    if (data.length >= PACKET_SIZE)
//                        System.out.println(data.length);
                    this.data = data;
                }

                @Override
                public void run() {

                    try {
                        DatagramPacket sendPacket = new DatagramPacket(data, data.length, inetAddress, SCREEN_PORT);
                        employeeUDPSocket.send(sendPacket);
                    } catch (Exception e) {
//                        e.printStackTrace();
                    }

                }

            }

        }

    }

}
