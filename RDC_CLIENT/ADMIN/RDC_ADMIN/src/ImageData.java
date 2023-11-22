import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.TreeMap;

public class ImageData {
    private int numOfPart;
    private int imgByteLen;
    private int partReceived;
    private boolean haveHeader;
    private byte[] IV;

    private final byte[][] imagePart;

    public ImageData() {
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

        int partID = bytesToInt(Arrays.copyOfRange(raw_data, 0, 2));
        if (partID == 0) { // header

            numOfPart = bytesToInt(Arrays.copyOfRange(raw_data, 2, 4));
            IV = Arrays.copyOfRange(raw_data, 4, raw_data.length);
            haveHeader = true;

        } else { // normal image part

            if (imagePart[partID] != null) return;
            byte[] part = Arrays.copyOfRange(raw_data, 2, raw_data.length);
            imagePart[partID] = part;
            imgByteLen += part.length;
            partReceived++;

        }

    }

    public boolean isCompleted() {
        return (partReceived == numOfPart) && haveHeader;
    }

    public BufferedImage getImage(AES aes) throws Exception {

        if (!isCompleted())
            return null;

        byte[] data = new byte[imgByteLen];
        int i = 0;
        for (int k = 1; k <= numOfPart; k++) {
            for (int j = 0; j < imagePart[k].length; j++) {
                data[i] = imagePart[k][j];
                i++;
            }
        }

        String crypImgStr = AES.encode(data);
        String imgStr = aes.decrypt(crypImgStr, IV);

        InputStream is = new ByteArrayInputStream(AES.decode(imgStr));
        return ImageIO.read(is);


    }

}
