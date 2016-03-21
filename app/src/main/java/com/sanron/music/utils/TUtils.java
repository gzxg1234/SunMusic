package com.sanron.music.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast工具类
 * Created by Administrator on 2015/12/25.
 */
public class TUtils {

    public static final int SHORT = Toast.LENGTH_SHORT;
    public static final int LONG = Toast.LENGTH_LONG;

    public static void show(Context context,String msg,int duration){
        Toast.makeText(context,msg,duration).show();
    }

    public static void show(Context context,String msg){
        show(context,msg,SHORT);
    }
}
