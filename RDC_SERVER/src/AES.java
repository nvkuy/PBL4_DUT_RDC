import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    private SecretKey key;
    private static final int KEY_SIZE = 128;
    private static final int T_LEN = 128;

    public AES() throws Exception {

        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(KEY_SIZE);
        key = generator.generateKey();

    }

    public AES(String secretKeyStr) {

        key = new SecretKeySpec(decode(secretKeyStr),"AES");

    }

    public AES(byte[] secretKeyByte) {

        key = new SecretKeySpec(secretKeyByte, "AES");

    }

    public byte[] generateIV() throws Exception {

        Cipher encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        encryptionCipher.init(Cipher.ENCRYPT_MODE, key);
        return encryptionCipher.getIV();

    }

    public byte[] encrypt(byte[] message, byte[] IV) throws Exception {
        Cipher encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(T_LEN, IV);
        encryptionCipher.init(Cipher.ENCRYPT_MODE, key, spec);
        return encryptionCipher.doFinal(message);
    }

    public String encrypt(String message, byte[] IV) throws Exception {
        byte[] messageInBytes = message.getBytes();
        return encode(encrypt(messageInBytes, IV));
    }

    public byte[] decrypt(byte[] encryptedMessage, byte[] IV) throws Exception {
        Cipher decryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(T_LEN, IV);
        decryptionCipher.init(Cipher.DECRYPT_MODE, key, spec);
        return decryptionCipher.doFinal(encryptedMessage);
    }

    public String decrypt(String encryptedMessage, byte[] IV) throws Exception {
        byte[] messageInBytes = decode(encryptedMessage);
        return new String(decrypt(messageInBytes, IV));
    }

    public static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    public byte[] getKeyByte() {
        return key.getEncoded();
    }
    public String getKeyStr() {
        return encode(getKeyByte());
    }

    public static String getIVStr(byte[] IV) {
        return encode(IV);
    }

    public static byte[] getIVFromStr(String IVStr) {
        return decode(IVStr);
    }

}
