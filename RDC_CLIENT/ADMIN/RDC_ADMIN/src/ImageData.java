import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.TreeMap;

public class ImageData {
    private int numOfPart;
    private int imgByteLen;
    private int partReceived;
    private boolean haveHeader;
    private byte[] IV;

    private byte[][] imagePart;

    public ImageData() {
        haveHeader = false;
        imgByteLen = 0;
        partReceived = 0;
        imagePart = new byte[16][];
    }

    public void add(String raw_data) {

        int partID = Integer.parseInt(raw_data.substring(0, 3));
        if (partID == 0) { // header

            numOfPart = Integer.parseInt(raw_data.substring(3, 6));
            IV = AES.getIVFromStr(raw_data.substring(6));
            haveHeader = true;

        } else { // normal image part

            if (imagePart[partID] != null) return;
            String partStr = raw_data.substring(3);
            byte[] part = AES.decode(partStr);
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
