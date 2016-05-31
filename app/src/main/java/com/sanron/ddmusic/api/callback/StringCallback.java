package com.sanron.ddmusic.api.callback;

import java.io.IOException;

import okhttp3.Response;

/**
 * Created by sanron on 16-3-18.
 */

public abstract class StringCallback extends UICallback<String> {

    @Override
    protected String parser(Response response) throws IOException {
        return response.body().string();
    }
}
