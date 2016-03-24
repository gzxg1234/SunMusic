package com.sanron.music.db.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.sanron.music.db.DBHelper;

/**
 * Created by Administrator on 2015/12/19.
 */
public class Music {


    private long id;

    /**
     * 类型
     */
    private int type;

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

    private String albumKey;

    /**
     * 艺术家
     */
    private String artist;

    private String artistKey;

    /**
     * 文件路径
     */
    private String path;

    private long modifiedDate;


    /**
     * 时长
     */
    private int duration;

    /**
     * 歌曲id(网络歌曲)
     */
    private String songId;

    /**
     * 比特率
     */
    private Integer bitrate;

    /**
     * 歌词
     */
    private String lyric;

    /**
     * 歌曲图片
     *
     * @return
     */
    private String pic;

    public long getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(long modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    public Integer getBitrate() {
        return bitrate;
    }

    public void setBitrate(Integer bitrate) {
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

    public String getAlbumKey() {
        return albumKey;
    }

    public void setAlbumKey(String albumKey) {
        this.albumKey = albumKey;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getArtistKey() {
        return artistKey;
    }

    public void setArtistKey(String artistKey) {
        this.artistKey = artistKey;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }


    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }


    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(DBHelper.Music.SONG_ID, songId);
        values.put(DBHelper.Music.TYPE, type);
        values.put(DBHelper.Music.BITRATE, bitrate);
        values.put(DBHelper.Music.ALBUM, album);
        values.put(DBHelper.Music.DATE_MODIFIED, modifiedDate);
        values.put(DBHelper.Music.ALBUM_KEY, albumKey);
        values.put(DBHelper.Music.ARTIST, artist);
        values.put(DBHelper.Music.ARTIST_KEY, artistKey);
        values.put(DBHelper.Music.PATH, path);
        values.put(DBHelper.Music.DURATION, duration);
        values.put(DBHelper.Music.TITLE, title);
        values.put(DBHelper.Music.TITLE_KEY, titleKey);
        values.put(DBHelper.Music.DISPLAY, displayName);
        values.put(DBHelper.Music.PIC, pic);
        values.put(DBHelper.Music.LYRIC, lyric);
        return values;
    }

    public static Music fromCursor(Cursor cursor) {
        Music music = new Music();
        music.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.ID)));
        music.setSongId(cursor.getString(cursor.getColumnIndex(DBHelper.Music.SONG_ID)));
        music.setType(cursor.getInt(cursor.getColumnIndex(DBHelper.Music.TYPE)));
        music.setBitrate(cursor.getInt(cursor.getColumnIndex(DBHelper.Music.BITRATE)));
        music.setAlbum(cursor.getString(cursor.getColumnIndex(DBHelper.Music.ALBUM)));
        music.setAlbumKey(cursor.getString(cursor.getColumnIndex(DBHelper.Music.ALBUM_KEY)));
        music.setArtist(cursor.getString(cursor.getColumnIndex(DBHelper.Music.ARTIST)));
        music.setArtistKey(cursor.getString(cursor.getColumnIndex(DBHelper.Music.ARTIST_KEY)));
        music.setPath(cursor.getString(cursor.getColumnIndex(DBHelper.Music.PATH)));
        music.setDuration(cursor.getInt(cursor.getColumnIndex(DBHelper.Music.DURATION)));
        music.setTitle(cursor.getString(cursor.getColumnIndex(DBHelper.Music.TITLE)));
        music.setTitleKey(cursor.getString(cursor.getColumnIndex(DBHelper.Music.TITLE_KEY)));
        music.setDisplayName(cursor.getString(cursor.getColumnIndex(DBHelper.Music.DISPLAY)));
        music.setPic(cursor.getString(cursor.getColumnIndex(DBHelper.Music.PIC)));
        music.setLyric(cursor.getString(cursor.getColumnIndex(DBHelper.Music.LYRIC)));
        return music;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Music) {
            Music m = (Music) o;
            return m.id == id;
        }
        return false;
    }
}
