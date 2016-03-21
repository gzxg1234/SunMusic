package com.sanron.music.net.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by sanron on 16-3-21.
 */
public class HotTagResult {

    @JsonProperty("taglist")
    private List<Tag> hotTags;

    public List<Tag> getHotTags() {
        return hotTags;
    }

    public void setHotTags(List<Tag> hotTags) {
        this.hotTags = hotTags;
    }
}
