package com.sanron.ddmusic.task;

import android.os.AsyncTask;

import com.sanron.ddmusic.db.DataProvider;

/**
 * Created by sanron on 16-3-29.
 */
public class DeleteTask extends AsyncTask<Void, Void, Integer> {

    private String table;
    private String where;
    private String[] whereArgs;
    private DeleteCallback callback;
    private DataProvider.Access access;

    public DeleteTask table(String table) {
        this.table = table;
        return this;
    }

    public DeleteTask where(String where) {
        this.where = where;
        return this;
    }

    public DeleteTask whereArgs(String... whereArgs) {
        this.whereArgs = whereArgs;
        return this;
    }

    public void execute(DeleteCallback callback){
        this.callback = callback;
        execute();
    }

    @Override
    protected Integer doInBackground(Void... params) {
        access = DataProvider.get().newAccess(table);
        return access.delete(where, whereArgs);
    }

    @Override
    protected void onPostExecute(Integer integer) {
        callback.onDeleteFinish(integer);
        access.close();
    }

    @Override
    protected void onPreExecute() {
        callback.onPreDelete();
    }

    public interface DeleteCallback {
        void onPreDelete();

        void onDeleteFinish(int deleteCount);
    }
}
