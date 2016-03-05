package com.sanron.sunmusic.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/3/3.
 */
public class PlayerUtils {

    public static IMusicPlayer musicPlayer;
    public static void bindToService(Context context, final ServiceConnection callback) {
        Intent intent = new Intent(context, MusicService.class);
        context.startService(intent);
        context.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (musicPlayer == null) {
                    musicPlayer = (MusicService.MusicPlayer) service;
                }
                if(callback != null){
                    callback.onServiceConnected(name,service);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicPlayer = null;
                if(callback != null){
                    callback.onServiceDisconnected(name);
                }
            }
        }, Context.BIND_AUTO_CREATE);
    }

    public static IMusicPlayer getService() {
        return musicPlayer;
    }
}
