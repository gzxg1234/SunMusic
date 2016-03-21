package com.sanron.music.net.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    private String gedanId;

    /**
     * 歌单标签
     */
    @JsonProperty("tag")
    private String tag;

    @JsonProperty("pic")
    private String pic;

    @JsonProperty("pic_300")
    private String pic300;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGedanId() {
        return gedanId;
    }

    public void setGedanId(String gedanId) {
        this.gedanId = gedanId;
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
