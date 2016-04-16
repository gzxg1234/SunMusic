package com.sanron.music.net;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.sanron.music.utils.MyLog;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by sanron on 16-3-18.
 */

public abstract class ApiCallback<T> implements Callback {

    private UIHandler uiHandler = new UIHandler(this);

    public abstract void onSuccess(T data);

    public abstract void onFailure(Exception e);

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        String json = null;
        T data = null;
        try {
            json = response.body().string();
            Type superClass = this.getClass().getGenericSuperclass();
            Class<T> clazz = (Class<T>) ((ParameterizedType) superClass).getActualTypeArguments()[0];
            data = JSON.parseObject(json, clazz);
        } catch (JSONException e) {
            MyLog.d("MusicApi", "error json:" + json);
            e.printStackTrace();
        }
        Message msg = Message.obtain();
        msg.what = UIHandler.SUCCESS;
        msg.obj = data;
        uiHandler.sendMessage(msg);
    }

    @Override
    public void onFailure(Call call, IOException e) {
        if ("Canceled".equals(e.getMessage())) {
            //如果是被取消的不触发失败回调
            return;
        }
        Message msg = Message.obtain();
        msg.what = UIHandler.FAILURE;
        msg.obj = e;
        uiHandler.sendMessage(msg);
    }


    static class UIHandler<T> extends Handler {
        public WeakReference<ApiCallback<T>> callback;

        public static final int FAILURE = 0;
        public static final int SUCCESS = 1;

        public UIHandler(ApiCallback callback) {
            super(Looper.getMainLooper());
            this.callback = new WeakReference<ApiCallback<T>>(callback);
        }

        @Override
        public void handleMessage(Message msg) {
            ApiCallback<T> callback = this.callback.get();
            if (callback == null) {
                return;
            }

            if (msg.what == SUCCESS) {
                if (msg.obj == null) {
                    callback.onSuccess(null);
                } else {
                    callback.onSuccess((T) msg.obj);
                }
            } else {
                callback.onFailure((Exception) msg.obj);
            }
        }
    }

}
