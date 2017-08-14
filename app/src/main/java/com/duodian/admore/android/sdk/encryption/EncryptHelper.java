package com.duodian.admore.android.sdk.encryption;

import com.duodian.admore.android.sdk.utils.LogUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EncryptHelper {
    public static final String NONCE_STR = "abcdefghijklmnopqrstuvwxyz";

    public static String getKey(String privateKey, String appKey, String appSecret, String sid, String deviceId, String idfa, String time, String bundleId, String systemVersion, String appVersion, String sdkVersion, String nonceStr) {
        if (privateKey == null || appSecret == null || sid == null || time == null || appKey == null || bundleId == null || systemVersion == null || appVersion == null || sdkVersion == null) {
            return null;
        }
        Date date = new Date(Long.valueOf(time));
        StringBuffer keyBuf = new StringBuffer();
        Calendar cal = Calendar.getInstance(Locale.CHINA);
        TimeZone timeZone = TimeZone.getTimeZone("GMT+8");
        cal.setTime(date);
        cal.setTimeZone(timeZone);
        for (char ch : nonceStr.toUpperCase().toCharArray()) {
            if (ch == 'A') {
                keyBuf.append(appKey);
            } else if (ch == 'B') {
                keyBuf.append(bundleId);
            } else if (ch == 'C') {
                keyBuf.append("Copy");
            } else if (ch == 'D') {
                keyBuf.append(cal.get(Calendar.DAY_OF_MONTH));
            } else if (ch == 'E') {
                keyBuf.append("ear");
            } else if (ch == 'F') {
                keyBuf.append("fee");
            } else if (ch == 'G') {
                keyBuf.append("gif");
            } else if (ch == 'H') {
                keyBuf.append(cal.get(Calendar.HOUR_OF_DAY));
            } else if (ch == 'I') {
                if (idfa != null) {
                    keyBuf.append(idfa);
                }
            } else if (ch == 'J') {
                keyBuf.append("jpg");
            } else if (ch == 'K') {
                if (privateKey != null) {
                    keyBuf.append(privateKey);
                }
            } else if (ch == 'L') {
                keyBuf.append("lol");
            } else if (ch == 'M') {
                keyBuf.append(cal.get(Calendar.MONTH) + 1);
            } else if (ch == 'N') {
                keyBuf.append("nut");
            } else if (ch == 'O') {
                keyBuf.append(sdkVersion);
            } else if (ch == 'P') {
                keyBuf.append("~!@&^^%");
            } else if (ch == 'Q') {
                keyBuf.append("qi");
            } else if (ch == 'R') {
                keyBuf.append("ren");
            } else if (ch == 'S') {
                keyBuf.append(sdkVersion);
            } else if (ch == 'T') {
                keyBuf.append(time);
            } else if (ch == 'U') {
                if (deviceId != null) {
                    keyBuf.append(deviceId);
                }
            } else if (ch == 'V') {
                keyBuf.append(appVersion);
            } else if (ch == 'W') {
                keyBuf.append("work");
            } else if (ch == 'X') {
                keyBuf.append("x-man");
            } else if (ch == 'Y') {
                keyBuf.append(cal.get(Calendar.YEAR));
            } else if (ch == 'Z') {
                keyBuf.append(systemVersion);
            }
            keyBuf.append('.');
        }
        LogUtil.e("key", keyBuf.toString());
        return MD5.md5(keyBuf.toString());
    }
}
