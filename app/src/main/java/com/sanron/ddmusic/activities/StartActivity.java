package com.sanron.ddmusic.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Window;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.FIFOLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.sanron.ddmusic.AppConfig;
import com.sanron.ddmusic.AppContext;

import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 2016/3/5.
 */
public class StartActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkExternalStorage();
        initImageLoader();
        gotoMainActivity();

    }


    private void gotoMainActivity() {
        Intent intent = new Intent(StartActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void initImageLoader() {
        ImageLoader imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(getApplicationContext());

        int memoryCacheSize = (int) (Runtime.getRuntime().maxMemory() * AppConfig.MEMORY_CACHE_PERCENTAGE);
        memoryCacheSize = Math.min(memoryCacheSize, AppConfig.MAX_MEMORY_CACHE_SIZE);

        DiskCache diskCache = null;
        try {
            File cacheDir = new File(Environment.getExternalStorageDirectory(),
                    AppConfig.IMG_CACHE_PATH);
            diskCache = new LruDiskCache(cacheDir,
                    null,
                    new Md5FileNameGenerator(),
                    AppConfig.IMG_DISK_CACHE_MAX_SIZE,
                    AppConfig.IMG_DISK_CACHE_MAX_COUNT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DisplayImageOptions defaultImageOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();

        builder.memoryCache(new FIFOLimitedMemoryCache(memoryCacheSize))
                .diskCache(diskCache)
                .imageDownloader(new BaseImageDownloader(this, 5 * 1000, 30 * 1000))
                .threadPoolSize(AppConfig.THREAD_POOL_SIZE)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCacheExtraOptions(1000, 1000)
                .defaultDisplayImageOptions(defaultImageOptions);

        imageLoader.init(builder.build());
    }

    //检测是否有外置存储
    private void checkExternalStorage() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("抱歉，手机无外置存储，无法正常使用app");
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    ((AppContext) getApplicationContext()).closeApp();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.show();
        }
    }
}
