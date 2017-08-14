package com.duodian.admore.android.sdk.log;

import org.json.JSONObject;

public class OSSAuthInfo {
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String endpoint;
    private long expiration;
    private String fileLink;
    private String securityToken;

    public OSSAuthInfo(JSONObject jsonObject) {
        String accessKeyId = jsonObject.optString("accessKeyId");
        String accessKeySecret = jsonObject.optString("accessKeySecret");
        String securityToken = jsonObject.optString("securityToken");
        String endpoint = jsonObject.optString("endpoint");
        String bucketName = jsonObject.optString("bucketName");
        long expiration = jsonObject.optLong("expiration");
        String fileLink = jsonObject.optString("fileLink");
        setAccessKeyId(accessKeyId);
        setAccessKeySecret(accessKeySecret);
        setSecurityToken(securityToken);
        setExpiration(expiration);
        setFileLink(fileLink);
        setEndpoint(endpoint);
        setBucketName(bucketName);
    }

    public String getBucketName() {
        return this.bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKeyId() {
        return this.accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return this.accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getSecurityToken() {
        return this.securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public long getExpiration() {
        return this.expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public String getFileLink() {
        return this.fileLink;
    }

    public void setFileLink(String fileLink) {
        this.fileLink = fileLink;
    }
}
