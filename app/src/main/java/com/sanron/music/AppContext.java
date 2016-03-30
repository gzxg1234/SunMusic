package com.sanron.music;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.view.View;

import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.FIFOLimitedMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.sanron.music.db.DataProvider;
import com.sanron.music.net.ApiHttpClient;
import com.sanron.music.service.IPlayer;
import com.sanron.music.service.PlayerService;
import com.sanron.music.utils.MyLog;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Created by Administrator on 2015/12/25.
 */
public class AppContext extends Application {

    private IPlayer musicPlayer;
    private ServiceConnection connection;
    private int statusBarHeight;

    public static final int DISK_CACHE_SIZE = 50 * 1024 * 1024;//磁盘缓存大小
    public static final int DISK_CACHE_MAX_COUNT = 100;//磁盘缓存文件数量
    public static final int THREAD_POOL_SIZE = 3;
    public static final int MAX_MEMORY_CACHE_SIZE = 16 * 1024 * 1024;//RAM缓存最多16MB
    public static final float MEMORY_CACHE_PERCENTAGE = 0.1f;//默认10%程序最大内存的ram缓存
    public static final String CACHE_PATH = "img_cache";

    public static final String TAG = AppContext.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.i(TAG, "app create");
        ApiHttpClient.init(this);
        DataProvider.instance().init(this);
        initImageLoader();
        initStatusBarHeight();
    }

    private void initImageLoader() {
        ImageLoader imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(getApplicationContext());


        int memoryCacheSize = (int) (Runtime.getRuntime().maxMemory() * MEMORY_CACHE_PERCENTAGE);
        if (memoryCacheSize > MAX_MEMORY_CACHE_SIZE) {
            memoryCacheSize = MAX_MEMORY_CACHE_SIZE;
        }
        builder.memoryCache(new FIFOLimitedMemoryCache(memoryCacheSize));
        File diskCacheFile = new File(getExternalCacheDir(), CACHE_PATH);
        File reserveCacheFile = new File(getCacheDir(), CACHE_PATH);
        try {
            builder.diskCache(new LruDiskCache(diskCacheFile,
                    reserveCacheFile,
                    new Md5FileNameGenerator(),
                    DISK_CACHE_SIZE,
                    DISK_CACHE_MAX_COUNT));
        } catch (IOException e) {
            e.printStackTrace();
        }
        builder.imageDownloader(new BaseImageDownloader(this,5*1000,30*1000));
        builder.threadPoolSize(THREAD_POOL_SIZE);
        builder.threadPriority(Thread.NORM_PRIORITY - 2);
        imageLoader.init(builder.build());
    }

    public void closeApp() {
        AppManager.instance().finishAllActivity();
        unbindService(connection);
        connection = null;
        stopService(new Intent(this, PlayerService.class));
        ImageLoader.getInstance().destroy();
        DataProvider.instance().destroy();
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

    /**
     * 设置view的内间距适应状态栏
     *
     * @param view
     */
    public void setViewFitsStatuBar(View view) {
        if (Build.VERSION.SDK_INT < 19) {
            return;
        }

        view.setPadding(view.getPaddingLeft(), statusBarHeight,
                view.getPaddingRight(), view.getPaddingBottom());
        view.requestLayout();
        view.invalidate();
    }


    private void initStatusBarHeight() {
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
