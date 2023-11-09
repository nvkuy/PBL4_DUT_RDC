import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class RemoteControlHandler implements Runnable {

    private AES aes;
    private String targetIP;
    private Graphics g;
    private final Integer PORT = 6969;

    byte[] receiveData;
    byte[] sendData;

    DatagramSocket adminSocket;
    DatagramPacket receivePacket;

    public RemoteControlHandler(String key, String ip, Graphics g) throws Exception {
        this.aes = new AES(key);
        this.targetIP = ip;
        this.g = g;
    }

    @Override
    public void run() {

        try {

            // TODO: Divide package and send.. will do if have time..
            receiveData = new byte[1 << 18];
            sendData = new byte[1 << 8];

            adminSocket = new DatagramSocket(PORT);
            receivePacket = new DatagramPacket(receiveData, receiveData.length);

            while (true) {

                adminSocket.receive(receivePacket);
                if (!receivePacket.getAddress().getHostAddress().equals(targetIP)) continue;
                Thread screenHandler = new Thread(new ScreenHandler());
                screenHandler.start();

            }

        } catch (Exception e) { }

    }

    private class ScreenHandler implements Runnable {

        @Override
        public void run() {

            try {
                String rawData = new String(receivePacket.getData());
                String IVStr = rawData.substring(0, 16);
                byte[] IV = AES.getIVFromStr(IVStr);
                String crypImg = rawData.substring(16);
                String imgStr = aes.decrypt(crypImg, IV);
                InputStream is = new ByteArrayInputStream(imgStr.getBytes());
                BufferedImage img = ImageIO.read(is);
                g.drawImage(img, 0, 0, null);
            } catch (Exception e) { }

        }
    }

}
