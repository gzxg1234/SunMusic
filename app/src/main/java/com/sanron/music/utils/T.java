package com.sanron.music.utils;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

/**
 * Toast工具类
 * Created by Administrator on 2015/12/25.
 */
public class T {

    public static final int SHORT = Toast.LENGTH_SHORT;
    public static final int LONG = Toast.LENGTH_LONG;
    public static Context appContext;

    public static void init(Application appContext) {
        T.appContext = appContext;
    }

    public static void show(String msg, int duration) {
        Toast.makeText(appContext, msg, duration).show();
    }

    public static void show(String msg) {
        show(msg, SHORT);
    }
}
