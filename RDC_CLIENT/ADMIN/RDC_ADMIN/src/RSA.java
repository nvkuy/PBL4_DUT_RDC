import java.io.BufferedReader;
import java.io.FileReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

public class RSA {

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private static final Integer KEY_SIZE = 1024;

    public RSA() throws Exception {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(KEY_SIZE);
        KeyPair pair = generator.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();

    }

    public RSA(String path) throws Exception {

        BufferedReader bf = new BufferedReader(new FileReader(path));
        String pubKeyStr = bf.readLine();
        String priKeyStr = bf.readLine();
        bf.close();

        X509EncodedKeySpec keySpecPublic = new X509EncodedKeySpec(decode(pubKeyStr));
        PKCS8EncodedKeySpec keySpecPrivate = new PKCS8EncodedKeySpec(decode(priKeyStr));

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        publicKey = keyFactory.generatePublic(keySpecPublic);
        privateKey = keyFactory.generatePrivate(keySpecPrivate);

    }

    public static PublicKey getPublicKeyFromStr(String publicKeyStr) throws Exception {
        X509EncodedKeySpec keySpecPublic = new X509EncodedKeySpec(decode(publicKeyStr));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpecPublic);
    }

    private static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private static byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    public void printKeys(){
        System.out.println("Public key: " + encode(publicKey.getEncoded()));
        System.out.println("Private key: " + encode(privateKey.getEncoded()));
    }

    public String getPrivateKeyStr() {
        return encode(privateKey.getEncoded());
    }

    public String getPublicKeyStr() {
        return encode(publicKey.getEncoded());
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String encrypt(String message) throws Exception {
        byte[] messageToBytes = message.getBytes();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(messageToBytes);
        return encode(encryptedBytes);
    }

    public static String encrypt(String message, PublicKey key) throws Exception {
        byte[] messageToBytes = message.getBytes();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(messageToBytes);
        return encode(encryptedBytes);
    }

    public String decrypt(String encryptedMessage) throws Exception {
        byte[] encryptedBytes = decode(encryptedMessage);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
        return new String(decryptedMessage, "UTF8");
    }

    public static String decrypt(String encryptedMessage, PrivateKey key) throws Exception {
        byte[] encryptedBytes = decode(encryptedMessage);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
        return new String(decryptedMessage, "UTF8");
    }

    public String sign(String testMes) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(testMes.getBytes());
        byte[] signature = privateSignature.sign();
        return Base64.getEncoder().encodeToString(signature);
    }

    public static boolean verify(String testMes, String signature, PublicKey publicKey) throws Exception {
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(testMes.getBytes());
        byte[] signatureBytes = Base64.getDecoder().decode(signature);
        return publicSignature.verify(signatureBytes);
    }

}
