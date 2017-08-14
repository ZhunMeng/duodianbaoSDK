package com.duodian.admore.android.sdk.imagecache;

import com.duodian.admore.android.sdk.imagecache.CacheHandlerManager.OnHandledCallback;

public abstract class CacheHandler {
    private static final String TAG = "CacheHandler";
    private CacheHandler nextCacheHandler;

    protected abstract Object getCache(String str, int i);

    public void setNextCacheHandler(CacheHandler nextCacheHandler) {
        this.nextCacheHandler = nextCacheHandler;
    }

    public void handleRequest(String key, int type, OnHandledCallback onHandledCallback) {
        Object object = getCache(key, type);
        if (object != null) {
            if (onHandledCallback != null) {
                onHandledCallback.onHandledSuccess(object);
            }
        } else if (this.nextCacheHandler != null) {
            this.nextCacheHandler.handleRequest(key, type, onHandledCallback);
        } else if (onHandledCallback != null) {
            onHandledCallback.onHandledFailed();
        }
    }
}
