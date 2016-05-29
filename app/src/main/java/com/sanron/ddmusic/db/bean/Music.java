package com.sanron.ddmusic.db.bean;

import android.content.ContentValues;
import android.database.Cursor;

import com.sanron.ddmusic.db.BaseHelper;
import com.sanron.ddmusic.db.MusicHelper;

/**
 * Created by Administrator on 2015/12/19.
 */
public class Music {

    public static final int TYPE_LOCAL = 0;
    public static final int TYPE_WEB = 1;

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

    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

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
        values.put(MusicHelper.Columns.SONG_ID, songId);
        values.put(MusicHelper.Columns.BITRATE, bitrate);
        values.put(MusicHelper.Columns.ALBUM, album);
        values.put(MusicHelper.Columns.DATE_MODIFIED, modifiedDate);
        values.put(MusicHelper.Columns.ARTIST, artist);
        values.put(MusicHelper.Columns.DATA, data);
        values.put(MusicHelper.Columns.DURATION, duration);
        values.put(MusicHelper.Columns.TITLE, title);
        values.put(MusicHelper.Columns.TITLE_KEY, titleKey);
        values.put(MusicHelper.Columns.DISPLAY, displayName);
        values.put(MusicHelper.Columns.TYPE, type);
        return values;
    }

    public static Music fromCursor(Cursor cursor) {
        Music music = new Music();
        music.setId(cursor.getLong(cursor.getColumnIndex(BaseHelper.ID)));
        music.setSongId(cursor.getString(cursor.getColumnIndex(MusicHelper.Columns.SONG_ID)));
        music.setBitrate(cursor.getInt(cursor.getColumnIndex(MusicHelper.Columns.BITRATE)));
        music.setAlbum(cursor.getString(cursor.getColumnIndex(MusicHelper.Columns.ALBUM)));
        music.setArtist(cursor.getString(cursor.getColumnIndex(MusicHelper.Columns.ARTIST)));
        music.setData(cursor.getString(cursor.getColumnIndex(MusicHelper.Columns.DATA)));
        music.setDuration(cursor.getInt(cursor.getColumnIndex(MusicHelper.Columns.DURATION)));
        music.setTitle(cursor.getString(cursor.getColumnIndex(MusicHelper.Columns.TITLE)));
        music.setTitleKey(cursor.getString(cursor.getColumnIndex(MusicHelper.Columns.TITLE_KEY)));
        music.setDisplayName(cursor.getString(cursor.getColumnIndex(MusicHelper.Columns.DISPLAY)));
        music.setType(cursor.getInt(cursor.getColumnIndex(MusicHelper.Columns.TYPE)));
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
