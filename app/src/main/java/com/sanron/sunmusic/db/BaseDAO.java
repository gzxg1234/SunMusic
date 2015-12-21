package com.sanron.sunmusic.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Administrator on 2015/12/21.
 */
public abstract class BaseDAO {
    protected DBHelper mDbHelper;
    protected SQLiteDatabase mDatabase;
    public BaseDAO(Context context){
        mDbHelper = new DBHelper(context);
    }

    public SQLiteDatabase getDatabase(){
        if(mDatabase==null){
            mDatabase = mDbHelper.getWritableDatabase();
        }
        return mDatabase;
    }


    public void closeDatabase(){
        if(mDatabase!=null){
            mDatabase.close();
        }
    }
}
