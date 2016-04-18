package com.sanron.music;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.db.DataProvider;
import com.sanron.music.net.MusicApi;
import com.sanron.music.service.PlayerService;
import com.sanron.music.common.MyLog;
import com.sanron.music.common.T;
import com.sanron.music.common.ViewTool;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Administrator on 2015/12/25.
 */
public class AppContext extends Application {

    public static final String TAG = AppContext.class.getSimpleName();

    private BroadcastReceiver netChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int netType = checkNet();
            MyLog.i(TAG, "network change to " + netType);
            if (netType == -1) {
                MusicApi.sIsNetAvailable = false;
            } else {
                MusicApi.sIsNetAvailable = true;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
        T.init(this);
        ViewTool.init(this);
        DataProvider.instance().init(this);
        registerReceiver(netChangeReceiver,
                new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    public int checkNet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null
                || !networkInfo.isAvailable()) {
            return -1;
        } else {
            return networkInfo.getType();
        }
    }


    public void closeApp() {
        AppManager.instance().finishAllActivity();
        stopService(new Intent(this, PlayerService.class));
        ImageLoader.getInstance().destroy();
        unregisterReceiver(netChangeReceiver);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
