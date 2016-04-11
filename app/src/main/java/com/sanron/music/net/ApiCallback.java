package com.sanron.music.net;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
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

    @Override
    public void onResponse(Call call, Response response) {
        String json;
        T data = null;
        try {
            json = response.body().string();
            Type superClass = this.getClass().getGenericSuperclass();
            Class<T> clazz = (Class<T>) ((ParameterizedType)superClass).getActualTypeArguments()[0];
            data = JSON.parseObject(json,clazz);
            onSuccess(call, data);
        } catch (IOException e) {
            e.printStackTrace();
            onFailure(call, e);
            return;
        }
    }

    public abstract void onSuccess(Call call, T data);
}
