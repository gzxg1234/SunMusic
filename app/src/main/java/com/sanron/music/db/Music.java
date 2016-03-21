package com.sanron.music.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.sanron.music.service.Playable;

/**
 * Created by Administrator on 2015/12/19.
 */
public class Music extends Playable {

    public static final int TYPE_LOCAL = 1; //本地
    public static final int TYPE_WEB = 2;   //网络

    private long id = -1;

    /**
     * 类型
     */
    private int type = -1;

    /**
     * 文件路径
     */
    private String path;

    /**
     * 文件名
     */
    private String displayName;

    /**
     * 歌曲名
     */
    private String title;

    /**
     * 专辑
     */
    private String album;

    /**
     * 艺术家
     */
    private String artist;

    /**
     * 时长
     */
    private int duration = -1;

    /**
     * 比特率
     */
    private int bitrate = -1;


    /**
     * 首字母
     */
    private String letter;

    /**
     * 歌曲id(网络歌曲)
     */
    private String songId;

    /**
     * 歌曲图片
     *
     * @return
     */
    private String picPath;

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public static Music fromCursor(Cursor cursor) {
        Music music = new Music();
        music.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.ID)));
        music.setSongId(cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_MUSICID)));
        music.setType(cursor.getInt(cursor.getColumnIndex(DBHelper.MUSIC_TYPE)));
        music.setBitrate(cursor.getInt(cursor.getColumnIndex(DBHelper.MUSIC_BITRATE)));
        music.setAlbum(cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_ALBUMNAME)));
        music.setLetter(cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_LETTER)));
        music.setArtist(cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_ARTISTNAME)));
        music.setPath(cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_PATH)));
        music.setDuration(cursor.getInt(cursor.getColumnIndex(DBHelper.MUSIC_DURATION)));
        music.setDisplayName(cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_DISPLAYNAME)));
        music.setTitle(cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_TITLE)));
        music.setPicPath(cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_PIC)));
        return music;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        if (id != -1) {
            values.put(DBHelper.ID, id);
        }
        if (type != -1) {
            values.put(DBHelper.MUSIC_TYPE, type);
        }
        if (displayName != null) {
            values.put(DBHelper.MUSIC_DISPLAYNAME, displayName);
        }
        if (title != null) {
            values.put(DBHelper.MUSIC_TITLE, title);
        }
        if (album != null) {
            values.put(DBHelper.MUSIC_ALBUMNAME, album);
        }
        if (artist != null) {
            values.put(DBHelper.MUSIC_ARTISTNAME, artist);
        }
        if (duration != -1) {
            values.put(DBHelper.MUSIC_DURATION, duration);
        }
        if (path != null) {
            values.put(DBHelper.MUSIC_PATH, path);
        }
        if (letter != null) {
            values.put(DBHelper.MUSIC_LETTER, letter);
        }
        if (songId != null) {
            values.put(DBHelper.MUSIC_MUSICID, songId);
        }
        if (picPath != null) {
            values.put(DBHelper.MUSIC_PIC, picPath);
        }
        return values;
    }

    @Override
    public String url() {
        return path;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String album() {
        return album;
    }

    @Override
    public String artist() {
        return artist;
    }

    @Override
    public String pic() {
        return picPath;
    }
}
