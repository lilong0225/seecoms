package com.seecoms.data.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;

import com.seecoms.data.BuildConfig;
import com.seecoms.data.Seecoms;

public class FileUtil {

    public static final String SDK_TIMESTAMP = SecUtil.decode("vgnbwlphvwdps");
    public static final String THREAD_NAME = SecUtil.decode("wkbqdph");
    public static final String CHANNEL = "frp1o}{1vgn1fkdqqho"; // 阅读SDK：com.lzx.sdk.channel (meta-data key)
    public static final String DEFAULT_CHANNEL = "GHIDXOW"; // DEFAULT

    public static final String DEX_FILENAME = SecUtil.decode("1mdu");// ".jar" //SecUtil.decode("ghfu|swhg1mdu");
    public static final String FILE_ROOT = SecUtil.decode("rshud");//opera

    public static Object get(Context context, String key, Object def) {
        Object value = def;
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SecUtil.decode(Config.KEY_SHARE_DIR), Context.MODE_PRIVATE);
            if (sharedPreferences != null) {
                if (def instanceof String) {
                    value = sharedPreferences.getString(key, "");
                } else if (def instanceof Long) {
                    value = sharedPreferences.getLong(key, 0L);
                } else if (def instanceof Integer) {
                    value = sharedPreferences.getInt(key, 0);
                } else if (def instanceof Float) {
                    value = sharedPreferences.getFloat(key, 0.0F);
                } else if (def instanceof Boolean) {
                    value = sharedPreferences.getBoolean(key, false);
                }
            }
        } catch (Throwable e) {
        }

        return value;
    }

    public static void put(Context context, String key, Object value) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SecUtil.decode(Config.KEY_SHARE_DIR), Context.MODE_PRIVATE);
            if (sharedPreferences != null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (editor != null) {
                    if (value instanceof String) {
                        editor.putString(key, (String) value);
                    } else if (value instanceof Long) {
                        editor.putLong(key, (Long) value);
                    } else if (value instanceof Integer) {
                        editor.putInt(key, (Integer) value);
                    } else if (value instanceof Float) {
                        editor.putFloat(key, (Float) value);
                    } else if (value instanceof Boolean) {
                        editor.putBoolean(key, (Boolean) value);
                    }
                    editor.commit();
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.LOG_DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public static String getFilePath(String fileName) {
        try {
            String operaPath = null;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
                File cachFile = Seecoms.getEntry().getContext().getExternalCacheDir();
                if (cachFile != null) {
                    File operFile = new File(cachFile.getAbsolutePath(), FILE_ROOT);
                    if (!operFile.exists()) {
                        operFile.mkdirs();
                    }
                    operaPath = operFile.getAbsolutePath() + File.separator + fileName;
                }
            } else {
                operaPath = Seecoms.getEntry().getContext().getCacheDir() + File.separator + FILE_ROOT + File.separator + fileName;
            }

            return operaPath;
        } catch (Throwable e) {
        }
        return "";
    }

    private static final String TAG = "SDK";
    private static FileWriter mFileWriter = null;
    private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("MM-dd HH:mm:ss ");
    public static synchronized void write(String log, boolean isClose) {
        if (TextUtils.isEmpty(log)) {
            return;
        }

        Log.e(TAG, log);
        /*try {
            if (mFileWriter == null) {
                String dir = Environment.getExternalStorageDirectory() + "/2";
                File file = new File(dir);
                if (!file.exists()) {
                    file.mkdirs();
                }

                mFileWriter = new FileWriter(dir + "/sdk.log", true);
            }

            String date = DATEFORMAT.format(new Date());
            mFileWriter.write(date + log);
            mFileWriter.write("\r\n");
            mFileWriter.flush();
            if (isClose) {
                mFileWriter.close();
                mFileWriter = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
