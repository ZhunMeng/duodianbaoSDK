package com.duodian.admore.android.sdk.utils;

import com.duodian.admore.android.sdk.log.LogEncryptHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    public static void writeToFile(File file, String content, boolean createNewFile) {
        if (createNewFile) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
             }
        }
        if (file.exists()) {
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(file, true);
                byte[] bytes = LogEncryptHelper.encodeBytes(content + "\r\n\r\n");
                fileOutputStream.write(Util.intToByteArray(bytes.length));
                fileOutputStream.write(bytes);
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
