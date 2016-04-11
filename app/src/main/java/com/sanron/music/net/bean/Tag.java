package com.sanron.music.net.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by sanron on 16-3-21.
 */
public class Tag {
    @JSONField(name = "title")
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
