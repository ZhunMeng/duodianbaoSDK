package com.duodian.admore.android.sdk.adview.appwall;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import com.duodian.admore.android.sdk.AdActivity;
import com.duodian.admore.android.sdk.R;
import com.duodian.admore.android.sdk.adview.BaseAdView;
import com.duodian.admore.android.sdk.adview.drift.CircularImageView;
import com.duodian.admore.android.sdk.config.AdmoreSdkConfig;
import com.duodian.admore.android.sdk.http.HttpCallbackAdapter;
import com.duodian.admore.android.sdk.http.HttpUtil;
import com.duodian.admore.android.sdk.imagecache.ImageDiskLruCache;
import com.duodian.admore.android.sdk.imagecache.ImageLruCache;
import com.duodian.admore.android.sdk.imagecache.ImageUtil;
import com.duodian.admore.android.sdk.log.LogInfo;
import com.duodian.admore.android.sdk.log.LoggerUtil;
import com.duodian.admore.android.sdk.model.AdInfo;
import com.duodian.admore.android.sdk.utils.LogUtil;
import com.duodian.admore.android.sdk.utils.Util;

public class AdmoreAppWallView extends BaseAdView {
    private static final String TAG = "AdmoreAppWallView";
    private boolean animate;
    private int animateDuration;
    private CircularImageView circularImageView;
    private int closeBtnHeight;
    private int closeBtnWidth;
    private ImageView closeImageView;
    private int mShape;

    public AdmoreAppWallView(Context context) {
        this(context, null);
    }

    public AdmoreAppWallView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdmoreAppWallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes(context, attrs, defStyleAttr);
        initViews(context);
    }

    private void initAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        setVisibility(View.VISIBLE);
        setAlpha(0.0f);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AdmoreDriftView, defStyleAttr, 0);
        this.mShape = typedArray.getInt(R.styleable.AdmoreDriftView_admore_shape, 1);
        this.closeBtnWidth = (int) typedArray.getDimension(R.styleable.AdmoreDriftView_admore_closeBtnWidth, Util.dp2px(context, 16.0f));
        this.closeBtnHeight = (int) typedArray.getDimension(R.styleable.AdmoreDriftView_admore_closeBtnHeight, Util.dp2px(context, 16.0f));
        this.animate = typedArray.getBoolean(R.styleable.AdmoreDriftView_admore_animate, true);
        this.animateDuration = typedArray.getInt(R.styleable.AdmoreDriftView_admore_animateDuration, 300);
        typedArray.recycle();
    }

    private void initViews(Context context) {
        if (getChildCount() == 0) {
            this.circularImageView = new CircularImageView(context, this.mShape);
            LayoutParams params = new LayoutParams(-1, -1);
            this.circularImageView.setVisibility(View.INVISIBLE);
            addView(this.circularImageView, params);
            this.closeImageView = new ImageView(context);
            LayoutParams layoutParams = new LayoutParams(-2, -2);
            layoutParams.width = this.closeBtnWidth;
            layoutParams.height = this.closeBtnHeight;
            layoutParams.addRule(11);
            this.closeImageView.setVisibility(View.INVISIBLE);
            addView(this.closeImageView, layoutParams);
        }
    }

    protected void onAdFetchedSuccess(AdInfo adInfo) {
        super.onAdFetchedSuccess(adInfo);
        if (adInfo.getAdIconVisible() == 1) {
            if (adInfo.getAdCloseVisible() == 1) {
                obtainImage(adInfo.getAdClose(), 1);
            }
            obtainImage(adInfo.getImageUrl(), 0);
        }
    }

    private void obtainImage(final String url, final int imgType) {
        if (!TextUtils.isEmpty(url)) {
            Bitmap bitmapCache = ImageLruCache.getInstance().getBitmapFromCache(Util.getUrlMd5(url));
            if (bitmapCache == null || bitmapCache.isRecycled()) {
                bitmapCache = ImageDiskLruCache.getInstance(getContext()).getBitmapFromDiskCache(Util.getUrlMd5(url));
                if (bitmapCache != null && !bitmapCache.isRecycled()) {
                    if (imgType == 0) {
                        OnBitmapObtained(bitmapCache);
                        this.adShowed = true;
                    } else if (1 == imgType) {
                        onCloseBitmapObtained(bitmapCache);
                    }
                    ImageLruCache.getInstance().addBitmapToCache(Util.getUrlMd5(url), bitmapCache);
                } else if (HttpUtil.isNetConnected(getContext())) {
                    HttpUtil.doGetBitmapAsync(url, new HttpCallbackAdapter() {
                        public void onRequestComplete(byte[] bytes) {
                            super.onRequestComplete(bytes);

                            Options options = new Options();
                            options.inJustDecodeBounds = true;
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                            if (imgType == 0) {
                                options.inSampleSize = ImageUtil.calculateInSampleSize(options, AdmoreAppWallView.this.circularImageView.getWidth(), AdmoreAppWallView.this.circularImageView.getHeight());
                                options.inJustDecodeBounds = false;
                                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                                if (!(bitmap == null || bitmap.isRecycled())) {
                                    AdmoreAppWallView.this.OnBitmapObtained(bitmap);
                                }
                                AdmoreAppWallView.this.adShowed = true;
                            } else if (1 == imgType) {
                                options.inSampleSize = ImageUtil.calculateInSampleSize(options, AdmoreAppWallView.this.closeImageView.getWidth(), AdmoreAppWallView.this.closeImageView.getHeight());
                                options.inJustDecodeBounds = false;
                                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                                if (!(bitmap == null || bitmap.isRecycled())) {
                                    AdmoreAppWallView.this.onCloseBitmapObtained(bitmap);
                                }
                            }
                            if (bitmap != null && !bitmap.isRecycled()) {
                                ImageLruCache.getInstance().addBitmapToCache(Util.getUrlMd5(url), bitmap);
                                ImageDiskLruCache.getInstance(AdmoreAppWallView.this.getContext()).addBitmapToDiskCache(Util.getUrlMd5(url), bitmap);
                            }
                        }

                        public void onRequestError(String result) {
                            super.onRequestError(result);
                            if (AdmoreAppWallView.this.adListener != null) {
                                AdmoreAppWallView.this.adListener.onLoadFailed();
                            }
                        }
                    });
                }
            } else if (imgType == 0) {
                OnBitmapObtained(bitmapCache);
                this.adShowed = true;
            } else if (1 == imgType) {
                onCloseBitmapObtained(bitmapCache);
            }
        }
    }

    private void OnBitmapObtained(final Bitmap bitmap) {
        if (this.circularImageView != null) {
            this.circularImageView.post(new Runnable() {
                public void run() {
                    if (AdmoreAppWallView.this.adInfo.getAdIconVisible() == 1) {
                        AdmoreAppWallView.this.circularImageView.setVisibility(View.VISIBLE);
                    }
                    AdmoreAppWallView.this.circularImageView.setImageBitmap(bitmap);
                    AdmoreAppWallView.this.circularImageView.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            Intent intentWeb = new Intent(AdmoreAppWallView.this.getContext(), AdActivity.class);
                            intentWeb.putExtra(AdmoreSdkConfig.URL, AdmoreAppWallView.this.adInfo.getClickUrl());
                            AdmoreAppWallView.this.getContext().startActivity(intentWeb);
                            if (AdmoreAppWallView.this.adListener != null) {
                                AdmoreAppWallView.this.adListener.onAdClick();
                            }
                            LogInfo logInfo = new LogInfo(AdmoreAppWallView.this.getContext().getApplicationContext());
                            logInfo.setT(System.currentTimeMillis());
                            logInfo.setI(LogInfo.AdmoreLogTypeAdvClick);
                            logInfo.setU(AdmoreAppWallView.this.adUnitId);
                            LoggerUtil.saveLog(AdmoreAppWallView.this.getContext().getApplicationContext(), logInfo.getJsonString());
                        }
                    });
                    AdmoreAppWallView.this.setVisibility(View.VISIBLE);
                    if (AdmoreAppWallView.this.animate) {
                        ViewCompat.animate(AdmoreAppWallView.this).alpha(1.0f).setDuration((long) AdmoreAppWallView.this.animateDuration).start();
                    } else {
                        AdmoreAppWallView.this.setAlpha(1.0f);
                    }
                    if (AdmoreAppWallView.this.adListener != null) {
                        AdmoreAppWallView.this.adListener.onAdExpose();
                    }
                }
            });
        }
    }

    private void onCloseBitmapObtained(final Bitmap bitmap) {
        if (this.closeImageView != null) {
            this.closeImageView.post(new Runnable() {
                public void run() {
                    if (AdmoreAppWallView.this.adInfo.getAdCloseVisible() == 1) {
                        AdmoreAppWallView.this.closeImageView.setVisibility(View.VISIBLE);
                    }
                    AdmoreAppWallView.this.closeImageView.setImageBitmap(bitmap);
                    AdmoreAppWallView.this.closeImageView.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            AdmoreAppWallView.this.setVisibility(View.INVISIBLE);
                            AdmoreAppWallView.this.setAlpha(0.0f);
                            if (AdmoreAppWallView.this.adListener != null) {
                                AdmoreAppWallView.this.adListener.onCloseClick();
                            }
                        }
                    });
                }
            });
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.circularImageView != null) {
            this.circularImageView.removeCallbacks(null);
        }
        if (this.closeImageView != null) {
            this.closeImageView.removeCallbacks(null);
        }
    }
}
