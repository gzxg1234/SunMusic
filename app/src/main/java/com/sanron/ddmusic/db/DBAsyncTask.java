package com.sanron.ddmusic.db;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public abstract class DBAsyncTask<T> extends AsyncTask<Void, Void, T> {
    private WeakReference<ResultCallback<T>> mCallbackRef;

    public DBAsyncTask(ResultCallback<T> callback) {
        mCallbackRef = new WeakReference<>(callback);
    }

    private DBAsyncTask() {
    }

    @Override
    protected T doInBackground(Void... params) {
        return run();
    }

    protected abstract T run();

    @Override
    protected void onPostExecute(T t) {
        ResultCallback<T> callback = mCallbackRef.get();
        if (callback != null) {
            callback.onResult(t);
        }
    }
}