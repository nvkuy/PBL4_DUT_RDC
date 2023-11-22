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
    private static final int TIME_RANGE = 1 << 16;
    private static final int FPS = 24;
    private static final int SLEEP_BETWEEN_FRAME = (int)(1000.0 / FPS);
    private static final float IMAGE_QUALITY = 0.3f;

    private final AES aes;
    private final String targetIP;
    private DatagramSocket employeeSocket;
    private InetAddress inetAddress;
    private Rectangle area;

    private int sendFPS = 0;

    /*

    image packet structure:

    - first 2 bytes: timeID (current time millisecond % TIME_RANGE)
    - next 2 bytes: partID (0 if it is header)
    - if packet is header:
        + next 2 bytes: number of parts which image was divided
        + next 16 bytes: IV
    - else: image part data

     */

    public RemoteControlHandler(String key, String ip) {
        this.aes = new AES(key);
        this.targetIP = ip;
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

            Thread benchmarkFPS = new Thread(new BenchmarkFPS());
            benchmarkFPS.start();

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

            private static byte[] intToBytes(int num) {
                byte[] result = new byte[2];
                for (int i = 1; i >= 0; i--) {
                    result[i] = (byte)(num & 0xFF);
                    num >>= 8;
                }
                return result;
            }

            private static byte[] concat(byte[]... arrs) {

                int length = 0;
                for (byte[] arr : arrs)
                    length += arr.length;
                byte[] res = new byte[length];
                int i = 0;
                for (byte[] arr : arrs) {
                    for (byte b : arr) {
                        res[i] = b;
                        i++;
                    }
                }

                return res;

            }

            private void sendImagePart(byte[] data) {

                Thread imagePartSender = new Thread(new ImagePartSender(data));
                imagePartSender.start();

            }

            @Override
            public void run() {

                try {

                    byte[] curTimeID = intToBytes((int)System.currentTimeMillis() % TIME_RANGE);

                    Robot robot = new Robot();
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
                    ImageWriter writer = writers.next();
                    ImageOutputStream ios = ImageIO.createImageOutputStream(os);
                    writer.setOutput(ios);
                    ImageWriteParam param = writer.getDefaultWriteParam();
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(IMAGE_QUALITY);

                    BufferedImage image = robot.createScreenCapture(area);
                    writer.write(null, new IIOImage(image, null, null), param);
                    byte[] data = os.toByteArray();
                    byte[] IV = aes.generateIV();
                    byte[] crypImg = aes.encrypt(data, IV);

                    int numOfPart = (crypImg.length + DATA_SIZE - 1) / DATA_SIZE;
//                    System.out.println("Packet: " + numOfPart + " " + data.length);

                    byte[] header = concat(curTimeID, intToBytes(0), intToBytes(numOfPart), IV);
                    sendImagePart(header);

                    for (int id = 1; id <= numOfPart; id++) {
                        int start = (id - 1) * DATA_SIZE;
                        int end = Math.min(crypImg.length, start + DATA_SIZE);
                        byte[] part = Arrays.copyOfRange(crypImg, start, end);
                        byte[] packetData = concat(curTimeID, intToBytes(id), part);

                        // TODO: Implement thread pool later..
                        sendImagePart(packetData);
                    }

                    sendFPS++;

                } catch (Exception e) {
//                    e.printStackTrace();
                }

            }

            private class ImagePartSender implements Runnable {

                byte[] data;

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
