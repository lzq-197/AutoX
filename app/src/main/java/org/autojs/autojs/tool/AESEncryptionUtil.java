package org.autojs.autojs.tool;

import android.content.Context;
import android.util.Base64;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESEncryptionUtil {

    public static String encrypt(String input, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(input.getBytes("UTF-8"));
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }

    public static String decrypt(String encryptedInput, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.decode(encryptedInput, Base64.DEFAULT));
        return new String(decryptedBytes, "UTF-8");
    }

    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256); // 使用256位的AES密钥
        return keyGenerator.generateKey();
    }

    public static SecretKey convertStringToSecretKey(String keyInString) {
        byte[] decodedKey = Base64.decode(keyInString, Base64.DEFAULT);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    public static String decryptText(String encryptedText) throws Exception {
        String storedPrivateKeyString = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCZoOi8BDlPPbySVZdV1fieeYWFjiLyk5jqx1lxlrREKlL2YnZu87xkxGYutL7vc+fzfzoGv4/HAPRvNa71Vs77S+w0kvieIaR3b63+2fHAYq1QNJq9tIQS3GsZPEerlJYZgwKNq7Qqr5s4+G51WcgdYESdSAqMt58TYEiLhAVvAykoYdi2OI9RYMeEkGlFTbcbgZsTCA1JyGHcs00VM/+fcu06vVjJ1bjD+0TFORyhY/s5bjJDHiSDsKc13Yk8lr4BuMMLdGItvmwd75lKz1w8SvuShvu3JP3wuaiZdgaAKEDNK/6vFXDTVl8DABUPBGhiSf1rV/mXKMuob/P0bD1ZAgMBAAECggEAEAPmPzNxhgE5l0e07w3PTTX95V5b5k3csxz1BhJf2HtRCr5Y+lgOW7LdtCDDhtLea1PoCKwPoP9IfA9sncbe7PONPC3WHSYEry7FRLwTp6qhxqq1NvGKkyzHPWo7Tne+7gnqSjqMHacQ12+OlE03v1g5zuujree2fZ6b8W9h4aCZUNWdvT2P2SmPvlw8thBsBE63h+xHpsz1hj9i4LqYMM7hzyHCZqHRv9XYF4iodWr+SAuhtjkxtnbeC7oy8qplTzEuVrg38uyU1m/HPm4tJ/MuQU1+0pVVrstrBv5RX0nOQNzLs4el30acumD6AqbcK7efVFX5OBODIXe0JfVEwQKBgQDRe98tKE36dJts9FlPI+F//ZdcCtfiMJ/ILin5xeQDNHQmx8qW04crXG10mIilm76DgSehzSv244InWlQwEXhPL3RGB2v1FwcFStbQnHdkZ1H2nAHI2FgFVjbdbG0r11biN4C76czgVqUlmjfFiPpta8r9qJvaIwKLXFnpGC73cwKBgQC7ve/WnRSoZRyNOgiF9cOrP6+l5xI1LMU3PQp7eV/W0dmBk8nunNGJJVIpmmAgwvgQfIzxg+0VqEDqehp8DwpZen4iyjtvqzsB/X2XlijFPeDOPNMiFJpM+5gCPhIiF+lxXrKVfmnJLuKJHCDgeWqMjW+Yyu35wZyJ2l5gFNeNAwKBgDIuPSjhFcTgN/cilV461yLEyYQ36Lz7LttSC6YUfGk0IHrFs0bsBt4hNusOOvWI2FtCDg9ZSaXLm/r3TWFyXeKCGJVJ49eDG6Bt7AVu5b68bUro1hiZkoQ8wcnK1mABJoPsDb914yj+OHNIfQGprWuWvbqDsEnRxyNBDRykOWP3AoGAEj956mBGO7/oMIntfifPJvv/tJ5rFKQPXzkdpba8bALlo0ScIgc4Dp6EB+srYMPSnLeec3MhPjOma6QGovxLQzs2u2bws//usljTNkKwH8vN5/3df7iE7uqdccpatNmiJRTRQtqQqa9W8YC8aVUj2Bl/OViwHNCGyJdjCO+9q8UCgYEArxJvjOXDIFXela/KUOoA6c8eQ2bxCu77g4XLLMfoFb73ynB4oN5T5KV6jlGXp/WdpvmJuLcdNHnLC7fKXCGZcIhZAkK4dHqjAJ9hKggmoljRAfoJBSfnUs0MieWdVRnsaTrxCNKjGhaA4IUaYmNPi4lxmLunOb7JjVKuWkcs16Y=";
        byte[] privateKeyBytes = Base64.decode(storedPrivateKeyString, Base64.DEFAULT);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.decode(encryptedText, Base64.DEFAULT));

        return new String(decryptedBytes);
    }

    public static String encryptText(String plainText) throws Exception {
        // Get stored public key from SharedPreferences
        String storedPublicKeyString = DatabaseHelper.publicKey;
        byte[] publicKeyBytes = Base64.decode(storedPublicKeyString, Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());

        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }
}
