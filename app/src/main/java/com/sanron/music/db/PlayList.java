package com.sanron.music.db;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;
import java.util.List;

/**
 * 播放列表
 * Created by Administrator on 2015/12/21.
 */
public class PlayList implements Serializable {


    public static final int TYPE_FAVORITE = 1;//我喜欢
    public static final int TYPE_RECENT = 2;//最近播放
    public static final int TYPE_USER = 3;//用户添加

    private long id = -1;

    /**
     * 表名
     */
    private String name;

    /**
     * 表类型
     */
    private int type = -1;

    /**
     * 音乐数量
     */
    private int songNum = -1;

    /**
     * 歌曲
     */
    private List<Music> songs;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<Music> getSongs() {
        return songs;
    }

    public void setSongs(List<Music> songs) {
        this.songs = songs;
    }

    public int getSongNum() {
        return songNum;
    }

    public void setSongNum(int songNum) {
        this.songNum = songNum;
    }

    public static PlayList fromCursor(Cursor cursor) {
        PlayList playList = new PlayList();
        playList.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.ID)));
        playList.setSongNum(cursor.getInt(cursor.getColumnIndex(DBHelper.PLAYLIST_MUSICNUM)));
        playList.setName(cursor.getString(cursor.getColumnIndex(DBHelper.PLAYLIST_NAME)));
        playList.setType(cursor.getInt(cursor.getColumnIndex(DBHelper.PLAYLIST_TYPE)));
        return playList;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        if (type != -1) {
            values.put(DBHelper.PLAYLIST_TYPE, type);
        }
        if (name != null) {
            values.put(DBHelper.PLAYLIST_NAME, name);
        }
        if (songNum != -1) {
            values.put(DBHelper.PLAYLIST_MUSICNUM, songNum);
        }
        if (id != -1) {
            values.put(DBHelper.ID, id);
        }
        return values;
    }
}

