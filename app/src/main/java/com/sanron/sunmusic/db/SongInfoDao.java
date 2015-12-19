package com.sanron.sunmusic.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sanron.sunmusic.model.SongInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/20.
 */
public class SongInfoDao {

    private DBHelper mDbHelper;

    public SongInfoDao(Context context) {
        mDbHelper = new DBHelper(context);
    }

    /**
     * 添加
     */
    public long add(SongInfo songInfo) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        ContentValues values = toContentValues(songInfo);
        long id = database.insert(DBHelper.SONG_TABLE, null, values);
        database.close();
        return id;
    }

    public long add(List<SongInfo> songInfos) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long num = 0;
        for (int i = 0; i < songInfos.size(); i++) {
            SongInfo songInfo = songInfos.get(i);
            ContentValues values = toContentValues(songInfo);
            if (database.insert(DBHelper.SONG_TABLE, null, values) != -1) {
                num++;
            }
        }
        database.close();
        return num;
    }

    /**
     * 删除本地歌曲
     */
    public long deleteLocal() {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int num = database.delete(DBHelper.SONG_TABLE, "type=?",
                new String[]{String.valueOf(SongInfo.TYPE_LOCAL)});
        database.close();
        return num;
    }

    /**
     * 所有歌曲
     */
    public List<SongInfo> queryAll() {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        List<SongInfo> songInfos = new ArrayList<>();
        Cursor cursor = database.query(DBHelper.SONG_TABLE, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            songInfos.add(toSongInfo(cursor));
        }
        database.close();
        return songInfos;
    }

    public List<SongInfo> queryByType(int type) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        List<SongInfo> songInfos = new ArrayList<>();
        Cursor cursor = database.query(DBHelper.SONG_TABLE,
                null,
                DBHelper.SONG_TYPE + "=?",
                new String[]{String.valueOf(SongInfo.TYPE_LOCAL)},
                null, null, null);

        while (cursor.moveToNext()) {
            songInfos.add(toSongInfo(cursor));
        }
        database.close();
        return songInfos;
    }


    private SongInfo toSongInfo(Cursor cursor) {
        SongInfo songInfo = new SongInfo();
        songInfo.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.ID)));
        songInfo.setTitle(cursor.getString(cursor.getColumnIndex(DBHelper.SONG_TITLE)));
        songInfo.setType(cursor.getInt(cursor.getColumnIndex(DBHelper.SONG_TYPE)));
        songInfo.setPath(cursor.getString(cursor.getColumnIndex(DBHelper.SONG_PATH)));
        songInfo.setDuration(cursor.getInt(cursor.getColumnIndex(DBHelper.SONG_DURATION)));
        songInfo.setAlbum(cursor.getString(cursor.getColumnIndex(DBHelper.SONG_ALBUM)));
        songInfo.setArtist(cursor.getString(cursor.getColumnIndex(DBHelper.SONG_ARTIST)));
        songInfo.setSongId(cursor.getString(cursor.getColumnIndex(DBHelper.SONG_SONGID)));
        songInfo.setPinyin(cursor.getString(cursor.getColumnIndex(DBHelper.SONG_PINYIN)));
        songInfo.setDisplayName(cursor.getString(cursor.getColumnIndex(DBHelper.SONG_DISPLAYNAME)));
        return songInfo;
    }

    private ContentValues toContentValues(SongInfo songInfo) {
        ContentValues values = new ContentValues();
        values.put("type", songInfo.getType());
        values.put("display_name", songInfo.getDisplayName());
        values.put("title", songInfo.getTitle());
        values.put("album", songInfo.getAlbum());
        values.put("artist", songInfo.getArtist());
        values.put("duration", songInfo.getDuration());
        values.put("path", songInfo.getPath());
        values.put("pinyin", songInfo.getPinyin());
        values.put("songid", songInfo.getSongId());
        return values;
    }
}
