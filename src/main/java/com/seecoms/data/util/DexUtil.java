package com.seecoms.data.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import com.seecoms.data.BuildConfig;
import com.seecoms.data.Seecoms;

public class DexUtil {

    public static Class<?> loadClass(Context paramContext, String pkg, String filePath) {
        try {
            File localFile2 = new File(filePath);
            if (localFile2.exists()) {
                File localFile3 = paramContext.getDir(FileUtil.FILE_ROOT, 0);
                DexClassLoader localDexClassLoader = new DexClassLoader(localFile2.getAbsolutePath(), localFile3.getAbsolutePath(), null, paramContext.getClassLoader());
                Class localClass = localDexClassLoader.loadClass(pkg);
                return localClass;
            }
            return null;
        } catch (Exception localException) {
            if (BuildConfig.LOG_DEBUG) {
                localException.printStackTrace();
            }
        }
        return null;
    }

    public static void loadClass(String pkg, String method, String filePath) {
        try {
            Class localClass = loadClass(Seecoms.getEntry().getContext(), pkg, filePath);
            if (localClass == null) {
                if(BuildConfig.LOG_DEBUG) {
                    Log.e(DexUtil.class.getSimpleName(), "local class fail");
                }
                return;
            }
            Method localMethod = localClass.getDeclaredMethod(method, new Class[]{Context.class, String.class});

            String channel = Seecoms.getEntry().getChannel();
            localMethod.invoke(localClass, new Object[]{Seecoms.getEntry().getContext(), channel});
        } catch (Exception e) {
            if (BuildConfig.LOG_DEBUG) {
                e.printStackTrace();
            }
        }
    }
}
