package com.sanron.music.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by sanron on 16-3-20.
 */
public class SongList {

    /**
     * 歌单名
     */
    @JSONField(name = "title")
    public String title;

    /**
     * 歌单id
     */
    @JSONField(name = "listid")
    public String listId;

    /**
     * 歌单标签
     */
    @JSONField(name = "tag")
    public String tag;

    /**
     * 描述
     */
    @JSONField(name = "desc")
    public String desc;

    /**
     * 图片
     */
    @JSONField(name = "pic")
    public String pic;

    @JSONField(name = "pic_300")
    public String pic300;

    @JSONField(name = "pic_500")
    public String pic500;

    @JSONField(name = "pic_w700")
    public String picW700;

    /**
     * 链接
     */
    @JSONField(name = "url")
    public String url;

    /**
     * 音乐
     */
    @JSONField(name = "content")
    public List<Song> songs;

}
