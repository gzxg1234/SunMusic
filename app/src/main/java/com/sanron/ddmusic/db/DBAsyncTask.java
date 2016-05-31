package com.sanron.ddmusic.db;

import android.os.AsyncTask;

public abstract class DBAsyncTask<T> extends AsyncTask<Void, Void, T> {
    private ResultCallback<T> mCallback;

    public DBAsyncTask(ResultCallback<T> callback) {
        mCallback = callback;
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
        if (mCallback != null) {
            mCallback.onResult(t);
        }
    }
}