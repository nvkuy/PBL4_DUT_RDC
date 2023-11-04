import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Gzip {

//    private static String encode(byte[] data) {
//        return Base64.getEncoder().encodeToString(data);
//    }
//
//    private static byte[] decode(String data) {
//        return Base64.getDecoder().decode(data);
//    }

    private static final Charset UTF8_CHARSET = StandardCharsets.UTF_8;

    private static String encode(byte[] bytes) {
        return new String(bytes, UTF8_CHARSET);
    }

    private static byte[] decode(String string) {
        return string.getBytes(UTF8_CHARSET);
    }

    public static String compress(String str) throws Exception {
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes("UTF-8"));
        gzip.close();
        return encode(obj.toByteArray());
    }

    public static String decompress(String strData) throws Exception {
        byte[] data = decode(strData);
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(data));
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bf.readLine()) != null) sb.append(line);
        return sb.toString();
    }

}
