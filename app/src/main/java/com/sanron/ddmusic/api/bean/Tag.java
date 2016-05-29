package com.sanron.ddmusic.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by sanron on 16-3-21.
 */
public class Tag {
    @JSONField(name = "title")
    public String title;

    @JSONField(name = "hot")
    public int isHot;
}
