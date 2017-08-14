package com.duodian.admore.android.sdk.imagecache;

import android.content.Context;
import android.graphics.Bitmap;

public class CacheHandlerManager {
    private static CacheHandlerManager INSTANCE = null;
    private static final String TAG = "CacheHandlerManager";
    public static int TYPE_GIF = 2;
    public static int TYPE_H5 = 4;
    public static int TYPE_IMG = 1;
    public static int TYPE_VIDEO = 3;

    public interface OnHandledCallback {
        void onHandledFailed();

        void onHandledSuccess(Bitmap bitmap);

        void onHandledSuccess(Object obj);

        void onHandledSuccess(String str);

        void onHandledSuccess(byte[] bArr);
    }

    public static class OnHandledCallbackAdapter implements OnHandledCallback {
        public void onHandledSuccess(byte[] bytes) {
        }

        public void onHandledSuccess(String path) {
        }

        public void onHandledSuccess(Bitmap bitmap) {
        }

        public void onHandledSuccess(Object object) {
        }

        public void onHandledFailed() {
        }
    }

    private CacheHandlerManager(Context context) {
        ImageLruCache.getInstance().setNextCacheHandler(ImageDiskLruCache.getInstance(context));
    }

    public static CacheHandlerManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CacheHandlerManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CacheHandlerManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public void handleRequest(String key, int type, OnHandledCallback onHandledCallback) {
        ImageLruCache.getInstance().handleRequest(key, type, onHandledCallback);
    }
}
