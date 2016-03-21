package com.sanron.music.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

/**
 * Created by Administrator on 2015/12/21.
 */
public class DataProvider extends Observable {
    private DBHelper mDbHelper;
    private Set<String> changedTable;
    private static DataProvider mInstance;

    public static DataProvider instance(){
        if(mInstance == null){
            synchronized (DataProvider.class){
                if(mInstance == null){
                    mInstance = new DataProvider();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        mDbHelper = new DBHelper(context);
        changedTable = new HashSet<>();
    }

    public class Access{
        private SQLiteDatabase mDatabase;
        private String mTableName;
        private List<Cursor> mCursors;
        private Access(String table){
            mTableName = table;
            mDatabase = mDbHelper.getWritableDatabase();
            mCursors = new ArrayList<>();
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

        public Cursor query(String[] colunms, String selection, String[] selectionArgs) {
            Cursor cursor = mDatabase.query(mTableName,
                    colunms,
                    selection,
                    selectionArgs,
                    null, null, null);
            mCursors.add(cursor);
            return cursor;
        }

        public int blukInsert(ContentValues... contentValues) {
            int num = 0;
            for (int i = 0; i < contentValues.length; i++) {
                if (mDatabase.insert(mTableName, null, contentValues[i]) != -1) {
                    num++;
                }
            }
            if (num != 0) {
                changedTable.add(mTableName);
            }
            return num;
        }

        public long insert(ContentValues contentValues){
            long id = mDatabase.insert(mTableName,null,contentValues);
            if (id != -1) {
                changedTable.add(mTableName);
            }
            return id;
        }

        public int delete(String where, String... whereArgs) {
            int num = mDatabase.delete(mTableName, where, whereArgs);
            if (num > 0) {
                changedTable.add(mTableName);
            }
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
            int num = mDatabase.update(mTableName, contentValues, where, whereArgs);
            if (num > 0) {
                changedTable.add(mTableName);
            }
            return num;
        }

        public void execSQL(String sql, String... args) {
            SQLiteDatabase database = mDbHelper.getWritableDatabase();
            database.execSQL(sql, args);
            database.close();
        }

        public void close(){
            mDatabase.close();
            for(int i=0;i<mCursors.size(); i++){
                mCursors.get(i).close();
            }
            if(changedTable.size() > 0){
                setChanged();
                notifyObservers(mTableName);
                changedTable.remove(mTableName);
            }
        }
    }

    public Access getAccess(String table){
        return new Access(table);
    }

}
