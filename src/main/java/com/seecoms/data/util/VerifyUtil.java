package com.seecoms.data.util;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;

import com.seecoms.data.BuildConfig;

public class VerifyUtil {
    private static final String TAG = VerifyUtil.class.getSimpleName();

    public static boolean decryptFile(String encrypt, String decrypt) {
        FileOutputStream fos = null;
        FileInputStream fis = null;
        try {
            File decryptFile = new File(decrypt);
            if ((decryptFile != null) && (decryptFile.exists())) {
                decryptFile.delete();
            }
            decryptFile.createNewFile();

            fos = new FileOutputStream(decryptFile, false);
            fis = new FileInputStream(encrypt);
            int i = 512;
            int j = 0;
            byte[] bytes = new byte[i];
            while ((j = fis.read(bytes)) != -1) {
                for (int k = 0; k < j; k++) {
                    int index = k;
                    byte[] tempBytes = bytes;
                    tempBytes[index] = ((byte) (tempBytes[index] ^ 0xF0));
                }
                fos.write(bytes, 0, j);
            }
            fos.flush();
            return true;
        } catch (Exception e) {
            if(BuildConfig.LOG_DEBUG) {
                e.printStackTrace();
            }
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e2) {
                return false;
            }
        }
        return false;
    }

    private static final char[] a = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String getFileMD5(byte[] paramArrayOfByte) {
        StringBuilder localStringBuilder = new StringBuilder(paramArrayOfByte.length * 2);
        for (int i = 0; i < paramArrayOfByte.length; i++) {
            localStringBuilder.append(a[((paramArrayOfByte[i] & 0xF0) >>> 4)]);
            localStringBuilder.append(a[(paramArrayOfByte[i] & 0xF)]);
        }
        return localStringBuilder.toString();
    }

    public static String getFileMD5(String paramString) {
        byte[] arrayOfByte = new byte[1024];
        int i = 0;
        try {
            FileInputStream localFileInputStream = new FileInputStream(paramString);
            MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
            while ((i = localFileInputStream.read(arrayOfByte)) > 0) {
                localMessageDigest.update(arrayOfByte, 0, i);
            }
            localFileInputStream.close();
            String str = getFileMD5(localMessageDigest.digest());
            return TextUtils.isEmpty(str) ? "" : str;
        } catch (Exception localException) {
            if (BuildConfig.LOG_DEBUG) {
                localException.printStackTrace();
            }
        }
        return "";
    }
}
