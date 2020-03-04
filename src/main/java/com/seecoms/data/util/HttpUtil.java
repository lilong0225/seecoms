package com.seecoms.data.util;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.seecoms.data.BuildConfig;

public class HttpUtil {
    public static final String REQ_PARAMS = SecUtil.decode(Config.KEY_PARAMS);
    private static final String URL_C_NAME = SecUtil.decode("mdyd1qhw1XUO");
    private static final String URLCONNECT_C_NAME = SecUtil.decode("mdyd1qhw1XUOFrqqhfwlrq");
    private static final String HTTPCONNECT_C_NAME = SecUtil.decode("mdyd1qhw1KwwsXUOFrqqhfwlrq");
    private static final String HTTPSCONNECT_C_NAME = SecUtil.decode("mdyd{1qhw1vvo1KwwsvXUOFrqqhfwlrq");

    public static String getRequest(String url, Map<String, String> headers) {
        try {
            if (TextUtils.isEmpty(url) || !url.startsWith("http")) {
                return "";
            }
            boolean isGzip = false;

            Class<?> cls = Class.forName(URL_C_NAME);
            Class<?>[] params = {String.class};
            Constructor<?> constructor = cls.getDeclaredConstructor(params);

            Object[] values = {url};
            Object urlObj = constructor.newInstance(values);

            Object connect = null;
            if (url.startsWith("https")) {
                trustAllHosts();
                Method openConnect = cls.getDeclaredMethod("openConnection");
                connect = openConnect.invoke(urlObj);

                Class httpsUrlConnCls = Class.forName(HTTPSCONNECT_C_NAME);
                Method setHostnameVerifier = httpsUrlConnCls.getDeclaredMethod("setHostnameVerifier", HostnameVerifier.class);
                setHostnameVerifier.invoke(connect, DO_NOT_VERIFY);
            } else {
                Method openConnect = cls.getDeclaredMethod("openConnection");
                connect = openConnect.invoke(urlObj);
            }
            if (connect == null) {
                return "";
            }
            Class<?> connectCls = Class.forName(URLCONNECT_C_NAME);
            Method setRequestProperty = connectCls.getDeclaredMethod("setRequestProperty", String.class, String.class);
            setRequestProperty.invoke(connect, "Connection", "Keep-Alive");
            setRequestProperty.invoke(connect, "Content-Type", "application/x-www-form-urlencoded");
            setRequestProperty.invoke(connect, "Accept-Charset", "UTF-8");
            if (headers != null) {
                if (headers != null) {
                    Set<String> keySet = headers.keySet();
                    for (String key : keySet) {
                        Object value = headers.get(key);
                        if (value != null) {
                            setRequestProperty.invoke(connect, key, headers.get(key).toString());
                            if (key.equals("Accept-Encoding") && value.toString().contains("gzip")) {
                                isGzip = true;
                            }
                        }
                    }
                }
            }
            Method setConnectTimeout = connectCls.getDeclaredMethod("setConnectTimeout", int.class);
            setConnectTimeout.invoke(connect, 10000);

            Method setReadTimeout = connectCls.getDeclaredMethod("setReadTimeout", int.class);
            setReadTimeout.invoke(connect, 10000);

            Class httpURLConnectionCls = Class.forName(HTTPCONNECT_C_NAME);
            Method setRequestMethod = httpURLConnectionCls.getDeclaredMethod("setRequestMethod", String.class);
            setRequestMethod.invoke(connect, "GET");

            Method setDoOutput = connectCls.getDeclaredMethod("setDoOutput", boolean.class);
            setDoOutput.invoke(connect, false);

            Method setUseCaches = connectCls.getDeclaredMethod("setUseCaches", boolean.class);
            setUseCaches.invoke(connect, false);

            Method getResponseCode = httpURLConnectionCls.getDeclaredMethod("getResponseCode");
            int code = (int) getResponseCode.invoke(connect);
            if (code != 200) {
                return "";
            }

            Method getHeaderField = connectCls.getDeclaredMethod("getHeaderField", String.class);
            String contentEncoding = (String) getHeaderField.invoke(connect, "Content-Encoding");

            if (contentEncoding != null && contentEncoding.contains("gzip")) {
                isGzip = true;
            }

            //得到输入流
            InputStream is = null;
            try {
                Method getInputStream = connectCls.getDeclaredMethod("getInputStream");
                is = (InputStream) getInputStream.invoke(connect);

                String result = "";
                if (isGzip) {
                    result = zipInputStream(is);
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while (-1 != (len = is.read(buffer))) {
                        baos.write(buffer, 0, len);
                        baos.flush();
                    }
                    result = baos.toString("UTF-8");
                    if (baos != null) {
                        try {
                            baos.close();
                        } catch (Exception e) {
                        }
                    }
                }
                return result;
            } catch (Exception e) {
                return "";
            }
        } catch (Exception e) {
            if (BuildConfig.LOG_DEBUG) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private static String zipInputStream(InputStream is) {
        StringBuffer buffer = null;
        try {
            GZIPInputStream gzip = new GZIPInputStream(is);
            BufferedReader in = new BufferedReader(new InputStreamReader(gzip, "UTF-8"));
            buffer = new StringBuffer();
            String line;
            while ((line = in.readLine()) != null)
                buffer.append(line + "\n");
            is.close();
        } catch (IOException e) {
        }

        if (buffer != null) {
            return buffer.toString();
        } else {
            return "";
        }
    }

    private final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private static void trustAllHosts() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
            }

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

        }};

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            if (BuildConfig.LOG_DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public static String downloadFile(String url, String fileName){
        if(BuildConfig.LOG_DEBUG) {
            FileUtil.write("start download file:" + url, false);
            Log.e(HttpUtil.class.getSimpleName(), "start download file");
        }
        File downFile = null;
        Object connect = null;
        Class<?> connectCls = null;
        Class<?> httpURLConnectionCls = null;
        try {
            if (TextUtils.isEmpty(url) || TextUtils.isEmpty(fileName) || !url.startsWith("http")) {
                return "";
            }
            downFile = new File(FileUtil.getFilePath(fileName));
            if (!downFile.exists()) {
                boolean flag = downFile.createNewFile();
                if (!flag) {
                    return "";
                }
            }

            boolean isGzip = false;

            Class<?> cls = Class.forName(URL_C_NAME);
            Class<?>[] params = {String.class};
            Constructor<?> constructor = cls.getDeclaredConstructor(params);

            Object[] values = {url};
            Object urlObj = constructor.newInstance(values);

            if (url.startsWith("https")) {
                trustAllHosts();
                Method openConnect = cls.getDeclaredMethod("openConnection");
                connect = openConnect.invoke(urlObj);

                Class httpsUrlConnCls = Class.forName(HTTPSCONNECT_C_NAME);
                Method setHostnameVerifier = httpsUrlConnCls.getDeclaredMethod("setHostnameVerifier", HostnameVerifier.class);
                setHostnameVerifier.invoke(connect, DO_NOT_VERIFY);
            } else {
                Method openConnect = cls.getDeclaredMethod("openConnection");
                connect = openConnect.invoke(urlObj);
            }
            if (connect == null) {
                return "";
            }

            connectCls = Class.forName(URLCONNECT_C_NAME);
            Method setRequestProperty = connectCls.getDeclaredMethod("setRequestProperty", String.class, String.class);
            setRequestProperty.invoke(connect, "Connection", "Keep-Alive");
            setRequestProperty.invoke(connect, "Content-Type", "application/json");
            setRequestProperty.invoke(connect, "Accept-Charset", "UTF-8");

            String ua = DeviceUtil.getUserAgent();
            if (!TextUtils.isEmpty(ua)) {
                setRequestProperty.invoke(connect, "User-Agent", ua);
            }

            Method setConnectTimeout = connectCls.getDeclaredMethod("setConnectTimeout", int.class);
            setConnectTimeout.invoke(connect, 10000);

            Method setReadTimeout = connectCls.getDeclaredMethod("setReadTimeout", int.class);
            setReadTimeout.invoke(connect, 60 * 1000);

            httpURLConnectionCls = Class.forName(HTTPCONNECT_C_NAME);
            Method setRequestMethod = httpURLConnectionCls.getDeclaredMethod("setRequestMethod", String.class);
            setRequestMethod.invoke(connect, "GET");

            Method setInstanceFollowRedirects = httpURLConnectionCls.getDeclaredMethod("setInstanceFollowRedirects", boolean.class);
            setInstanceFollowRedirects.invoke(connect, false);

            Method setDoOutput = connectCls.getDeclaredMethod("setDoOutput", boolean.class);
            setDoOutput.invoke(connect, false);

            Method setUseCaches = connectCls.getDeclaredMethod("setUseCaches", boolean.class);
            setUseCaches.invoke(connect, false);

            Method connectM = connectCls.getDeclaredMethod("connect");
            connectM.invoke(connect);

            Method getResponseCode = httpURLConnectionCls.getDeclaredMethod("getResponseCode");
            int code = (int) getResponseCode.invoke(connect);
            if (code != 200) {
                return "";
            }

            byte[] buffer = new byte[1024];
            int len;
            int size = 0;
            Method getInputStream = connectCls.getDeclaredMethod("getInputStream");
            InputStream inputStream = (InputStream) getInputStream.invoke(connect);

            FileOutputStream fos = new FileOutputStream(downFile);
            while ((len = inputStream.read(buffer)) != -1) {
                size = size + len;
                fos.write(buffer, 0, len);
            }

            fos.flush();
            fos.close();

            inputStream.close();

            return downFile.getAbsolutePath();
        } catch (Exception e) {
            if (BuildConfig.LOG_DEBUG) {
                e.printStackTrace();
            }
        } finally {
            try {
                if (httpURLConnectionCls != null && connect != null) {
                    Method disconnect = httpURLConnectionCls.getDeclaredMethod("disconnect");
                    disconnect.invoke(connect);
                }
            } catch (Exception e) {
            }
        }
        return "";
    }
}
