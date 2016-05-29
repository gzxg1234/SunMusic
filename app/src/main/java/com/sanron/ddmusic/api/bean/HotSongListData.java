package com.sanron.ddmusic.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by sanron on 16-4-12.
 */
public class HotSongListData {

    @JSONField(name = "error_code")
    public int errorCode;
    @JSONField(name = "content")
    public Content content;

    public static class Content {
        @JSONField(name = "title")
        public String title;
        @JSONField(name = "list")
        public List<SongList> songLists;
    }
}
