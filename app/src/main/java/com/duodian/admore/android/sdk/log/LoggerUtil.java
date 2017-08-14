package com.duodian.admore.android.sdk.log;

import android.content.Context;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.RequestParameters;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.duodian.admore.android.sdk.config.AdmoreSdkConfig;
import com.duodian.admore.android.sdk.http.HttpCallbackAdapter;
import com.duodian.admore.android.sdk.http.HttpConfig;
import com.duodian.admore.android.sdk.http.HttpUtil;
import com.duodian.admore.android.sdk.utils.FileUtil;
import com.duodian.admore.android.sdk.utils.LogUtil;
import com.duodian.admore.android.sdk.utils.Util;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

public class LoggerUtil {
    private static final int FILE_MAX_LENGTH = 100000;
    private static final String TAG = "LoggerUtil";
    private static ExecutorService service = Executors.newSingleThreadExecutor();
    private static ExecutorService serviceUpload = Executors.newFixedThreadPool(3);
    public static long upLoadLogInterval = AdmoreSdkConfig.LOG_STRATEGY_DURATION;

//    private static OnLogSave onLogSave;
//    public interface OnLogSave {
//        void onSave(File file);
//    }
//    public void setOnLogSave(OnLogSave onLogSave) {
//        onLogSave = onLogSave;
//    }

    public static synchronized void saveLog(final Context context, final String content) {
        synchronized (LoggerUtil.class) {
            service.execute(new Runnable() {
                public void run() {
                    File file;
                    if (Util.externalStorageAvailable()) {
                        file = new File(context.getExternalCacheDir(), "com.duodian.admore.android.sdk");
                        LogUtil.e(LoggerUtil.TAG, "saveLog:path:" + file.getAbsolutePath());
                        LogUtil.e(LoggerUtil.TAG, "使用外部存储的私有目录");
                    } else {
                        file = new File(context.getCacheDir(), "com.duodian.admore.android.sdk");
                        LogUtil.e(LoggerUtil.TAG, "saveLog:path:" + file.getAbsolutePath());
                        LogUtil.e(LoggerUtil.TAG, "使用内部存储");
                    }
                    File logCacheDirectory;
                    if (file.isDirectory()) {
                        logCacheDirectory = new File(file, AdmoreSdkConfig.LOG_CACHE_DIRECTORY);
                        if (logCacheDirectory.isDirectory()) {
                            File[] files = logCacheDirectory.listFiles();
                            if (files.length <= 0) {
                                FileUtil.writeToFile(new File(logCacheDirectory.getAbsolutePath(), UUID.randomUUID() + "-time-" + System.currentTimeMillis() + ".log"), content, true);
                            } else if (files[files.length - 1].length() < 100000) {
                                FileUtil.writeToFile(files[files.length - 1], content, false);
                            } else {
                                FileUtil.writeToFile(new File(logCacheDirectory.getAbsolutePath(), UUID.randomUUID() + "-time-" + System.currentTimeMillis() + ".log"), content, true);
                            }
                        } else if (logCacheDirectory.mkdirs()) {
                            FileUtil.writeToFile(new File(logCacheDirectory.getAbsolutePath(), UUID.randomUUID() + "-time-" + System.currentTimeMillis() + ".log"), content, true);
                        }
                    } else if (file.mkdirs()) {
                        logCacheDirectory = new File(file, AdmoreSdkConfig.LOG_CACHE_DIRECTORY);
                        if (logCacheDirectory.mkdirs()) {
                            FileUtil.writeToFile(new File(logCacheDirectory.getAbsolutePath(), UUID.randomUUID() + "-time-" + System.currentTimeMillis() + ".log"), content, true);
                        }
                    }
                }
            });
        }
    }

    public static void readLog(final Context context) {
        serviceUpload.execute(new Runnable() {
            public void run() {
                File file;
                if (Util.externalStorageAvailable()) {
                    file = new File(context.getExternalCacheDir(), "com.duodian.admore.android.sdk");
                    LogUtil.e(LoggerUtil.TAG, "readLog:path:" + file.getAbsolutePath());
                    LogUtil.e(LoggerUtil.TAG, "读取外部存储的私有目录");
                } else {
                    file = new File(context.getCacheDir(), "com.duodian.admore.android.sdk");
                    LogUtil.e(LoggerUtil.TAG, "readLog:path:" + file.getAbsolutePath());
                    LogUtil.e(LoggerUtil.TAG, "读取内部存储");
                }
                if (file.isDirectory()) {
                    File logCacheDirectory = new File(file, AdmoreSdkConfig.LOG_CACHE_DIRECTORY);
                    if (logCacheDirectory.isDirectory()) {
                        File[] files = logCacheDirectory.listFiles();
                        for (File subFile : files) {
                            String fileName = subFile.getName();
                            try {
                                if (System.currentTimeMillis() - Long.parseLong(fileName.substring(fileName.lastIndexOf("-time-") + 4, fileName.lastIndexOf("."))) >= LoggerUtil.upLoadLogInterval || subFile.length() >= 100000) {
                                    LoggerUtil.uploadLog(context, subFile);
                                    LogUtil.e("uploadLog", fileName);
                                }
                            } catch (Exception e) {
                                LoggerUtil.uploadLog(context, subFile);
                                LogUtil.e("uploadLog", "Exception" + fileName);
                            }
                        }
                    }
                }
            }
        });
    }

    private static synchronized void uploadLog(Context context, final File file) {

        synchronized (LoggerUtil.class) {
            if (HttpUtil.isNetConnected(context.getApplicationContext()) && (OSSManager.LOG_WIFI != 1 || HttpUtil.isWifiConnected(context.getApplicationContext()))) {
                if (OSSConstants.RESOURCE_NAME_OSS.equalsIgnoreCase(OSSManager.OSS_TYPE)) {
                    LogUtil.e("uploadLog", "uploadLog+OSSManager.OSS_TYPE");
                    if (file == null || !file.exists()) {
                        return;
                    }
                    try {
                        OSSManager.getInstance().uploadFile(file.getAbsolutePath(), file.getName(), new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
                            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                                LogUtil.e("PutObject", "UploadSuccess");
//                                if (LoggerUtil.onLogSave != null) {
//                                    LoggerUtil.onLogSave.onSave(file);
//                                }
                                file.delete();
                            }

                            public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                                if (clientException != null) {
                                    clientException.printStackTrace();
                                }
                                if (serviceException == null) {
                                }
                            }
                        });

                    } catch (Exception ignored) {

                    }
                } else {
                    if (file == null || !file.exists()) {
                        return;
                    }
                    HttpUtil.doPostFileAsync(HttpConfig.URL_LOGFILE_UPLOAD, null, null, HttpUtil.setHeader(context, LogInfo.AdmoreLogTypeAdvUploadLog), false, file, new HttpCallbackAdapter() {
                        public void onRequestComplete(byte[] result) {
                            super.onRequestComplete(result);
                            try {
                                String resultString = new String(result);
                                LogUtil.e(LoggerUtil.TAG, resultString);
                                if ("200".equalsIgnoreCase(new JSONObject(resultString).optString("code"))) {
                                    file.delete();
                                    LogUtil.e(LoggerUtil.TAG, RequestParameters.SUBRESOURCE_DELETE);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
    }
}
