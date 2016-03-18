package com.sanron.sunmusic.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Json工具
 * Created by sanron on 16-3-19.
 */
public class JsonUtils {

    public static ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> T fromJson(String json,Class<T> type) throws IOException {
        return objectMapper.readValue(json,type);
    }
}
