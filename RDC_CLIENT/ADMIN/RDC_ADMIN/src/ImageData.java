import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

public class ImageData {

    /*

    image packet structure:

    - first 8 bytes: timeID
    - next 2 bytes: partID (0 if it is header)
    - if packet is header:
        + next 2 bytes: number of parts which image was divided
        + other bytes: IV
    - else: image part data

     */

    private int numOfPart;
    private int imgByteLen;
    private int partReceived;
    private byte[] IV;
    private byte[][] imagePart;

    private ReentrantLock lock;

    public ImageData() {

        lock = new ReentrantLock(true);

        numOfPart = imgByteLen = partReceived = 0;
        imagePart = new byte[8][];

    }

    public void addPart(byte[] rawData) {

        try {

            lock.lock();

            int partID = (int)Util.bytesToLong(Arrays.copyOfRange(rawData, 8, 10));
            if (partID == 0) { // header

                IV = Arrays.copyOfRange(rawData, 12, rawData.length);
                numOfPart = (int)Util.bytesToLong(Arrays.copyOfRange(rawData, 10, 12));

            } else { // normal image part

                if (imagePart[partID] != null) return; // udp package can be duplicate
                byte[] part = Arrays.copyOfRange(rawData, 10, rawData.length);
                imagePart[partID] = part;
                imgByteLen += part.length;
                partReceived++;

            }

        } finally {
            lock.unlock();
        }

    }

    public boolean isCompleted() {
        return (numOfPart > 0) && (numOfPart == partReceived);
    }

    public BufferedImage getImage(AES aes) throws Exception {

        byte[] data = new byte[imgByteLen];
        int i = 0;
        for (int k = 1; k <= numOfPart; k++) {
            for (int j = 0; j < imagePart[k].length; j++) {
                data[i] = imagePart[k][j];
                i++;
            }
        }

        InputStream is = new ByteArrayInputStream(aes.decrypt(data, IV));
        return ImageIO.read(is);

    }

}
