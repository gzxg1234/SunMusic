package com.sanron.sunmusic.net;

import android.content.Context;

import com.fasterxml.jackson.core.type.TypeReference;

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

    public static OkHttpClient httpClient;

    /**
     * http缓存目录
     */
    public static String HTTP_CACHE_PATH;

    public static final int HTTP_CACHE_MAX_AGE = 1 * 60;//1分钟

    public static final int HTTP_CACHE_MAX_STALE = 7 * 60 * 60 * 24;//7天

    public static void init(Context context) {
        HTTP_CACHE_PATH = context.getCacheDir().getAbsolutePath() + "/http_cache";
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(5, TimeUnit.SECONDS);
        builder.cache(new Cache(new File(HTTP_CACHE_PATH), 10 * 1024 * 1024));
        builder.networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                String cacheControl = String.format(Locale.CHINA, "max-age=%d,max-stale=%d",
                        HTTP_CACHE_MAX_AGE,
                        HTTP_CACHE_MAX_STALE);

                return response
                        .newBuilder()
                        .header("Cache-Control", cacheControl)
                        .build();
            }
        });
        httpClient = builder.build();
    }

    public static void get(String url,Callback callback){
        getCall(url).enqueue(callback);
    }

    public static Call getCall(String url) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        return httpClient.newCall(builder.build());
    }
}
