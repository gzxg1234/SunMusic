package com.sanron.ddmusic.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by sanron on 16-3-21.
 */
public class HotTagData {

    @JSONField(name = "error_code")
    public int errorCode;
    @JSONField(name = "taglist")
    public List<Tag> tags;
}
