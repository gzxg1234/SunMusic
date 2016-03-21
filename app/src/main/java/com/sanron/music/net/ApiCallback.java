package com.sanron.music.net;

import com.sanron.music.utils.JsonUtils;
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
    public void onResponse(Call call, Response response) throws IOException {
        try {
            String json = response.body().string();
            MyLog.d("MusicApi","json:"+json);
            Type superClass = getClass().getGenericSuperclass();
            Class<T> clazz = (Class<T>) ((ParameterizedType) superClass).getActualTypeArguments()[0];
            T data = JsonUtils.fromJson(json,clazz);
            onSuccess(data);
        } catch (Exception e) {
            e.printStackTrace();
            onFailure();
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {
        onFailure();
    }

    public abstract void onFailure();

    public abstract void onSuccess(T data);
}
