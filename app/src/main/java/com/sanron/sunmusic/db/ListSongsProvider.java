package com.sanron.sunmusic.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/23.
 */
public class ListSongsProvider extends DataProvider {

    private ListSongsProvider() {
        super(DBHelper.TABLE_LISTSONGS);
    }

    private static volatile ListSongsProvider mInstance = null;

    public static ListSongsProvider instance() {
        if (mInstance == null) {
            synchronized (ListSongsProvider.class) {
                if (mInstance == null) {
                    mInstance = new ListSongsProvider();
                }
            }
        }
        return mInstance;
    }

}
