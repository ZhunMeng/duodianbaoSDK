package com.duodian.admore.android.sdk.adview.splash;

import android.text.TextUtils;

public class SplashAdManager {
    private String adUnitId;
    private Class targetClass;

    private static class SplashAdManagerHolder {
        private static SplashAdManager splashAdManager = new SplashAdManager();

        private SplashAdManagerHolder() {
        }
    }

    private SplashAdManager() {
    }

    public static SplashAdManager getInstance() {
        return SplashAdManagerHolder.splashAdManager;
    }

    public void loadAd(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            throw new IllegalArgumentException("adUnitId can not be null");
        }
        this.adUnitId = adUnitId;
    }

    public SplashAdManager setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
        return this;
    }

    public String getAdUnitId() {
        return this.adUnitId;
    }

    public Class getTargetClass() {
        return this.targetClass;
    }
}
