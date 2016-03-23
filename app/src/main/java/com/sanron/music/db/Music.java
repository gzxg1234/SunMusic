package com.sanron.music.db;

import android.database.Cursor;

import com.sanron.music.service.Playable;

/**
 * Created by Administrator on 2015/12/19.
 */
public class Music extends Playable {

    public static final int TYPE_LOCAL = 1; //本地
    public static final int TYPE_WEB = 2;   //网络

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
    private int bitrate;

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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
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

    public static Music fromCursor(Cursor cursor) {
        Music music = new Music();
        music.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.ID)));
        music.setSongId(cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_SONGID)));
        music.setType(cursor.getInt(cursor.getColumnIndex(DBHelper.MUSIC_TYPE)));
        music.setBitrate(cursor.getInt(cursor.getColumnIndex(DBHelper.MUSIC_BITRATE)));
        music.setAlbum(cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_ALBUM)));
        music.setArtist(cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_ARTIST)));
        music.setPath(cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_PATH)));
        music.setDuration(cursor.getInt(cursor.getColumnIndex(DBHelper.MUSIC_DURATION)));
        music.setTitle(cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_TITLE)));
        music.setPic(cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_PIC)));
        return music;
    }

    @Override
    public String uri() {
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
        return pic;
    }

    @Override
    public int type() {
        return Playable.TYPE_FILE;
    }
}
