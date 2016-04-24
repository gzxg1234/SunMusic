package com.sanron.music.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by sanron on 16-3-19.
 */

public class FocusPicData {

    @JSONField(name = "error_code")
    public int errorCode;
    @JSONField(name = "pic")
    public List<FocusPic> pics;
}
