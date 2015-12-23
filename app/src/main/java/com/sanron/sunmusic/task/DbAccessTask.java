package com.sanron.sunmusic.task;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2015/12/22.
 */
public abstract class DBAccessTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        onPostData(result);
    }

    abstract protected void onPostData(Result result);
}
