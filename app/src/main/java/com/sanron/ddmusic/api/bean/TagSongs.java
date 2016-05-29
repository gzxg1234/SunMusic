package com.sanron.ddmusic.api.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;

/**
 * Created by sanron on 16-4-12.
 */
public class TagSongs {

    @JSONField(name = "taginfo")
    public Taginfo taginfo;

    @JSONField(name = "error_code")
    public int errorCode;

    public static class Taginfo {
        /**
         * 歌曲总数
         */
        @JSONField(name = "count")
        public int count;
        /**
         * 是否有更多
         */
        @JSONField(name = "havemore")
        public int havemore;

        /**
         * 歌曲
         */
        @JSONField(name = "songlist", serialzeFeatures = {SerializerFeature.WriteNullListAsEmpty})
        public List<Song> songs;
    }
}
