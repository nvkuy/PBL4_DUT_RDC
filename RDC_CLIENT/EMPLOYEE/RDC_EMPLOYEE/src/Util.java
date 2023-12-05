import java.util.Base64;

public class Util {

    private static final int TWO_BYTE = 1 << 16;

    public static String byteToStr(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] strToByte(String data) {
        return Base64.getDecoder().decode(data);
    }

    public static byte[] intToBytes(int num) {
        num %= TWO_BYTE;
        byte[] result = new byte[2];
        for (int i = 1; i >= 0; i--) {
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

}
