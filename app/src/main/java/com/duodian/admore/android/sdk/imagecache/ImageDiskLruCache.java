package com.duodian.admore.android.sdk.imagecache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import com.duodian.admore.android.sdk.config.AdmoreSdkConfig;
import com.duodian.admore.android.sdk.imagecache.DiskLruCache.Editor;
import com.duodian.admore.android.sdk.imagecache.DiskLruCache.Snapshot;
import com.duodian.admore.android.sdk.utils.LogUtil;
import com.duodian.admore.android.sdk.utils.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

public class ImageDiskLruCache extends CacheHandler {
    private static volatile ImageDiskLruCache INSTANCE;
    private static final String TAG = "ImageDiskLruCache";
    private Context context;
    private DiskLruCache diskLruCache;
    private List<String> listName;

    private ImageDiskLruCache(Context context) {
        init(context);
    }

    public static ImageDiskLruCache getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ImageDiskLruCache.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ImageDiskLruCache(context);
                }
            }
        }
        return INSTANCE;
    }

    private void init(Context context) {
        LogUtil.e(TAG, "init()");
        this.context = context;
        File cacheFile = getDiskCacheDir(context);
        if (cacheFile != null && cacheFile.isDirectory()) {
            try {
                this.diskLruCache = DiskLruCache.open(cacheFile, 1, 1, 10485760);
            } catch (IOException e) {
                e.printStackTrace();
                this.diskLruCache = null;
            }
        }
    }

    private File getDiskCacheDir(Context context) {
        File file;
        if (Util.externalStorageAvailable()) {
            file = new File(context.getExternalCacheDir(), "com.duodian.admore.android.sdk");
            LogUtil.e(TAG, "init:path:" + file.getAbsolutePath());
            LogUtil.e(TAG, "使用外部存储的私有目录");
        } else {
            file = new File(context.getCacheDir(), "com.duodian.admore.android.sdk");
            LogUtil.e(TAG, "saveLog:path:" + file.getAbsolutePath());
            LogUtil.e(TAG, "使用内部存储");
        }
        File imageCacheDirectory;
        if (file.isDirectory()) {
            imageCacheDirectory = new File(file, AdmoreSdkConfig.IMAGE_CACHE_DIRECTORY);
            if (imageCacheDirectory.isDirectory() || imageCacheDirectory.mkdirs()) {
                return imageCacheDirectory;
            }
        } else if (file.mkdirs()) {
            imageCacheDirectory = new File(file, AdmoreSdkConfig.IMAGE_CACHE_DIRECTORY);
            if (imageCacheDirectory.mkdirs()) {
                return imageCacheDirectory;
            }
        }
        return null;
    }

    public void addBitmapToDiskCache(String key, Bitmap bitmap) {
        if (this.diskLruCache != null && key != null) {
            try {
                Editor editor = this.diskLruCache.edit(key);
                if (editor != null) {
                    OutputStream outputStream = editor.newOutputStream(0);
                    if (bitmap != null) {
                        if (bitmap.compress(CompressFormat.PNG, 100, outputStream)) {
                            editor.commit();
                            LogUtil.e(TAG, "editor.commit()" + key);
                        } else {
                            editor.abort();
                        }
                    }
                    outputStream.flush();
                    outputStream.close();
                    this.diskLruCache.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addBytesToDiskCache(String key, byte[] bytes) {
        if (this.diskLruCache != null && key != null) {
            try {
                Editor editor = this.diskLruCache.edit(key);
                if (editor != null) {
                    OutputStream outputStream = editor.newOutputStream(0);
                    if (bytes == null || bytes.length <= 0) {
                        editor.abort();
                    } else {
                        outputStream.write(bytes);
                        editor.commit();
                        LogUtil.e(TAG, "editor.commit()" + key);
                    }
                    outputStream.flush();
                    outputStream.close();
                    this.diskLruCache.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap getBitmapFromDiskCache(String key) {
        Bitmap bitmap = null;
        if (!(this.diskLruCache == null || key == null)) {
            try {
                LogUtil.e(TAG, "getBitmapFromDiskCache:" + key);
                Snapshot snapShot = this.diskLruCache.get(key);
                if (snapShot != null) {
                    InputStream inputStream = snapShot.getInputStream(0);
                    Options options = new Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(inputStream, null, options);
                    options.inSampleSize = ImageUtil.calculateInSampleSize(options, AdmoreSdkConfig.width, AdmoreSdkConfig.height);
                    LogUtil.e(TAG, "inSampleSize:" + options.inSampleSize);
                    options.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public byte[] getBytesFromDiskCache(String key) {
        if (!(this.diskLruCache == null || key == null)) {
            try {
                LogUtil.e(TAG, "getStreamFromDiskCache:" + key);
                Snapshot snapShot = this.diskLruCache.get(key);
                if (snapShot != null) {
                    return Util.readStream(snapShot.getInputStream(0));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public InputStream getGifFromDiskCache(String key) {
        if (!(this.diskLruCache == null || key == null)) {
            try {
                LogUtil.e(TAG, "getStreamFromDiskCache:" + key);
                Snapshot snapShot = this.diskLruCache.get(key);
                if (snapShot != null) {
                    return snapShot.getInputStream(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getDirPath() {
        return this.diskLruCache.getDirectory().getAbsolutePath();
    }

    public boolean isExist(String name) {
        if (this.diskLruCache == null) {
            return false;
        }
        this.listName = Arrays.asList(this.diskLruCache.getDirectory().list());
        return this.listName.contains(name);
    }

    protected Object getCache(String key, int type) {
        if (type == CacheHandlerManager.TYPE_IMG) {
            return getBitmapFromDiskCache(key);
        }
        if (type == CacheHandlerManager.TYPE_GIF) {
            return getGifFromDiskCache(key);
        }
        if (type == CacheHandlerManager.TYPE_VIDEO && isExist(key)) {
            String path = getDirPath();
            if (path != null) {
                return path.endsWith("/") ? path + key : path + "/" + key;
            }
        }
        return null;
    }
}
