package com.sanron.sunmusic;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.browse.MediaBrowser;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;

import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.FIFOLimitedMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.sanron.sunmusic.db.DataProvider;
import com.sanron.sunmusic.net.ApiHttpClient;
import com.sanron.sunmusic.service.IMusicPlayer;
import com.sanron.sunmusic.service.MusicService;
import com.sanron.sunmusic.utils.MyLog;

import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 2015/12/25.
 */
public class AppContext extends Application {

    private static IMusicPlayer musicPlayer;
    private static ServiceConnection connection;


    public static final String TAG = AppContext.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.i(TAG,"app create");
        ApiHttpClient.init(this);
        DataProvider.instance().init(this);
        initImageLoader();
    }

    private void initImageLoader(){
        ImageLoader imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(getApplicationContext());

        builder.memoryCache(new FIFOLimitedMemoryCache(2*1024*1024))
                .threadPoolSize(3);
        imageLoader.init(builder.build());
    }

    public void tryToStopService(){
        unbindService(connection);
        stopService(new Intent(this,MusicService.class));
        connection = null;
        musicPlayer = null;
    }

    public boolean bindService(final ServiceConnection callback){
        Intent intent = new Intent(this,MusicService.class);
        startService(intent);
        if(connection != null){
            unbindService(connection);
        }
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                musicPlayer = (IMusicPlayer) service;
                if(callback!=null){
                    callback.onServiceConnected(name,service);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicPlayer = null;
                connection = null;
                if(callback!=null){
                    callback.onServiceDisconnected(name);
                }
            }
        };
        return bindService(intent,connection,Context.BIND_AUTO_CREATE);
    }

    public IMusicPlayer getMusicPlayer(){
        return musicPlayer;
    }
}
