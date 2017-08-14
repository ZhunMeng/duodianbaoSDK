package com.duodian.admore.android.sdk.callbacks;

public interface AdListener {
    void onAdClick();

    void onAdExpose();

    void onCloseClick();

    void onLoadFailed();

    void onReceiveAd();

    void onReceiveAdFailed();
}
