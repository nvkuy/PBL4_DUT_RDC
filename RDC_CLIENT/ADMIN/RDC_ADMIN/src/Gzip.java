import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Gzip {

    private static final Charset UTF8_CHARSET = StandardCharsets.UTF_8;

    public static String compress(String str) throws Exception {
        if (str == null || str.isEmpty())
            return null;
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes(UTF8_CHARSET));
        gzip.close();
        return Util.byteToStr(obj.toByteArray());
    }

    public static String decompress(String strData) throws Exception {
        if (strData == null || strData.isEmpty()) {
            return null;
        }
        byte[] data = Util.strToByte(strData);
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(data));
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, UTF8_CHARSET));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bf.readLine()) != null) sb.append(line);
        return sb.toString();
    }

}
