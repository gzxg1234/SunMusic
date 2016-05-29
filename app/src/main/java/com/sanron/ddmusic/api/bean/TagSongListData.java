package com.sanron.ddmusic.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by sanron on 16-5-5.
 */
public class TagSongListData {

    @JSONField(name = "havemore")
    public int havemore;
    @JSONField(name = "error_code")
    public int errorCode;
    @JSONField(name = "total")
    public int total;
    @JSONField(name = "content")
    public List<SongList> songLists;

}
