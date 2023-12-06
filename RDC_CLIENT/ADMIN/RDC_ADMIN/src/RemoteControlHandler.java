import java.awt.image.BufferedImage;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class RemoteControlHandler implements Runnable {

    private final AES aes;
    private final String targetIP;
    private static final int PORT = 6969;
    private static final int PACKET_SIZE = 1 << 15;
    private static final long MAX_DELAY = 1000;
    private ImageQueue frameQueue;
    private DatagramSocket adminSocket;
    private InetAddress inetAddress;
    private RemoteControlDetail mRemoteControl;

    private int paintFramePerSecond = 0;
    private long sumDelay = 0;
    private int packetCnt = 0;
    private static final boolean BENCHMARK = true;

    /*

    image packet structure:

    - first 8 bytes: timeID
    - next 2 bytes: partID (0 if it is header)
    - if packet is header:
        + next 2 bytes: number of parts which image was divided
        + other bytes: IV
    - else: image part data

     */

    public RemoteControlHandler(String key, String ip, RemoteControlDetail mRemoteControl) {
        this.aes = new AES(key);
        this.targetIP = ip;
        this.mRemoteControl = mRemoteControl;
    }

    @Override
    public void run() {

        try {

            adminSocket = new DatagramSocket(PORT);
            inetAddress = InetAddress.getByName(targetIP);

            System.out.println("RDC: " + inetAddress.getHostAddress());

            frameQueue = new ImageQueue(MAX_DELAY);

            Thread screenReceiver = new Thread(new ScreenReceiver());
            screenReceiver.start();

            Thread screenRender = new Thread(new ScreenRender());
            screenRender.start();

            Thread controlSignalSender = new Thread(new ControlSignalSender());
            controlSignalSender.start();

            if (BENCHMARK) {
                Thread benchmarkFPS = new Thread(new BenchmarkFPS());
                benchmarkFPS.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class BenchmarkFPS implements Runnable {

        @Override
        public void run() {

            while (true) {
                try {
                    Thread.sleep(1000);
                    System.out.println("FPS: " + paintFramePerSecond
                            + " - AVG DELAY: " + (sumDelay / packetCnt));
                    paintFramePerSecond = 0;
                    sumDelay = 0;
                    packetCnt = 0;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

    }

    private class ControlSignalSender implements Runnable {

        @Override
        public void run() {

            // TODO: capture mouse position and action, send to employee..

        }
    }

    private class ScreenRender implements Runnable {

        @Override
        public void run() {

            while (true) {

                try {

                    Thread.sleep(2);

                    BufferedImage img = frameQueue.getNextImage(aes);
                    if (img == null) continue;

                    mRemoteControl.screen.display(img);
                    if (BENCHMARK)
                        paintFramePerSecond++;

                    Thread.sleep(2);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }

    private class ScreenReceiver implements Runnable {

        @Override
        public void run() {

            while (true) {

                try {

                    byte[] receiveData = new byte[PACKET_SIZE];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    adminSocket.receive(receivePacket);
                    if (!receivePacket.getAddress().getHostAddress().equals(targetIP)) continue;

                    Thread packetDataProcessor = new Thread(new PacketDataProcessor(receivePacket.getData(), receivePacket.getLength()));
                    packetDataProcessor.start();

                } catch (Exception e) {
                    e.printStackTrace();
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
                    e.printStackTrace();
                }

            }

        }

    }

}
