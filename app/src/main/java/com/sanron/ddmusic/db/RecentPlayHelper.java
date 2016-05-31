package com.sanron.ddmusic.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by sanron on 16-5-31.
 */
public class RecentPlayHelper {

    public static final int MAX_SIZE = 100;//最多存储100条最近播放

    public static void onCreate(SQLiteDatabase db) {
        Map<String, String> columnTypes = new LinkedHashMap<>();
        columnTypes.put(Columns.MUSIC_ID, "integer");
        columnTypes.put(Columns.PLAY_TIME, "integer");
        String sql = BaseHelper.buildCreateSql(Columns.TABLE, columnTypes);
        db.execSQL(sql);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static long add(SQLiteDatabase db, long time, long id) {
        ContentValues values = new ContentValues(2);
        values.put(Columns.MUSIC_ID, id);
        values.put(Columns.PLAY_TIME, time);
        return db.insert(Columns.TABLE, null, values);
    }

    public static int updateTime(SQLiteDatabase db, long musicId, long time) {
        ContentValues values = new ContentValues(1);
        values.put(Columns.PLAY_TIME, time);
        return db.update(Columns.TABLE, values, BaseHelper.ID + "=" + musicId, null);
    }

    public static long getIdByMusicId(SQLiteDatabase db, long musicId) {
        long id = -1;
        Cursor cursor = db.query(Columns.TABLE,
                new String[]{BaseHelper.ID},
                Columns.MUSIC_ID + "=" + musicId, null, null, null, null);
        if (cursor.moveToFirst()) {
            id = cursor.getLong(0);
        }
        cursor.close();
        return id;
    }


    public static void deleteFarthest(SQLiteDatabase db) {
        String sql = "delete from " + Columns.TABLE
                + " where " + Columns.PLAY_TIME
                + "= (select min(" + Columns.PLAY_TIME + ") from " + Columns.TABLE + ")";
        db.execSQL(sql);
    }

    public static int deleteAll(SQLiteDatabase db) {
        return db.delete(Columns.TABLE, null, null);
    }

    public static int getCount(SQLiteDatabase db) {
        int count = 0;
        String sql = "select count(1) from " + Columns.TABLE;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public interface Columns {
        String TABLE = "recent_play";
        String MUSIC_ID = "music_id";
        String PLAY_TIME = "play_time";
    }
}
