import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.TreeMap;

public class RemoteControlHandler implements Runnable {

    private AES aes;
    private String targetIP;
    private final Integer PORT = 6969;
    private static final int PACKAGE_SIZE = 256;
    private static final int MAX_DELAY = 500;
    private volatile TreeMap<Long, ImageData> frameQueue;
    private DatagramSocket adminSocket;
    private InetAddress inetAddress;
    private TestRemoteControl testRemoteControl;

    public RemoteControlHandler(String key, String ip, TestRemoteControl testRemoteControl) throws Exception {
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

            frameQueue = new TreeMap<>();

            Thread screenReceiver = new Thread(new ScreenReceiver());
            screenReceiver.start();

            Thread screenRender = new Thread(new ScreenRender());
            screenRender.start();

            Thread controlSignalSender = new Thread(new ControlSignalSender());
            controlSignalSender.start();

        } catch (Exception e) {
            e.printStackTrace();
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
                    Thread.sleep(7);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                long curTime = System.currentTimeMillis();
                while (!frameQueue.isEmpty()) {
                    long id = frameQueue.firstKey();
                    if (curTime - id > MAX_DELAY)
                        frameQueue.remove(id);
                }

                if (frameQueue.isEmpty()) continue;
                long frameID = frameQueue.firstKey();
                if (!frameQueue.get(frameID).isCompleted()) continue;

                try {

                    testRemoteControl.screen = frameQueue.get(frameID).getImage(aes);
                    testRemoteControl.repaint();
                    frameQueue.remove(frameID);
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

                    byte[] receiveData = new byte[PACKAGE_SIZE];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    adminSocket.receive(receivePacket);
                    if (!receivePacket.getAddress().getHostAddress().equals(targetIP)) continue;

                    Thread packageDataProcessor = new Thread(new PackageDataProcessor(receivePacket.getData()));
                    packageDataProcessor.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

        }

        private class PackageDataProcessor implements Runnable {

            private String rawData;

            public PackageDataProcessor(byte[] rawData) {
                this.rawData = new String(rawData);
            }

            @Override
            public void run() {

                long curTimeID = System.currentTimeMillis();
                long timeID = Long.parseLong(rawData.substring(0, 18));
                if (curTimeID - timeID > MAX_DELAY) return;

                if (!frameQueue.containsKey(timeID))
                    frameQueue.put(timeID, new ImageData());
                frameQueue.get(timeID).add(rawData.substring(18));

            }

        }

    }

}