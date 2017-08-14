package com.duodian.admore.android.sdk.utils;

import android.util.Log;

public class LogUtil {
    public static final int LOG_LEVEL_ALL = 5;
    public static final int LOG_LEVEL_DEBUG = 1;
    public static final int LOG_LEVEL_ERROR = 4;
    public static final int LOG_LEVEL_INFO = 2;
    public static final int LOG_LEVEL_NONE = 0;
    public static final int LOG_LEVEL_WARN = 3;
    private static int mLogLevel = LOG_LEVEL_ALL;

    public static int getLogLevel() {
        return mLogLevel;
    }

    public static void setLogLevel(int level) {
        mLogLevel = level;
    }

    public static void d(String tag, String msg) {
        if (getLogLevel() >= 1) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (getLogLevel() >= 2) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (getLogLevel() >= 3) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (getLogLevel() >= 4) {
            Log.e(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (getLogLevel() >= 5) {
            Log.v(tag, msg);
        }
    }
}
