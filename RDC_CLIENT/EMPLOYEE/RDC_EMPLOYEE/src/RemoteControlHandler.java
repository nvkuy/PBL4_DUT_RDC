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
    private static final int SLEEP_TIME = (int)(1000.0 / FPS);
    private static final float IMAGE_QUALITY = 0.3f;

    private AES aes;
    private String targetIP;
    private DatagramSocket employeeSocket;
    private InetAddress inetAddress;
    private Rectangle area;

    private int sendFPS = 0;

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

                    Thread.sleep(SLEEP_TIME);

                    Thread screenSender = new Thread(new ScreenSender());
                    screenSender.start();

                } catch (Exception e) {
//                    e.printStackTrace();
                }

            }

        }

        private class ScreenSender implements Runnable {

            private String numberEncode(long num, int digit) {

                StringBuilder tmp = new StringBuilder(num + "");
                while (tmp.length() < digit)
                    tmp.insert(0, '0');
                return tmp.toString();

            }

            private void sendImagePart(String data) {

                Thread imagePartSender = new Thread(new ImagePartSender(data.getBytes()));
                imagePartSender.start();

            }

            @Override
            public void run() {

                try {

                    String curTimeID = numberEncode(System.currentTimeMillis(), 18);

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
                    String imgStr = AES.encode(data);
                    byte[] IV = aes.generateIV();
                    String crypImgStr = aes.encrypt(imgStr, IV);
                    String IVStr = AES.getIVStr(IV);

                    byte[] imgData = AES.decode(crypImgStr);
                    int numOfPart = (imgData.length + DATA_SIZE - 1) / DATA_SIZE;

                    String header = curTimeID + numberEncode(0, 3) + numberEncode(numOfPart, 3) + IVStr;
                    sendImagePart(header);

                    for (int id = 1; id <= numOfPart; id++) {
                        int start = (id - 1) * DATA_SIZE;
                        int end = Math.min(imgData.length, start + DATA_SIZE);
                        byte[] part = Arrays.copyOfRange(imgData, start, end);
                        String partStr = AES.encode(part);

                        String packetStr = curTimeID + numberEncode(id, 3) + partStr;

                        // TODO: Implement thread pool later..
                        sendImagePart(packetStr);
                    }

                    sendFPS++;

                } catch (Exception e) {
//                    e.printStackTrace();
                }

            }

            private class ImagePartSender implements Runnable {

                byte[] data;

                public ImagePartSender(byte[] data) {
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
