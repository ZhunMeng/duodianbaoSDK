package com.duodian.admore.android.sdk.http;

public interface HttpCallback {
    void onRequestComplete(String str);

    void onRequestComplete(byte[] bArr);

    void onRequestError(String str);
}
