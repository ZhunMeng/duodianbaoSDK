package com.duodian.admore.android.sdk.adview.splash.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build.VERSION;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class GifView extends View {
    private static final int DEFAULT_MOVIE_VIEW_DURATION = 1000;
    private int mCurrentAnimationTime;
    private float mLeft;
    private int mMeasuredMovieHeight;
    private int mMeasuredMovieWidth;
    private int mMovieResourceId;
    private long mMovieStart;
    private volatile boolean mPaused;
    private float mScale;
    private float mTop;
    private boolean mVisible;
    private Movie movie;

    public GifView(Context context) {
        this(context, null);
    }

    public GifView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GifView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mVisible = true;
        if (VERSION.SDK_INT >= 11) {
            setLayerType(1, null);
        }
    }

    public void setGifResource(InputStream inputStream) {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, 16384);
        bufferedInputStream.mark(16384);
        this.movie = Movie.decodeStream(bufferedInputStream);
        requestLayout();
    }

    public void play() {
        if (this.mPaused) {
            this.mPaused = false;
            this.mMovieStart = SystemClock.uptimeMillis() - ((long) this.mCurrentAnimationTime);
            invalidate();
        }
    }

    public void pause() {
        if (!this.mPaused) {
            this.mPaused = true;
            invalidate();
        }
    }

    public boolean isPaused() {
        return this.mPaused;
    }

    public boolean isPlaying() {
        return !this.mPaused;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.movie != null) {
            int movieWidth = this.movie.width();
            int movieHeight = this.movie.height();
            float scaleH = 1.0f;
            if (MeasureSpec.getMode(widthMeasureSpec) != 0) {
                scaleH = ((float) movieWidth) / ((float) MeasureSpec.getSize(widthMeasureSpec));
            }
            float scaleW = 1.0f;
            if (MeasureSpec.getMode(heightMeasureSpec) != 0) {
                scaleW = ((float) movieHeight) / ((float) MeasureSpec.getSize(heightMeasureSpec));
            }
            this.mScale = 1.0f / Math.min(scaleH, scaleW);
            this.mMeasuredMovieWidth = (int) (((float) movieWidth) * this.mScale);
            this.mMeasuredMovieHeight = (int) (((float) movieHeight) * this.mScale);
            setMeasuredDimension(this.mMeasuredMovieWidth, this.mMeasuredMovieHeight);
            return;
        }
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mLeft = ((float) (getWidth() - this.mMeasuredMovieWidth)) / 2.0f;
        this.mTop = ((float) (getHeight() - this.mMeasuredMovieHeight)) / 2.0f;
        this.mVisible = getVisibility() == 0;
    }

    protected void onDraw(Canvas canvas) {
        if (this.movie == null) {
            return;
        }
        if (this.mPaused) {
            drawMovieFrame(canvas);
            return;
        }
        updateAnimationTime();
        drawMovieFrame(canvas);
        invalidateView();
    }

    private void invalidateView() {
        if (!this.mVisible) {
            return;
        }
        if (VERSION.SDK_INT >= 16) {
            postInvalidateOnAnimation();
        } else {
            invalidate();
        }
    }

    private void updateAnimationTime() {
        long now = SystemClock.uptimeMillis();
        if (this.mMovieStart == 0) {
            this.mMovieStart = now;
        }
        int dur = this.movie.duration();
        if (dur == 0) {
            dur = 1000;
        }
        this.mCurrentAnimationTime = (int) ((now - this.mMovieStart) % ((long) dur));
    }

    private void drawMovieFrame(Canvas canvas) {
        this.movie.setTime(this.mCurrentAnimationTime);
        canvas.save(1);
        canvas.scale(this.mScale, this.mScale);
        this.movie.draw(canvas, this.mLeft / this.mScale, this.mTop / this.mScale);
        canvas.restore();
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        this.mVisible = visibility == 0;
        invalidateView();
    }

    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        this.mVisible = visibility == 0;
        invalidateView();
    }
}
