package com.sanron.music;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Build;
import android.os.IBinder;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.db.DataProvider;
import com.sanron.music.net.MusicApi;
import com.sanron.music.service.IPlayer;
import com.sanron.music.service.PlayerService;
import com.sanron.music.utils.MyLog;
import com.sanron.music.utils.NetTool;
import com.sanron.music.utils.T;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Administrator on 2015/12/25.
 */
public class AppContext extends Application {

    private IPlayer musicPlayer;
    private ServiceConnection connection;
    private int statusBarHeight = -1;
    private int netType;

    public static final String TAG = AppContext.class.getSimpleName();
    private BroadcastReceiver netChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            netType = NetTool.checkNet(context);
            MyLog.i(TAG, "network change to " + netType);
            if (netType == -1) {
                MusicApi.isNetAvailable = false;
            } else {
                MusicApi.isNetAvailable = true;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.i(TAG, "app create");
        LeakCanary.install(this);
        T.init(this);
        registerReceiver(netChangeReceiver,
                new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        DataProvider.instance().init(this);
    }

    public int getNetType() {
        return netType;
    }

    public void closeApp() {
        AppManager.instance().finishAllActivity();
        if (connection != null) {
            unbindService(connection);
            connection = null;
        }
        stopService(new Intent(this, PlayerService.class));
        ImageLoader.getInstance().destroy();
        unregisterReceiver(netChangeReceiver);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public boolean bindService(final ServiceConnection callback) {
        Intent intent = new Intent(this, PlayerService.class);
        startService(intent);
        if (connection != null) {
            unbindService(connection);
        }
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                musicPlayer = (IPlayer) service;
                if (callback != null) {
                    callback.onServiceConnected(name, service);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicPlayer = null;
                connection = null;
                if (callback != null) {
                    callback.onServiceDisconnected(name);
                }
            }
        };
        return bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public void setViewFitsStatusBar(View view) {
        if (statusBarHeight == 0) {
            return;
        } else if (statusBarHeight == -1) {
            initStatusBarHeight();
        }
        view.setPadding(view.getPaddingLeft(), statusBarHeight,
                view.getPaddingRight(), view.getPaddingBottom());
        view.requestLayout();
        view.invalidate();
    }

    private void initStatusBarHeight() {
        if (Build.VERSION.SDK_INT < 19) {
            statusBarHeight = 0;
            return;
        }

        int resId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            statusBarHeight = Resources.getSystem().getDimensionPixelSize(resId);
        } else {
            statusBarHeight = getResources().getDimensionPixelSize(R.dimen.status_height);
        }
    }

    public IPlayer getMusicPlayer() {
        return musicPlayer;
    }
}
