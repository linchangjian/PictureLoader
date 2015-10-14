package com.lcj.pictureloader;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by aniu520 on 10/12/2015.
 */
public class FileUtils {
    public static final String TAG = FileUtils.class.getSimpleName();

    private FileUtils(){

    }

    public static void copyStream(InputStream is, OutputStream os){
        final int buffer_size = 1024;

        byte[] bytes = new byte[buffer_size];

            try {
             while(true){
                 int count = is.read(bytes, 0 , buffer_size);
                 if(count == -1) {
                     break;
                 }
                 os.write(bytes, 0, count);
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(),e);
            }

    }
}
