package com.sanron.music.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Json工具
 * Created by sanron on 16-3-19.
 */
public class JsonUtil {

    public static ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> T fromJson(String json,Class<T> clazz) throws IOException {
        return objectMapper.readValue(json,clazz);
    }

    public static <T> T fromJson(String json,JavaType type) throws IOException {
        return objectMapper.readValue(json,type);
    }

    public static <T> T fromJson(String json,TypeReference<T> typeReference) throws IOException {
        return objectMapper.readValue(json,typeReference);
    }

}
