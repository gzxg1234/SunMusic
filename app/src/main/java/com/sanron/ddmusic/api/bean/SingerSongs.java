package com.sanron.ddmusic.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by sanron on 16-4-18.
 */
public class SingerSongs {

    /**
     * 歌曲总数
     */
    @JSONField(name = "songnums")
    public String songnums;

    @JSONField(name = "havemore")
    public int havemore;

    @JSONField(name = "error_code")
    public int errorCode;

    @JSONField(name = "songlist")
    public List<Song> songs;
}
