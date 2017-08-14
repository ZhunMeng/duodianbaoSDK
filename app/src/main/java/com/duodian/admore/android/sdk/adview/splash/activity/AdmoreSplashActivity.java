package com.duodian.admore.android.sdk.adview.splash.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import com.duodian.admore.android.sdk.adview.splash.SplashAdManager;
import com.duodian.admore.android.sdk.adview.splash.view.AdmoreSplashView;

public class AdmoreSplashActivity extends Activity {
    private AdmoreSplashView admoreSplashView;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.admoreSplashView = new AdmoreSplashView(this);
        addContentView(this.admoreSplashView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        initViews();
    }

    private void initViews() {
        if (SplashAdManager.getInstance().getTargetClass() == null) {
            throw new IllegalStateException("target class is null");
        } else if (TextUtils.isEmpty(SplashAdManager.getInstance().getAdUnitId())) {
            startActivity(new Intent(this, SplashAdManager.getInstance().getTargetClass()));
            finish();
        } else {
            this.admoreSplashView.setTargetClass(this, SplashAdManager.getInstance().getTargetClass());
            this.admoreSplashView.loadAd(SplashAdManager.getInstance().getAdUnitId());
        }
    }
}
