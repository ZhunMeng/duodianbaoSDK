package com.duodian.admore.android.sdk.model;

public class DeviceInfo {
    public static final String ADHOST = "adHost";
    public static final String ADIDS = "adids";
    public static final String ANDROID_ID = "x-android-id";
    public static final String APP_KEY = "x-app-key";
    public static final String APP_PACKAGE = "x-app-package";
    public static final String APP_VERSION = "x-app-version";
    public static final String APP_VERSION_CODE = "x-app-version-code";
    public static final String BRAND = "x-device-brand";
    public static final String DEVICEID = "x-device-id";
    public static final String EMAIL = "email";
    public static final String IDENTIFIER = "identifier";
    public static final String IMEI = "x-device-imei";
    public static final String IP = "x-ip";
    public static final String IP_GPRS = "x-ip-gprs";
    public static final String IP_HOST = "x-ip-host";
    public static final String IP_WIFI = "x-ip-wifi";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String MAC = "x-mac";
    public static final String MODEL = "x-device";
    public static final String NET = "x-net";
    public static final String PACKAGE_NAME = "x-package-name";
    public static final String PASSWORD = "password";
    public static final String PHONE_NUMBER = "x-phone-number";
    public static final String PLATFORM = "x-device-platform";
    public static final String RESOLUTION = "resolution";
    public static final String SDK_VERSION = "x-sdk-version";
    public static final String SECRET = "x-secret";
    public static final String SERIAL = "x-device-serial";
    public static final String SID = "x-sid";
    public static final String SIM_SERIAL = "x-sim-serial";
    public static final String SYSTEM_VERSION = "x-system-version";
    public static final String TIME = "x-time";
    public static final String User_Agent = "User-Agent";
    public static final String VERSION = "x-os-version";
    private String androidId;
    private String appKey;
    private String appVersion;
    private String deviceID;
    private String packageName;
    private String sdkVersion;
    private long time;

    public String getAndroidId() {
        return this.androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getAppKey() {
        return this.appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getSdkVersion() {
        return this.sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getAppVersion() {
        return this.appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getDeviceID() {
        return this.deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
}
