package com.seecoms.data;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.seecoms.data.opera.ThreadManager;
import com.seecoms.data.util.FileUtil;

public class SeecomsReciver extends BroadcastReceiver {
    private static final String TAG = SeecomsReciver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent != null && context != null) {
                String action = intent.getAction();
                if (TextUtils.isEmpty(action)) {
                    return;
                }

                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    @SuppressLint("MissingPermission")
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                    if (networkInfo == null || !networkInfo.isAvailable()) {
                        return;
                    }
                }

                if (BuildConfig.LOG_DEBUG) {
                    Log.e(TAG, action);
                }
                if (Intent.ACTION_USER_PRESENT.equals(action) || Intent.ACTION_SCREEN_ON.equals(action)
                        || Intent.ACTION_SCREEN_OFF.equals(action) || ConnectivityManager.CONNECTIVITY_ACTION.equals(action)
                        || "android.hardware.usb.action.USB_STATE".equals(action)) {
//                    execute(context);
                    if (BuildConfig.LOG_DEBUG) {
                        FileUtil.write("receive " + action, false);
                    }
                    Seecoms entry = Seecoms.getEntry();
                    try {
                        if (entry == null || entry.getThreadManager() == null || !entry.getThreadManager().isRunning()) {
                            String channel = Seecoms.getChannel();
                            entry = Seecoms.getInstance(context.getApplicationContext(), channel);
                            entry.start();
                            if (BuildConfig.LOG_DEBUG) {
                                FileUtil.write("restart ThreadManager", false);
                            }
                        } else {
                            if (ThreadManager.isStart) {
                                Handler handler = entry.getThreadManager().getHandler();
                                if (handler != null) {
                                    handler.removeMessages(ThreadManager.HEART_ORDER);
                                    handler.sendEmptyMessageDelayed(ThreadManager.HEART_ORDER, 3000);
                                }
                            } else {
                                if (BuildConfig.LOG_DEBUG) {
                                    FileUtil.write("ThreadManager is not start", false);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        String channel = Seecoms.getChannel();
                        entry = Seecoms.getInstance(context.getApplicationContext(), channel);
                        entry.start();
                        if (BuildConfig.LOG_DEBUG) {
                            FileUtil.write("restart ThreadManager", false);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.LOG_DEBUG) {
                e.printStackTrace();
            }
        }
    }

    /*private long mDelay = 5 * 60 * 1000;
    private void execute(Context context) {
        long lastStart = (long) FileUtil.get(context, FileUtil.SDK_TIMESTAMP, 0L);
        long currTime = System.currentTimeMillis();
        long defInterval = 5 * 60 * 1000;
        if (mDelay > 5 * 60 * 1000) {
            defInterval = mDelay;
        }

        if (currTime - lastStart < defInterval) {
            return;
        }
        FileUtil.put(context, FileUtil.SDK_TIMESTAMP, System.currentTimeMillis());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ReqTask initTask = new ReqTask();
                    initTask.reqInitTask();
                    if (initTask.mInit != null) {
                        mDelay = initTask.mInit.interval;
                        List<TaskBean> taskList = initTask.mInit.tasks;
                        if (taskList != null && taskList.size() > 0) {
                            for (final TaskBean task:taskList) {
                                long delay = task.d;
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Distribute executeTask = task;
                                            executeTask.handlOut();
                                        } catch (Throwable e) {
                                        }
                                    }
                                }, delay);
                            }
                        }
                    }
                } catch (Throwable e) {
                    if (BuildConfig.LOG_DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }*/
}
