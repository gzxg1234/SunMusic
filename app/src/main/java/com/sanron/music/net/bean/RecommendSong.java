package com.sanron.music.net.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class RecommendSong extends Song {
    /**
     * 推荐理由
     */
    @JSONField(name = "recommend_reason")
    public String recommendReason;
}