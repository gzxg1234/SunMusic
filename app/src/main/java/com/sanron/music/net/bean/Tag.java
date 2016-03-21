package com.sanron.music.net.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sanron on 16-3-21.
 */
public class Tag {
    @JsonProperty("title")
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
