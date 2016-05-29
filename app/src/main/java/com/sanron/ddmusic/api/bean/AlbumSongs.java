package com.sanron.ddmusic.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by sanron on 16-4-23.
 */
public class AlbumSongs {

    @JSONField(name = "albumInfo")
    public Album albumInfo;

    @JSONField(name = "songlist")
    public List<Song> songs;

}
