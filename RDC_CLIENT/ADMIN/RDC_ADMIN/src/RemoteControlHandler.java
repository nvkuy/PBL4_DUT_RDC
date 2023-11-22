import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListMap;

public class RemoteControlHandler implements Runnable {

    private final AES aes;
    private final String targetIP;
    private static final int PORT = 6969;
    private static final int PACKET_SIZE = 1 << 15;
    private static final long TIME_RANGE = 1 << 16;
    private static final int MAX_DELAY = 500;
    private ConcurrentSkipListMap<Integer, ImageData> frameQueue;
    private DatagramSocket adminSocket;
    private InetAddress inetAddress;
    private final TestRemoteControl testRemoteControl;

    private int paintFramePerSecond = 0;
    private int lateFramePerSecond = 0;

    /*

    image packet structure:

    - first 2 bytes: timeID (current time millisecond % TIME_RANGE)
    - next 2 bytes: partID (0 if it is header)
    - if packet is header:
        + next 2 bytes: number of parts which image was divided
        + other bytes: IV
    - else: image part data

     */

    public RemoteControlHandler(String key, String ip, TestRemoteControl testRemoteControl) {
        this.aes = new AES(key);
        this.targetIP = ip;
        this.testRemoteControl = testRemoteControl;
    }

    @Override
    public void run() {

        try {

            adminSocket = new DatagramSocket(PORT);
            inetAddress = InetAddress.getByName(targetIP);

            System.out.println("RDC: " + inetAddress.getHostAddress());

            frameQueue = new ConcurrentSkipListMap<>(Comparator.comparingInt(o -> o));

            Thread screenReceiver = new Thread(new ScreenReceiver(this));
            screenReceiver.start();

            Thread screenRender = new Thread(new ScreenRender(this));
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

        private final RemoteControlHandler remoteControlHandler;
        public ScreenRender(RemoteControlHandler remoteControlHandler) {
            this.remoteControlHandler = remoteControlHandler;
        }

        @Override
        public void run() {

            while (true) {

                try {

                    Thread.sleep(1);

                    long curTime = System.currentTimeMillis();
                    while (true) {
                        if (remoteControlHandler.frameQueue.isEmpty()) break;
                        int id = remoteControlHandler.frameQueue.firstKey();
                        if (curTime - id <= MAX_DELAY) break;
                        lateFramePerSecond++;
                        remoteControlHandler.frameQueue.remove(id);
                    }

                    if (remoteControlHandler.frameQueue.isEmpty()) continue;
                    int frameID = remoteControlHandler.frameQueue.firstKey();
                    if (!remoteControlHandler.frameQueue.get(frameID).isCompleted()) continue;

                    testRemoteControl.screen.display(remoteControlHandler.frameQueue.get(frameID).getImage(aes));
                    paintFramePerSecond++;
                    remoteControlHandler.frameQueue.remove(frameID);

                    Thread.sleep(4);

                } catch (Exception e) {
//                    e.printStackTrace();
                }

            }

        }
    }

    private class ScreenReceiver implements Runnable {

        private final RemoteControlHandler remoteControlHandler;

        public ScreenReceiver(RemoteControlHandler remoteControlHandler) {
            this.remoteControlHandler = remoteControlHandler;
        }

        @Override
        public void run() {

            while (true) {

                try {

                    byte[] receiveData = new byte[PACKET_SIZE];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    adminSocket.receive(receivePacket);
                    if (!receivePacket.getAddress().getHostAddress().equals(targetIP)) continue;

                    Thread packetDataProcessor = new Thread(new PacketDataProcessor(remoteControlHandler, receivePacket.getData(), receivePacket.getLength()));
                    packetDataProcessor.start();

                } catch (Exception e) {
//                    e.printStackTrace();
                }


            }

        }

        private class PacketDataProcessor implements Runnable {

            private final RemoteControlHandler remoteControlHandler;
            private final byte[] rawData;
            private final int length;

            private static int bytesToInt(final byte[] b) {
                int result = 0;
                for (int i = 0; i <= 1; i++) {
                    result <<= 8;
                    result |= (b[i] & 0xFF);
                }
                return result;
            }

            public PacketDataProcessor(RemoteControlHandler remoteControlHandler, byte[] rawData, int length) {
                this.rawData = rawData;
                this.length = length;
                this.remoteControlHandler = remoteControlHandler;
            }

            @Override
            public void run() {

                try {
                    int curTimeID = (int)(System.currentTimeMillis() % TIME_RANGE);
                    int timeID = bytesToInt(Arrays.copyOfRange(rawData, 0, 2));

//                    System.out.println(curTimeID - timeID);
                    if (curTimeID - timeID > MAX_DELAY) return;

                    if (!remoteControlHandler.frameQueue.containsKey(timeID))
                        remoteControlHandler.frameQueue.put(timeID, new ImageData());
                    remoteControlHandler.frameQueue.get(timeID).add(Arrays.copyOfRange(rawData, 2, length));

                } catch (Exception e) {
//                    e.printStackTrace();
                }

            }

        }

    }

}
