package com.sanron.ddmusic.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by sanron on 16-4-24.
 */
public class BillSongList {

    @JSONField(name = "billboard")
    public Billboard billboard;

    @JSONField(name = "error_code")
    public int errorCode;

    @JSONField(name = "song_list")
    public List<RankSong> songs;

    public static class Billboard {
        @JSONField(name = "billboard_type")
        public String billboardType;
        @JSONField(name = "billboard_no")
        public String billboardNo;
        /**
         * 更新时间
         */
        @JSONField(name = "update_date")
        public String updateDate;

        /**
         * 是否有更多(无效，是错误的数据）
         */
        @JSONField(name = "havemore")
        public int havemore;

        @JSONField(name = "name")
        public String name;

        @JSONField(name = "comment")
        public String comment;

        //640x640
        @JSONField(name = "pic_s640")
        public String picS640;
        @JSONField(name = "pic_s444")
        public String picS444;
        @JSONField(name = "pic_s260")
        public String picS260;
        @JSONField(name = "pic_s210")
        public String picS210;
        @JSONField(name = "web_url")
        public String webUrl;
    }

    public static class RankSong extends Song {
        //排名变化
        @JSONField(name = "rank_change")
        public String rankChange;

        //排名
        @JSONField(name = "rank")
        public String rank;
    }
}
