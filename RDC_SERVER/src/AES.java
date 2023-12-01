import java.util.Base64;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    private SecretKey key;
    private static final int KEY_SIZE = 128;
    private static final int T_LEN = 128;

    private ReentrantLock lock;

    public AES() throws Exception {

        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(KEY_SIZE);
        key = generator.generateKey();
        lock = new ReentrantLock(true);

    }

    public AES(String secretKeyStr) {

        key = new SecretKeySpec(decode(secretKeyStr),"AES");
        lock = new ReentrantLock(true);

    }

    public AES(byte[] secretKeyByte) {

        key = new SecretKeySpec(secretKeyByte, "AES");
        lock = new ReentrantLock(true);

    }

    public byte[] generateIV() throws Exception {

        try {
            lock.lock();
            Cipher encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
            encryptionCipher.init(Cipher.ENCRYPT_MODE, key);
            return encryptionCipher.getIV();
        } finally {
            lock.unlock();
        }

    }

    public byte[] encrypt(byte[] message, byte[] IV) throws Exception {

        try {
            lock.lock();
            Cipher encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(T_LEN, IV);
            encryptionCipher.init(Cipher.ENCRYPT_MODE, key, spec);
            return encryptionCipher.doFinal(message);
        } finally {
            lock.unlock();
        }

    }

    public String encrypt(String message, byte[] IV) throws Exception {
        byte[] messageInBytes = message.getBytes();
        return encode(encrypt(messageInBytes, IV));
    }

    public byte[] decrypt(byte[] encryptedMessage, byte[] IV) throws Exception {
        try {
            lock.lock();
            Cipher decryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(T_LEN, IV);
            decryptionCipher.init(Cipher.DECRYPT_MODE, key, spec);
            return decryptionCipher.doFinal(encryptedMessage);
        } finally {
            lock.unlock();
        }
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
