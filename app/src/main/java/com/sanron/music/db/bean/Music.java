package com.sanron.music.db.bean;

import android.content.ContentValues;
import android.database.Cursor;

import com.sanron.music.db.DBHelper;

/**
 * Created by Administrator on 2015/12/19.
 */
public class Music {


    private long id;


    /**
     * 名称
     */
    private String displayName;

    /**
     * 歌曲名
     */
    private String title;

    private String titleKey;

    /**
     * 专辑
     */
    private String album;

    /**
     * 艺术家
     */
    private String artist;

    /**
     * 文件路径
     */
    private String data;

    private long modifiedDate;


    /**
     * 时长
     */
    private int duration;

    /**
     * 歌曲网络id
     */
    private String songId;

    /**
     * 比特率
     */
    private int bitrate;

    public long getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(long modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }


    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(DBHelper.Music.SONG_ID, songId);
        values.put(DBHelper.Music.BITRATE, bitrate);
        values.put(DBHelper.Music.ALBUM, album);
        values.put(DBHelper.Music.DATE_MODIFIED, modifiedDate);
        values.put(DBHelper.Music.ARTIST, artist);
        values.put(DBHelper.Music.DATA, data);
        values.put(DBHelper.Music.DURATION, duration);
        values.put(DBHelper.Music.TITLE, title);
        values.put(DBHelper.Music.TITLE_KEY, titleKey);
        values.put(DBHelper.Music.DISPLAY, displayName);
        return values;
    }

    public static Music fromCursor(Cursor cursor) {
        Music music = new Music();
        music.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.ID)));
        music.setSongId(cursor.getString(cursor.getColumnIndex(DBHelper.Music.SONG_ID)));
        music.setBitrate(cursor.getInt(cursor.getColumnIndex(DBHelper.Music.BITRATE)));
        music.setAlbum(cursor.getString(cursor.getColumnIndex(DBHelper.Music.ALBUM)));
        music.setArtist(cursor.getString(cursor.getColumnIndex(DBHelper.Music.ARTIST)));
        music.setData(cursor.getString(cursor.getColumnIndex(DBHelper.Music.DATA)));
        music.setDuration(cursor.getInt(cursor.getColumnIndex(DBHelper.Music.DURATION)));
        music.setTitle(cursor.getString(cursor.getColumnIndex(DBHelper.Music.TITLE)));
        music.setTitleKey(cursor.getString(cursor.getColumnIndex(DBHelper.Music.TITLE_KEY)));
        music.setDisplayName(cursor.getString(cursor.getColumnIndex(DBHelper.Music.DISPLAY)));
        return music;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        Music m = (Music) o;
        long mId = m.getId();
        String mSongId = m.getSongId();
        return (id != 0 && id == mId)
                ||
                (songId != null && songId.length() != 0 && songId.equals(mSongId));

    }
}
