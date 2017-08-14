package com.duodian.admore.android.sdk.adview.splash.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.InputDeviceCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.VideoView;

import com.duodian.admore.android.sdk.AdActivity;
import com.duodian.admore.android.sdk.adview.BaseAdView;
import com.duodian.admore.android.sdk.config.AdmoreSdkConfig;
import com.duodian.admore.android.sdk.encryption.AESEncryption;
import com.duodian.admore.android.sdk.encryption.EncryptHelper;
import com.duodian.admore.android.sdk.http.HttpCallbackAdapter;
import com.duodian.admore.android.sdk.http.HttpConfig;
import com.duodian.admore.android.sdk.http.HttpUtil;
import com.duodian.admore.android.sdk.imagecache.CacheHandlerManager;
import com.duodian.admore.android.sdk.imagecache.CacheHandlerManager.OnHandledCallbackAdapter;
import com.duodian.admore.android.sdk.imagecache.ImageDiskLruCache;
import com.duodian.admore.android.sdk.imagecache.ImageLruCache;
import com.duodian.admore.android.sdk.imagecache.ImageUtil;
import com.duodian.admore.android.sdk.log.LogInfo;
import com.duodian.admore.android.sdk.log.LoggerUtil;
import com.duodian.admore.android.sdk.model.AdInfo;
import com.duodian.admore.android.sdk.model.DeviceInfo;
import com.duodian.admore.android.sdk.service.RequestService;
import com.duodian.admore.android.sdk.utils.LogUtil;
import com.duodian.admore.android.sdk.utils.SharedPreferenceUtil;
import com.duodian.admore.android.sdk.utils.Util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.json.JSONObject;

public class AdmoreSplashView extends BaseAdView implements SkipTextView.OnSkipEventListener {
    private static int FULL_SCREEN = 2;
    private static int HALF_SCREEN = 1;
    private static int IMAGE_AD = 1;
    private static int IMAGE_AD_ICON = 3;
    private static int IMAGE_LOGO = 2;
    private static final String TAG = "AdmoreSplashView";
    private static int TYPE_GIF = 2;
    private static int TYPE_H5 = 5;
    private static int TYPE_IMG = 1;
    private static int TYPE_VIDEO = 3;
    private long DEFAULT_TIME_OUT = 2000;
    private WeakReference<Activity> activity;
    private int animateDuration = 350;
    private GifView gifImageView;
    private Handler handler;
    private ImageView imageViewAd;
    private ImageView imageViewAdIcon;
    private ImageView imageViewLogo;
    private Class targetClass;
    private boolean timeOut;
    private Runnable timeOutRunnable;
    private boolean useCache;
    private VideoView videoView;

    public AdmoreSplashView(Context context) {
        this(context, null);
    }

    public AdmoreSplashView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdmoreSplashView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.timeOutRunnable = new Runnable() {
            public void run() {
                AdmoreSplashView.this.gotoTargetClass("timeOutRunnable");
            }
        };
        initViews();
    }

    private void initViews() {
        if (getChildCount() > 0) {
            throw new IllegalStateException("do not add other Views to AdView");
        }
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void loadAd(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            throw new IllegalArgumentException("adUnitId can not be null");
        }
        this.handler.postDelayed(this.timeOutRunnable, this.DEFAULT_TIME_OUT);
        this.adUnitId = adUnitId;
        getAdCache();
        requestAd();
    }

    private void getAdCache() {
        String adInfoString = SharedPreferenceUtil.getInstance(getContext()).getString(AdmoreSdkConfig.ADINFO_CACHE, null);
        if (adInfoString != null) {
            try {
                this.adInfo = Util.getObjectFromString(adInfoString);
                Log.e(TAG, adInfo.getAdExpiryDate() - System.currentTimeMillis() + "+" + adInfo.getMaterialType());
                if (adInfo == null || adInfo.getAdExpiryDate() < System.currentTimeMillis() || adInfo.getMaterialType() == 0) {
                    gotoTargetClass("adInfo == null || this.adInfo.getAdExpiryDate() > System.currentTimeMillis() || this.adInfo.getMaterialType() == 0");
                } else {
                    useCache(this.adInfo);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                gotoTargetClass("ClassNotFoundException");
            }
        } else {
            gotoTargetClass("adInfo==null");
        }
    }

    private void useCache(final AdInfo adInfoCache) {
        this.useCache = true;
        super.onAdFetchedSuccess(adInfoCache);
        if (AdmoreSplashView.TYPE_IMG == adInfoCache.getMaterialType()) {
            AdmoreSplashView.this.initImageAdView();
        } else if (AdmoreSplashView.TYPE_GIF == adInfoCache.getMaterialType()) {
            AdmoreSplashView.this.initGifAdView();
        } else if (AdmoreSplashView.TYPE_VIDEO == adInfoCache.getMaterialType()) {
            AdmoreSplashView.this.initVideoAdView();
        } else if (AdmoreSplashView.TYPE_H5 == adInfoCache.getMaterialType()) {
        }
        AdmoreSplashView.this.initAdIconView();
//        handler.post(new Runnable() {
//            public void run() {
//
//            }
//        });
    }

    private void requestAd() {
        this.adRequested = true;
        Intent intent = new Intent(getContext(), RequestService.class);
        Bundle bundle = new Bundle();
        bundle.putString(AdmoreSdkConfig.URL, HttpConfig.URL_AD_FETCH);
        bundle.putString(AdmoreSdkConfig.AD_UNITID, this.adUnitId);
        intent.putExtras(bundle);
        getContext().startService(intent);
    }

    public void onAppRegistered() {
        fetchAd(this.adUnitId, LogInfo.AdmoreLogTypeAdvFetch);
    }

    private void initSkipTextView() {
        SkipTextView skipTextView = new SkipTextView(getContext());
        LayoutParams paramsSkip = new LayoutParams(-2, -2);
        if (this.adInfo.getSplashType() == HALF_SCREEN) {
            paramsSkip.addRule(11);
            paramsSkip.setMargins(0, (int) Util.dp2px(getContext(), 20.0f), (int) Util.dp2px(getContext(), 20.0f), 0);
        } else if (this.adInfo.getSplashType() == FULL_SCREEN) {
            paramsSkip.addRule(12);
            paramsSkip.addRule(11);
            paramsSkip.setMargins(0, 0, (int) Util.dp2px(getContext(), 20.0f), (int) Util.dp2px(getContext(), 20.0f));
        }
        addView(skipTextView, paramsSkip);
        skipTextView.setOnSkipEventListener(this);
        skipTextView.setAdInfo(this.adInfo);
    }

    private void initDesView(String description) {
        TextView textView = new TextView(getContext());
        LayoutParams paramsSkip = new LayoutParams(-2, -2);
        if (this.adInfo.getSplashType() == HALF_SCREEN) {
            paramsSkip.addRule(14);
            paramsSkip.setMargins(0, (int) Util.dp2px(getContext(), 20.0f), 0, 0);
        } else if (this.adInfo.getSplashType() == FULL_SCREEN) {
            paramsSkip.addRule(12);
            paramsSkip.addRule(14);
            paramsSkip.setMargins(0, 0, 0, (int) Util.dp2px(getContext(), 20.0f));
        }
        addView(textView, paramsSkip);
        textView.setTextColor(-1);
        textView.setTextSize(2, 12.0f);
        int padding = (int) Util.dp2px(getContext(), 6.0f);
        textView.setPadding(0, padding, 0, padding);
        textView.setText(description);
    }

    private void initAdIconView() {
        this.imageViewAdIcon = new ImageView(getContext());
        this.imageViewAdIcon.setScaleType(ScaleType.FIT_XY);
        LayoutParams params = new LayoutParams((int) Util.dp2px(getContext(), 30.0f), (int) Util.dp2px(getContext(), 15.0f));
        params.setMargins((int) Util.dp2px(getContext(), 20.0f), (int) Util.dp2px(getContext(), 20.0f), 0, 0);
        addView(this.imageViewAdIcon, params);
        if (this.adInfo != null && this.adInfo.getAdIconVisible() == 1) {
            obtainImage(this.adInfo.getAdIcon(), IMAGE_AD_ICON);
        }
    }

    private void initImageAdView() {
        LayoutParams params;
        imageViewAd = new ImageView(getContext());
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (this.adInfo.getSplashType() == 1) {
            this.imageViewAd.setId(InputDeviceCompat.SOURCE_KEYBOARD);
            params.width = getResources().getDisplayMetrics().widthPixels;
            params.height = (getResources().getDisplayMetrics().heightPixels * 4) / 5;
            addView(this.imageViewAd, params);
            initImageLogoView(InputDeviceCompat.SOURCE_KEYBOARD);
        } else if (this.adInfo.getSplashType() == 2) {
            params.width = getResources().getDisplayMetrics().widthPixels;
            params.height = getResources().getDisplayMetrics().heightPixels;
            addView(this.imageViewAd, params);
        }
        if (this.adInfo.getStretchType() == 1) {
            this.imageViewAd.setScaleType(ScaleType.FIT_XY);
        } else if (this.adInfo.getStretchType() == 2) {
            this.imageViewAd.setScaleType(ScaleType.CENTER_CROP);
        }
        obtainImage(this.adInfo.getImageUrl(), IMAGE_AD);
        if (this.adInfo.getSplashType() == 1) {
            obtainImage(this.adInfo.getBaseImageUrl(), IMAGE_LOGO);
        }
    }

    private void initGifAdView() {
        LayoutParams params;
        if (this.adInfo.getSplashType() == 1) {
            this.gifImageView = new GifView(getContext());
            this.gifImageView.setId(0x256);
            params = new LayoutParams(-2, -2);
            params.width = getResources().getDisplayMetrics().widthPixels;
            params.height = (getResources().getDisplayMetrics().heightPixels * 4) / 5;
            addView(this.gifImageView, params);
            initImageLogoView(0x256);
        } else if (this.adInfo.getSplashType() == 2) {
            this.gifImageView = new GifView(getContext());
            params = new LayoutParams(-2, -2);
            params.width = getResources().getDisplayMetrics().widthPixels;
            params.height = getResources().getDisplayMetrics().heightPixels;
            addView(this.gifImageView, params);
        }
        this.gifImageView.requestLayout();
        obtainGif(this.adInfo.getImageUrl());
        if (this.adInfo.getSplashType() == 1) {
            obtainImage(this.adInfo.getBaseImageUrl(), IMAGE_LOGO);
        }
    }

    private void initVideoAdView() {
        this.videoView = new VideoView(getContext());
        LayoutParams params = new LayoutParams(-2, -2);
        params.width = getResources().getDisplayMetrics().widthPixels;
        if (this.adInfo.getSplashType() == 1) {
            params.height = (getResources().getDisplayMetrics().heightPixels * 4) / 5;
            this.videoView.setId(0x128);
            initImageLogoView(0x128);
        } else if (this.adInfo.getSplashType() == 2) {
            params.height = getResources().getDisplayMetrics().heightPixels;
        }
        addView(this.videoView, params);
        obtainVideo(this.adInfo.getImageUrl());
        if (this.adInfo.getSplashType() == 1) {
            obtainImage(this.adInfo.getBaseImageUrl(), IMAGE_LOGO);
        }
    }

    private void initImageLogoView(int id) {
        this.imageViewLogo = new ImageView(getContext());
        this.imageViewLogo.setScaleType(ScaleType.CENTER_CROP);
        LayoutParams paramsLogo = new LayoutParams(-2, -2);
        paramsLogo.width = getResources().getDisplayMetrics().widthPixels;
        paramsLogo.height = getResources().getDisplayMetrics().heightPixels / 5;
        paramsLogo.addRule(3, id);
        addView(this.imageViewLogo, paramsLogo);
    }

    protected void onAdFetchedSuccess(final AdInfo adInfo) {
        super.onAdFetchedSuccess(adInfo);
        SharedPreferenceUtil.getInstance(getContext()).putString(AdmoreSdkConfig.ADINFO_CACHE, Util.getStringFromObject(adInfo)).apply();
        if (!this.useCache) {
            post(new Runnable() {
                public void run() {
                    if (AdmoreSplashView.TYPE_IMG == adInfo.getMaterialType()) {
                        AdmoreSplashView.this.initImageAdView();
                    } else if (AdmoreSplashView.TYPE_GIF == adInfo.getMaterialType()) {
                        AdmoreSplashView.this.initGifAdView();
                    } else if (AdmoreSplashView.TYPE_VIDEO == adInfo.getMaterialType()) {
                        AdmoreSplashView.this.initVideoAdView();
                    } else if (AdmoreSplashView.TYPE_H5 == adInfo.getMaterialType()) {
                    }
                    AdmoreSplashView.this.initAdIconView();
                }
            });
        }
    }

    protected void onAdFetchedFailed(int type) {
        super.onAdFetchedFailed(type);
        gotoTargetClass("onAdFetchedFailed");
    }

    private void obtainImage(final String url, final int type) {
        final String key = Util.getUrlMd5(url);
        CacheHandlerManager.getInstance(getContext()).handleRequest(key, CacheHandlerManager.TYPE_IMG, new OnHandledCallbackAdapter() {
            public void onHandledSuccess(Object object) {
                super.onHandledSuccess(object);
                Bitmap bitmapCache = (Bitmap) object;
                bitmapCache.getRowBytes();
                if (bitmapCache != null && !bitmapCache.isRecycled()) {
                    AdmoreSplashView.this.OnBitmapObtained(bitmapCache, type);
                    if (type == AdmoreSplashView.IMAGE_AD) {
                        AdmoreSplashView.this.adShowed = true;
                    }
                }
            }

            public void onHandledFailed() {
                super.onHandledFailed();
                if (HttpUtil.isNetConnected(AdmoreSplashView.this.getContext())) {
                    HttpUtil.doGetBitmapAsync(url, new HttpCallbackAdapter() {
                        public void onRequestComplete(byte[] bytes) {
                            super.onRequestComplete(bytes);
                            try {
                                Options options = new Options();
                                options.inJustDecodeBounds = true;
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                                if (type == AdmoreSplashView.IMAGE_AD) {
                                    options.inSampleSize = ImageUtil.calculateInSampleSize(options, AdmoreSplashView.this.imageViewAd.getWidth(), AdmoreSplashView.this.imageViewAd.getHeight());
                                } else if (type == AdmoreSplashView.IMAGE_LOGO) {
                                    options.inSampleSize = ImageUtil.calculateInSampleSize(options, AdmoreSplashView.this.imageViewLogo.getWidth(), AdmoreSplashView.this.imageViewLogo.getHeight());
                                } else {
                                    options.inSampleSize = 1;
                                }
                                options.inJustDecodeBounds = false;
                                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                                if (!(bitmap == null || bitmap.isRecycled())) {
                                    AdmoreSplashView.this.OnBitmapObtained(bitmap, type);
                                }
                                if (type == AdmoreSplashView.IMAGE_AD) {
                                    AdmoreSplashView.this.adShowed = true;
                                }
                                ImageLruCache.getInstance().addBitmapToCache(key, bitmap);
                                ImageDiskLruCache.getInstance(AdmoreSplashView.this.getContext()).addBitmapToDiskCache(key, bitmap);
                            } catch (Exception e) {
                                e.printStackTrace();
                                if (type == AdmoreSplashView.IMAGE_AD && AdmoreSplashView.this.adListener != null) {
                                    AdmoreSplashView.this.adListener.onLoadFailed();
                                }
                            }
                        }

                        public void onRequestError(String result) {
                            super.onRequestError(result);
                            if (type == AdmoreSplashView.IMAGE_AD && AdmoreSplashView.this.adListener != null) {
                                AdmoreSplashView.this.adListener.onLoadFailed();
                            }
                        }
                    });
                }
            }
        });
    }

    private void OnBitmapObtained(final Bitmap bitmap, int type) {
        this.handler.removeCallbacks(this.timeOutRunnable);
        if (type == IMAGE_LOGO) {
            if (this.imageViewLogo != null) {
                LogUtil.e("OnBitmapObtained", "imageViewLogo" + bitmap.getRowBytes());
                this.imageViewLogo.post(new Runnable() {
                    public void run() {
                        AdmoreSplashView.this.imageViewLogo.setImageBitmap(bitmap);
                    }
                });
            }
        } else if (type == IMAGE_AD) {
            if (this.imageViewAd != null) {
                this.imageViewAd.post(new Runnable() {
                    public void run() {
                        AdmoreSplashView.this.imageViewAd.setImageBitmap(bitmap);
                        AdmoreSplashView.this.initSkipTextView();
                        if (AdmoreSplashView.this.adListener != null) {
                            AdmoreSplashView.this.adListener.onAdExpose();
                        }
                        LoggerUtil.saveLog(AdmoreSplashView.this.getContext().getApplicationContext(), new LogInfo(AdmoreSplashView.this.getContext().getApplicationContext(), LogInfo.AdmoreLogTypeLaunchShow, AdmoreSplashView.this.adUnitId).getJsonString());
                        AdmoreSplashView.this.imageViewAd.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                AdmoreSplashView.this.gotoAdActivity(String.copyValueOf(AdmoreSplashView.this.adInfo.getClickUrl().toCharArray()));
                                if (AdmoreSplashView.this.adListener != null) {
                                    AdmoreSplashView.this.adListener.onAdClick();
                                }
                                LoggerUtil.saveLog(AdmoreSplashView.this.getContext().getApplicationContext(), new LogInfo(AdmoreSplashView.this.getContext().getApplicationContext(), LogInfo.AdmoreLogTypeAdvClick, AdmoreSplashView.this.adUnitId).getJsonString());
                                Activity activity = (Activity) AdmoreSplashView.this.activity.get();
                                if (activity != null) {
                                    activity.finish();
                                }
                            }
                        });
                    }
                });
            }
        } else if (type == IMAGE_AD_ICON && this.imageViewAdIcon != null) {
            this.imageViewAdIcon.post(new Runnable() {
                public void run() {
                    AdmoreSplashView.this.imageViewAdIcon.setImageBitmap(bitmap);
                }
            });
        }
    }

    private void obtainVideo(final String url) {
        final String key = Util.getUrlMd5(url);
        CacheHandlerManager.getInstance(getContext()).handleRequest(key + ".0", CacheHandlerManager.TYPE_VIDEO, new OnHandledCallbackAdapter() {
            public void onHandledSuccess(Object object) {
                String path = (String) object;
                LogUtil.e(AdmoreSplashView.TAG, path);
                AdmoreSplashView.this.videoView.setVideoPath(path);
                AdmoreSplashView.this.onVideoObtained();
                AdmoreSplashView.this.adShowed = true;
            }

            public void onHandledFailed() {
                if (HttpUtil.isNetConnected(AdmoreSplashView.this.getContext())) {
                    HttpUtil.doGetBitmapAsync(url, new HttpCallbackAdapter() {
                        public void onRequestComplete(byte[] bytes) {
                            super.onRequestComplete(bytes);
                            if (bytes != null) {
                                try {
                                    if (bytes.length > 0) {
                                        ImageDiskLruCache.getInstance(AdmoreSplashView.this.getContext()).addBytesToDiskCache(key, bytes);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        public void onRequestError(String result) {
                            super.onRequestError(result);
                        }
                    });
                }
                AdmoreSplashView.this.gotoTargetClass("CacheHandlerManager:onHandledFailed");
            }
        });
    }

    private void onVideoObtained() {
        this.handler.removeCallbacks(this.timeOutRunnable);
        if (this.videoView != null) {
            this.videoView.setOnPreparedListener(new OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    AdmoreSplashView.this.videoView.seekTo(0);
                    AdmoreSplashView.this.videoView.start();
                    AdmoreSplashView.this.initSkipTextView();
                    if (AdmoreSplashView.this.adListener != null) {
                        AdmoreSplashView.this.adListener.onAdExpose();
                    }
                    LoggerUtil.saveLog(AdmoreSplashView.this.getContext().getApplicationContext(), new LogInfo(AdmoreSplashView.this.getContext().getApplicationContext(), LogInfo.AdmoreLogTypeLaunchShow, AdmoreSplashView.this.adUnitId).getJsonString());
                    if (HttpUtil.isMobileConnected(AdmoreSplashView.this.getContext())) {
                        AdmoreSplashView.this.initDesView(AdmoreSplashView.this.adInfo.getAdExtraDes());
                    }
                    AdmoreSplashView.this.videoView.setOnTouchListener(new OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getAction() == 0) {
                                AdmoreSplashView.this.gotoAdActivity(String.copyValueOf(AdmoreSplashView.this.adInfo.getClickUrl().toCharArray()));
                                if (AdmoreSplashView.this.adListener != null) {
                                    AdmoreSplashView.this.adListener.onAdClick();
                                }
                                LoggerUtil.saveLog(AdmoreSplashView.this.getContext().getApplicationContext(), new LogInfo(AdmoreSplashView.this.getContext().getApplicationContext(), LogInfo.AdmoreLogTypeAdvClick, AdmoreSplashView.this.adUnitId).getJsonString());
                                Activity activity = (Activity) AdmoreSplashView.this.activity.get();
                                if (activity != null) {
                                    activity.finish();
                                }
                            }
                            return false;
                        }
                    });
                }
            });
        }
    }

    private void obtainGif(final String url) {
        final String key = Util.getUrlMd5(url);
        CacheHandlerManager.getInstance(getContext()).handleRequest(key, CacheHandlerManager.TYPE_GIF, new OnHandledCallbackAdapter() {
            public void onHandledSuccess(Object object) {
                InputStream inputStream = (InputStream) object;
                if (inputStream != null) {
                    try {
                        AdmoreSplashView.this.gifImageView.setGifResource(inputStream);
                        AdmoreSplashView.this.requestLayout();
                        AdmoreSplashView.this.onGifObtained();
                        AdmoreSplashView.this.adShowed = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        AdmoreSplashView.this.gotoTargetClass("obtainGif:Exception");
                    }
                }
            }

            public void onHandledFailed() {
                if (HttpUtil.isNetConnected(AdmoreSplashView.this.getContext())) {
                    HttpUtil.doGetBitmapAsync(url, new HttpCallbackAdapter() {
                        public void onRequestComplete(byte[] bytes) {
                            super.onRequestComplete(bytes);
                            if (bytes != null) {
                                try {
                                    if (bytes.length > 0) {
                                        ImageDiskLruCache.getInstance(AdmoreSplashView.this.getContext()).addBytesToDiskCache(key, bytes);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        public void onRequestError(String result) {
                            super.onRequestError(result);
                        }
                    });
                }
                AdmoreSplashView.this.gotoTargetClass("obtainGif:CacheHandlerManager:onHandledFailed");
            }
        });
    }

    private void onGifObtained() {
        this.handler.removeCallbacks(this.timeOutRunnable);
        if (this.gifImageView != null) {
            this.gifImageView.post(new Runnable() {
                public void run() {
                    if (HttpUtil.isMobileConnected(AdmoreSplashView.this.getContext())) {
                        AdmoreSplashView.this.initDesView(AdmoreSplashView.this.adInfo.getAdExtraDes());
                    }
                    AdmoreSplashView.this.initSkipTextView();
                    AdmoreSplashView.this.gifImageView.play();
                    if (AdmoreSplashView.this.adListener != null) {
                        AdmoreSplashView.this.adListener.onAdExpose();
                    }
                    LoggerUtil.saveLog(AdmoreSplashView.this.getContext().getApplicationContext(), new LogInfo(AdmoreSplashView.this.getContext().getApplicationContext(), LogInfo.AdmoreLogTypeLaunchShow, AdmoreSplashView.this.adUnitId).getJsonString());
                    AdmoreSplashView.this.gifImageView.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            AdmoreSplashView.this.gotoAdActivity(String.copyValueOf(AdmoreSplashView.this.adInfo.getClickUrl().toCharArray()));
                            if (AdmoreSplashView.this.adListener != null) {
                                AdmoreSplashView.this.adListener.onAdClick();
                            }
                            LoggerUtil.saveLog(AdmoreSplashView.this.getContext().getApplicationContext(), new LogInfo(AdmoreSplashView.this.getContext().getApplicationContext(), LogInfo.AdmoreLogTypeAdvClick, AdmoreSplashView.this.adUnitId).getJsonString());
                            Activity activity = (Activity) AdmoreSplashView.this.activity.get();
                            if (activity != null) {
                                activity.finish();
                            }
                        }
                    });
                }
            });
        }
    }

    public void setTargetClass(Activity activity, Class targetClass) {
        this.targetClass = targetClass;
        this.activity = new WeakReference(activity);
    }

    private void gotoTargetClass(String message) {
        LogUtil.e(TAG, message);
        removeCallbacks();
        this.handler.removeCallbacks(this.timeOutRunnable);
        if (this.activity != null) {
            Activity activity = (Activity) this.activity.get();
            if (activity != null) {
                Intent intent = new Intent(getContext(), this.targetClass);
                activity.finish();
                activity.startActivity(intent);
            }
        }
    }

    private void gotoAdActivity(String url) {
        Intent intent = new Intent(getContext(), AdActivity.class);
        intent.putExtra(AdmoreSdkConfig.URL, url);
        intent.putExtra(AdmoreSdkConfig.FROM_SPLASH, true);
        getContext().startActivity(intent);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks();
    }

    private void removeCallbacks() {
        if (this.imageViewAd != null) {
            this.imageViewAd.removeCallbacks(null);
        }
        if (this.imageViewLogo != null) {
            this.imageViewLogo.removeCallbacks(null);
        }
        if (this.gifImageView != null) {
            this.gifImageView.removeCallbacks(null);
        }
        if (this.videoView != null) {
            this.videoView.removeCallbacks(null);
        }
        removeCallbacks(null);
    }

    public void onCountDownFinish() {
        gotoTargetClass("onCountDownFinish");
    }

    public void onSkipClick() {
        LogUtil.e(TAG, "onSkipClick");
        if (this.adListener != null) {
            this.adListener.onCloseClick();
        }
        post(new Runnable() {
            public void run() {
                if (HttpUtil.isNetConnected(AdmoreSplashView.this.getContext().getApplicationContext())) {
                    Map<String, String> header = HttpUtil.setHeader(AdmoreSplashView.this.getContext(), LogInfo.AdmoreLogTypeLaunchSkip);
                    String privateKey = SharedPreferenceUtil.getInstance(AdmoreSplashView.this.getContext().getApplicationContext()).getString(AdmoreSdkConfig.PRIVATE_KEY, null);
                    if (privateKey == null) {
                        privateKey = (String) AdmoreSdkConfig.KEY_MAP.get(AdmoreSdkConfig.PRIVATE_KEY);
                    }
                    String nonceStr = SharedPreferenceUtil.getInstance(AdmoreSplashView.this.getContext().getApplicationContext()).getString(AdmoreSdkConfig.NONCESTR, null);
                    if (nonceStr == null) {
                        nonceStr = (String) AdmoreSdkConfig.KEY_MAP.get(AdmoreSdkConfig.NONCESTR);
                    }
                    if (privateKey == null || nonceStr == null) {
                        privateKey = AdmoreSdkConfig.PRIVATE_KEY_DEFAULT;
                        nonceStr = AdmoreSdkConfig.NONCESTR_DEFAULT;
                    }
                    final AESEncryption aesEncryption = new AESEncryption(EncryptHelper.getKey(privateKey, (String) header.get(DeviceInfo.APP_KEY), "", (String) header.get(DeviceInfo.SID), (String) header.get(DeviceInfo.DEVICEID), (String) header.get(DeviceInfo.ANDROID_ID), (String) header.get(DeviceInfo.TIME), (String) header.get(DeviceInfo.PACKAGE_NAME), (String) header.get(DeviceInfo.SYSTEM_VERSION), (String) header.get(DeviceInfo.APP_VERSION), AdmoreSdkConfig.SDK_VERSION, nonceStr));
                    String latitude = "0";
                    String longitude = "0";
                    Location location = Util.getLocation(AdmoreSplashView.this.getContext().getApplicationContext());
                    if (location != null) {
                        latitude = String.valueOf(location.getLatitude());
                        longitude = String.valueOf(location.getLongitude());
                    }
                    Map<String, String> params = new HashMap();
                    params.put("latitude", latitude);
                    params.put("longitude", longitude);
                    params.put(AdmoreSdkConfig.AD_UNITID, AdmoreSplashView.this.adUnitId);
                    Map<String, String> map = params;
                    map.put(AdmoreSdkConfig.REQUESTID, UUID.randomUUID().toString());
                    params.put("adId", AdmoreSdkConfig.APP_KEY);
                    params.put("activityId", String.valueOf(LogInfo.AdmoreLogTypeLaunchSkip));
                    final String finalPrivateKey = privateKey;
                    final String finalNonceStr = nonceStr;
                    final Map<String, String> map2 = header;
                    HttpUtil.doPostAsync(HttpConfig.URL_SKIP, aesEncryption, params, header, true, new HttpCallbackAdapter() {
                        public void onRequestComplete(byte[] result) {
                            super.onRequestComplete(result);
                            try {
                                String jsonResult = new String(aesEncryption.decodeBytes(result));
                                LogUtil.e("jsonResult", jsonResult);
                                if (!"200".equalsIgnoreCase(new JSONObject(jsonResult).optString("code"))) {
                                }
                            } catch (Exception e) {
                                StringBuilder stringBuilder = new StringBuilder(finalPrivateKey + "\n" + finalNonceStr + "\n");
                                for (Entry entry : map2.entrySet()) {
                                    stringBuilder.append(entry.getKey() + ":" + entry.getValue() + "\n");
                                }
                                throw new RuntimeException(stringBuilder.toString());
                            }
                        }

                        public void onRequestError(String result) {
                            super.onRequestError(result);
                        }
                    });
                }
            }
        });
        LoggerUtil.saveLog(getContext().getApplicationContext(), new LogInfo(getContext().getApplicationContext(), LogInfo.AdmoreLogTypeLaunchSkip, this.adUnitId).getJsonString());
        gotoTargetClass("onSkipClick");
    }
}
