package com.sanron.music.net.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by sanron on 16-3-20.
 */
public class SongList {

    /**
     * 歌单名
     */
    @JsonProperty("title")
    private String title;

    /**
     * 歌单id
     */
    @JsonProperty("listid")
    private String listId;

    /**
     * 歌单标签
     */
    @JsonProperty("tag")
    private String tag;

    /**
     * 描述
     */
    @JsonProperty("desc")
    private String desc;

    /**
     * 图片
     */
    @JsonProperty("pic")
    private String pic;

    @JsonProperty("pic_300")
    private String pic300;

    @JsonProperty("pic_500")
    private String pic500;

    @JsonProperty("pic_w700")
    private String pic700;

    /**
     * 音乐
     */
    @JsonProperty("content")
    private List<Song> songs;

    /**
     * 链接
     */
    @JsonProperty("url")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPic500() {
        return pic500;
    }

    public String getPic700() {
        return pic700;
    }

    public void setPic700(String pic700) {
        this.pic700 = pic700;
    }

    public void setPic500(String pic500) {
        this.pic500 = pic500;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getPic300() {
        return pic300;
    }

    public void setPic300(String pic300) {
        this.pic300 = pic300;
    }
}
