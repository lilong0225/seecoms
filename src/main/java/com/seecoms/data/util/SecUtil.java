package com.seecoms.data.util;

import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecUtil {

    public static final String CHANNEL = SecUtil.decode(FileUtil.CHANNEL);
    public static final String DEFAULT_CHANNEL = SecUtil.decode(FileUtil.DEFAULT_CHANNEL);

    public static byte[] decrypt(String key, byte[] crypted) throws GeneralSecurityException {
        byte[] keyBytes = getKeyBytes(key);
        byte[] buf = new byte[16];
        System.arraycopy(keyBytes, 0, buf, 0, keyBytes.length > buf.length ? keyBytes.length : buf.length);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(buf, "AES"), new IvParameterSpec(keyBytes));
        return cipher.doFinal(crypted);
    }

    private static byte[] getKeyBytes(String key) {
        byte[] bytes = key.getBytes();
        return bytes.length == 16 ? bytes : Arrays.copyOf(bytes, 16);
    }

    public static String decrypt(String key, String val) throws GeneralSecurityException {
        byte[] crypted = toByte(val);
        byte[] origData = decrypt(key, crypted);
        return new String(origData);
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
    }

    private SecUtil() {
    }

    public static String decode(String input) {
        if (input == null) return null;
        char[] array = input.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : array) {
            sb.append((char) (c - 3));
        }
        return sb.toString();
    }

}
