package com.sanron.sunmusic;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.SystemClock;

import com.sanron.sunmusic.db.DataProvider;
import com.sanron.sunmusic.service.MusicService;
import com.sanron.sunmusic.service.PlayerUtils;

/**
 * Created by Administrator on 2015/12/25.
 */
public class AppContext extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        DataProvider.instance().init(this);
    }
}
