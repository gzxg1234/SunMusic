package com.sanron.ddmusic.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

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
    @JSONField(name = "list")
    public List<Song> songs;
}
