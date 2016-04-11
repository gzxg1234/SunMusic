package com.sanron.music.net;

import android.content.Context;
import android.os.Environment;

import com.sanron.music.AppConfig;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by sanron on 16-3-18.
 */
public class ApiHttpClient {

    private static OkHttpClient httpClient;

    public static void init() {
        File cacheDir = new File(Environment.getExternalStorageDirectory(),
                AppConfig.HTTP_CACHE_PATH);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT, TimeUnit.SECONDS);
        builder.readTimeout(AppConfig.HTTP_READ_TIMEOUT, TimeUnit.SECONDS);
        builder.cache(new Cache(cacheDir, AppConfig.HTTP_CACHE_MAX_SIZE));
        httpClient = builder.build();
    }

    public static Call get(String url, Callback callback) {
        return get(url, 0, callback);
    }

    public static Call get(String url, int cacheAge, Callback callback) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (cacheAge != 0) {
            builder.header("Cache-Control", "max-stale=" + cacheAge).cacheControl(CacheControl.FORCE_NETWORK);
        }
        Call call = httpClient.newCall(builder.build());
        call.enqueue(callback);
        return call;
    }
}
