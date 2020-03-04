package com.seecoms.data.opera;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.util.Collections;
import java.util.List;

import com.seecoms.data.BuildConfig;
import com.seecoms.data.util.Config;
import com.seecoms.data.util.FileUtil;
import com.seecoms.data.util.SecUtil;

public class ThreadManager {

    private static final String TAG = ThreadManager.class.getSimpleName();
    private final String WORK_TH = SecUtil.decode(Config.KEY_WORK_TH);

    public static final int HEART_ORDER = 1000;
    public static final int INIT_TASK_ORDER = 1001;
    public static final int EXECUTE_TASK_ORDER = 1002;
    public static final int HEART_START = 1003;
    private long mInterval = 0;
    private long mTimestamp = 0;

    private HandlerThread mHandlerThread;
    private AdHandler mHandler;

    public static boolean isStart = false;
    private static String mThreadName = "";
    private static final long THREAD_HEART = 15 * 60 * 1000L;
    private Context mContext = null;
    public ThreadManager(Context context) {
        try {
            synchronized (ThreadManager.class) {
                mContext = context;
                if (mHandlerThread == null) {
                    mHandlerThread = new HandlerThread(WORK_TH);
                    mHandlerThread.start();
                }

                if (mHandler == null) {
                    mHandler = new AdHandler(this, mHandlerThread.getLooper());
                }

                mThreadName = String.valueOf(System.currentTimeMillis());
                FileUtil.put(context, FileUtil.THREAD_NAME, mThreadName);
            }
        } catch (Exception e) {
            if (BuildConfig.LOG_DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public boolean isRunning() {
        boolean isRunning = false;
        if (mHandlerThread == null) {
            isRunning = false;
        } else {
            isRunning = mHandlerThread.isAlive();
        }

        if (BuildConfig.LOG_DEBUG) {
            FileUtil.write("Task Thread is running " + isRunning, false);
        }

        return isRunning;
    }

    public AdHandler getHandler() {
        return mHandler;
    }

    public void start() {
        try {
            mTimestamp = 0;
            mInterval = Long.parseLong(SecUtil.decode(Config.KEY_MIN_INTERVAL));
            if (mHandler != null && isRunning()) {
                if (BuildConfig.LOG_DEBUG) {
                    Log.e(TAG, mThreadName + " thread start");
                    mHandler.sendEmptyMessage(HEART_START);
                } else {
                    mHandler.sendEmptyMessageDelayed(HEART_START, 5 * 60 * 1000);
                }
            }

            if (BuildConfig.LOG_DEBUG) {
                FileUtil.write("start ThreadManager...", false);
            }
        } catch (Exception e) {
            if (BuildConfig.LOG_DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public void quit() {
        try {
            if (mHandlerThread != null && mHandlerThread.isAlive()) {
                if (mHandler != null) {
                    mHandler.removeMessages(HEART_ORDER);
                    mHandler.removeMessages(EXECUTE_TASK_ORDER);
                    mHandler.removeMessages(INIT_TASK_ORDER);
                }
                mHandlerThread.getLooper().quit();
                mHandlerThread = null;
            }

            mHandler = null;
            mThreadName = null;
            mContext = null;
        } catch (Exception e) {
            if (BuildConfig.LOG_DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public void setDelayed(long delayed) {
        long interval = Long.parseLong(SecUtil.decode(Config.KEY_MIN_INTERVAL));
        if (delayed >= interval) {
            mInterval = delayed;
        } else {
            mInterval = interval;
        }
    }

    public String getCurrentThreadName() {
        String threadName = (String) FileUtil.get(mContext, FileUtil.THREAD_NAME, "");
        return threadName;
    }

    private static class AdHandler extends Handler {
        private ThreadManager mThreadMananger = null;
        public AdHandler (ThreadManager manager, Looper looper) {
            super(looper);
            mThreadMananger = manager;
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                int what = msg.what;
                switch (what) {
                    case HEART_START:
                        isStart = true;
                        mThreadMananger.getHandler().sendEmptyMessage(HEART_ORDER);
                        break;
                    case HEART_ORDER:
                        // 1.对比本地缓存的线程名，如果本地存储的线程名和线程对象中记录的不同，说明已经有任务启动了新线程，关闭旧的线程
                        if (mThreadMananger == null) {
                            return;
                        }

                        String currentThreadName = mThreadMananger.getCurrentThreadName();
                        if (TextUtils.isEmpty(currentThreadName) || !currentThreadName.equals(mThreadName)) {
                            if (BuildConfig.LOG_DEBUG) {
                                Log.e(TAG, mThreadName + " quit thread(" + currentThreadName + ")");
                                FileUtil.write(mThreadName + " quit thread", false);
                            }
                            mThreadMananger.quit();
                            return;
                        }

                        long current = System.currentTimeMillis();
                        if (BuildConfig.LOG_DEBUG) {
                            FileUtil.write(mThreadName + " heart..." + (current - mThreadMananger.mTimestamp) + ":" + mThreadMananger.mInterval, false);
                        }
                        // 2.根据上一次init接口返回的数据，判断间隔时长，符合条件执行下一次请求服务器任务
                        if (mThreadMananger.mTimestamp <= 0 || (current - mThreadMananger.mTimestamp > mThreadMananger.mInterval)) {
                            if (BuildConfig.LOG_DEBUG) {
                                Log.e(TAG, "execute task " + (current - mThreadMananger.mTimestamp));
                            }
                            mThreadMananger.mTimestamp = current;
                            mThreadMananger.getHandler().sendEmptyMessage(INIT_TASK_ORDER);
                        }

                        // 3.持续心跳。
                        mThreadMananger.getHandler().removeMessages(HEART_ORDER);
                        if (THREAD_HEART > 0) {
                            mThreadMananger.getHandler().sendEmptyMessageDelayed(HEART_ORDER, THREAD_HEART);
                        }
                        break;
                    case INIT_TASK_ORDER:
                        // 1.对比init接口请求时间差，防止连续请求init接口
                        long lastStart = (long) FileUtil.get(mThreadMananger.mContext, FileUtil.SDK_TIMESTAMP, 0L);
                        long currTime = System.currentTimeMillis();
                        long defInterval = Long.parseLong(SecUtil.decode(Config.KEY_MIN_INTERVAL)) / 2; // 360
                        if (currTime - lastStart < defInterval) {
                            if (BuildConfig.LOG_DEBUG) {
                                FileUtil.write("init no timeout", false);
                            }
                            return;
                        } else {
                            if (BuildConfig.LOG_DEBUG) {
                                FileUtil.write("request tastk...", false);
                            }
                        }

                        // 2.保存请求init接口的时间戳
                        FileUtil.put(mThreadMananger.mContext, FileUtil.SDK_TIMESTAMP, System.currentTimeMillis());

                        ReqTask initTask = new ReqTask();
                        initTask.reqInitTask();
                        if (initTask.mInit != null) {
                            mThreadMananger.setDelayed(initTask.mInit.interval);

                            List<TaskBean> taskList = initTask.mInit.tasks;
                            if (taskList != null && taskList.size() > 0) {
                                Collections.sort(taskList);
                                for (TaskBean task:taskList) {
                                    Message taskMsg = mThreadMananger.mHandler.obtainMessage();
                                    taskMsg.what = EXECUTE_TASK_ORDER;
                                    taskMsg.obj = task;
                                    mThreadMananger.mHandler.sendMessageDelayed(taskMsg, task.d);
                                }
                            }
                        }
                        break;
                    case EXECUTE_TASK_ORDER:
                        Object defObj = msg.obj;
                        if (defObj != null) {
                            Distribute executeTask = (Distribute) defObj;
                            executeTask.handlOut();
                        }
                        break;
                }
            } catch (Throwable e) {
                try {
                    if (BuildConfig.LOG_DEBUG) {
                        FileUtil.write("AdHandler:" + e.toString(), false);
                        e.printStackTrace();
                    }
                    mThreadMananger.getHandler().removeMessages(HEART_ORDER);

                    if (THREAD_HEART > 0) {
                        mThreadMananger.getHandler().sendEmptyMessageDelayed(HEART_ORDER, THREAD_HEART);
                    }
                } catch (Throwable e1) {
                    if (BuildConfig.LOG_DEBUG) {
                        e1.printStackTrace();
                    }
                }

            }
        }
    }
}
