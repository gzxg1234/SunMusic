package com.sanron.ddmusic.db.bean;

import android.content.ContentValues;
import android.database.Cursor;

import com.sanron.ddmusic.db.DBHelper;

/**
 * Created by Administrator on 2015/12/28.
 */
public class Album {

    private Long id;
    private String name;
    private String artistName;
    private String picPath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public ContentValues toContentValues() {
        return null;
    }

    public static Album fromCursor(Cursor cursor) {
        Album album = new Album();
        album.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.ID)));
        album.setName(cursor.getString(cursor.getColumnIndex(DBHelper.Album.NAME)));
        album.setArtistName(cursor.getString(cursor.getColumnIndex(DBHelper.Album.ARTIST)));
        return album;
    }
}
