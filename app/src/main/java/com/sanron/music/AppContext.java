package com.sanron.music;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.FIFOLimitedMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.sanron.music.db.DataProvider;
import com.sanron.music.net.ApiHttpClient;
import com.sanron.music.service.IPlayer;
import com.sanron.music.service.PlayerService;
import com.sanron.music.utils.MyLog;

import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 2015/12/25.
 */
public class AppContext extends Application {

    private IPlayer musicPlayer;
    private ServiceConnection connection;

    public static final int DISK_CACHE_SIZE = 50*1024*1024;//磁盘缓存大小
    public static final int DISK_CACHE_MAX_COUNT = 100;//磁盘缓存文件数量
    public static final int THREAD_POOL_SIZE = 2;
    public static final float MEMORY_CACHE_PERCENT = 0.2f;//
    public static final String CACHE_PATH = "img_cache";


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

        long availableMemory = Runtime.getRuntime().maxMemory();
        int memoryCacheSize = (int) (availableMemory * MEMORY_CACHE_PERCENT);
        builder.memoryCache(new FIFOLimitedMemoryCache(memoryCacheSize));
        File diskCacheFile =  new File(getExternalCacheDir(),CACHE_PATH);
        File reserveCacheFile = new File(getCacheDir(),CACHE_PATH);
        try {
            builder.diskCache(new LruDiskCache(diskCacheFile,
                    reserveCacheFile,
                    new Md5FileNameGenerator(),
                    DISK_CACHE_SIZE,
                    DISK_CACHE_MAX_COUNT));
        } catch (IOException e) {
            e.printStackTrace();
        }

        builder.threadPoolSize(THREAD_POOL_SIZE);

        imageLoader.init(builder.build());
    }

    public void tryToStopService(){
        unbindService(connection);
        stopService(new Intent(this,PlayerService.class));
        connection = null;
        musicPlayer = null;
    }

    public boolean bindService(final ServiceConnection callback){
        Intent intent = new Intent(this,PlayerService.class);
        startService(intent);
        if(connection != null){
            unbindService(connection);
        }
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                musicPlayer = (IPlayer) service;
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

    public IPlayer getMusicPlayer(){
        return musicPlayer;
    }
}
