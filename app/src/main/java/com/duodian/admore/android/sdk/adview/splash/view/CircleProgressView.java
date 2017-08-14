package com.duodian.admore.android.sdk.adview.splash.view;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class CircleProgressView extends View {
    private float height_text;
    private OnProgressFinishedListener listener;
    private int maxDuration;
    private RectF ovalRect;
    private Paint paint;
    private Paint paint_text;
    private Rect rect_text;
    private float scale;
    private int startAngle;
    private float strokeWidth;
    private int sweepAngle;
    private String text;
    private ValueAnimator valueAnimator;
    private float width;
    private float width_text;

    interface OnProgressFinishedListener {
        void onProgressFinish();
    }

    public CircleProgressView(Context context) {
        this(context, null);
    }

    public CircleProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.maxDuration = 5;
        this.startAngle = 0;
        this.text = "跳过";
        this.scale = context.getResources().getDisplayMetrics().density;
        this.strokeWidth = 2.0f * this.scale;
        this.paint = new Paint(1);
        this.paint.setStrokeWidth(this.strokeWidth);
        this.paint.setColor(Color.parseColor("#CCD7246C"));
        this.paint.setStyle(Style.STROKE);
        this.paint.setAntiAlias(true);
        this.paint_text = new Paint();
        this.paint_text.setTextSize((float) Math.round(12.0f * this.scale));
        this.rect_text = new Rect();
        this.paint_text.getTextBounds(this.text, 0, this.text.length(), this.rect_text);
        this.height_text = (float) this.rect_text.height();
        this.width_text = (float) this.rect_text.width();
        this.width = this.scale * 40.0f;
    }

    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
        startDrawAnimation();
        postInvalidate();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.ovalRect != null) {
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, 3));
            canvas.drawArc(this.ovalRect, (float) this.startAngle, (float) this.sweepAngle, false, this.paint);
            this.paint_text.setColor(Color.parseColor("#33000000"));
            this.paint_text.setStyle(Style.FILL);
            canvas.drawCircle(this.width / 2.0f, this.width / 2.0f, (this.width / 2.0f) - this.strokeWidth, this.paint_text);
            this.paint_text.setColor(Color.parseColor("#ffffff"));
            canvas.drawText(this.text, (this.width / 2.0f) - (this.width_text / 2.0f), (this.width / 2.0f) + (this.height_text / 2.0f), this.paint_text);
        }
    }

    public boolean post(Runnable action) {
        return super.post(action);
    }

    private void startDrawAnimation() {
        if (this.width == 0.0f) {
            this.width = 40.0f * this.scale;
        }
        float centre = this.width / 2.0f;
        float radius = centre - this.strokeWidth;
        this.ovalRect = new RectF(centre - radius, centre - radius, centre + radius, centre + radius);
        this.valueAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f}).setDuration((long) (this.maxDuration * 1000));
        this.valueAnimator.setInterpolator(new LinearInterpolator());
        this.valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float factor = ((Float) animation.getAnimatedValue()).floatValue();
                CircleProgressView.this.sweepAngle = (int) (360.0f * factor);
                CircleProgressView.this.postInvalidate();
                if (factor == 1.0f && CircleProgressView.this.listener != null) {
                    CircleProgressView.this.postDelayed(new Runnable() {
                        public void run() {
                            CircleProgressView.this.listener.onProgressFinish();
                        }
                    }, 500);
                }
            }
        });
        this.valueAnimator.start();
    }

    protected void stopDrawAnimation() {
        if (this.valueAnimator != null) {
            this.valueAnimator.cancel();
            removeCallbacks(null);
            this.sweepAngle = 0;
        }
    }

    public void setOnProgressFinishedListener(OnProgressFinishedListener listener) {
        this.listener = listener;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopDrawAnimation();
    }
}
