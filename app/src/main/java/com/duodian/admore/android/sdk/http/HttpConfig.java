package com.duodian.admore.android.sdk.http;

public class HttpConfig {
    public static final String DOMAIN_OFFICIAL = "https://active.admore.com.cn";
    public static final String DOMAIN_TEST = "http://101.201.122.124:8004";
    public static final String DOMAIN = DOMAIN_TEST;
    public static final String URL_AD_FETCH = DOMAIN + "/active/api/android/fetch/";
    public static final String URL_AD_TERMINAL = DOMAIN + "/active/api/android/terminal/";
    public static final String URL_DEVICE_REGISTER = DOMAIN + "/active/api/android/start/";
    public static final String URL_LOGFILE_UPLOAD = DOMAIN + "/active/api/android/logFile/upload/";
    public static final String URL_SKIP = DOMAIN + "/active/api/android/skip/";
    public static final String URL_UPLOADFILE_AUTH = DOMAIN + "/active/api/android/uploadFile/auth/";
}
