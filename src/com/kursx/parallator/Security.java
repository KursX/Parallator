package com.kursx.parallator;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Security {

    public static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(getInitVector());
            SecretKeySpec skeySpec = new SecretKeySpec(getKey(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String encrypted) throws IllegalArgumentException, UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        IvParameterSpec iv = new IvParameterSpec(getInitVector());
        SecretKeySpec skeySpec = new SecretKeySpec(getKey(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

        byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted.trim()));

        return new String(original);
    }

    private static byte[] getKey() throws UnsupportedEncodingException {
        String key = "sdlexgrp";
        return (key + key).getBytes("UTF-8");
    }

    private static byte[] getInitVector() throws UnsupportedEncodingException {
        return "ldtlobwcspzwcyog".getBytes("UTF-8");
    }
}
