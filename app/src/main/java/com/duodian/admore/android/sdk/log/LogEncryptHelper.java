package com.duodian.admore.android.sdk.log;

import com.duodian.admore.android.sdk.encryption.AESEncryption;

public class LogEncryptHelper {
    private static AESEncryption aesEncryption = new AESEncryption("VcHxmIECoW7040lpkIAG1y7x7Gn6FlMx");

    private LogEncryptHelper() {
    }

    public static String encode(String text) {
        return aesEncryption.encode(text);
    }

    public static byte[] encodeBytes(String text) {
        return aesEncryption.encodeBytes(text);
    }

    public static String decode(String encryptString) {
        return aesEncryption.decode(encryptString);
    }

    public static byte[] decodeBytes(byte[] bytes) {
        return aesEncryption.decodeBytes(bytes);
    }
}
