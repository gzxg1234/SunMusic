package com.sanron.music;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.db.DataProvider;
import com.sanron.music.service.IPlayer;
import com.sanron.music.service.PlayerService;
import com.sanron.music.utils.MyLog;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2015/12/25.
 */
public class AppContext extends Application {

    private IPlayer musicPlayer;
    private ServiceConnection connection;
    private int statusBarHeight = -1;


    public static final String TAG = AppContext.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.i(TAG, "app create");
        DataProvider.instance().init(this);
    }


    public void closeApp() {
        AppManager.instance().finishAllActivity();
        if(connection!=null) {
            unbindService(connection);
            connection = null;
        }
        stopService(new Intent(this, PlayerService.class));
        ImageLoader.getInstance().destroy();
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
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object obj = clazz.newInstance();
            Field field = clazz.getField("status_bar_height");
            int resId = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = getResources().getDimensionPixelSize(resId);
        } catch (Exception e) {
            e.printStackTrace();
            statusBarHeight = getResources().getDimensionPixelSize(R.dimen.status_height);
        }
    }

    public IPlayer getMusicPlayer() {
        return musicPlayer;
    }
}
