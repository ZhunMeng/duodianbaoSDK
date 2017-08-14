package com.duodian.admore.android.sdk.log;

import android.content.Context;
import android.provider.BaseColumns;
import com.duodian.admore.android.sdk.config.AdmoreSdkConfig;
import com.duodian.admore.android.sdk.utils.SharedPreferenceUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class LogInfo {
    public static int AdmoreLogTypeAdvClick = 3;
    public static int AdmoreLogTypeAdvFetch = 1;
    public static int AdmoreLogTypeAdvFetchUpdate = 2;
    public static int AdmoreLogTypeAdvTerminal = 6;
    public static int AdmoreLogTypeAdvUploadLog = 7;
    public static int AdmoreLogTypeGetAuth = 8;
    public static int AdmoreLogTypeLaunchShow = 4;
    public static int AdmoreLogTypeLaunchSkip = 5;
    public static int AdmoreLogTypeStart = 0;
    private String a;
    private int i;
    private String r;
    private long t;
    private String u;

    public static class LogEntry implements BaseColumns {
        public static final String COLUMN1 = "column1";
        public static final String COLUMN2 = "column2";
        public static final String CONTENT = "content";
        public static final String TABLE_NAME = "AdmoreAdSdkLogTable";
        public static final String TIME = "time";
    }

    public LogInfo(Context context) {
        this.r = SharedPreferenceUtil.getInstance(context).getString(AdmoreSdkConfig.DEVICE_ID, null);
        this.a = SharedPreferenceUtil.getInstance(context).getString(AdmoreSdkConfig.APP_KEY, null);
        this.u = SharedPreferenceUtil.getInstance(context).getString(AdmoreSdkConfig.AD_UNITID, null);
    }

    public LogInfo(Context context, int type, String adUnitId) {
        this.r = SharedPreferenceUtil.getInstance(context).getString(AdmoreSdkConfig.DEVICE_ID, null);
        this.a = SharedPreferenceUtil.getInstance(context).getString(AdmoreSdkConfig.APP_KEY, null);
        this.u = adUnitId;
        this.t = System.currentTimeMillis();
        this.i = type;
    }

    public int getI() {
        return this.i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public long getT() {
        return this.t;
    }

    public void setT(long t) {
        this.t = t;
    }

    public String getU() {
        return this.u;
    }

    public void setU(String u) {
        this.u = u;
    }

    public String getR() {
        return this.r;
    }

    public void setR(String r) {
        this.r = r;
    }

    public String getA() {
        return this.a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getJsonString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("i", this.i);
            jsonObject.put("t", this.t);
            jsonObject.put("u", this.u);
            jsonObject.put("r", this.r);
            jsonObject.put("a", this.a);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
