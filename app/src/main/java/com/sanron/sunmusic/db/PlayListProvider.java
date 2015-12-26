package com.sanron.sunmusic.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.Entity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2015/12/21.
 */
public class PlayListProvider extends DataProvider {

    private PlayListProvider() {
        super(DBHelper.TABLE_PLAYLIST);
    }

    private static volatile PlayListProvider mInstance = null;
    public static PlayListProvider instance() {
        if (mInstance == null) {
            synchronized (PlayListProvider.class) {
                if (mInstance == null) {
                    mInstance = new PlayListProvider();
                }
            }
        }
        return mInstance;
    }

}
