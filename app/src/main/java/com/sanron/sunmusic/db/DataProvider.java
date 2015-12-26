package com.sanron.sunmusic.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Map;
import java.util.Observable;
import java.util.Set;

/**
 * Created by Administrator on 2015/12/21.
 */
public abstract class DataProvider extends Observable {
    protected DBHelper mDbHelper;
    protected String mTableName;

    public DataProvider(String tableName) {
        mTableName = tableName;
    }

    public void init(Context context) {
        mDbHelper = new DBHelper(context);
    }

    public Cursor query(String selection, String... selectionArgs) {
        return query(null, selection, selectionArgs);
    }

    public Cursor query(String[] columns, ContentValues values) {
        StringBuffer selection = new StringBuffer();
        String[] selectionArgs = new String[values.size()];
        Set<Map.Entry<String, Object>> set = values.valueSet();
        int i = 0;
        for (Map.Entry<String, Object> entry : set) {
            selection.append(entry.getKey()).append("=? and ");
            selectionArgs[i++] = String.valueOf(entry.getValue());
        }
        selection.replace(selection.length() - 5, selection.length(), "");
        return query(columns, selection.toString(), selectionArgs);
    }

    public Cursor query(ContentValues values) {
        return query(null, values);
    }

    public Cursor query(String[] colunms, String selection, String... selectionArgs) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor = database.query(mTableName,
                colunms,
                selection,
                selectionArgs,
                null, null, null);
        return cursor;
    }

    public int insert(ContentValues... contentValues) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int num = 0;
        for (int i = 0; i < contentValues.length; i++) {
            if (database.insert(mTableName, null, contentValues[i]) != -1) {
                num++;
            }
        }
        if (num != 0) {
            setChanged();
        }
        database.close();
        return num;
    }

    public int delete(String where, String... whereArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int num = database.delete(mTableName, where, whereArgs);
        if (num > 0) {
            setChanged();
        }
        database.close();
        return num;
    }

    public int delete(ContentValues values) {
        StringBuffer where = new StringBuffer();
        String[] whereArgs = new String[values.size()];
        Set<Map.Entry<String, Object>> set = values.valueSet();
        int i = 0;
        for (Map.Entry<String, Object> entry : set) {
            where.append(entry.getKey()).append("=? and ");
            whereArgs[i++] = String.valueOf(entry.getValue());
        }
        where.replace(where.length() - 5, where.length(), "");
        return delete(where.toString(), whereArgs);
    }

    public int update(ContentValues contentValues, String where, String... whereArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int num = database.update(mTableName, contentValues, where, whereArgs);
        if (num > 0) {
            setChanged();
        }
        database.close();
        return num;
    }

    public void execSQL(String sql, String... args) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        database.execSQL(sql, args);
        database.close();
    }

    public void notifyDataChanged(){
        setChanged();
        notifyObservers();
    }
}
