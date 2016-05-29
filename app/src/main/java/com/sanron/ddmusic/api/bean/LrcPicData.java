package com.sanron.ddmusic.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by sanron on 16-4-10.
 */
public class LrcPicData {

    @JSONField(name = "songinfo")
    public List<LrcPic> lrcPics;

    public static class LrcPic {
        /**
         * 歌词
         */
        @JSONField(name = "lrclink")
        public String lrc;

        @JSONField(name = "song_id")
        public String songId;

        @JSONField(name = "author")
        public String author;

        @JSONField(name = "song_title")
        public String title;

        /**
         * 艺术家图片
         */
        @JSONField(name = "artist_480_480")
        public String artist480x480;
        @JSONField(name = "artist_640_1136")
        public String artist640x1136;
        @JSONField(name = "artist_1000_1000")
        public String artist1000x1000;

        /**
         * 歌曲相关图片
         */
        @JSONField(name = "pic_s180")
        public String pic180x180;
        @JSONField(name = "pic_s500")
        public String pic500x500;
        @JSONField(name = "pic_s1000")
        public String pic1000x1000;

        /**
         * 头像
         */
        @JSONField(name = "avatar_s180")
        public String avatar180x180;
        @JSONField(name = "avatar_s500")
        public String avatar500x500;

    }
}
