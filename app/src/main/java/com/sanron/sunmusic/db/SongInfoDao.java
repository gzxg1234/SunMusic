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
public class SongInfoDao  extends BaseDAO{

    public SongInfoDao(Context context) {
        super(context);
    }

    public SQLiteDatabase getDatabase(){
        if(mDatabase==null){
            mDatabase = mDbHelper.getWritableDatabase();
        }
        return mDatabase;
    }
    /**
     * 添加
     */
    public long add(SongInfo songInfo) {
        SQLiteDatabase database = getDatabase();
        ContentValues values = toContentValues(songInfo);
        long id = database.insert(DBHelper.TABLE_SONG, null, values);
        if (id != -1) {
            songInfo.setId(id);
        }
        return id;
    }

    public long add(List<SongInfo> songInfos) {
        SQLiteDatabase database = getDatabase();
        long num = 0;
        for (int i = 0; i < songInfos.size(); i++) {
            SongInfo songInfo = songInfos.get(i);
            ContentValues values = toContentValues(songInfo);
            long id = database.insert(DBHelper.TABLE_SONG, null, values);
            if (id != -1) {
                num++;
                songInfo.setId(id);
            }
        }
        return num;
    }

    /**
     * 删除本地歌曲
     */
    public long deleteLocal() {
        SQLiteDatabase database = getDatabase();
        int num = database.delete(DBHelper.TABLE_SONG, "type=?",
                new String[]{String.valueOf(SongInfo.TYPE_LOCAL)});
        return num;
    }

    /**
     * 所有歌曲
     */
    public List<SongInfo> queryAll() {
        List<SongInfo> songInfos = query(null,null);
        return songInfos;
    }

    public List<SongInfo> queryByType(int type) {
        List<SongInfo> songInfos = query(DBHelper.SONG_TYPE + "=?",new String[]{String.valueOf(SongInfo.TYPE_LOCAL)});
        return songInfos;
    }

    public SongInfo queryById(long id){
        List<SongInfo> songInfos = query(DBHelper.ID + "=?",new String[]{String.valueOf(id)});
        if(songInfos.size()>0){
            return songInfos.get(0);
        }
        return null;
    }

    public List<SongInfo> query(String selection,String[] selectionArgs){
        SQLiteDatabase database = getDatabase();
        List<SongInfo> songInfos = new ArrayList<>();
        Cursor cursor = database.query(DBHelper.TABLE_SONG,
                null,
                selection,
                selectionArgs,
                null, null, null);
        while (cursor.moveToNext()) {
            songInfos.add(toSongInfo(cursor));
        }
        cursor.close();
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
        songInfo.setLetter(cursor.getString(cursor.getColumnIndex(DBHelper.SONG_LETTER)));
        songInfo.setDisplayName(cursor.getString(cursor.getColumnIndex(DBHelper.SONG_DISPLAYNAME)));
        return songInfo;
    }

    private ContentValues toContentValues(SongInfo songInfo) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.SONG_TYPE, songInfo.getType());
        values.put(DBHelper.SONG_DISPLAYNAME, songInfo.getDisplayName());
        values.put(DBHelper.SONG_TITLE, songInfo.getTitle());
        values.put(DBHelper.SONG_ALBUM, songInfo.getAlbum());
        values.put(DBHelper.SONG_ARTIST, songInfo.getArtist());
        values.put(DBHelper.SONG_DURATION, songInfo.getDuration());
        values.put(DBHelper.SONG_PATH, songInfo.getPath());
        values.put(DBHelper.SONG_LETTER, songInfo.getLetter());
        values.put(DBHelper.SONG_SONGID, songInfo.getSongId());
        return values;
    }


}
