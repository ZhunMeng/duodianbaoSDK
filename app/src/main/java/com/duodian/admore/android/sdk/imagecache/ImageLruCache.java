package com.duodian.admore.android.sdk.imagecache;

import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.util.LruCache;

public class ImageLruCache extends CacheHandler {
    private LruCache<String, Bitmap> lruCache;
    private int maxMemory;

    private static class ImageLruCacheHolder {
        private static ImageLruCache imageLruCache = new ImageLruCache();

        private ImageLruCacheHolder() {
        }
    }

    private ImageLruCache() {
        this.maxMemory = 1048576;
        this.lruCache = new LruCache<String, Bitmap>(this.maxMemory) {
            protected int sizeOf(String key, Bitmap value) {
                if (VERSION.SDK_INT >= 12) {
                    return value.getByteCount();
                }
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    public static ImageLruCache getInstance() {
        return ImageLruCacheHolder.imageLruCache;
    }

    public void addBitmapToCache(String key, Bitmap bitmap) {
        int i = 1;
        int i2 = key != null ? 1 : 0;
        if (bitmap == null) {
            i = 0;
        }
        if ((i & i2) != 0 && this.lruCache != null && this.lruCache.get(key) == null) {
            this.lruCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromCache(String key) {
        if (this.lruCache != null) {
            return (Bitmap) this.lruCache.get(key);
        }
        return null;
    }

    public void clearCache() {
        if (this.lruCache != null) {
            this.lruCache.evictAll();
        }
    }

    protected Object getCache(String key, int type) {
        if (type == CacheHandlerManager.TYPE_IMG) {
            return getBitmapFromCache(key);
        }
        return null;
    }
}
