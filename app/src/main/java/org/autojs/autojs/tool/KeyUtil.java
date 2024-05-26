package org.autojs.autojs.tool;

import android.content.Context;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import java.io.ByteArrayInputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class KeyUtil {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String KEY_ALIAS = "DyKey";

    public static void saveEncryptedKey(String publicKey, String password) throws Exception {
        // Convert the public key string to bytes
        byte[] publicKeyBytes = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            publicKeyBytes = Base64.getDecoder().decode(publicKey);
        }

        // Generate AES key from password
        SecretKey aesKey = generateAESKey(password);

        // Encrypt the public key bytes using AES
        byte[] encryptedKeyBytes = encryptWithAES(publicKeyBytes, aesKey);

        // Convert the encrypted key bytes to X.509 certificate object
        X509Certificate certificate = generateCertificate(encryptedKeyBytes);

        // Get Android KeyStore instance
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        // Save the certificate as a public key entry in KeyStore
        keyStore.setCertificateEntry(KEY_ALIAS, certificate);
    }

    private static SecretKey generateAESKey(String password) throws NoSuchAlgorithmException {
        // Generate AES key from password
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(password.getBytes());
        return new SecretKeySpec(keyBytes, AES_ALGORITHM);
    }

    private static byte[] encryptWithAES(byte[] data, SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // Encrypt data using AES
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    private static X509Certificate generateCertificate(byte[] publicKeyBytes) throws CertificateException {
        // Convert encrypted public key bytes to X.509 certificate object
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(publicKeyBytes));
    }

    public static SecretKey generatorKey() {
        String keyString = "NCb}+qFB$xPT";
        return AESEncryptionUtil.convertStringToSecretKey(keyString);
    }
}
