package com.duodian.admore.android.sdk;

import android.app.Application;
import com.duodian.admore.android.sdk.adview.splash.SplashAdManager;
import com.duodian.admore.android.sdk.utils.LogUtil;

public class SdkApplication extends Application {
    public void onCreate() {
        super.onCreate();
        LogUtil.e("SdkApplication", "onCreate");
        AdmoreSdk.getInstance().initialize(this, "meida-553b0646f7e5e");
        SplashAdManager.getInstance().setTargetClass(MainActivity.class).loadAd("unit-556aebe3186f0");
    }

    public void onTerminate() {
        LogUtil.e("terminate", "terminate");
        AdmoreSdk.terminate(this);
        LogUtil.e("terminate", "terminateAfter");
        super.onTerminate();
    }
}
