import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Base64;

public class Util {

    public static String byteToStr(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] strToByte(String data) {
        return Base64.getDecoder().decode(data);
    }

    public static byte[] longToBytes(long num, int len) {
        byte[] result = new byte[len];
        for (int i = len - 1; i >= 0; i--) {
            result[i] = (byte)(num & 0xFF);
            num >>= 8;
        }
        return result;
    }

    public static byte[] concat(byte[]... arrs) {

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

    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

}
