package com.sanron.music.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.sanron.music.common.MyLog;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Response;

/**
 * Created by sanron on 16-3-18.
 */

public abstract class JsonCallback<T> extends UICallback<T> {

    @Override
    protected T parser(Response response) throws IOException {
        T data = null;
        String json = null;
        try {
            json = response.body().string();
            Type superClass = this.getClass().getGenericSuperclass();
            Class<T> clazz = (Class<T>) ((ParameterizedType) superClass).getActualTypeArguments()[0];
            data = JSON.parseObject(json, clazz);
        } catch (JSONException e) {
            MyLog.d("MusicApi", "error json:" + json);
            e.printStackTrace();
        }
        return data;
    }
}
