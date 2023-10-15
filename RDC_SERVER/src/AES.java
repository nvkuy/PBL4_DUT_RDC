import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

	private SecretKey key;
    private int KEY_SIZE = 128;
    private int T_LEN = 128;
    private byte[] IV;
    
    public AES() throws Exception {
    	
    	KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(KEY_SIZE);
        key = generator.generateKey();
    	
    }
    
    public AES(String secretKeyStr, String IV) throws Exception {
    	
    	key = new SecretKeySpec(decode(secretKeyStr),"AES");
    	this.IV = decode(IV);
    	
    }
    
    public String encrypt(String message) throws Exception {
    	byte[] messageInBytes = message.getBytes();
        Cipher encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(T_LEN, IV);
        encryptionCipher.init(Cipher.ENCRYPT_MODE, key, spec);
        byte[] encryptedBytes = encryptionCipher.doFinal(messageInBytes);
        return encode(encryptedBytes);
    }

    public String decrypt(String encryptedMessage) throws Exception {
    	byte[] messageInBytes = decode(encryptedMessage);
        Cipher decryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(T_LEN, IV);
        decryptionCipher.init(Cipher.DECRYPT_MODE, key, spec);
        byte[] decryptedBytes = decryptionCipher.doFinal(messageInBytes);
        return new String(decryptedBytes);
    }

    private String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }
    
    public String getKeyStr() {
    	return encode(key.getEncoded());
    }
    
    public String getIVStr() {
    	return encode(IV);
    }
	
}
