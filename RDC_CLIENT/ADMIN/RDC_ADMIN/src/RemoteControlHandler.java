import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.TreeMap;

public class RemoteControlHandler implements Runnable {

    private AES aes;
    private String targetIP;
    private final int PORT = 6969;
    private static final int PACKET_SIZE = 1 << 15;
    private static final int MAX_DELAY = 2000;
    private TreeMap<Long, ImageData> frameQueue;
    private DatagramSocket adminSocket;
    private InetAddress inetAddress;
    private RemoteControlDetail mRemoteControl;

    private int paintFramePerSecond = 0;
    private int lateFramePerSecond = 0;

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

            frameQueue = new TreeMap<>();

            Thread screenReceiver = new Thread(new ScreenReceiver());
            screenReceiver.start();

            Thread screenRender = new Thread(new ScreenRender());
            screenRender.start();

            Thread controlSignalSender = new Thread(new ControlSignalSender());
            controlSignalSender.start();

            Thread benchmarkFPS = new Thread(new BenchmarkFPS());
            benchmarkFPS.start();

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
                            + " - LATE: " + lateFramePerSecond
                            + " - ERROR: " + (24 - paintFramePerSecond - lateFramePerSecond));
                    lateFramePerSecond = 0;
                    paintFramePerSecond = 0;
                } catch (Exception e) {

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

                    Thread.sleep(1);

                    long curTime = System.currentTimeMillis();
                    while (true) {
                        if (frameQueue.isEmpty()) break;
                        long id = frameQueue.firstKey();
                        if (curTime - id <= MAX_DELAY) break;
                        lateFramePerSecond++;
                        frameQueue.remove(id);
                    }

                    if (frameQueue.isEmpty()) continue;
                    long frameID = frameQueue.firstKey();
                    if (!frameQueue.get(frameID).isCompleted()) continue;

                    mRemoteControl.screen.display(frameQueue.get(frameID).getImage(aes));
                    paintFramePerSecond++;
                    frameQueue.remove(frameID);

                    Thread.sleep(4);

                } catch (Exception e) {
//                    e.printStackTrace();
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
//                    e.printStackTrace();
                }


            }

        }

        private class PacketDataProcessor implements Runnable {

            private String rawData;
            private int length;

            public PacketDataProcessor(byte[] rawData, int length) {
                this.rawData = new String(rawData);
                this.length = length;
            }

            @Override
            public void run() {

                try {
                    long curTimeID = System.currentTimeMillis();
                    long timeID = Long.parseLong(rawData.substring(0, 18));
                    if (curTimeID - timeID > MAX_DELAY) return;

                    if (!frameQueue.containsKey(timeID))
                        frameQueue.put(timeID, new ImageData());
                    frameQueue.get(timeID).add(rawData.substring(18, length));
                } catch (Exception e) {
//                    e.printStackTrace();
                }

            }

        }

    }

}
