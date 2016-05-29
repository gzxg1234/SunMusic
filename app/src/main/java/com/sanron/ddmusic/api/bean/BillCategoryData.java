package com.sanron.ddmusic.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by sanron on 16-4-24.
 */
public class BillCategoryData {

    @JSONField(name = "error_code")
    public int errorCode;

    @JSONField(name = "content")
    public List<BillCategory> billCategories;

    public static class BillCategory {

        @JSONField(name = "name")
        public String name;

        @JSONField(name = "type")
        public int type;

        @JSONField(name = "count")
        public int count;

        @JSONField(name = "comment")
        public String comment;

        @JSONField(name = "web_url")
        public String webUrl;

        //546x546
        @JSONField(name = "pic_s192")
        public String picS192;

        //444x260
        @JSONField(name = "pic_s444")
        public String picS444;

        //260x260
        @JSONField(name = "pic_s260")
        public String picS260;

        //210x130
        @JSONField(name = "pic_s210")
        public String picS210;

        @JSONField(name = "content")
        public List<TopSong> topSongs;

        public static class TopSong {
            @JSONField(name = "title")
            public String title;
            @JSONField(name = "author")
            public String author;
            @JSONField(name = "song_id")
            public String songId;
            @JSONField(name = "album_id")
            public String albumId;
            @JSONField(name = "album_title")
            public String albumTitle;
            @JSONField(name = "rank_change")
            public String rankChange;
            @JSONField(name = "all_rate")
            public String allRate;
        }
    }
}
