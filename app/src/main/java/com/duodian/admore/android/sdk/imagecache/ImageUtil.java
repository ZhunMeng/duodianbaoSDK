package com.duodian.admore.android.sdk.imagecache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import com.duodian.admore.android.sdk.utils.LogUtil;

public class ImageUtil {
    public static Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;
        if (width > reqWidth || height > reqHeight) {
            inSampleSize = Math.max(Math.round((((float) width) * 1.0f) / ((float) reqWidth)), Math.round((((float) height) * 1.0f) / ((float) reqHeight)));
        }
        LogUtil.e("size", width + "*" + height + "--" + reqWidth + "*" + reqHeight + "--" + inSampleSize);
        return inSampleSize;
    }
}
