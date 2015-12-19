package com.sanron.sunmusic.model;

/**
 * Created by Administrator on 2015/12/19.
 */
public class SongInfo {

    public static final int TYPE_LOCAL = 1; //本地
    public static final int TYPE_WEB = 2;   //网络

    private int id = 0;

    /**
     * 类型
     */
    private int type = 0;

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
    private int duration = 0;

    /**
     * 拼音
     */
    private String pinyin;

    /**
     * 歌曲id(网络歌曲)
     */
    private String songId;


    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }
}
