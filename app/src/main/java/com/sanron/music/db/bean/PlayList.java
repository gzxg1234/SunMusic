package com.sanron.music.db.bean;

import android.content.ContentValues;
import android.database.Cursor;

import com.sanron.music.db.DBHelper;

import java.io.Serializable;

/**
 * 播放列表
 * Created by Administrator on 2015/12/21.
 */
public class PlayList implements Serializable {


    private long id;

    /**
     * 表名
     */
    private String title;

    /**
     * 表类型
     */
    private int type;

    private long addTime;

    private String icon;

    private String listId;

    private int songNum;

    public int getSongNum() {
        return songNum;
    }

    public void setSongNum(int songNum) {
        this.songNum = songNum;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
        playList.setTitle(cursor.getString(cursor.getColumnIndex(DBHelper.List.TITLE)));
        playList.setIcon(cursor.getString(cursor.getColumnIndex(DBHelper.List.ICON)));
        playList.setType(cursor.getInt(cursor.getColumnIndex(DBHelper.List.TYPE)));
        playList.setListId(cursor.getString(cursor.getColumnIndex(DBHelper.List.LIST_ID)));
        playList.setAddTime(cursor.getInt(cursor.getColumnIndex(DBHelper.List.ADD_TIME)));
        return playList;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(DBHelper.List.TYPE, type);
        values.put(DBHelper.List.TITLE, title);
        values.put(DBHelper.List.ICON, icon);
        values.put(DBHelper.List.LIST_ID, listId);
        values.put(DBHelper.List.ADD_TIME, addTime);
        return values;
    }
}

