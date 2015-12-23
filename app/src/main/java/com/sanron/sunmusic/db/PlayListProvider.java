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

    public List<PlayList> query(PlayList playList){
        ContentValues values = toContentValues(playList);
        List<PlayList> playLists = new ArrayList<>();
        Cursor cursor = query(values);
        while (cursor.moveToNext()){
            playLists.add(toPlayList(cursor));
        }
        cursor.close();
        return playLists;
    }

    public int insert(PlayList playList) {
        ContentValues values = toContentValues(playList);
        return insert(values);
    }

    public int delete(PlayList playList){
        ContentValues values = toContentValues(playList);
        return delete(values);
    }

    public int update(PlayList playList){
        ContentValues values = toContentValues(playList);
        return update(values,DBHelper.ID+"=?",playList.getId()+"");
    }

    private ContentValues toContentValues(PlayList playList) {
        ContentValues values = new ContentValues();
        if(playList.getType()!=-1){
            values.put(DBHelper.PLAYLIST_TYPE, playList.getType());
        }
        if(playList.getName()!=null){
            values.put(DBHelper.PLAYLIST_NAME, playList.getName());
        }
        if(playList.getSongNum()!=-1){
            values.put(DBHelper.PLAYLIST_NUM, playList.getSongNum());
        }
        if(playList.getId()!=-1){
            values.put(DBHelper.ID, playList.getId());
        }
        return values;
    }

    private PlayList toPlayList(Cursor cursor) {
        PlayList playList = new PlayList();
        long id = cursor.getInt(cursor.getColumnIndex(DBHelper.ID));
        String name = cursor.getString(cursor.getColumnIndex(DBHelper.PLAYLIST_NAME));
        int songNum = cursor.getInt(cursor.getColumnIndex(DBHelper.PLAYLIST_NUM));
        int type = cursor.getInt(cursor.getColumnIndex(DBHelper.PLAYLIST_TYPE));
        playList.setId(id);
        playList.setName(name);
        playList.setType(type);
        playList.setSongNum(songNum);
        return playList;
    }

}
