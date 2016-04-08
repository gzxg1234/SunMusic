package com.sanron.music.db.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.sanron.music.db.DBHelper;

/**
 * Created by Administrator on 2015/12/28.
 */
public class Artist {

    private Long id;
    private String name;
    private String picPath;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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


    public ContentValues toContentValues() {
        return null;
    }

    public static Artist fromCursor(Cursor cursor) {
        Artist artist = new Artist();
        artist.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.ID)));
        artist.setName(cursor.getString(cursor.getColumnIndex(DBHelper.Artist.NAME)));
        artist.setPicPath(cursor.getString(cursor.getColumnIndex(DBHelper.Artist.PIC)));
        return artist;
    }
}
