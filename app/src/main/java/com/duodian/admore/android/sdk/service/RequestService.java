package com.duodian.admore.android.sdk.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.duodian.admore.android.sdk.config.AdmoreSdkConfig;
import com.duodian.admore.android.sdk.encryption.AESEncryption;
import com.duodian.admore.android.sdk.encryption.EncryptHelper;
import com.duodian.admore.android.sdk.http.HttpCallbackAdapter;
import com.duodian.admore.android.sdk.http.HttpConfig;
import com.duodian.admore.android.sdk.http.HttpUtil;
import com.duodian.admore.android.sdk.imagecache.CacheHandlerManager;
import com.duodian.admore.android.sdk.imagecache.CacheHandlerManager.OnHandledCallbackAdapter;
import com.duodian.admore.android.sdk.imagecache.ImageDiskLruCache;
import com.duodian.admore.android.sdk.imagecache.ImageUtil;
import com.duodian.admore.android.sdk.log.LogInfo;
import com.duodian.admore.android.sdk.log.LoggerUtil;
import com.duodian.admore.android.sdk.log.OSSAuthInfo;
import com.duodian.admore.android.sdk.log.OSSManager;
import com.duodian.admore.android.sdk.model.AdInfo;
import com.duodian.admore.android.sdk.model.DeviceInfo;
import com.duodian.admore.android.sdk.utils.LogUtil;
import com.duodian.admore.android.sdk.utils.SharedPreferenceUtil;
import com.duodian.admore.android.sdk.utils.Util;
import com.duodian.admore.android.sdk.utils.ViewControllerUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.json.JSONObject;

public class RequestService extends Service {
    private static boolean netConnected = false;
    private static int netWorkType = -1;
    private static final int registerInterval = 5000;
    public static long upLoadLogInterval = AdmoreSdkConfig.LOG_STRATEGY_DURATION;
    private String TAG = "RequestService";
    private String adUnitid;
    private Handler initHandler;
    private HandlerThread initHandlerThread;
    private Runnable initRunnable;
    private BroadcastReceiver netWorkStateChangedReceiver;
    private Runnable registerRunnable;
    private boolean requestAd;
    private Runnable uploadLogRunnable;

    public class RequestBinder extends Binder {
        public RequestService getService() {
            return RequestService.this;
        }
    }

    public void onCreate() {
        super.onCreate();
        LogUtil.e(this.TAG, "onCreate");
        long time = System.currentTimeMillis();
        this.netWorkStateChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (!"android.net.conn.CONNECTIVITY_CHANGE".equalsIgnoreCase(intent.getAction())) {
                    return;
                }
                if (HttpUtil.isNetConnected(RequestService.this.getApplicationContext())) {
                    RequestService.netConnected = true;
                    if (HttpUtil.isWifiConnected(RequestService.this.getApplicationContext())) {
                        RequestService.netWorkType = 1;
                    } else if (HttpUtil.isMobileConnected(RequestService.this.getApplicationContext())) {
                        RequestService.netWorkType = 0;
                    }
                    if (!ViewControllerUtil.getInstance().isRegistered() && !ViewControllerUtil.getInstance().isRegistering()) {
                        LogUtil.e(RequestService.this.TAG, "netWorkStateChangedReceiver");
                        RequestService.this.registerDevice();
                        return;
                    }
                    return;
                }
                RequestService.netConnected = false;
            }
        };

        this.initHandlerThread = new HandlerThread("initHandlerThread");
        this.initHandlerThread.start();
        this.initHandler = new Handler(this.initHandlerThread.getLooper());
        this.initRunnable = new Runnable() {
            public void run() {

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                registerReceiver(netWorkStateChangedReceiver, intentFilter);

                ImageDiskLruCache.getInstance(RequestService.this.getApplicationContext());
                ViewControllerUtil.getInstance();
                if (RequestService.this.registerRunnable == null) {
                    RequestService.this.registerRunnable = new Runnable() {
                        public void run() {
                            LogUtil.e(RequestService.this.TAG, "registerRunnable");
                            if (ViewControllerUtil.getInstance().isRegistered()) {
                                RequestService.this.initHandler.removeCallbacks(this);
                                return;
                            }
                            if (!ViewControllerUtil.getInstance().isRegistering()) {
                                RequestService.this.registerDevice();
                            }
                            RequestService.this.initHandler.postDelayed(this, 5000);
                        }
                    };
                }
                RequestService.this.initHandler.postDelayed(RequestService.this.registerRunnable, 3000);

                if (RequestService.this.uploadLogRunnable == null) {
                    RequestService.this.uploadLogRunnable = new Runnable() {
                        public void run() {
                            LogUtil.e(RequestService.this.TAG, "uploadLogRunnable");
                            LoggerUtil.readLog(RequestService.this.getApplicationContext());
                            RequestService.this.initHandler.postDelayed(this, RequestService.upLoadLogInterval);
                        }
                    };
                }
                RequestService.this.initHandler.postDelayed(RequestService.this.uploadLogRunnable, 5 * 1000);
            }
        };
        this.initHandler.post(this.initRunnable);
        LogUtil.e(this.TAG, (System.currentTimeMillis() - time) + ":time");
    }

    public void onDestroy() {
        LogUtil.e("RequestService", "onDestroy");
        unregisterReceiver(this.netWorkStateChangedReceiver);
        if (this.initHandler != null) {
            this.initHandler.removeCallbacks(this.uploadLogRunnable);
            this.initHandler.removeCallbacks(this.registerRunnable);
            this.initHandler.removeCallbacks(null);
        }
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.e(this.TAG, "onStartCommand");
        if (!(intent == null || intent.getExtras() == null)) {
            Bundle bundle = intent.getExtras();
            String url = bundle.getString(AdmoreSdkConfig.URL);
            if (HttpConfig.URL_DEVICE_REGISTER.equalsIgnoreCase(url)) {
                registerDevice();
            } else if (HttpConfig.URL_AD_FETCH.equalsIgnoreCase(url)) {
                this.adUnitid = bundle.getString(AdmoreSdkConfig.AD_UNITID);
                fetchAd(this.adUnitid, LogInfo.AdmoreLogTypeAdvFetch);
            } else if (HttpConfig.URL_AD_TERMINAL.equalsIgnoreCase(url)) {
                onAppTerminal();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void registerDevice() {
        if (HttpUtil.isNetConnected(getApplicationContext())) {
            ViewControllerUtil.getInstance().setRegistering(true);
            Map<String, String> header = HttpUtil.setHeader(getApplicationContext(), LogInfo.AdmoreLogTypeStart);
            final AESEncryption aesEncryption = new AESEncryption(EncryptHelper.getKey("", (String) header.get(DeviceInfo.APP_KEY), "", "", (String) header.get(DeviceInfo.DEVICEID), (String) header.get(DeviceInfo.ANDROID_ID), (String) header.get(DeviceInfo.TIME), (String) header.get(DeviceInfo.PACKAGE_NAME), (String) header.get(DeviceInfo.SYSTEM_VERSION), (String) header.get(DeviceInfo.APP_VERSION), AdmoreSdkConfig.SDK_VERSION, EncryptHelper.NONCE_STR));
            String latitude = "0";
            String longitude = "0";
            Location location = Util.getLocation(getApplicationContext());
            if (location != null) {
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
            }
            Map<String, String> params = new HashMap();
            params.put("latitude", latitude);
            params.put("longitude", longitude);
            String finalPrivateKey = "";
            String finalNonceStr = EncryptHelper.NONCE_STR;
            final Map<String, String> map = header;
            HttpUtil.doPostAsync(HttpConfig.URL_DEVICE_REGISTER, aesEncryption, params, header, true, new HttpCallbackAdapter() {
                public void onRequestComplete(byte[] result) {
                    super.onRequestComplete(result);
                    try {
                        String jsonResult = new String(aesEncryption.decodeBytes(result));
                        LogUtil.e("jsonResult", jsonResult);
                        JSONObject jsonObject = new JSONObject(jsonResult);
                        if ("200".equalsIgnoreCase(jsonObject.optString("code"))) {
                            JSONObject jsonObjectResult = jsonObject.optJSONObject("result");
                            if (jsonObjectResult != null) {
                                String privateKey = jsonObjectResult.optString(AdmoreSdkConfig.PRIVATE_KEY);
                                String deviceId = jsonObjectResult.optString(AdmoreSdkConfig.DEVICE_ID);
                                String sid = jsonObjectResult.optString(AdmoreSdkConfig.SID);
                                String logStrategy = jsonObjectResult.optString(AdmoreSdkConfig.LOG_STRATEGY);
                                String nonceStr = jsonObjectResult.optString(AdmoreSdkConfig.NONCESTR);
                                LoggerUtil.upLoadLogInterval = (long) (jsonObjectResult.optInt(AdmoreSdkConfig.LOG_STRATEGY, 300) * 1000);
                                AdmoreSdkConfig.KEY_MAP.put(AdmoreSdkConfig.PRIVATE_KEY, privateKey);
                                AdmoreSdkConfig.KEY_MAP.put(AdmoreSdkConfig.NONCESTR, nonceStr);
                                AdmoreSdkConfig.KEY_MAP.put(AdmoreSdkConfig.SID, sid);
                                SharedPreferenceUtil.getInstance(RequestService.this.getApplicationContext()).putString(AdmoreSdkConfig.PRIVATE_KEY, privateKey).putString(AdmoreSdkConfig.NONCESTR, nonceStr).putString(AdmoreSdkConfig.DEVICE_ID, deviceId).putString(AdmoreSdkConfig.SID, sid).putString(AdmoreSdkConfig.LOG_STRATEGY, logStrategy).apply();
                                OSSAuthInfo oSSAuthInfo = new OSSAuthInfo(jsonObjectResult);
                                String logType = jsonObjectResult.optString("logType");
                                int logWifi = jsonObjectResult.optInt("logWifi");
                                OSSManager.OSS_TYPE = logType;
                                OSSManager.LOG_WIFI = logWifi;
                                OSSManager.getInstance().setOssAuthInfo(oSSAuthInfo).initOSS(RequestService.this.getApplicationContext());
                                ViewControllerUtil.getInstance().setRegistered(true);
                                LogInfo logInfo = new LogInfo(RequestService.this.getApplicationContext());
                                logInfo.setT(System.currentTimeMillis());
                                logInfo.setI(LogInfo.AdmoreLogTypeStart);
                                LoggerUtil.saveLog(RequestService.this.getApplicationContext(), logInfo.getJsonString());
                                if (RequestService.this.requestAd) {
                                    RequestService.this.fetchAd(RequestService.this.adUnitid, LogInfo.AdmoreLogTypeAdvFetch);
                                }
                            }
                        }
                        ViewControllerUtil.getInstance().setRegistering(false);
                    } catch (Exception e) {
                        StringBuilder stringBuilder = new StringBuilder("\nabcdefghijklmnopqrstuvwxyz\n");
                        for (Entry entry : map.entrySet()) {
                            stringBuilder.append(entry.getKey() + ":" + entry.getValue() + "\n");
                        }
                        throw new RuntimeException(stringBuilder.toString());
                    }
                }

                public void onRequestError(String result) {
                    super.onRequestError(result);
                    ViewControllerUtil.getInstance().setRegistering(false);
                }
            });
        }
    }

    private void onAppTerminal() {
        if (HttpUtil.isNetConnected(getApplicationContext())) {
            Map<String, String> header = HttpUtil.setHeader(getApplicationContext(), LogInfo.AdmoreLogTypeAdvTerminal);
            String privateKey = SharedPreferenceUtil.getInstance(getApplicationContext()).getString(AdmoreSdkConfig.PRIVATE_KEY, null);
            if (privateKey == null) {
                privateKey = (String) AdmoreSdkConfig.KEY_MAP.get(AdmoreSdkConfig.PRIVATE_KEY);
            }
            String nonceStr = SharedPreferenceUtil.getInstance(getApplicationContext()).getString(AdmoreSdkConfig.NONCESTR, null);
            if (nonceStr == null) {
                nonceStr = (String) AdmoreSdkConfig.KEY_MAP.get(AdmoreSdkConfig.NONCESTR);
            }
            if (privateKey == null || nonceStr == null) {
                privateKey = AdmoreSdkConfig.PRIVATE_KEY_DEFAULT;
                nonceStr = AdmoreSdkConfig.NONCESTR_DEFAULT;
            }
            String key = EncryptHelper.getKey(privateKey, (String) header.get(DeviceInfo.APP_KEY), "", (String) header.get(DeviceInfo.SID), (String) header.get(DeviceInfo.DEVICEID), (String) header.get(DeviceInfo.ANDROID_ID), (String) header.get(DeviceInfo.TIME), (String) header.get(DeviceInfo.PACKAGE_NAME), (String) header.get(DeviceInfo.SYSTEM_VERSION), (String) header.get(DeviceInfo.APP_VERSION), AdmoreSdkConfig.SDK_VERSION, nonceStr);
            LogUtil.e("key", key);
            AESEncryption aesEncryption = new AESEncryption(key);
            String latitude = "0";
            String longitude = "0";
            Location location = Util.getLocation(getApplicationContext());
            if (location != null) {
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
            }
            Map<String, String> params = new HashMap();
            params.put("latitude", latitude);
            params.put("longitude", longitude);
            HttpUtil.doPostAsync(HttpConfig.URL_AD_TERMINAL, aesEncryption, params, header, true, new HttpCallbackAdapter() {
                public void onRequestComplete(byte[] result) {
                    super.onRequestComplete(result);
                    LogInfo logInfo = new LogInfo(RequestService.this.getApplicationContext());
                    logInfo.setT(System.currentTimeMillis());
                    logInfo.setI(LogInfo.AdmoreLogTypeAdvTerminal);
                    LoggerUtil.saveLog(RequestService.this.getApplicationContext(), logInfo.getJsonString());
                }
            });
        }
    }

    private void fetchAd(String adUnitId, int type) {
        this.requestAd = true;
//        if (ViewControllerUtil.getInstance().isRegistered() && HttpUtil.isNetConnected(getApplicationContext())) {
        if (HttpUtil.isNetConnected(getApplicationContext())) {
            Map<String, String> header = HttpUtil.setHeader(getApplicationContext(), type);
            String privateKey = SharedPreferenceUtil.getInstance(getApplicationContext()).getString(AdmoreSdkConfig.PRIVATE_KEY, null);
            LogUtil.e(this.TAG, "privateKey:" + privateKey);
            if (privateKey == null) {
                privateKey = (String) AdmoreSdkConfig.KEY_MAP.get(AdmoreSdkConfig.PRIVATE_KEY);
            }
            String nonceStr = SharedPreferenceUtil.getInstance(getApplicationContext()).getString(AdmoreSdkConfig.NONCESTR, null);
            if (nonceStr == null) {
                nonceStr = (String) AdmoreSdkConfig.KEY_MAP.get(AdmoreSdkConfig.NONCESTR);
            }
            LogUtil.e(this.TAG, "nonceStr:" + nonceStr);
            if (privateKey == null || nonceStr == null) {
                privateKey = AdmoreSdkConfig.PRIVATE_KEY_DEFAULT;
                nonceStr = AdmoreSdkConfig.NONCESTR_DEFAULT;
            }
            final AESEncryption aesEncryption = new AESEncryption(EncryptHelper.getKey(privateKey, (String) header.get(DeviceInfo.APP_KEY), "", (String) header.get(DeviceInfo.SID), (String) header.get(DeviceInfo.DEVICEID), (String) header.get(DeviceInfo.ANDROID_ID), (String) header.get(DeviceInfo.TIME), (String) header.get(DeviceInfo.PACKAGE_NAME), (String) header.get(DeviceInfo.SYSTEM_VERSION), (String) header.get(DeviceInfo.APP_VERSION), AdmoreSdkConfig.SDK_VERSION, nonceStr));
            String latitude = "0";
            String longitude = "0";
            Location location = Util.getLocation(getApplicationContext());
            if (location != null) {
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
            }
            Map<String, String> params = new HashMap();
            params.put("latitude", latitude);
            params.put("longitude", longitude);
            params.put(AdmoreSdkConfig.AD_UNITID, adUnitId);
            Map<String, String> map = params;
            map.put(AdmoreSdkConfig.REQUESTID, UUID.randomUUID().toString());
            final String finalPrivateKey = privateKey;
            final String finalNonceStr = nonceStr;
            final int i = type;
            final String str = adUnitId;
            final Map<String, String> map2 = header;
            HttpUtil.doPostAsync(HttpConfig.URL_AD_FETCH, aesEncryption, params, header, true, new HttpCallbackAdapter() {
                public void onRequestComplete(byte[] result) {
                    super.onRequestComplete(result);
                    try {
                        String jsonResult = new String(aesEncryption.decodeBytes(result));
                        LogUtil.e("jsonResult", jsonResult);
                        JSONObject jsonObject = new JSONObject(jsonResult);
                        if ("200".equalsIgnoreCase(jsonObject.optString("code"))) {
                            JSONObject jsonObjectResult = jsonObject.optJSONObject("result");
                            if (jsonObjectResult != null) {
                                try {
                                    AdInfo adInfo = (AdInfo) new AdInfo(jsonObjectResult).getBean();
                                    SharedPreferenceUtil.getInstance(RequestService.this.getApplicationContext()).putString(AdmoreSdkConfig.ADINFO_CACHE, Util.getStringFromObject(adInfo)).apply();
                                    if (!TextUtils.isEmpty(adInfo.getImageUrl())) {
                                        if (adInfo.getSplashType() == 1) {
                                            RequestService.this.downLoadImage(adInfo.getImageUrl(), adInfo.getMaterialType(), RequestService.this.getResources().getDisplayMetrics().widthPixels, (RequestService.this.getResources().getDisplayMetrics().heightPixels * 4) / 5);
                                        } else {
                                            RequestService.this.downLoadImage(adInfo.getImageUrl(), adInfo.getMaterialType(), RequestService.this.getResources().getDisplayMetrics().widthPixels, RequestService.this.getResources().getDisplayMetrics().heightPixels);
                                        }
                                    }
                                    if (!TextUtils.isEmpty(adInfo.getBaseImageUrl())) {
                                        RequestService.this.downLoadImage(adInfo.getBaseImageUrl(), CacheHandlerManager.TYPE_IMG, RequestService.this.getResources().getDisplayMetrics().widthPixels, RequestService.this.getResources().getDisplayMetrics().heightPixels / 5);
                                    }
                                    if (!TextUtils.isEmpty(adInfo.getAdIcon())) {
                                        RequestService.this.downLoadImage(adInfo.getAdIcon(), CacheHandlerManager.TYPE_IMG, RequestService.this.getResources().getDisplayMetrics().widthPixels, RequestService.this.getResources().getDisplayMetrics().heightPixels);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            LoggerUtil.saveLog(RequestService.this.getApplicationContext(), new LogInfo(RequestService.this.getApplicationContext(), i, str).getJsonString());
                        }
                    } catch (Exception e2) {
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

    private void downLoadImage(String url, final int type, final int width, final int height) {
        String key = Util.getUrlMd5(url);
        if (type == CacheHandlerManager.TYPE_VIDEO) {
            key = key + ".0";
        }
        final String str = url;
        final String finalKey = key;
        CacheHandlerManager.getInstance(getApplicationContext()).handleRequest(key, type, new OnHandledCallbackAdapter() {
            public void onHandledSuccess(Object object) {
            }

            public void onHandledFailed() {
                if (HttpUtil.isNetConnected(RequestService.this.getApplicationContext())) {
                    HttpUtil.doGetBitmapAsync(str, new HttpCallbackAdapter() {
                        public void onRequestComplete(byte[] bytes) {
                            super.onRequestComplete(bytes);
                            if (bytes != null && bytes.length > 0) {
                                LogUtil.e(TAG, bytes.length + "");
                                try {
                                    if (CacheHandlerManager.TYPE_IMG == type) {
                                        Options options = new Options();
                                        options.inJustDecodeBounds = true;
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                                        options.inSampleSize = ImageUtil.calculateInSampleSize(options, width, height);
                                        options.inJustDecodeBounds = false;
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                                        if (bitmap != null && !bitmap.isRecycled()) {
                                            ImageDiskLruCache.getInstance(RequestService.this.getApplicationContext()).addBytesToDiskCache(finalKey, bytes);
                                        }
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
            }
        });
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return new RequestBinder();
    }
}
