package com.sanron.ddmusic.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

/**
 * Created by Administrator on 2015/12/21.
 */
public class DataProvider extends Observable {
    private DBHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private volatile static DataProvider mInstance;

    public static DataProvider get() {
        if (mInstance == null) {
            synchronized (DataProvider.class) {
                if (mInstance == null) {
                    mInstance = new DataProvider();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        mDbHelper = new DBHelper(context);
        mDatabase = mDbHelper.getWritableDatabase();
    }

    public class Access {
        private String mTableName;
        private List<Cursor> mCursors;
        private boolean mHasChanged;

        private Access(String table) {
            mTableName = table;
            mCursors = new LinkedList<>();
        }

        public Cursor rawQuery(String sql, String... selectionArgs) {
            Cursor cursor = mDatabase.rawQuery(sql, selectionArgs);
            mCursors.add(cursor);
            return cursor;
        }

        public Cursor query(String[] columns, String selection,
                            String[] selectionArgs, String groupBy, String having,
                            String orderBy, String limit) {
            Cursor cursor = mDatabase.query(mTableName,
                    columns,
                    selection,
                    selectionArgs,
                    groupBy,
                    having,
                    orderBy,
                    limit);
            mCursors.add(cursor);
            return cursor;
        }

        public Cursor query(String[] colunms, String selection, String[] selectionArgs) {
            return query(colunms, selection, selectionArgs, null, null, null, null);
        }

        public int bulkInsert(ContentValues... contentValues) {
            int num = 0;
            for (int i = 0; i < contentValues.length; i++) {
                if (mDatabase.insert(mTableName, null, contentValues[i]) != -1) {
                    num++;
                }
            }
            if (num != 0) {
                mHasChanged = true;
            }
            return num;
        }

        public long insert(String nullColumnHack, ContentValues values) {
            long id = mDatabase.insert(mTableName, null, values);
            if (id != -1) {
                mHasChanged = true;
            }
            return id;
        }

        public int delete(String where, String... whereArgs) {
            int num = mDatabase.delete(mTableName, where, whereArgs);
            if (num > 0) {
                mHasChanged = true;
            }
            return num;
        }

        public int update(ContentValues contentValues, String where, String... whereArgs) {
            int num = mDatabase.update(mTableName, contentValues, where, whereArgs);
            if (num > 0) {
                mHasChanged = true;
            }
            return num;
        }

        public void close() {
            for (int i = 0; i < mCursors.size(); i++) {
                mCursors.get(i).close();
            }
            synchronized (DataProvider.this) {
                if (mHasChanged) {
                    setChanged();
                    notifyObservers(mTableName);
                }
            }
        }
    }

    public SQLiteDatabase getDatabase() {
        if (mDatabase == null
                || !mDatabase.isOpen()) {
            mDatabase = mDbHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    public void beginTransaction() {
        mDatabase.beginTransaction();
    }

    public void setTransactionSuccessful() {
        mDatabase.setTransactionSuccessful();
    }

    public void endTransaction() {
        mDatabase.endTransaction();
    }

    public Access newAccess(String table) {
        return new Access(table);
    }
}