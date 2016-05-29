package com.sanron.ddmusic.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sanron.ddmusic.db.bean.Music;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by sanron on 16-5-29.
 */
public class ListMemberHelper {

    public interface Columns {
        String TABLE = "list_member_datas";
        String MUSIC_ID = "music_id";
        String LIST_ID = "list_id";
        String ADD_TIME = "add_time";
    }

    public static void onCreate(SQLiteDatabase db) {
        Map<String, String> columnTypes = new LinkedHashMap<>();
        columnTypes.put(Columns.LIST_ID, "integer");
        columnTypes.put(Columns.MUSIC_ID, "integer");
        columnTypes.put(Columns.ADD_TIME, "integer");
        String sql = BaseHelper.buildCreateSql(Columns.TABLE, columnTypes);
        db.execSQL(sql);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static List<Music> getMusicsByListId(SQLiteDatabase db, long listid) {
        List<Music> musics = new LinkedList<>();
        Cursor cursor = db.query(Columns.TABLE,
                new String[]{Columns.MUSIC_ID},
                Columns.LIST_ID + "=" + listid, null, null, null, Columns.ADD_TIME);
        while (cursor.moveToNext()) {
            long musicId = cursor.getLong(0);
            Music music = MusicHelper.getMusicById(db, musicId);
            if (music != null) {
                musics.add(music);
            }
        }
        cursor.close();
        return musics;
    }

    public static int getMusicCountByListid(SQLiteDatabase db, long listid) {
        String sql = "select count(1) from " + Columns.TABLE
                + " where " + Columns.LIST_ID + "=?";
        Cursor c = db.rawQuery(sql, new String[]{listid + ""});
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        return count;
    }

    public static boolean isExistByMusicIdAndListId(SQLiteDatabase db, long listid, long musicid) {
        String sql = "select 1 from " + ListMemberHelper.Columns.TABLE
                + " where " + ListMemberHelper.Columns.LIST_ID + "=?"
                + " and " + ListMemberHelper.Columns.MUSIC_ID + "=?";
        Cursor c = db.rawQuery(sql, new String[]{listid + "", musicid + ""});
        boolean exist = c.moveToFirst();
        c.close();
        return exist;
    }

    public static int deleteByMusicId(SQLiteDatabase db, long musicid) {
        return db.delete(Columns.TABLE, Columns.MUSIC_ID + "=" + musicid, null);
    }

    public static int deleteByListId(SQLiteDatabase db, long listid) {
        return db.delete(Columns.TABLE, Columns.LIST_ID + "=" + listid, null);
    }

    public static int delete(SQLiteDatabase db, long listid, long musicid) {
        return db.delete(Columns.TABLE,
                Columns.MUSIC_ID + "=" + musicid + " and " + Columns.LIST_ID + "=" + listid, null);
    }


    public static long addMusicToList(SQLiteDatabase db, long musicid, long listid, long time) {
        ContentValues values = new ContentValues(3);
        values.put(Columns.ADD_TIME, time);
        values.put(Columns.MUSIC_ID, musicid);
        values.put(Columns.LIST_ID, listid);
        return db.insert(Columns.TABLE, null, values);
    }

}
