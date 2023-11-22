import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

public class ImageData {
    private int numOfPart;
    private int imgByteLen;
    private int partReceived;
    private boolean haveHeader;
    private byte[] IV;

    private final byte[][] imagePart;
    private ImageData instance;

    private ImageData() {
        haveHeader = false;
        imgByteLen = 0;
        partReceived = 0;
        imagePart = new byte[16][];
    }

    private static int bytesToInt(final byte[] b) {
        int result = 0;
        for (int i = 0; i <= 1; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    public void add(byte[] raw_data) {

        if (instance == null)
            instance = new ImageData();

        int partID = bytesToInt(Arrays.copyOfRange(raw_data, 0, 2));
        if (partID == 0) { // header

            instance.numOfPart = bytesToInt(Arrays.copyOfRange(raw_data, 2, 4));
            instance.IV = Arrays.copyOfRange(raw_data, 4, raw_data.length);
            instance.haveHeader = true;

        } else { // normal image part

            if (instance.imagePart[partID] != null) return;
            byte[] part = Arrays.copyOfRange(raw_data, 2, raw_data.length);
            instance.imagePart[partID] = part;
            instance.imgByteLen += part.length;
            instance.partReceived++;

        }

    }

    public boolean isCompleted() {
        return (instance.partReceived == instance.numOfPart) && instance.haveHeader;
    }

    public BufferedImage getImage(AES aes) throws Exception {

        if (!instance.isCompleted())
            return null;

        byte[] data = new byte[imgByteLen];
        int i = 0;
        for (int k = 1; k <= instance.numOfPart; k++) {
            for (int j = 0; j < instance.imagePart[k].length; j++) {
                data[i] = instance.imagePart[k][j];
                i++;
            }
        }

        InputStream is = new ByteArrayInputStream(aes.decrypt(data, IV));
        return ImageIO.read(is);


    }

}
