package com.sanron.music.db.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.sanron.music.db.DBHelper;

import java.io.Serializable;
import java.util.List;

/**
 * 播放列表
 * Created by Administrator on 2015/12/21.
 */
public class PlayList implements Serializable {


    private long id;

    /**
     * 表名
     */
    private String name;

    /**
     * 表类型
     */
    private int type;

    private List<Long> musicIds;

    public List<Long> getMusicIds() {
        return musicIds;
    }

    public void setMusicIds(List<Long> musicIds) {
        this.musicIds = musicIds;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static PlayList fromCursor(Cursor cursor) {
        PlayList playList = new PlayList();
        playList.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.ID)));
        playList.setName(cursor.getString(cursor.getColumnIndex(DBHelper.List.NAME)));
        playList.setType(cursor.getInt(cursor.getColumnIndex(DBHelper.List.TYPE)));
        return playList;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(DBHelper.List.TYPE, type);
        values.put(DBHelper.List.NAME, name);
        values.put(DBHelper.ID, id);
        return values;
    }
}

