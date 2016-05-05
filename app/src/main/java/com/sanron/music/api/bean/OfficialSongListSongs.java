package com.sanron.music.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by sanron on 16-5-5.
 */
public class OfficialSongListSongs {

    @JSONField(name = "name")
    public String name;
    @JSONField(name = "pic")
    public String pic;
    @JSONField(name = "createTime")
    public String createTime;
    @JSONField(name = "desc")
    public String desc;
    @JSONField(name = "code")
    public String code;
    @JSONField(name = "background")
    public String background;
    @JSONField(name = "url")
    public String url;
    @JSONField(name = "urltext")
    public String urltext;
    @JSONField(name = "ipadBackground")
    public String ipadBackground;
    @JSONField(name = "pic_s640")
    public String picS640;
    @JSONField(name = "refresh")
    public int refresh;
    @JSONField(name = "courl")
    public String courl;
    @JSONField(name = "list")
    public java.util.List<List> list;

    public static class List {
        @JSONField(name = "name")
        public String name;
        @JSONField(name = "desc")
        public String desc;
        @JSONField(name = "songList")
        public java.util.List<Song> songs;
    }
}
