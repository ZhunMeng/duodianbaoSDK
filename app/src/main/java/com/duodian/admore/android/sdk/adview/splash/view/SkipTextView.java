package com.duodian.admore.android.sdk.adview.splash.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;

import com.duodian.admore.android.sdk.model.AdInfo;
import com.duodian.admore.android.sdk.utils.Util;

public class SkipTextView extends android.support.v7.widget.AppCompatTextView {
    private AdInfo adInfo;
    private int countDown;
    private Runnable countDownRunnable;
    private boolean isSkip;
    private OnSkipEventListener onSkipEventListener;

    interface OnSkipEventListener {
        void onCountDownFinish();

        void onSkipClick();
    }

    public SkipTextView(Context context) {
        this(context, null);
    }

    public SkipTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SkipTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.countDown = 5;
        setTextColor(Color.parseColor("#ffffffff"));
        setTextSize(2, 14.0f);
        setGravity(17);
        setVisibility(View.INVISIBLE);
    }

    public void setAdInfo(final AdInfo adInfo) {
        this.adInfo = adInfo;
        this.countDown = adInfo.getCountDown();
        if (adInfo.getCountDownVisible() == 0) {
            if (adInfo.getSkipVisible() == 1) {
                setRoundBackground();
                setText("跳过");
                setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (SkipTextView.this.onSkipEventListener != null) {
                            SkipTextView.this.removeCallbacks(SkipTextView.this.countDownRunnable);
                            SkipTextView.this.onSkipEventListener.onSkipClick();
                            SkipTextView.this.isSkip = true;
                        }
                    }
                });
            } else {
                setVisibility(View.INVISIBLE);
            }
        } else if (adInfo.getCountDownVisible() == 1) {
            if (adInfo.getSkipVisible() == 1) {
                setRoundBackground();
                setText("跳过 " + this.countDown + " ");
                setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (SkipTextView.this.onSkipEventListener != null) {
                            SkipTextView.this.removeCallbacks(SkipTextView.this.countDownRunnable);
                            SkipTextView.this.onSkipEventListener.onSkipClick();
                            SkipTextView.this.isSkip = true;
                        }
                    }
                });
            } else {
                setCircularBackground();
                setText(String.valueOf(this.countDown));
                setFocusable(false);
            }
        }
        this.countDownRunnable = new Runnable() {
            public void run() {
                if (SkipTextView.this.countDown > 1) {
                    SkipTextView.this.countDown = SkipTextView.this.countDown - 1;
                    if (adInfo.getSkipTime() >= SkipTextView.this.countDown) {
                        SkipTextView.this.setVisibility(View.VISIBLE);
                    }
                    SkipTextView.this.postInvalidate();
                    SkipTextView.this.postDelayed(this, 1000);
                } else if (SkipTextView.this.onSkipEventListener != null && !SkipTextView.this.isSkip) {
                    SkipTextView.this.onSkipEventListener.onCountDownFinish();
                }
            }
        };
        setVisibility(View.VISIBLE);
        if (adInfo.getSkipTime() >= this.countDown) {
            setVisibility(View.VISIBLE);
        }
        postDelayed(this.countDownRunnable, 1000);
        postInvalidate();
    }

    private void setRoundBackground() {
        int padding = (int) Util.dp2px(getContext(), 6.0f);
        setPadding(padding * 2, padding, padding * 2, padding);
        ShapeDrawable shapeDrawable = new ShapeDrawable();
        shapeDrawable.setShape(new RoundRectShape(new float[]{50.0f, 50.0f, 50.0f, 50.0f, 50.0f, 50.0f, 50.0f, 50.0f}, new RectF(), new float[]{50.0f, 50.0f, 50.0f, 50.0f, 50.0f, 50.0f, 50.0f, 50.0f}));
        shapeDrawable.getPaint().setColor(Color.parseColor("#20000000"));
        shapeDrawable.getPaint().setAntiAlias(true);
        setBackgroundDrawable(shapeDrawable);
    }

    private void setCircularBackground() {
        if (getLayoutParams() instanceof LayoutParams) {
            LayoutParams params = (LayoutParams) getLayoutParams();
            params.width = (int) TypedValue.applyDimension(2, 30.0f, getResources().getDisplayMetrics());
            params.height = (int) TypedValue.applyDimension(2, 30.0f, getResources().getDisplayMetrics());
            setLayoutParams(params);
        }
        ShapeDrawable shapeDrawable = new ShapeDrawable();
        shapeDrawable.setShape(new ArcShape(0.0f, 360.0f) {
            public void draw(Canvas canvas, Paint paint) {
                super.draw(canvas, paint);
                canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, getHeight() / 2.0f, paint);
            }
        });
        shapeDrawable.getPaint().setColor(Color.parseColor("#20000000"));
        shapeDrawable.getPaint().setAntiAlias(true);
        setBackgroundDrawable(shapeDrawable);
    }

    public void postInvalidate() {
        super.postInvalidate();
        if (this.adInfo.getCountDownVisible() == 1) {
            if (this.adInfo.getSkipVisible() == 1) {
                setText("跳过 " + this.countDown + " ");
            } else {
                setText(String.valueOf(this.countDown));
            }
        } else if (this.adInfo.getSkipVisible() == 1) {
            setText("跳过 ");
        } else {
            setVisibility(View.INVISIBLE);
        }
    }

    public void setOnSkipEventListener(OnSkipEventListener onSkipEventListener) {
        this.onSkipEventListener = onSkipEventListener;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.countDownRunnable);
    }
}
