package com.sanron.ddmusic;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.ddmusic.api.MusicApi;
import com.sanron.ddmusic.common.MyLog;
import com.sanron.ddmusic.common.ViewTool;
import com.sanron.ddmusic.db.DataProvider;
import com.sanron.ddmusic.service.DDPlayService;
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
        ViewTool.init(this);
        DataProvider.get().init(this);
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

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case TRIM_MEMORY_UI_HIDDEN: {
                ImageLoader.getInstance().clearMemoryCache();
            }
            break;
        }
    }

    public void closeApp() {
        AppManager.instance().finishAllActivity();
        stopService(new Intent(this, DDPlayService.class));
        ImageLoader.getInstance().destroy();
        unregisterReceiver(netChangeReceiver);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
