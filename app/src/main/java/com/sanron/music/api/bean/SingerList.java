package com.sanron.music.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by sanron on 16-4-17.
 */
public class SingerList {

    /**
     * 数量
     */
    @JSONField(name = "nums")
    public String nums;

    /**
     * 是否还有更多
     */
    @JSONField(name = "havemore")
    public int havemore;

    @JSONField(name = "artist")
    public List<Singer> singers;
}
