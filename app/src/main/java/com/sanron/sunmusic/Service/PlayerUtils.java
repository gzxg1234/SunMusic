package com.sanron.sunmusic.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Administrator on 2016/3/3.
 */
public class PlayerUtils {

    public static MusicService.MusicPlayer musicPlayer;

    public static void bindToService(Context context) {
        Intent intent = new Intent(context, MusicService.class);
        context.bindService(intent, new PlayerBinder(), Context.BIND_AUTO_CREATE);
    }

    public static class PlayerBinder implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicPlayer = (MusicService.MusicPlayer) service;
            Log.i("MusicService", "service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicPlayer = null;
        }
    }

    public static MusicService.MusicPlayer getService() {
        return musicPlayer;
    }
}
