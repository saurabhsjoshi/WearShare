package com.sau.wearshare.utils;

import android.util.Log;

/**
 * Created by saurabh on 15-07-09.
 */
public class Logger {

    public static void LOGD(final String tag, String message){
        if(Log.isLoggable(tag, Log.DEBUG))
                System.out.print("Debug: " + tag + ": " + message);
    }

    public static void LOGE(final String tag, String message){
        if(Log.isLoggable(tag, Log.ERROR))
            System.out.print("Error: " + tag + ": " + message);
    }
}
