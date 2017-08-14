package com.duodian.admore.android.sdk.log;

import android.content.Context;
import android.location.Location;
import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.duodian.admore.android.sdk.config.AdmoreSdkConfig;
import com.duodian.admore.android.sdk.encryption.AESEncryption;
import com.duodian.admore.android.sdk.encryption.EncryptHelper;
import com.duodian.admore.android.sdk.http.HttpCallbackAdapter;
import com.duodian.admore.android.sdk.http.HttpConfig;
import com.duodian.admore.android.sdk.http.HttpUtil;
import com.duodian.admore.android.sdk.model.DeviceInfo;
import com.duodian.admore.android.sdk.utils.LogUtil;
import com.duodian.admore.android.sdk.utils.SharedPreferenceUtil;
import com.duodian.admore.android.sdk.utils.Util;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class OSSManager {
    public static int LOG_WIFI = 1;
    public static String OSS_TYPE;
    private OSSCredentialProvider credentialProvider;
    private OSS oss;
    private OSSAuthInfo ossAuthInfo;

    private static class OSSManagerHolder {
        private static OSSManager ossManager = new OSSManager();

        private OSSManagerHolder() {
        }
    }

    public OSSManager setOssAuthInfo(OSSAuthInfo ossAuthInfo) {
        this.ossAuthInfo = ossAuthInfo;
        return OSSManagerHolder.ossManager;
    }

    private OSSManager() {
    }

    public static OSSManager getInstance() {
        return OSSManagerHolder.ossManager;
    }

    public OSSAuthInfo getOssAuthInfo() {
        return this.ossAuthInfo;
    }

    public void getAuth(Context context) {
        if (HttpUtil.isNetConnected(context.getApplicationContext())) {
            Map<String, String> header = HttpUtil.setHeader(context.getApplicationContext(), LogInfo.AdmoreLogTypeGetAuth);
            String privateKey = SharedPreferenceUtil.getInstance(context.getApplicationContext()).getString(AdmoreSdkConfig.PRIVATE_KEY, null);
            if (privateKey == null) {
                privateKey = (String) AdmoreSdkConfig.KEY_MAP.get(AdmoreSdkConfig.PRIVATE_KEY);
            }
            String nonceStr = SharedPreferenceUtil.getInstance(context.getApplicationContext()).getString(AdmoreSdkConfig.NONCESTR, null);
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
            Location location = Util.getLocation(context.getApplicationContext());
            if (location != null) {
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
            }
            Map<String, String> params = new HashMap();
            params.put("latitude", latitude);
            params.put("longitude", longitude);
            final Context context2 = context;
            HttpUtil.doPostAsync(HttpConfig.URL_UPLOADFILE_AUTH, aesEncryption, params, header, true, new HttpCallbackAdapter() {
                public void onRequestComplete(byte[] result) {
                    super.onRequestComplete(result);
                    try {
                        String jsonResult = new String(aesEncryption.decodeBytes(result));
                        LogUtil.e("OSSjsonResult", jsonResult);
                        JSONObject jsonObject = new JSONObject(jsonResult);
                        if ("200".equalsIgnoreCase(jsonObject.optString("code"))) {
                            JSONObject jsonObjectResult = jsonObject.optJSONObject("result");
                            if (jsonObjectResult != null) {
                                OSSManager.this.ossAuthInfo = new OSSAuthInfo(jsonObjectResult);
                                String accessKeyId = jsonObjectResult.optString("accessKeyId");
                                String accessKeySecret = jsonObjectResult.optString("accessKeySecret");
                                String securityToken = jsonObjectResult.optString("securityToken");
                                long expiration = jsonObjectResult.optLong("expiration");
                                String fileLink = jsonObjectResult.optString("fileLink");
                                OSSManager.this.ossAuthInfo.setAccessKeyId(accessKeyId);
                                OSSManager.this.ossAuthInfo.setAccessKeySecret(accessKeySecret);
                                OSSManager.this.ossAuthInfo.setSecurityToken(securityToken);
                                OSSManager.this.ossAuthInfo.setExpiration(expiration);
                                OSSManager.this.ossAuthInfo.setFileLink(fileLink);
                                OSSManager.this.initOSS(context2);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                public void onRequestError(String result) {
                    super.onRequestError(result);
                }
            });
        }
    }

    public void initOSS(Context context) {
        this.credentialProvider = new OSSStsTokenCredentialProvider(this.ossAuthInfo.getAccessKeyId(), this.ossAuthInfo.getAccessKeySecret(), this.ossAuthInfo.getSecurityToken());
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15000);
        conf.setSocketTimeout(15000);
        conf.setMaxConcurrentRequest(5);
        conf.setMaxErrorRetry(2);
        this.oss = new OSSClient(context.getApplicationContext(), this.ossAuthInfo.getEndpoint(), this.credentialProvider, conf);
    }

    void uploadFile(String filePath, String fileName, OSSCompletedCallback oSSCompletedCallback) {
        LogUtil.e("OSSManager", "uploadFile: " + filePath);
        PutObjectRequest put = new PutObjectRequest(this.ossAuthInfo.getBucketName(), this.ossAuthInfo.getFileLink() + fileName, filePath);
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                LogUtil.e("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
            }
        });
        this.oss.asyncPutObject(put, oSSCompletedCallback).waitUntilFinished();
    }
}
