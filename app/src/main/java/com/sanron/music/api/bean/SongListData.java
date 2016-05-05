package com.sanron.music.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by sanron on 16-5-2.
 */
public class SongListData {

    @JSONField(name = "error_code")
    public int errorCode;

    @JSONField(name = "total")
    public int total;

    @JSONField(name = "havemore")
    public int havemore;

    @JSONField(name = "content")
    public List<SongList> songLists;

//    public static class Content {
//        @JSONField(name = "listid")
//        public String listid;
//        @JSONField(name = "listenum")
//        public String listenum;
//        @JSONField(name = "collectnum")
//        public String collectnum;
//        @JSONField(name = "title")
//        public String title;
//        @JSONField(name = "pic_300")
//        public String pic300;
//        @JSONField(name = "tag")
//        public String tag;
//        @JSONField(name = "desc")
//        public String desc;
//        @JSONField(name = "pic_w300")
//        public String picW300;
//        @JSONField(name = "width")
//        public String width;
//        @JSONField(name = "height")
//        public String height;
//    }
}
