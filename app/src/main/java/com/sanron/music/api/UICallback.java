package com.sanron.music.api;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by sanron on 16-4-19.
 */
public abstract class UICallback<T> implements Callback {
    private static Handler sUiHandler = new Handler(Looper.getMainLooper());

    protected abstract T parser(Response response) throws IOException;

    @Override
    public void onFailure(Call call, final IOException e) {
        if (!"Canceled".equals(e.getMessage())) {
            sendFailure(e);
        }
    }

    private void sendFailure(final Exception e) {
        sUiHandler.post(new Runnable() {
            @Override
            public void run() {
                onFailure(e);
            }
        });
    }

    private void sendSuccess(final T t) {
        sUiHandler.post(new Runnable() {
            @Override
            public void run() {
                onSuccess(t);
            }
        });
    }

    @Override
    public void onResponse(Call call, final Response response) throws IOException {
        if (!response.isSuccessful()) {
            sendFailure(new Exception("response code " + response.code()));
        } else {
            try {
                final T t = parser(response);
                sendSuccess(t);
            } catch (final IOException e) {
                sendFailure(e);
            }
        }
    }

    public abstract void onSuccess(T t);

    public abstract void onFailure(Exception e);
}
