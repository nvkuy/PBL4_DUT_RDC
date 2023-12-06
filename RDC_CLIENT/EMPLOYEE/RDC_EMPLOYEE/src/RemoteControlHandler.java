import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Iterator;

public class RemoteControlHandler implements Runnable {

    private static final int PORT = 6969;
    private static final int PACKET_SIZE = 1 << 15;
    private static final int DATA_SIZE = 1 << 14;
    private static final int FPS = 24;
    private static final int SLEEP_BETWEEN_FRAME = (int)(1000.0 / FPS);
    private static final float IMAGE_QUALITY = 0.3f;
    private final int TARGET_SCREEN_WIDTH;
    private final int TARGET_SCREEN_HEIGHT;

    private final AES aes;
    private final String targetIP;
    private DatagramSocket employeeSocket;
    private InetAddress inetAddress;
    private Rectangle area;

    private int sendFPS = 0;
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

    public RemoteControlHandler(String key, String ip, Integer targetScreenWidth, Integer targetScreenHeight) {
        this.aes = new AES(key);
        this.targetIP = ip;
        this.TARGET_SCREEN_WIDTH = targetScreenWidth;
        this.TARGET_SCREEN_HEIGHT = targetScreenHeight;
    }

    @Override
    public void run() {

        try {


            employeeSocket = new DatagramSocket(PORT);
            inetAddress = InetAddress.getByName(targetIP);

            System.out.println("RDC: " + inetAddress.getHostAddress());

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            area = new Rectangle(0, 0, (int)screenSize.getWidth(), (int)screenSize.getHeight());

            Thread screenHandler = new Thread(new ScreenShareHandler());
            screenHandler.start();

            Thread controlSignalHandler = new Thread(new ControlSignalHandler());
            controlSignalHandler.start();

            if (BENCHMARK) {
                Thread benchmarkFPS = new Thread(new BenchmarkFPS());
                benchmarkFPS.start();
            }

        } catch (Exception e) {
//            e.printStackTrace();
        }

    }

    private class BenchmarkFPS implements Runnable {

        @Override
        public void run() {

            while (true) {
                try {
                    Thread.sleep(1000);
                    System.out.println(sendFPS);
                    sendFPS = 0;
                } catch (Exception e) {

                }
            }

        }

    }

    private class ControlSignalHandler implements Runnable {

        @Override
        public void run() {

            while (true) {

                try {

                    byte[] receiveData = new byte[PACKET_SIZE];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    employeeSocket.receive(receivePacket);
                    if (!receivePacket.getAddress().getHostAddress().equals(targetIP)) continue;

                    // TODO: handle control signals from admin..

                } catch (Exception e) {
//                    e.printStackTrace();
                }

            }

        }

    }

    private class ScreenShareHandler implements Runnable {

        @Override
        public void run() {

            while (true) {

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

                    Robot robot = new Robot();
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
                    ImageWriter writer = writers.next();
                    ImageOutputStream ios = ImageIO.createImageOutputStream(os);
                    writer.setOutput(ios);
                    ImageWriteParam param = writer.getDefaultWriteParam();
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(IMAGE_QUALITY);

                    long curTimeID = System.currentTimeMillis();

                    BufferedImage image = robot.createScreenCapture(area);
                    BufferedImage resizedImg = Util.resizeImage(image, TARGET_SCREEN_WIDTH, TARGET_SCREEN_HEIGHT);
                    writer.write(null, new IIOImage(resizedImg, null, null), param);

                    byte[] data = os.toByteArray();
                    Thread imageSender = new Thread(new ImageSender(curTimeID, data));
                    imageSender.start();

                } catch (Exception e) {
//                    e.printStackTrace();
                }

            }

            private class ImageSender implements Runnable {

                private byte[] data;
                private byte[] curTimeID;

                public ImageSender(long curTimeID, byte[] data) {
                    this.data = data;
                    this.curTimeID = Util.longToBytes(curTimeID, 8);
                }

                private void sendImagePart(byte[] data) {

                    Thread imagePartSender = new Thread(new ImagePartSender(data));
                    imagePartSender.start();

                }

                @Override
                public void run() {

                    try {

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

                        sendFPS++;

                    } catch (Exception e) {
//                        e.printStackTrace();
                    }

                }

                private class ImagePartSender implements Runnable {

                    private byte[] data;

                    public ImagePartSender(byte[] data) {
//                    if (data.length >= PACKET_SIZE)
//                        System.out.println(data.length);
                        this.data = data;
                    }

                    @Override
                    public void run() {

                        try {
                            DatagramPacket sendPacket = new DatagramPacket(data, data.length, inetAddress, PORT);
                            employeeSocket.send(sendPacket);
                        } catch (Exception e) {
//                        e.printStackTrace();
                        }

                    }

                }

            }

        }

    }

}
