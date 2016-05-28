package com.sanron.music.task;

import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.music.db.DataProvider;

/**
 * Created by sanron on 16-3-27.
 */

public class QueryTask extends AsyncTask<Void, Void, Cursor> {

    private String table;
    private String[] columns;
    private String selection;
    private String[] selectionArgs;
    private String groupBy;
    private String having;
    private String orderBy;
    private String limit;
    private DataProvider.Access access;
    private QueryCallback callback;

    public QueryTask table(String table) {
        this.table = table;
        return this;
    }

    public QueryTask columns(String... columns) {
        this.columns = columns;
        return this;
    }

    public QueryTask selection(String selection) {
        this.selection = selection;
        return this;
    }

    public QueryTask selectionArgs(String... selectionArgs) {
        this.selectionArgs = selectionArgs;
        return this;
    }

    public QueryTask groupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public QueryTask having(String having) {
        this.having = having;
        return this;
    }

    public QueryTask orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public QueryTask limit(String limit) {
        this.limit = limit;
        return this;
    }

    public void execute(QueryCallback callback){
        this.callback = callback;
        execute();
    }

    @Override
    protected Cursor doInBackground(Void... params) {
        access = DataProvider.get().newAccess(table);
        return access.query(columns,
                        selection,
                        selectionArgs,
                        groupBy,having,orderBy,limit);
    }

    @Override
    protected void onPreExecute() {
        callback.onPreQuery();
    }

    @Override
    protected void onPostExecute(Cursor cursor) {
        callback.onQueryFinish(cursor);
        access.close();
    }

    public interface QueryCallback {
        void onPreQuery();

        void onQueryFinish(Cursor cursor);
    }
}
