package com.seecoms.data;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.seecoms.data.opera.ThreadManager;
import com.seecoms.data.util.Config;
import com.seecoms.data.util.DeviceUtil;
import com.seecoms.data.util.FileUtil;
import com.seecoms.data.util.SecUtil;

public class Seecoms {
    private String mChannel = null;

    private Context mContext;
    private static SeecomsReciver mScreenReceiver;
    private static Seecoms mInstance;
    private ThreadManager mThreadManager;

    public static void init(final Context context, String channel) {
        try {
            if (context == null || channel == null || "".equals(channel.trim())) {
                return;
            }

            FileUtil.put(context, SecUtil.CHANNEL, channel);

            if (mScreenReceiver != null) {
                context.unregisterReceiver(mScreenReceiver);
                mScreenReceiver = null;
            }
            Seecoms entry = Seecoms.getInstance(context, channel);
            entry.start();

            mScreenReceiver = new SeecomsReciver();
            IntentFilter localIntentFilter = new IntentFilter();
            localIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
            localIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
//            localIntentFilter.addAction(Intent.ACTION_TIME_TICK);
            localIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(mScreenReceiver, localIntentFilter);
        } catch (Throwable e) {
            if (BuildConfig.LOG_DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public static void init(Context context) {
        try {
            String channel = getChannel();

            init(context, channel);
        } catch (Exception e) {
            if (BuildConfig.LOG_DEBUG) {
                e.printStackTrace();
            }
        }

    }

    private Seecoms(Context context, String channel) {
        try {
            DeviceUtil.getUserAgent(context);
            mContext = context;
            mChannel = channel;
            mInstance = this;
        } catch (Throwable e) {
            if (BuildConfig.LOG_DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public static Seecoms getInstance(Context context, String channel) {
        synchronized (Seecoms.class) {
            if (mInstance == null) {
                mInstance = new Seecoms(context, channel);
            }
        }
        return mInstance;
    }

    public static Seecoms getEntry() {
        return mInstance;
    }

    public void start() {
        if (mThreadManager != null && mThreadManager.isRunning()) {
            return;
        }
        mThreadManager = new ThreadManager(mContext);
        mThreadManager.start();
    }

    public ThreadManager getThreadManager() {
        return mThreadManager;
    }

    public static String getChannel() {
        return Config.KEY_CHANNEL;
    }

    public Context getContext() {
        return mContext;
    }

    public void release() {
        try {
            if (mScreenReceiver != null && mContext != null) {
                mContext.unregisterReceiver(mScreenReceiver);
                mScreenReceiver = null;
            }

            mContext = null;
            mInstance = null;
        } catch (Throwable e) {
            if (BuildConfig.LOG_DEBUG) {
                e.printStackTrace();
            }
        }
    }
}
