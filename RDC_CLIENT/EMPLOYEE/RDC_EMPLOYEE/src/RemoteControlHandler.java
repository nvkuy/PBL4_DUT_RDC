import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Base64;
import java.util.Iterator;

public class RemoteControlHandler implements Runnable {

    private AES aes;
    private String targetIP;
    private final Integer PORT = 6969;

    byte[] receiveData;
    byte[] sendData;

    DatagramSocket employeeSocket;
    DatagramPacket receivePacket;

    InetAddress inetAddress;

    Robot robot;
    ImageWriter writer;
    Rectangle area;
    ImageWriteParam param;
    ByteArrayOutputStream os;

    public RemoteControlHandler(String key, String ip) throws Exception {
        this.aes = new AES(key);
        this.targetIP = ip;
    }

    @Override
    public void run() {

        try {

            // TODO: Divide package and send.. will do if have time..
            receiveData = new byte[1 << 8];
            sendData = new byte[1 << 18];

            employeeSocket = new DatagramSocket(PORT);
            receivePacket = new DatagramPacket(receiveData, receiveData.length);

            inetAddress = InetAddress.getByName(targetIP);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            try {
                robot = new Robot();
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }
            float quality = 0.3f;
            area = new Rectangle(0, 0, (int)screenSize.getWidth(), (int)screenSize.getHeight());
            os = new ByteArrayOutputStream();
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            writer = writers.next();
            ImageOutputStream ios = null;
            try {
                ios = ImageIO.createImageOutputStream(os);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            writer.setOutput(ios);
            param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);

            Thread screenHandler = new Thread(new ScreenHandler());
            screenHandler.start();

        } catch (Exception e) { }

    }

    private class ScreenHandler implements Runnable {

        private final int FPS = 30;
        private final int SLEEP_TIME = (int)(1000.0 / FPS);

        @Override
        public void run() {

            while (true) {

                try {

                    Thread.sleep(SLEEP_TIME);
                    BufferedImage image = robot.createScreenCapture(area);
                    writer.write(null, new IIOImage(image, null, null), param);
                    byte[] data = os.toByteArray();
                    String imgStr = Base64.getEncoder().encodeToString(data);
                    byte[] IV = aes.generateIV();
                    String crypImgStr = aes.encrypt(imgStr, IV);
                    String IVStr = AES.getIVStr(IV);
                    String rawData = IVStr + crypImgStr;
                    sendData = rawData.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, inetAddress, PORT);
                    employeeSocket.send(sendPacket);

                } catch (Exception e) { }

            }

        }
    }

}
