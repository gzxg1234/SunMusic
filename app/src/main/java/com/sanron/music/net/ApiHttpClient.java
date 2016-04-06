package com.sanron.music.net;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sanron on 16-3-18.
 */
public class ApiHttpClient {

    private static OkHttpClient httpClient;

    /**
     * http缓存目录
     */
    public static String HTTP_CACHE_PATH;

    public static final int HTTP_CACHE_MAX_AGE = 1 * 60;//1分钟

    public static final int HTTP_CACHE_MAX_STALE = 7 * 60 * 60 * 24;//7天

    public static final int HTTP_CACHE_MAX_SIZE = 10 * 1024 * 1024;//10MB

    public static final int READ_TIMEOUT = 10;
    public static final int CONNECT_TIMEOUT = 10;

    public static void init(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            HTTP_CACHE_PATH = context.getExternalCacheDir().getAbsolutePath() + "/http_cache";
        } else {
            HTTP_CACHE_PATH = context.getCacheDir().getAbsolutePath() + "/http_cache";
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        builder.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
        builder.cache(new Cache(new File(HTTP_CACHE_PATH), HTTP_CACHE_MAX_SIZE));
        builder.networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                String cacheControl = String.format(Locale.CHINA, "max-age=%d",
                        HTTP_CACHE_MAX_AGE);

                return response
                        .newBuilder()
                        .header("Cache-Control", cacheControl)
                        .build();
            }
        });
        httpClient = builder.build();
    }

    public static Call get(String url, Callback callback) {
        Call call = getCall(url);
        call.enqueue(callback);
        return call;
    }

    public static Call getCall(String url) {
        Request.Builder builder = new Request.Builder();
        builder.url(url)
                .header("Cache-Control", "max-stale=" + HTTP_CACHE_MAX_STALE);
        return httpClient.newCall(builder.build());
    }
}
