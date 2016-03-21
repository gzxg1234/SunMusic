package com.sanron.music.net.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by sanron on 16-3-19.
 */

public class FocusPicResult {
    @JsonProperty("pic")
    private List<FocusPic> focusPicList;

    public List<FocusPic> getFocusPicList() {
        return focusPicList;
    }

    public void setFocusPicList(List<FocusPic> focusPicList) {
        this.focusPicList = focusPicList;
    }

}
