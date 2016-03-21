package com.sanron.music.net.converter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

/**
 * Created by sanron on 16-3-19.
 */
public class SplitConverter implements Converter<String, String[]> {
    @Override
    public String[] convert(String value) {
        if (value != null) {
            return value.split(",");
        }
        return null;
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructType(String.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructArrayType(String.class);
    }
}
