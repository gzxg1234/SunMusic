package com.sanron.sunmusic.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sanron.sunmusic.model.SongInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2015/12/20.
 */
public class SongInfoProvider extends DataProvider {


    private SongInfoProvider() {
        super(DBHelper.TABLE_SONG);
    }

    private static volatile SongInfoProvider mInstance = null;
    public static SongInfoProvider instance() {
        if (mInstance == null) {
            synchronized (SongInfoProvider.class) {
                if (mInstance == null) {
                    mInstance = new SongInfoProvider();
                }
            }
        }
        return mInstance;
    }

}
