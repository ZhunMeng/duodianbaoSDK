package com.duodian.admore.android.sdk.encryption;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESEncryption {
    private String encoding = "UTF-8";
    private SecretKeySpec key;

    public AESEncryption(String aesKey) {
        try {
            this.key = new SecretKeySpec(aesKey.getBytes(this.encoding), 0, 16, "AES");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized byte[] encrypt(byte[] bytes, int mode) {
        Cipher cipher;
        if (this.key == null) {
            throw new RuntimeException("aes key is null!");
        }
        if (bytes != null) {
            if (bytes.length != 0) {
                try {
                    cipher = Cipher.getInstance("AES");
                    cipher.init(mode, this.key);
                    return cipher.doFinal(bytes);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
        throw new RuntimeException("text is null!");
    }

    public String encode(String text) {
        return bytesToString(encodeBytes(text));
    }

    public byte[] encodeBytes(String text) {
        if (text == null) {
            return null;
        }
        try {
            return encrypt(text.getBytes(this.encoding), 1);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String decode(String encryptString) {
        String str = null;
        if (!TextUtils.isEmpty(encryptString)) {
            byte[] buffer = encrypt(toBytes(encryptString), 2);
            if (buffer != null) {
                try {
                    str = new String(buffer, this.encoding);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return str;
    }

    public byte[] decodeBytes(byte[] bytes) {
        return encrypt(bytes, 2);
    }

    private byte[] toBytes(String str) {
        int l = str.length();
        byte[] ret = new byte[((l / 2) + (l % 2))];
        for (int i = 0; i < l; i += 2) {
            String s;
            if (i + 2 >= str.length()) {
                s = str.substring(i);
            } else {
                s = str.substring(i, i + 2);
            }
            ret[i / 2] = (byte) Integer.parseInt(s, 16);
        }
        return ret;
    }

    private String bytesToString(byte[] buffer) {
        if (buffer == null) {
            return null;
        }
        StringBuilder tsResult = new StringBuilder();
        for (byte aBuffer : buffer) {
            CharSequence tsTemp = Integer.toHexString(aBuffer & 255);
            if (tsTemp.length() == 1) {
                tsTemp = new StringBuilder(2).append('0').append(tsTemp);
            }
            tsResult.append(tsTemp);
        }
        return tsResult.toString().toUpperCase();
    }
}
