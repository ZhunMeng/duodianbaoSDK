package com.duodian.admore.android.sdk.adview;

import android.content.Context;
import android.location.Location;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.duodian.admore.android.sdk.callbacks.AdListener;
import com.duodian.admore.android.sdk.config.AdmoreSdkConfig;
import com.duodian.admore.android.sdk.encryption.AESEncryption;
import com.duodian.admore.android.sdk.encryption.EncryptHelper;
import com.duodian.admore.android.sdk.http.HttpCallbackAdapter;
import com.duodian.admore.android.sdk.http.HttpConfig;
import com.duodian.admore.android.sdk.http.HttpUtil;
import com.duodian.admore.android.sdk.log.LogInfo;
import com.duodian.admore.android.sdk.log.LoggerUtil;
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

public abstract class BaseAdView extends RelativeLayout {
    protected AdInfo adInfo;
    protected AdListener adListener;
    protected boolean adRequested;
    protected boolean adShowed;
    protected String adUnitId;
    protected int requestCount = 12;
    private String uuid;

    public BaseAdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initId();
        ViewControllerUtil.getInstance().addView(this);
    }

    private void initId() {
        this.uuid = UUID.randomUUID().toString();
    }

    public String getUuid() {
        return this.uuid;
    }

    public void loadAd(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            throw new IllegalArgumentException("adUnitId can not be null");
        }
        this.adUnitId = adUnitId;
        this.adRequested = true;
        if (ViewControllerUtil.getInstance().isRegistered()) {
            fetchAd(adUnitId, LogInfo.AdmoreLogTypeAdvFetch);
        }
    }

    public void onAppRegistered() {
        if (!this.adShowed && this.adRequested) {
            fetchAd(this.adUnitId, LogInfo.AdmoreLogTypeAdvFetch);
        }
    }

    public void setAdListener(AdListener adListener) {
        this.adListener = adListener;
    }

    protected void onAdFetchedSuccess(AdInfo adInfo) {
        this.adInfo = adInfo;
        this.requestCount = 12;
        if (this.adListener != null) {
            this.adListener.onReceiveAd();
        }
//        postDelayed(new Runnable() {
//            public void run() {
//                BaseAdView.this.fetchAd(BaseAdView.this.adUnitId, LogInfo.AdmoreLogTypeAdvFetchUpdate);
//            }
//        }, adInfo.getExpire() * 1000);
    }

    protected void onAdFetchedFailed(final int type) {
        if (this.adListener != null) {
            this.adListener.onReceiveAdFailed();
        }
        postDelayed(new Runnable() {
            public void run() {
                if (BaseAdView.this.requestCount > 0) {
                    BaseAdView.this.fetchAd(BaseAdView.this.adUnitId, type);
                    BaseAdView baseAdView = BaseAdView.this;
                    baseAdView.requestCount--;
                }
            }
        }, 5000);
    }

    protected void fetchAd(final String adUnitId, final int type) {
        post(new Runnable() {
            public void run() {
                if (HttpUtil.isNetConnected(BaseAdView.this.getContext().getApplicationContext())) {
                    Map<String, String> header = HttpUtil.setHeader(BaseAdView.this.getContext().getApplicationContext(), type);
                    String privateKey = SharedPreferenceUtil.getInstance(BaseAdView.this.getContext().getApplicationContext()).getString(AdmoreSdkConfig.PRIVATE_KEY, null);
                    if (privateKey == null) {
                        privateKey = (String) AdmoreSdkConfig.KEY_MAP.get(AdmoreSdkConfig.PRIVATE_KEY);
                    }
                    String nonceStr = SharedPreferenceUtil.getInstance(BaseAdView.this.getContext().getApplicationContext()).getString(AdmoreSdkConfig.NONCESTR, null);
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
                    Location location = Util.getLocation(BaseAdView.this.getContext().getApplicationContext());
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
                                            BaseAdView.this.onAdFetchedSuccess((AdInfo) new AdInfo(jsonObjectResult).getBean());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            BaseAdView.this.onAdFetchedFailed(type);
                                        }
                                    } else {
                                        BaseAdView.this.onAdFetchedFailed(type);
                                    }
                                    LoggerUtil.saveLog(BaseAdView.this.getContext().getApplicationContext(), new LogInfo(BaseAdView.this.getContext().getApplicationContext(), type, adUnitId).getJsonString());
                                    return;
                                }
                                BaseAdView.this.onAdFetchedFailed(type);
                            } catch (Exception e2) {
                                StringBuilder stringBuilder = new StringBuilder(finalPrivateKey + "\n" + finalNonceStr + "\n");
                                for (Entry entry : map2.entrySet()) {
                                    stringBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
                                }
                                throw new RuntimeException(stringBuilder.toString());
                            }
                        }

                        public void onRequestError(String result) {
                            super.onRequestError(result);
                            BaseAdView.this.onAdFetchedFailed(type);
                        }
                    });
                }
            }
        });
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(null);
        ViewControllerUtil.getInstance().removeView(this);
    }
}
