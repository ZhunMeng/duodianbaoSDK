package com.duodian.admore.android.sdk.model;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.json.JSONObject;

public class AdInfo implements Serializable {
    private String adClose;
    private int adCloseVisible;
    private String adContent;
    private long adExpiryDate;
    private String adExtraDes;
    private int adExtraDesVisble;
    private String adIcon;
    private int adIconVisible;
    private String adId;
    private String adTitle;


    public int getAdType() {
        return adType;
    }

    public void setAdType(int adType) {
        this.adType = adType;
    }

    private int adType;
    private String adUnitId;
    private String baseImageUrl;
    private String clickUrl;
    private int countDown;
    private int countDownVisible;
    private int downNetType;
    private long expire;
    private String imageUrl;
    private int imgHeight;
    private int imgWidth;
    private JSONObject jsonObject;
    private int materialSize;
    private int materialType;
    private String requestId;
    private int skipTime;
    private int skipVisible;
    private int splashType;
    private int stretchType;

    public String getBaseImageUrl() {
        return this.baseImageUrl;
    }

    public void setBaseImageUrl(String baseImageUrl) {
        this.baseImageUrl = baseImageUrl;
    }

    public String getAdExtraDes() {
        return this.adExtraDes;
    }

    public void setAdExtraDes(String adExtraDes) {
        this.adExtraDes = adExtraDes;
    }

    public int getMaterialType() {
        return this.materialType;
    }

    public void setMaterialType(int materialType) {
        this.materialType = materialType;
    }

    public int getStretchType() {
        return this.stretchType;
    }

    public void setStretchType(int stretchType) {
        this.stretchType = stretchType;
    }

    public int getMaterialSize() {
        return this.materialSize;
    }

    public void setMaterialSize(int materialSize) {
        this.materialSize = materialSize;
    }

    public int getDownNetType() {
        return this.downNetType;
    }

    public void setDownNetType(int downNetType) {
        this.downNetType = downNetType;
    }

    public int getSplashType() {
        return this.splashType;
    }

    public void setSplashType(int splashType) {
        this.splashType = splashType;
    }

    public int getAdExtraDesVisble() {
        return this.adExtraDesVisble;
    }

    public void setAdExtraDesVisble(int adExtraDesVisble) {
        this.adExtraDesVisble = adExtraDesVisble;
    }

    public int getSkipVisible() {
        return this.skipVisible;
    }

    public void setSkipVisible(int skipVisible) {
        this.skipVisible = skipVisible;
    }

    public int getSkipTime() {
        return this.skipTime;
    }

    public void setSkipTime(int skipTime) {
        this.skipTime = skipTime;
    }

    public int getCountDownVisible() {
        return this.countDownVisible;
    }

    public void setCountDownVisible(int countDownVisible) {
        this.countDownVisible = countDownVisible;
    }

    public int getCountDown() {
        return this.countDown;
    }

    public void setCountDown(int countDown) {
        this.countDown = countDown;
    }

    public String getClickUrl() {
        return this.clickUrl;
    }

    public void setClickUrl(String clickUrl) {
        this.clickUrl = clickUrl;
    }

    public int getImgWidth() {
        return this.imgWidth;
    }

    public void setImgWidth(int imgWidth) {
        this.imgWidth = imgWidth;
    }

    public int getImgHeight() {
        return this.imgHeight;
    }

    public void setImgHeight(int imgHeight) {
        this.imgHeight = imgHeight;
    }

    public String getAdContent() {
        return this.adContent;
    }

    public void setAdContent(String adContent) {
        this.adContent = adContent;
    }

    public String getAdTitle() {
        return this.adTitle;
    }

    public void setAdTitle(String adTitle) {
        this.adTitle = adTitle;
    }

    public String getAdClose() {
        return this.adClose;
    }

    public void setAdClose(String adClose) {
        this.adClose = adClose;
    }

    public int getAdIconVisible() {
        return this.adIconVisible;
    }

    public void setAdIconVisible(int adIconVisible) {
        this.adIconVisible = adIconVisible;
    }

    public String getAdUnitId() {
        return this.adUnitId;
    }

    public void setAdUnitId(String adUnitId) {
        this.adUnitId = adUnitId;
    }


    public String getAdId() {
        return this.adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public int getAdCloseVisible() {
        return this.adCloseVisible;
    }

    public void setAdCloseVisible(int adCloseVisible) {
        this.adCloseVisible = adCloseVisible;
    }

    public String getAdIcon() {
        return this.adIcon;
    }

    public void setAdIcon(String adIcon) {
        this.adIcon = adIcon;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getExpire() {
        return this.expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public long getAdExpiryDate() {
        return this.adExpiryDate;
    }

    public void setAdExpiryDate(long adExpiryDate) {
        this.adExpiryDate = adExpiryDate;
    }

    public Object getBean() throws Exception {
        Object obj = AdInfo.class.newInstance();
        for (Field f : AdInfo.class.getDeclaredFields()) {
            String name = f.getName();
            f.setAccessible(true);
            Object value = this.jsonObject.opt(name);
            if (value != null) {
                f.set(obj, value);
            }
        }
        return obj;
    }

    public AdInfo() {

    }

    public AdInfo(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }
}
