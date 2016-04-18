package com.sanron.music.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by sanron on 16-4-6.
 */
public class NetTool {
    public static int checkNet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null
                || !networkInfo.isAvailable()) {
            return -1;
        } else {
            return networkInfo.getType();
        }
    }
}
