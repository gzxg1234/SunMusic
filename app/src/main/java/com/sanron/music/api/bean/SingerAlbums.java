package com.sanron.music.api.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;

/**
 * Created by sanron on 16-4-19.
 */
public class SingerAlbums {

    @JSONField(name = "albumnums")
    public String albumnums;

    @JSONField(name = "havemore")
    public int havemore;

    @JSONField(name = "albumlist", serialzeFeatures = {SerializerFeature.WriteNullListAsEmpty})
    public List<Album> albums;
}
