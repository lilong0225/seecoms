package com.seecoms.data.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.webkit.WebSettings;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;

import com.seecoms.data.BuildConfig;

public class DeviceUtil {

    private static String mAndroidID = "";
    private static String imei = "";

    public static String getAndroidID(Context context) {
        try {
            if (TextUtils.isEmpty(mAndroidID)) {
                mAndroidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        } catch (Exception e) {
        }

        return mAndroidID;
    }

    public static String getMac() {
        String macAddress = null;
        StringBuffer buf = new StringBuffer();
        NetworkInterface networkInterface = null;
        try {
            networkInterface = NetworkInterface.getByName("eth1");
            if (networkInterface == null) {
                networkInterface = NetworkInterface.getByName("wlan0");
            }
            if (networkInterface == null) {
                return "02:00:00:00:00:02";
            }
            byte[] addr = networkInterface.getHardwareAddress();
            for (byte b : addr) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            macAddress = buf.toString();
        } catch (SocketException e) {
            return "02:00:00:00:00:02";
        }
        return macAddress;
    }

    public static String getNetWorkName(Context context) {
        try {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); // 获取网络服务
            if (null == connManager) { // 为空则认为无网络
                return "";
            }
            @SuppressLint("MissingPermission")
            NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();
            if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
                return "";
            }

            String typeName = "";
            if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                @SuppressLint("MissingPermission")
                NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (null != wifiInfo) {
                    typeName = "WiFi";
                }
            } else if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                String _strSubTypeName = activeNetInfo.getSubtypeName();
                switch (activeNetInfo.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        typeName = "2G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        typeName = "3G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        typeName = "4G";
                        break;
                    default:
                        // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
                        if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                            typeName = "3G";
                        } else {
                            typeName = _strSubTypeName;
                        }
                        break;
                }
            }
            return typeName;
        } catch (Exception e) {
            if (BuildConfig.LOG_DEBUG) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            String versionName = packageInfo.versionName;
            if (!TextUtils.isEmpty(versionName) && versionName.length() > 64) {
                versionName = versionName.substring(0, 64);
            }
            return versionName;
        } catch (Throwable e) {
        }
        return "";

    }

    @SuppressLint("MissingPermission")
    public static String getIMEI(Context context) {
        if (!"".equals(imei)) {
            return imei;
        }
        try {
            TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telManager != null) {
                imei = telManager.getDeviceId();
            } else {
                imei = "";
            }
            if (!TextUtils.isEmpty(imei) && imei.length() > 64) {
                imei = imei.substring(0, 64);
            }
            return imei;
        } catch (Exception e) {
            return "";
        }
    }

    @SuppressLint("MissingPermission")
    public static String getIMSI(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (TextUtils.isEmpty(telephonyManager.getSubscriberId())) {
                return "";
            }
            String imsi = telephonyManager.getSubscriberId();
            if (!TextUtils.isEmpty(imsi) && imsi.length() > 64) {
                imsi = imsi.substring(0, 64);
            }
            return imsi;
        } catch (Throwable ex) {
        }
        return "";
    }

    private static String mUserAgent = "";

    public static String getUserAgent(Context context) {
        try {
            if (!TextUtils.isEmpty(mUserAgent)) {
                return mUserAgent;
            }

            String userAgent = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                try {
                    userAgent = WebSettings.getDefaultUserAgent(context);
                } catch (Exception e) {
                    userAgent = System.getProperty("http.agent");
                }
            } else {
                userAgent = System.getProperty("http.agent");
            }
            StringBuffer sb = new StringBuffer();
            for (int i = 0, length = userAgent.length(); i < length; i++) {
                char c = userAgent.charAt(i);
                if (c <= '\u001f' || c >= '\u007f') {
                    sb.append(String.format("\\u%04x", (int) c));
                } else {
                    sb.append(c);
                }
            }

            mUserAgent = sb.toString();
            return sb.toString();
        } catch (Throwable e) {
        }
        return "";
    }

    public static String getUserAgent() {
        return mUserAgent;
    }

    private static String mDeviceId = "";

    //获取设备ID
    public static String getDeviceID(Context context) {
        if (mDeviceId != null && !mDeviceId.equals("")) {
            return mDeviceId;
        }

        try {
            if (context != null) {
                String deviceName = Build.MODEL.replaceAll(" ", "");
                String macAddress = getMac();
                if (macAddress == null || macAddress.equals("00:00:00:00:00:00"))
                    macAddress = "0";
                else
                    macAddress = macAddress.replace(":", "");

                String androidID = getAndroidID(context);
                String ret = deviceName + "_" + macAddress + "_" + androidID;
                mDeviceId = UrlEnc(ret);
                if (!TextUtils.isEmpty(mDeviceId) && mDeviceId.length() > 64) {
                    mDeviceId = mDeviceId.substring(0, 64);
                }
            }
        } catch (Exception e) {
        }
        return mDeviceId;
    }

    public static boolean isNetWorkEnable(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
                return false;
            }

            @SuppressLint("MissingPermission")
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 判断当前网络是否已经连接
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
            return false;

        } catch (Throwable e) {
        }
        return false;
    }

    public static boolean isNetSystemUsable(Context context) {
        boolean isNetUsable = false;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            @SuppressLint("MissingPermission")
            NetworkCapabilities networkCapabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());
            isNetUsable = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        }
        return isNetUsable;
    }

    private static String UrlEnc(String s) {
        String res = "";
        try {
            res = URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            res = "";
        }

        return res;
    }
}
