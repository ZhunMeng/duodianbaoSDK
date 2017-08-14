package com.duodian.admore.android.sdk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreferenceUtil {
    private static Editor editor;
    private static SharedPreferenceUtil sharedPreferenceUtil;
    private static SharedPreferences sharedPreferences;

    private SharedPreferenceUtil(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences("com.duodian.admore.android.sdk", 0);
        editor = sharedPreferences.edit();
    }

    public static SharedPreferenceUtil getInstance(Context context) {
        if (sharedPreferenceUtil == null) {
            synchronized (SharedPreferenceUtil.class) {
                if (sharedPreferenceUtil == null) {
                    sharedPreferenceUtil = new SharedPreferenceUtil(context);
                }
            }
        }
        return sharedPreferenceUtil;
    }

    public SharedPreferenceUtil putString(String key, String value) {
        if (editor != null) {
            editor.putString(key, value);
        } else {
            editor = sharedPreferences.edit();
            editor.putString(key, value);
        }
        return sharedPreferenceUtil;
    }

    public void apply() {
        if (editor != null) {
            editor.apply();
        }
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }
}
