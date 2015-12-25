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

    public int insert(long listid, Long... songids) {
        ContentValues[] valuesList = new ContentValues[songids.length];
        for (int i = 0; i < songids.length; i++) {
            ContentValues values = new ContentValues();
            values.put(DBHelper.LISTSONGS_LISTID, listid);
            values.put(DBHelper.LISTSONGS_SONGID, songids[i]);
            valuesList[i] = values;
        }
        return insert(valuesList);
    }

    public int delete(long listid, Long... songids) {
        StringBuffer where = new StringBuffer(DBHelper.LISTSONGS_LISTID).append("=? and (");
        String[] whereArgs = new String[songids.length + 1];
        whereArgs[0] = listid + "";
        if (songids.length > 0) {
            //删除数量歌曲
            for (int i = 0; i < songids.length; i++) {
                where.append(DBHelper.LISTSONGS_SONGID).append("=? or ");
                whereArgs[i + 1] = String.valueOf(songids[i]);
            }
            where.replace(where.length() - 4, where.length(), ")");
        } else {
            //清空列表
            where.replace(where.length() - 6, where.length(), "");
        }
        return delete(where.toString(), whereArgs);
    }



    public Long[] query(long listid, long songid) {
        ContentValues values = new ContentValues();
        if (listid != -1) {
            values.put(DBHelper.LISTSONGS_LISTID, listid);
        }
        if (songid != -1) {
            values.put(DBHelper.LISTSONGS_SONGID, songid);
        }
        List<Long> songids = new ArrayList<>();
        Cursor cursor = query(values);
        while (cursor.moveToNext()) {
            songids.add(cursor.getLong(cursor.getColumnIndex(DBHelper.LISTSONGS_SONGID)));
        }
        return songids.toArray(new Long[songids.size()]);
    }

}
