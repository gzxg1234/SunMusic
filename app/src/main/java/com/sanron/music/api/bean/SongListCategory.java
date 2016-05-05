package com.sanron.music.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by sanron on 16-5-5.
 */
public class SongListCategory {

    @JSONField(name = "error_code")
    public int errorCode;
    @JSONField(name = "content")
    public List<Content> content;

    public static class Content {
        @JSONField(name = "title")
        public String title;
        @JSONField(name = "num")
        public int num;
        @JSONField(name = "tags")
        public List<Tag> tags;

        public static class Tag {
            @JSONField(name = "tag")
            public String tag;
            @JSONField(name = "type")
            public String type;
        }
    }
}
