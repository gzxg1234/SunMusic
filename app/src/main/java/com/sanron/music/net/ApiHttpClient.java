package com.sanron.music.net;

import android.content.Context;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by sanron on 16-3-18.
 */
public class ApiHttpClient {

    private static OkHttpClient httpClient;

    public static final int HTTP_CACHE_MAX_SIZE = 10 * 1024 * 1024;//10MB

    public static final int READ_TIMEOUT = 10;
    public static final int CONNECT_TIMEOUT = 10;

    public static void init(Context context) {
        File cacheDir;
        File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir != null) {
            cacheDir = new File(externalCacheDir, "http_cache");
        } else {
            cacheDir = new File(context.getCacheDir(), "http_cache");
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        builder.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
        builder.cache(new Cache(cacheDir, HTTP_CACHE_MAX_SIZE));
        httpClient = builder.build();
    }

    public static Call get(String url, Callback callback) {
        return get(url, 0, callback);
    }

    public static Call get(String url, int cacheAge, Callback callback) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (cacheAge != 0) {
            builder.header("Cache-Control", "max-stale=" + cacheAge);
        }
        Call call = httpClient.newCall(builder.build());
        call.enqueue(callback);
        return call;
    }
}
