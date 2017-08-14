package com.duodian.admore.android.sdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.duodian.admore.android.sdk.config.AdmoreSdkConfig;
import com.duodian.admore.android.sdk.http.HttpConfig;
import com.duodian.admore.android.sdk.service.RequestService;

public class AdmoreSdk {
    private static volatile AdmoreSdk INSTANCE;

    private AdmoreSdk() {
    }

    public static AdmoreSdk getInstance() {
        if (INSTANCE == null) {
            synchronized (AdmoreSdk.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AdmoreSdk();
                }
            }
        }
        return INSTANCE;
    }

    public void initialize(Context context, String appKey) {
        if (context == null) {
            throw new IllegalArgumentException("context can not be null");
        } else if (TextUtils.isEmpty(appKey)) {
            throw new IllegalArgumentException("appKey can not be null");
        } else {
            AdmoreSdkConfig.APPKEY = appKey;
            Intent intent = new Intent(context, RequestService.class);
            Bundle bundle = new Bundle();
            bundle.putString(AdmoreSdkConfig.URL, HttpConfig.URL_DEVICE_REGISTER);
            intent.putExtras(bundle);
            context.startService(intent);
        }
    }

    public static void terminate(Context context) {
        Intent intent = new Intent(context, RequestService.class);
        Bundle bundle = new Bundle();
        bundle.putString(AdmoreSdkConfig.URL, HttpConfig.URL_AD_TERMINAL);
        intent.putExtras(bundle);
        context.startService(intent);
    }
}
