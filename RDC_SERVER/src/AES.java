import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    private final SecretKey key;
    private static final int KEY_SIZE = 128;
    private static final int T_LEN = 128;

    private final ReentrantLock lock;

    public AES() throws Exception {

        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(KEY_SIZE);
        key = generator.generateKey();
        lock = new ReentrantLock(true);

    }

    public AES(String secretKeyStr) {

        key = new SecretKeySpec(Util.strToByte(secretKeyStr),"AES");
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
        try {
            lock.lock();
            byte[] messageInBytes = message.getBytes();
            return Util.byteToStr(encrypt(messageInBytes, IV));
        } finally {
            lock.unlock();
        }
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
        try {
            lock.lock();
            byte[] messageInBytes = Util.strToByte(encryptedMessage);
            return new String(decrypt(messageInBytes, IV));
        } finally {
            lock.unlock();
        }
    }

    public byte[] getKeyByte() {
        try {
            lock.lock();
            return key.getEncoded();
        } finally {
            lock.unlock();
        }
    }
    public String getKeyStr() {
        try {
            lock.lock();
            return Util.byteToStr(getKeyByte());
        } finally {
            lock.unlock();
        }
    }

}
