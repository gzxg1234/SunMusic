package com.sanron.sunmusic.activity;

import android.app.Application;

import com.sanron.sunmusic.db.DataProvider;

/**
 * Created by Administrator on 2015/12/25.
 */
public class SunApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DataProvider.instance().init(this);
    }
}
