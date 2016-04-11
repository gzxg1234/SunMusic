package com.sanron.music.net.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by sanron on 16-4-12.
 */
public class RecmdSongData {

    @JSONField(name = "error_code")
    public int errorCode;
    @JSONField(name = "content")
    public List<Content> content;

    public static class Content {
        @JSONField(name = "title")
        public String title;
        @JSONField(name = "song_list")
        public List<RecommendSong> songs;
    }
}
