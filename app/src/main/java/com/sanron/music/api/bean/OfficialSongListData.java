package com.sanron.music.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by sanron on 16-5-5.
 */
public class OfficialSongListData {

    @JSONField(name = "total")
    public String total;
    @JSONField(name = "havemore")
    public int havemore;
    @JSONField(name = "albumList")
    public List<AlbumList> albumLists;

    public static class AlbumList {
        @JSONField(name = "name")
        public String name;
        @JSONField(name = "createTime")
        public String createTime;
        @JSONField(name = "desc")
        public String desc;
        @JSONField(name = "code")
        public String code;
        @JSONField(name = "pic")
        public String pic;
        @JSONField(name = "pic_qq")
        public String picQq;
        @JSONField(name = "pic_s640")
        public String picS640;
    }
}
