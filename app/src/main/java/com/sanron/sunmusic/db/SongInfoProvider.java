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

    public int insert(SongInfo songInfo) {
        List<SongInfo> songInfos = new ArrayList<>();
        songInfos.add(songInfo);
        return insert(songInfos);
    }

    public int insert(List<SongInfo> songInfos) {
        ContentValues[] valuesList = new ContentValues[songInfos.size()];
        for (int i = 0; i < songInfos.size(); i++) {
            SongInfo songInfo = songInfos.get(i);
            valuesList[i] = toContentValues(songInfo);
        }
        return insert(valuesList);
    }

    public int delete(SongInfo songInfo){
        ContentValues values = toContentValues(songInfo);
        return delete(values);
    }

    public List<SongInfo> query(SongInfo songInfo){
        ContentValues values = toContentValues(songInfo);
        Cursor cursor = query(values);
        List<SongInfo> songInfos = new ArrayList<>();
        while (cursor.moveToNext()){
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
        songInfo.setBitrate(cursor.getInt(cursor.getColumnIndex(DBHelper.SONG_BITRATE)));
        songInfo.setDisplayName(cursor.getString(cursor.getColumnIndex(DBHelper.SONG_DISPLAYNAME)));
        return songInfo;
    }

    private ContentValues toContentValues(SongInfo songInfo) {
        ContentValues values = new ContentValues();
        if(songInfo.getId()!=-1) {
            values.put(DBHelper.ID, songInfo.getId());
        }
        if(songInfo.getType()!=-1) {
            values.put(DBHelper.SONG_TYPE, songInfo.getType());
        }
        if(songInfo.getDisplayName()!=null) {
            values.put(DBHelper.SONG_DISPLAYNAME, songInfo.getDisplayName());
        }
        if(songInfo.getTitle()!=null) {
            values.put(DBHelper.SONG_TITLE, songInfo.getTitle());
        }
        if(songInfo.getAlbum()!=null) {
            values.put(DBHelper.SONG_ALBUM, songInfo.getAlbum());
        }
        if(songInfo.getArtist()!=null) {
            values.put(DBHelper.SONG_ARTIST, songInfo.getArtist());
        }
        if(songInfo.getDuration()!=-1) {
            values.put(DBHelper.SONG_DURATION, songInfo.getDuration());
        }
        if(songInfo.getPath()!=null) {
            values.put(DBHelper.SONG_PATH, songInfo.getPath());
        }
        if(songInfo.getLetter()!=null) {
            values.put(DBHelper.SONG_LETTER, songInfo.getLetter());
        }
        if(songInfo.getSongId()!=null) {
            values.put(DBHelper.SONG_SONGID, songInfo.getSongId());
        }
        if(songInfo.getBitrate()!=-1) {
            values.put(DBHelper.SONG_BITRATE, songInfo.getBitrate());
        }
        return values;
    }
}
