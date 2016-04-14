package com.sanron.music.net;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.Feature;
import com.sanron.music.utils.MyLog;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by sanron on 16-3-18.
 */

public abstract class ApiCallback<T> implements Callback {

    public static final int FAILURE_CANCLE = 1;
    public static final int FAILURE_PARESE = 2;
    public static final int FAILURE_IO = 3;

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        String json = null;
        T data;
        try {
            json = response.body().string();
            Type superClass = this.getClass().getGenericSuperclass();
            Class<T> clazz = (Class<T>) ((ParameterizedType) superClass).getActualTypeArguments()[0];
            data = JSON.parseObject(json, clazz, Feature.IgnoreNotMatch);
            onSuccess(call, data);
        } catch (JSONException e) {
            MyLog.d("MusicApi", "error json:" + json);
            e.printStackTrace();
            onSuccess(call, null);
        }
    }


    public abstract void onSuccess(Call call, T data);
}
