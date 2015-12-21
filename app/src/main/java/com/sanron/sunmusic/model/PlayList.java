package com.sanron.sunmusic.model;

import java.io.Serializable;
import java.util.List;

/**播放列表
 * Created by Administrator on 2015/12/21.
 */
public class PlayList implements Serializable{


    public static final int TYPE_DEFAULT = 1;//默认列表
    public static final int TYPE_USER = 2;//用户添加
    public static final int TYPE_RECENT = 3;//最近播放

    private long id;

    /**
     * 表名
     */
    private String name;

    /**
     * 表类型
     */
    private int type;

    /**
     * 音乐数量
     */
    private int songNum;

    /**
     * 歌曲
     */
    private List<SongInfo> songs;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<SongInfo> getSongs() {
        return songs;
    }

    public void setSongs(List<SongInfo> songs) {
        this.songs = songs;
    }

    public int getSongNum() {
        return songNum;
    }

    public void setSongNum(int songNum) {
        this.songNum = songNum;
    }
}
