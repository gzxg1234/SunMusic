package com.sanron.music.net.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FocusPic {

    public static final int TYPE_ALBUM = 2;
    public static final int TYPE_GEDAN = 7;

    /**
     * 图片URL
     */
    @JsonProperty("randpic")
    private String picUrl;

    /**
     * 描述
     */
    @JsonProperty("randpic_desc")
    private String desc;

    /**
     * 类型
     */
    @JsonProperty("type")
    private int type;

    /**
     * 代码
     */
    @JsonProperty("code")
    private String code;

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
