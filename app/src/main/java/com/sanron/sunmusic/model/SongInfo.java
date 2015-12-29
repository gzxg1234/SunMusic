package com.sanron.sunmusic.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.sanron.sunmusic.db.DBHelper;

/**
 * Created by Administrator on 2015/12/19.
 */
public class SongInfo {

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

    public static SongInfo fromCursor(Cursor cursor) {
        SongInfo songInfo = new SongInfo();
        songInfo.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.ID)));
        songInfo.setSongId(cursor.getString(cursor.getColumnIndex(DBHelper.SONG_SONGID)));
        songInfo.setType(cursor.getInt(cursor.getColumnIndex(DBHelper.SONG_TYPE)));
        songInfo.setBitrate(cursor.getInt(cursor.getColumnIndex(DBHelper.SONG_BITRATE)));
        songInfo.setAlbum(cursor.getString(cursor.getColumnIndex(DBHelper.SONG_ALBUMNAME)));
        songInfo.setLetter(cursor.getString(cursor.getColumnIndex(DBHelper.SONG_LETTER)));
        songInfo.setArtist(cursor.getString(cursor.getColumnIndex(DBHelper.SONG_ARTISTNAME)));
        songInfo.setPath(cursor.getString(cursor.getColumnIndex(DBHelper.SONG_PATH)));
        songInfo.setDuration(cursor.getInt(cursor.getColumnIndex(DBHelper.SONG_DURATION)));
        songInfo.setDisplayName(cursor.getString(cursor.getColumnIndex(DBHelper.SONG_DISPLAYNAME)));
        songInfo.setTitle(cursor.getString(cursor.getColumnIndex(DBHelper.SONG_TITLE)));
        songInfo.setPicPath(cursor.getString(cursor.getColumnIndex(DBHelper.SONG_PIC)));
        return songInfo;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        if (id != -1) {
            values.put(DBHelper.ID, id);
        }
        if (type != -1) {
            values.put(DBHelper.SONG_TYPE, type);
        }
        if (displayName != null) {
            values.put(DBHelper.SONG_DISPLAYNAME, displayName);
        }
        if (title != null) {
            values.put(DBHelper.SONG_TITLE, title);
        }
        if (album != null) {
            values.put(DBHelper.SONG_ALBUMNAME, album);
        }
        if (artist != null) {
            values.put(DBHelper.SONG_ARTISTNAME, artist);
        }
        if (duration != -1) {
            values.put(DBHelper.SONG_DURATION, duration);
        }
        if (path != null) {
            values.put(DBHelper.SONG_PATH, path);
        }
        if (letter != null) {
            values.put(DBHelper.SONG_LETTER, letter);
        }
        if (songId != null) {
            values.put(DBHelper.SONG_SONGID, songId);
        }
        if (picPath != null) {
            values.put(DBHelper.SONG_PIC, picPath);
        }
        return values;
    }
}
