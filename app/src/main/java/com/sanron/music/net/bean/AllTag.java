package com.sanron.music.net.bean;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;
import java.util.Map;

/**
 * Created by sanron on 16-4-14.
 */
public class AllTag {


    @JSONField(name = "error_code")
    public int errorCode;

    @JSONField(name = "taglist")
    public Map<String,List<Tag>> tagList;

    @JSONField(name = "tags")
    public List<String> categories;
}
