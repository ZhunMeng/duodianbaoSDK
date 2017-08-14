package com.duodian.admore.android.sdk.adview.drift;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class CircularImageView extends android.support.v7.widget.AppCompatImageView {
    private Paint mPaint;
    private int mShape;

    public CircularImageView(Context context, int shape) {
        this(context, null);
        this.mShape = shape;
    }

    public CircularImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mPaint = new Paint(1);
        setScaleType(ScaleType.CENTER_CROP);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, 3));
        Drawable drawable = getDrawable();
        if (drawable == null || this.mShape != 0) {
            super.onDraw(canvas);
            return;
        }
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        if (bitmap != null) {
            Bitmap bitmapTarget = getCircularBitmap(bitmap);
            Rect rectSrc = new Rect(0, 0, bitmapTarget.getWidth(), bitmapTarget.getHeight());
            Rect rectDst = new Rect(0, 0, getWidth(), getHeight());
            this.mPaint.reset();
            canvas.drawBitmap(bitmapTarget, rectSrc, rectDst, this.mPaint);
            return;
        }
        super.onDraw(canvas);
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap bitmapTarget = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapTarget);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, 3));
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        this.mPaint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        this.mPaint.setColor(-1);
        int width = bitmap.getWidth();
        canvas.save();
        canvas.drawCircle((float) (width / 2), (float) (width / 2), (float) (width / 2), this.mPaint);
        this.mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, this.mPaint);
        canvas.restore();
        return bitmapTarget;
    }
}
