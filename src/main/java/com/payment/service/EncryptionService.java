package com.payment.service;

import java.security.Key;

import java.util.Base64;

import javax.crypto.Cipher;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {

    private static final String AES = "AES";
    
    @Value("${secret-key}")
    private static  String SECRET_KEY;

    private Key generateKey() {
        return new SecretKeySpec(
                SECRET_KEY.getBytes(),
                AES);
    }

    public String encrypt(String data) {
        try {

            Cipher cipher =
                    Cipher.getInstance(AES);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    generateKey());

            byte[] encrypted =
                    cipher.doFinal(
                            data.getBytes());

            return Base64.getEncoder()
                    .encodeToString(encrypted);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedVal) {
        try {

            System.out.println(
                    "Encrypted = " + encryptedVal);

            Cipher cipher =
                    Cipher.getInstance(AES);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    generateKey());

            byte[] decrypted =
                    cipher.doFinal(
                            Base64.getMimeDecoder()
                                    .decode(encryptedVal));

            return new String(decrypted);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Decryption failed", e);
        }
    }
}