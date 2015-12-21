package com.sanron.sunmusic.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.utils.MyLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

/**
 * Created by Administrator on 2015/12/21.
 */
public class PlayListDao extends BaseDAO{

    public PlayListDao(Context context) {
        super(context);
    }

    public List<PlayList> queryByType(int type) {
        SQLiteDatabase database = getDatabase();
        List<PlayList> playLists = new ArrayList<>();
        Cursor cursor = database.query(DBHelper.TABLE_PLAYLIST,
                null,
                DBHelper.PLAYLIST_TYPE + "=?",
                new String[]{String.valueOf(type)},
                null, null, null);
        while(cursor.moveToNext()){
            PlayList playList = toPlayList(cursor);
            playLists.add(playList);
        }
        cursor.close();
        return playLists;
    }

    public long add(PlayList playList){
        SQLiteDatabase database = getDatabase();
        ContentValues values = toContentValues(playList);
        long id = database.insert(DBHelper.TABLE_PLAYLIST,null,values);
        if(id!=-1){
            playList.setId(id);
        }
        return id;
    }

    /**
     * 添加至列表
     */
    public long addToList(long songid, long listid){
        SQLiteDatabase database = getDatabase();
        database.beginTransaction();
        long id;
        try {
            ContentValues values = new ContentValues();
            values.put(DBHelper.LISTSONGS_LISTID, listid);
            values.put(DBHelper.LISTSONGS_SONGID, songid);
            id = database.insert(DBHelper.TABLE_LISTSONGS, null, values);
            if (id != -1) {
                String sql = "update " + DBHelper.TABLE_PLAYLIST
                        + " set " + DBHelper.PLAYLIST_NUM + "=" + DBHelper.PLAYLIST_NUM + "+1"
                        + " where " + DBHelper.ID + "=" + listid;
                database.execSQL(sql);
                database.setTransactionSuccessful();
            }
        }finally {
            database.endTransaction();
        }
        return id;
    }

    /**
     * 是否已经在列表中
     */
    public boolean isSongAddedToList(long songid, long listid){
        SQLiteDatabase database = getDatabase();
        Cursor cursor = database.query(DBHelper.TABLE_LISTSONGS,
                null,
                DBHelper.LISTSONGS_LISTID+"=? and "+DBHelper.LISTSONGS_SONGID+"=?",
                new String[]{listid+"",songid+""},
                null,null,null);
        boolean isAdded = cursor.moveToFirst();
        cursor.close();
        return isAdded;
    }

    public List<Long> queryListSongs(long listid){
        SQLiteDatabase database = getDatabase();
        List<Long> songidList = new ArrayList<>();
        Cursor cursor = database.query(DBHelper.TABLE_LISTSONGS,
                null,
                DBHelper.LISTSONGS_LISTID+"=?",
                new String[]{listid+""},
                null,null,null);
        while(cursor.moveToNext()){
            long songId = cursor.getInt(cursor.getColumnIndex(DBHelper.LISTSONGS_SONGID));
            songidList.add(songId);
        }
        cursor.close();
        return songidList;
    }

    public long delete(long id){
        SQLiteDatabase database = getDatabase();
        long num = database.delete(DBHelper.TABLE_PLAYLIST,DBHelper.ID+"=?",new String[]{String.valueOf(id)});
        return num;
    }

    private ContentValues toContentValues(PlayList playList){
        ContentValues values = new ContentValues();
        values.put(DBHelper.PLAYLIST_NAME,playList.getName());
        values.put(DBHelper.PLAYLIST_TYPE,playList.getType());
        values.put(DBHelper.PLAYLIST_NUM,playList.getSongNum());
        return values;
    }

    private PlayList toPlayList(Cursor cursor){
        PlayList playList = new PlayList();
        long id = cursor.getInt(cursor.getColumnIndex(DBHelper.PLAYLIST_TYPE));
        String name = cursor.getString(cursor.getColumnIndex(DBHelper.PLAYLIST_NAME));
        int songNum = cursor.getInt(cursor.getColumnIndex(DBHelper.PLAYLIST_NUM));
        playList.setId(id);
        playList.setName(name);
        playList.setSongNum(songNum);
        return playList;
    }
}
