package com.sanron.music.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FocusPic {

    public static final int TYPE_ALBUM = 2;
    public static final int TYPE_SONG_LIST = 7;

    /**
     * 图片URL
     */
    @JSONField(name = "randpic")
    public String picUrl;

    /**
     * 描述
     */
    @JSONField(name = "randpic_desc")
    public String desc;

    /**
     * 类型
     */
    @JSONField(name = "type")
    public int type;

    /**
     * 代码
     */
    @JSONField(name = "code")
    public String code;

}
