import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.TreeMap;

public class ImageData {
    private int numOfPart;

    private int imgByteLen;
    private boolean header;
    private byte[] IV;
    private TreeMap<Integer, byte[]> imagePart;

    public ImageData() {
        header = false;
        imgByteLen = 0;
        imagePart = new TreeMap<>();
    }

    public void add(String raw_data) {

        int partID = Integer.parseInt(raw_data.substring(0, 3));
        if (partID == 0) { // header

            numOfPart = Integer.parseInt(raw_data.substring(3, 6));
            IV = AES.getIVFromStr(raw_data.substring(6));
            header = true;

        } else { // normal image part

            if (imagePart.containsKey(partID)) return;
            String partStr = raw_data.substring(3);
            byte[] part = AES.decode(partStr);
            imagePart.put(partID, part);
            imgByteLen += part.length;

        }

    }

    public boolean isCompleted() {
        return (imagePart.size() == numOfPart) & header;
    }

    public BufferedImage getImage(AES aes) throws Exception {

        if (!isCompleted())
            throw new Exception("Not completed image!");

        byte[] data = new byte[imgByteLen];
        int i = 0;
        for (byte[] part : imagePart.values()) {
            for (int j = 0; j < part.length; j++) {
                data[i] = part[j];
                i++;
            }
        }

        String crypImgStr = new String(data);
        String imgStr = aes.decrypt(crypImgStr, IV);

        InputStream is = new ByteArrayInputStream(AES.decode(imgStr));
        return ImageIO.read(is);


    }

}
