package com.sanron.ddmusic.api;

import android.os.Environment;
import android.text.TextUtils;

import com.sanron.ddmusic.AppConfig;

import java.io.File;
import java.io.IOException;
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
public class AppHttpClient {

    private static OkHttpClient sHttpClient;
    public static Interceptor CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            String cacheControl = request.cacheControl().toString();
            if (TextUtils.isEmpty(cacheControl)) {
                return chain.proceed(request);
            }
            return chain.proceed(request)
                    .newBuilder()
                    .addHeader("Cache-Control", cacheControl)
                    .build();
        }
    };

    static {
        File cacheDir = new File(Environment.getExternalStorageDirectory(),
                AppConfig.HTTP_CACHE_PATH);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT, TimeUnit.SECONDS);
        builder.readTimeout(AppConfig.HTTP_READ_TIMEOUT, TimeUnit.SECONDS);
        builder.cache(new Cache(cacheDir, AppConfig.HTTP_CACHE_MAX_SIZE));
        builder.addInterceptor(CACHE_CONTROL_INTERCEPTOR);
        builder.addNetworkInterceptor(CACHE_CONTROL_INTERCEPTOR);
        sHttpClient = builder.build();
    }

//    public static Call get(String url, Callback callback) {
//        return get(url, 0, callback);
//    }
//
//    public static Call get(String url, int maxAge, Callback callback) {
//        return get(url, maxAge, 0, callback);
//    }
//
//    public static Call get(String url, int maxAge, int maxStale, final Callback callback) {
//        CacheControl.Builder cacheControl = new CacheControl.Builder();
//        if (maxAge != 0) {
//            cacheControl.maxAge(maxAge, TimeUnit.SECONDS);
//        }
//        if (maxStale != 0) {
//            cacheControl.maxStale(maxStale, TimeUnit.SECONDS);
//        }
//        Request request = new Request.Builder()
//                .url(url)
//                .cacheControl(cacheControl.build())
//                .build();
//        Call call = sHttpClient.newCall(request);
//        call.enqueue(callback);
//        return call;
//    }

    public static Call get(Request request, Callback callback) {
        Call call = sHttpClient.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public static void cancel(Object tag) {
        if (tag == null) {
            return;
        }

        for (Call call : sHttpClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : sHttpClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

}
