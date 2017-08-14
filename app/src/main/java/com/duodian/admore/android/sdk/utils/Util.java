package com.duodian.admore.android.sdk.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.provider.Settings.System;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.TypedValue;

import com.alibaba.sdk.android.oss.common.RequestParameters;
import com.duodian.admore.android.sdk.model.AdInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;

public class Util {
    public static String getAndroidId(Context context) {
        return System.getString(context.getContentResolver(), "android_id");
    }

    public static String getDeviceId(Context context) {
        if (ContextCompat.checkSelfPermission(context, "android.permission.READ_PHONE_STATE") == 0) {
            return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        }
        return null;
    }

    public static String getAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getDateString() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date(java.lang.System.currentTimeMillis()));
    }

    public static String getDiskCacheDir(Context context) {
        if ("mounted".equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            return context.getExternalCacheDir().getPath();
        }
        return context.getCacheDir().getPath();
    }

    public static String getMac() {
        String macSerial = null;
        String str = "";
        try {
            LineNumberReader input = new LineNumberReader(new InputStreamReader(Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ").getInputStream()));
            while (str != null) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return macSerial;
    }

    public static String getGPRSLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                Enumeration<InetAddress> enumIpAddr = ((NetworkInterface) en.nextElement()).getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatIpAddress(int ipAdress) {
        return (ipAdress & 255) + "." + ((ipAdress >> 8) & 255) + "." + ((ipAdress >> 16) & 255) + "." + ((ipAdress >> 24) & 255);
    }

    public static String getHostIp() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                Enumeration<InetAddress> ipAddr = ((NetworkInterface) en.nextElement()).getInetAddresses();
                while (ipAddr.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress) ipAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            int len = inStream.read(buffer);
            if (len >= 0) {
                outSteam.write(buffer, 0, len);
            } else {
                outSteam.close();
                inStream.close();
                return outSteam.toByteArray();
            }
        }
    }

    public static byte[] bmpToByteArray(Bitmap bmp, boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static float dp2px(Context context, float dpVal) {
        return TypedValue.applyDimension(1, dpVal, context.getResources().getDisplayMetrics());
    }

    public static boolean isServiceRunning(Context context, String service_Name) {
//        for (RunningServiceInfo service : ((ActivityManager) context.getSystemService("activity")).getRunningServices(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED)) {
//            if (service_Name.equals(service.service.getClassName())) {
//                return true;
//            }
//        }
        return false;
    }

    public static Location getLocation(Context context) {
        if (ContextCompat.checkSelfPermission(context, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
        }
        return null;
    }

    public static boolean externalStorageAvailable() {
        return "mounted".equalsIgnoreCase(Environment.getExternalStorageState());
    }

    public static byte[] intToByteArray(int i) {
        return new byte[]{(byte) ((i >> 24) & 255), (byte) ((i >> 16) & 255), (byte) ((i >> 8) & 255), (byte) (i & 255)};
    }

    public static int byteToInt(byte[] b) {
        return (((b[3] & 255) | ((b[2] & 255) << 8)) | ((b[1] & 255) << 16)) | ((b[0] & 255) << 24);
    }

    public static String getUrlMd5(String url) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(url.getBytes());
            return bytesToHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            String md5 = String.valueOf(url.hashCode());
            e.printStackTrace();
            return md5;
        }
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 255);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static String getStringFromObject(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(baos).writeObject(object);
            String string = new String(Base64.encode(baos.toByteArray(), 0));
            LogUtil.e("string", string);
            return string;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static AdInfo getObjectFromString(String string) throws IOException, ClassNotFoundException {
        return (AdInfo) new ObjectInputStream(new ByteArrayInputStream(Base64.decode(string.getBytes(), 0))).readObject();
    }
}
