package com.duodian.admore.android.sdk.http;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build.VERSION;
import android.util.Log;

import com.duodian.admore.android.sdk.config.AdmoreSdkConfig;
import com.duodian.admore.android.sdk.encryption.AESEncryption;
import com.duodian.admore.android.sdk.log.LogInfo;
import com.duodian.admore.android.sdk.model.DeviceInfo;
import com.duodian.admore.android.sdk.utils.LogUtil;
import com.duodian.admore.android.sdk.utils.SharedPreferenceUtil;
import com.duodian.admore.android.sdk.utils.Util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

public class HttpUtil {
    private static final int TIMEOUT_IN_MILLIONS = 15000;
    private static final int TIMEOUT_IN_MILLIONS_READ = 15000;
    public static ExecutorService executorService = Executors.newCachedThreadPool();

    public static boolean isNetConnected(Context context) {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected() && networkInfo.getState() == State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getType() == 1 && networkInfo.getState() == State.CONNECTED) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMobileConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getType() == 0 && networkInfo.getState() == State.CONNECTED) {
                return true;
            }
        }
        return false;
    }

    public static void doGetBitmapAsync(final String urlStr, final HttpCallback callBack) {
        executorService.execute(new Thread() {
            public void run() {
                try {
                    byte[] bytes = HttpUtil.doGetBitmap(urlStr);
                    if (callBack == null) {
                        return;
                    }
                    if (bytes == null || bytes.length <= 0) {
                        callBack.onRequestError("bitmap is null");
                    } else {
                        callBack.onRequestComplete(bytes);
                    }
                } catch (Exception e) {
                    if (callBack != null) {
                        callBack.onRequestError(e.getMessage());
                    }
                    e.printStackTrace();
                }
            }
        });
    }

    public static void doPostAsync(String urlStr, AESEncryption aesEncryption, Map<String, String> params, Map<String, String> header, boolean needEncodeParams, HttpCallback callBack) {
        final String str = urlStr;
        final AESEncryption aESEncryption = aesEncryption;
        final Map<String, String> map = params;
        final Map<String, String> map2 = header;
        final boolean z = needEncodeParams;
        final HttpCallback httpCallback = callBack;
        executorService.execute(new Thread() {
            public void run() {
                try {
                    byte[] bytes = HttpUtil.doPostRequest(str, aESEncryption, map, map2, "UTF-8", z);
                    if (httpCallback == null) {
                        return;
                    }
                    if (bytes == null || bytes.length <= 0) {
                        httpCallback.onRequestError("response is null");
                    } else {
                        httpCallback.onRequestComplete(bytes);
                    }
                } catch (Exception e) {
                    if (httpCallback != null) {
                        httpCallback.onRequestError(e.getMessage());
                    }
                    e.printStackTrace();
                }
            }
        });
    }

    public static void doPostFileAsync(String urlStr, AESEncryption aesEncryption, Map<String, String> params, Map<String, String> header, boolean needEncodeParams, File file, HttpCallback callBack) {
        final String str = urlStr;
        final AESEncryption aESEncryption = aesEncryption;
        final Map<String, String> map = params;
        final Map<String, String> map2 = header;
        final boolean z = needEncodeParams;
        final File file2 = file;
        final HttpCallback httpCallback = callBack;
        executorService.execute(new Thread() {
            public void run() {
                try {
                    byte[] bytes = HttpUtil.doPostFileRequest(str, aESEncryption, map, map2, "UTF-8", z, file2);
                    if (httpCallback == null) {
                        return;
                    }
                    if (bytes == null || bytes.length <= 0) {
                        httpCallback.onRequestError("response is null");
                    } else {
                        httpCallback.onRequestComplete(bytes);
                    }
                } catch (Exception e) {
                    if (httpCallback != null) {
                        httpCallback.onRequestError(e.getMessage());
                    }
                    e.printStackTrace();
                }
            }
        });
    }

    private static byte[] doGetBitmap(String urlStr) {
        BufferedInputStream bufferedInputStream;
        HttpURLConnection httpURLConnection = null;
        InputStream is = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
//            String uri = urlStr + "?" + System.currentTimeMillis();
            String uri = urlStr;
            LogUtil.e("doGetBitmapUri", uri);
            httpURLConnection = (HttpURLConnection) new URL(uri).openConnection();
            HttpURLConnection.setFollowRedirects(true);
            httpURLConnection.setReadTimeout(15000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("accept", "*/*");
            httpURLConnection.setRequestProperty("connection", "Keep-Alive");
            boolean redirect = false;
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode != 200 && (responseCode == 302 || responseCode == 301 || responseCode == 303)) {
                redirect = true;
            }
            if (redirect) {
                String newUrl = httpURLConnection.getHeaderField("Location");
                httpURLConnection = (HttpURLConnection) new URL(newUrl).openConnection();
                httpURLConnection.setReadTimeout(15000);
                httpURLConnection.setConnectTimeout(15000);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("accept", "*/*");
                httpURLConnection.setRequestProperty("connection", "Keep-Alive");
                LogUtil.e(AdmoreSdkConfig.URL, "Redirect to URL : " + newUrl);
            }
            is = httpURLConnection.getInputStream();
            bufferedInputStream = new BufferedInputStream(is);
            byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bufferedInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
//            byteArrayOutputStream.flush();
            LogUtil.e("bytes", byteArrayOutputStream.toByteArray().length + "");
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {

            try {
                if (is != null) {
                    is.close();
                }

                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }

                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static byte[] doPostRequest(String urlRequest, AESEncryption
            aesEncryption, Map<String, String> params, Map<String, String> header, String encode,
                                        boolean needEncodeParams) throws NetworkErrorException {
        byte[] data;
        String dataString = new JSONObject(params).toString();
        LogUtil.e("jsonString", dataString);
        if (needEncodeParams) {
            data = aesEncryption.encodeBytes(dataString);
        } else {
            data = dataString.getBytes();
        }
        HttpURLConnection httpURLConnection = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            String uri = urlRequest + System.currentTimeMillis();
            LogUtil.e("uri", uri);
            LogUtil.e("map", params.toString());
            httpURLConnection = (HttpURLConnection) new URL(uri).openConnection();
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            for (Entry entry : header.entrySet()) {
                httpURLConnection.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
                LogUtil.e("entry", entry.getKey() + "-->" + entry.getValue());
            }
            outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                inputStream = httpURLConnection.getInputStream();
                byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                byteArrayOutputStream.flush();
                return byteArrayOutputStream.toByteArray();
            } else {
                throw new NetworkErrorException(" ResponseCode is " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }

                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }

                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    private static byte[] doPostFileRequest(String urlStr, AESEncryption aesEncryption, Map<String, String> params,
                                            Map<String, String> header, String charSet, boolean needEncodeParams, File file) throws NetworkErrorException {

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;

        String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
        String PREFIX = "--", LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/binary"; // 内容类型

        String uri = urlStr + System.currentTimeMillis();
        URL url = null;
        try {
            url = new URL(uri);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIMEOUT_IN_MILLIONS_READ);
            conn.setConnectTimeout(TIMEOUT_IN_MILLIONS);
            conn.setDoInput(true); // 允许输入流
            conn.setDoOutput(true); // 允许输出流
            conn.setUseCaches(false); // 不允许使用缓存
            conn.setRequestMethod("POST"); // 请求方式
            conn.setRequestProperty("Charset", charSet); // 设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            for (Entry entry : header.entrySet()) {
                conn.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
                LogUtil.e("entry", entry.getKey() + "-->" + entry.getValue());
            }
            dos = new DataOutputStream(
                    conn.getOutputStream());

            if (file != null) {
                StringBuffer sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                sb.append("Content-Disposition: form-data; name=\"file\"; filename=\""
                        + file.getName() + "\"" + LINE_END);
                sb.append("Content-Type: application/octet-stream; charset=").append(charSet).append(LINE_END);
                sb.append(LINE_END);
                dos.write(sb.toString().getBytes());
                FileInputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = is.read(bytes)) != -1) {
                    dos.write(bytes, 0, len);
                }
                is.close();
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
                        .getBytes();

                dos.write(end_data);
                dos.flush();
            }
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                inputStream = conn.getInputStream();
                byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                byteArrayOutputStream.flush();
                return byteArrayOutputStream.toByteArray();
            } else {
                throw new NetworkErrorException(" ResponseCode is " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }

                if (inputStream != null) {
                    inputStream.close();
                }

                if (dos != null) {
                    dos.close();
                }

                if (conn != null) {
                    conn.disconnect();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    public static HashMap<String, String> setHeader(Context context, int requestType) {
        String packageName = context.getApplicationContext().getPackageName();
        String androidId = Util.getAndroidId(context.getApplicationContext());
        String appVersion = Util.getAppVersion(context.getApplicationContext());
        String time = String.valueOf(System.currentTimeMillis());
        String systemVersion = VERSION.RELEASE;
        String sid = SharedPreferenceUtil.getInstance(context.getApplicationContext()).getString(AdmoreSdkConfig.SID, "");
        HashMap<String, String> header = new HashMap();
        header.put(DeviceInfo.TIME, time);
        header.put(DeviceInfo.APP_KEY, AdmoreSdkConfig.APPKEY == null ? AdmoreSdkConfig.APP_KEY_DEFAULT : AdmoreSdkConfig.APPKEY);
        header.put(DeviceInfo.SDK_VERSION, AdmoreSdkConfig.SDK_VERSION);
        String str = DeviceInfo.ANDROID_ID;
        if (androidId == null) {
            androidId = AdmoreSdkConfig.ANDROIDID_DEFAULT;
        }
        header.put(str, androidId);
        str = DeviceInfo.PACKAGE_NAME;
        if (packageName == null) {
            packageName = AdmoreSdkConfig.PACKAGE_NAME_DEFAULT;
        }
        header.put(str, packageName);
        str = DeviceInfo.APP_VERSION;
        if (appVersion == null) {
            appVersion = AdmoreSdkConfig.APP_VERSION_DEFAULT;
        }
        header.put(str, appVersion);
        str = DeviceInfo.SYSTEM_VERSION;
        if (systemVersion == null) {
            systemVersion = AdmoreSdkConfig.SYSTEM_VERSION_DEFAULT;
        }
        header.put(str, systemVersion);
        if (requestType != LogInfo.AdmoreLogTypeStart) {
            header.put(DeviceInfo.SID, sid);
            header.put(DeviceInfo.DEVICEID, SharedPreferenceUtil.getInstance(context.getApplicationContext()).getString(AdmoreSdkConfig.DEVICE_ID, "e5ebeec1deac491badf60d1573b6adef"));
        } else {
            header.put(DeviceInfo.DEVICEID, SharedPreferenceUtil.getInstance(context.getApplicationContext()).getString(AdmoreSdkConfig.DEVICE_ID, ""));
        }
        return header;
    }
}
