package com.sanron.music.net.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RecommendSong extends Song {
    /**
     * 推荐理由
     */
    @JsonProperty("recommend_reason")
    private String recommendReason;

    public String getRecommendReason() {
        return recommendReason;
    }

    public void setRecommendReason(String recommendReason) {
        this.recommendReason = recommendReason;
    }
}