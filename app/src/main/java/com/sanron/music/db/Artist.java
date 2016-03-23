package com.sanron.music.db;

import android.database.Cursor;

/**
 * Created by Administrator on 2015/12/28.
 */
public class Artist {

    private long id = -1;
    private String name;
    private int albumNum = -1;
    private String picPath;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAlbumNum() {
        return albumNum;
    }

    public void setAlbumNum(int albumNum) {
        this.albumNum = albumNum;
    }

    public static Artist fromCursor(Cursor cursor) {
        Artist artist = new Artist();
        artist.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.ID)));
        artist.setName(cursor.getString(cursor.getColumnIndex(DBHelper.ARTIST_NAME)));
        artist.setPicPath(cursor.getString(cursor.getColumnIndex(DBHelper.ARTIST_PIC)));
        return artist;
    }
}
