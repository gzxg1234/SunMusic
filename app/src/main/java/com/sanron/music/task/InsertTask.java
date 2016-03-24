package com.sanron.music.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.music.db.DataProvider;

/**
 * Created by sanron on 16-3-29.
 */
public class InsertTask extends AsyncTask<Void, Void, Integer> {

    private ContentValues contentValues;
    private String table;
    private DataProvider.Access access;
    private InsertCallback callback;

    public InsertTask table(String table) {
        this.table = table;
        return this;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        access = DataProvider.instance().getAccess(table);
        return null;
    }


    public interface InsertCallback {
        void onPreInsert();

        void onInsertFinish(Cursor cursor);
    }
}
