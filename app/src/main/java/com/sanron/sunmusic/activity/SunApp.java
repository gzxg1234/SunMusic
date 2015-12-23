package com.sanron.sunmusic.activity;

import android.app.Application;

import com.sanron.sunmusic.db.ListSongsProvider;
import com.sanron.sunmusic.db.PlayListProvider;
import com.sanron.sunmusic.db.SongInfoProvider;
import com.sanron.sunmusic.model.PlayList;

/**
 * Created by Administrator on 2015/12/25.
 */
public class SunApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ListSongsProvider.instance().init(this);
        PlayListProvider.instance().init(this);
        SongInfoProvider.instance().init(this);
    }
}
