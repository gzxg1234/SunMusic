package com.sanron.music.db;

import android.database.Cursor;

/**
 * Created by Administrator on 2015/12/28.
 */
public class Album {

    private long id = -1;
    private String name;
    private String artistName;
    private int songNum = -1;
    private String picPath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSongNum() {
        return songNum;
    }

    public void setSongNum(int songNum) {
        this.songNum = songNum;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public static Album fromCursor(Cursor cursor) {
        Album album = new Album();
        album.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.ID)));
        album.setName(cursor.getString(cursor.getColumnIndex(DBHelper.ALBUM_NAME)));
        album.setSongNum(cursor.getInt(cursor.getColumnIndex(DBHelper.ALBUM_MUSICNUM)));
        album.setArtistName(cursor.getString(cursor.getColumnIndex(DBHelper.ALBUM_ARTIST)));
        album.setPicPath(cursor.getString(cursor.getColumnIndex(DBHelper.ARTIST_PIC)));
        return album;
    }
}
