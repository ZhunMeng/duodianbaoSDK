package com.duodian.admore.android.sdk;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import com.duodian.admore.android.sdk.adview.splash.SplashAdManager;
import com.duodian.admore.android.sdk.adview.splash.view.AdmoreSplashView;
import com.duodian.admore.android.sdk.callbacks.AdListener;
import com.duodian.admore.android.sdk.config.AdmoreSdkConfig;

public class SplashActivity extends Activity {
    private AdmoreSplashView admoreSplashView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admoresdk_activity_splash);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        AdmoreSdkConfig.width = dm.widthPixels;
        AdmoreSdkConfig.height = dm.heightPixels;
        this.admoreSplashView = (AdmoreSplashView) findViewById(R.id.admoreSplashView);
        this.admoreSplashView.setAdListener(new AdListener() {
            public void onReceiveAd() {
            }

            public void onReceiveAdFailed() {
            }

            public void onLoadFailed() {
            }

            public void onCloseClick() {
            }

            public void onAdClick() {
            }

            public void onAdExpose() {
            }
        });
        this.admoreSplashView.setTargetClass(this, SplashAdManager.getInstance().getTargetClass());
        this.admoreSplashView.loadAd(SplashAdManager.getInstance().getAdUnitId());
    }
}
