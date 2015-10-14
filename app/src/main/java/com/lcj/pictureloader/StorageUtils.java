package com.lcj.pictureloader;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by aniu520 on 9/23/2015.
 */
public class StorageUtils {

    private StorageUtils(){

    }

    public static File getCacheDirectory(Context context) {
        File appCacheDir;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File appDataDir = new File(Environment.getExternalStorageDirectory(), Constants.APP_DIRECTORY);
            appCacheDir = new File(appDataDir, Constants.APP_CACHE_DIRECTORY);

        }else{
            appCacheDir = context.getCacheDir();
        }
        if(!appCacheDir.exists()){
            appCacheDir.mkdirs();
        }
        return appCacheDir;
    }
}
