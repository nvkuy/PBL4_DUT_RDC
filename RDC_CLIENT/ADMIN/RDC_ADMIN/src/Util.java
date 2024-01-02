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

    public static long bytesToLong(final byte[] b) {
        long result = 0;
        for (int i = 0; i < b.length; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

//    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
//        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
//        Graphics2D graphics2D = resizedImage.createGraphics();
//        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
//        graphics2D.dispose();
//        return resizedImage;
//    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        MultiStepRescaleOp mro = new MultiStepRescaleOp(targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        mro.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.None);
        return mro.filter(originalImage, null);
    }

//    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
//        int w = originalImage.getWidth();
//        double scaleRatio = 1.0 * targetWidth / w;
//        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
//        AffineTransform at = AffineTransform.getScaleInstance(scaleRatio, scaleRatio);
//        AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
//        return ato.filter(originalImage, scaledImage);
//    }


}
