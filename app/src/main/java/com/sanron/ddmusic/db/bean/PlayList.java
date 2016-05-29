package com.sanron.ddmusic.db.bean;

import android.content.ContentValues;
import android.database.Cursor;

import com.sanron.ddmusic.db.BaseHelper;
import com.sanron.ddmusic.db.PlayListHelper;

import java.io.Serializable;

/**
 * 播放列表
 * Created by Administrator on 2015/12/21.
 */
public class PlayList implements Serializable {

    public static final int TYPE_LOCAL = 1;//本地音乐
    public static final int TYPE_RECENT = 2;//最近播放
    public static final int TYPE_FAVORITE = 3;//我喜欢
    public static final int TYPE_USER = 4;//用户自建
    public static final int TYPE_COLLECTION = 5;//收藏歌单

    public static final long TYPE_LOCAL_ID = 1;
    public static final long TYPE_RECENT_ID = 2;
    public static final long TYPE_FAVORITE_ID = 3;

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
        playList.setId(cursor.getLong(cursor.getColumnIndex(BaseHelper.ID)));
        playList.setTitle(cursor.getString(cursor.getColumnIndex(PlayListHelper.Columns.TITLE)));
        playList.setIcon(cursor.getString(cursor.getColumnIndex(PlayListHelper.Columns.ICON)));
        playList.setType(cursor.getInt(cursor.getColumnIndex(PlayListHelper.Columns.TYPE)));
        playList.setListId(cursor.getString(cursor.getColumnIndex(PlayListHelper.Columns.LIST_ID)));
        playList.setAddTime(cursor.getInt(cursor.getColumnIndex(PlayListHelper.Columns.ADD_TIME)));
        return playList;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(PlayListHelper.Columns.TYPE, type);
        values.put(PlayListHelper.Columns.TITLE, title);
        values.put(PlayListHelper.Columns.ICON, icon);
        values.put(PlayListHelper.Columns.LIST_ID, listId);
        values.put(PlayListHelper.Columns.ADD_TIME, addTime);
        return values;
    }
}

