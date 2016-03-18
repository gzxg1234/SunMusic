package com.sanron.sunmusic.net;

import android.content.ContentValues;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanron.sunmusic.net.bean.FocusPicResult;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by sanron on 16-3-18.
 */
public class MusicApi {

    public static final String FORMATE = "json";
    public static final String BASE = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=android&version=5.6.5.6&format=" + FORMATE;

    public static ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static void focusPic(int num, ApiCallback<FocusPicResult> apiCallback) {
        ContentValues params = new ContentValues();
        params.put("method", "baidu.ting.plaza.getFocusPic");
        params.put("num", num);
        ApiHttpClient.get(url(params),apiCallback);
    }

    private static String url(ContentValues params) {
        StringBuffer sb = new StringBuffer(BASE);
        Set<String> keys = params.keySet();
        for (String name : keys) {
            String value = String.valueOf(params.get(name));
            sb.append("&").append(name).append("=").append(value);
        }
        return sb.toString();
    }
}
