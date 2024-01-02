import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;

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
            result[i] = (byte) (num & 0xFF);
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

//    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
//        MultiStepRescaleOp mro = new MultiStepRescaleOp(targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//        mro.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.None);
//        return mro.filter(originalImage, null);
//    }

//    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
//        int w = originalImage.getWidth();
//        double scaleRatio = 1.0 * targetWidth / w;
//        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
//        AffineTransform at = AffineTransform.getScaleInstance(scaleRatio, scaleRatio);
//        AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
//        return ato.filter(originalImage, scaledImage);
//    }

    public static byte[] compressImgToByte(BufferedImage img, float quality) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = writers.next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(os);
        writer.setOutput(ios);
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        writer.write(null, new IIOImage(img, null, null), param);

        try {
            return os.toByteArray();
        } finally {
            os.close();
            writer.dispose();
        }

    }

}
