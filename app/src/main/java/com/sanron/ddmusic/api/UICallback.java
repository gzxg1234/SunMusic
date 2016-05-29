package com.sanron.ddmusic.api;

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
    private Handler mUIHandler = new Handler(Looper.getMainLooper());

    protected abstract T parser(Response response) throws IOException;

    @Override
    public void onFailure(Call call, final IOException e) {
        if (call.isCanceled()) {
            return;
        }
        sendFailure(e);
    }

    @Override
    public void onResponse(Call call, final Response response) throws IOException {
        if (call.isCanceled()) {
            return;
        }

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

    private void sendFailure(final Exception e) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                onFailure(e);
            }
        });
    }

    private void sendSuccess(final T t) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                onSuccess(t);
            }
        });
    }

    public abstract void onSuccess(T t);

    public abstract void onFailure(Exception e);
}
