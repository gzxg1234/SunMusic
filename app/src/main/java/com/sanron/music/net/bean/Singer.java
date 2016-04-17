package com.sanron.music.net.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by sanron on 16-4-17.
 */
public class Singer {


    /**
     * 名字
     */
    @JSONField(name = "name")
    public String name;

    /**
     * 性别
     */
    @JSONField(name = "gender")
    public String gender;

    /**
     * 生日
     */
    @JSONField(name = "birth")
    public String birth;

    /**
     * 星座
     */
    @JSONField(name = "constellation")
    public String constellation;


    /**
     * tingUid，貌似也可作为歌手标识
     */
    @JSONField(name = "ting_uid")
    public String tingUid;

    /**
     * 体重
     */
    @JSONField(name = "weight")
    public String weight;

    /**
     * 身高
     */
    @JSONField(name = "stature")
    public String stature;


    /**
     * 别名
     */
    @JSONField(name = "aliasname")
    public String aliasname;

    /**
     * 国家
     */
    @JSONField(name = "country")
    public String country;

    @JSONField(name = "source")
    public String source;

    /**
     * 歌手简介
     */
    @JSONField(name = "intro")
    public String intro;

    @JSONField(name = "url")
    public String url;

    /**
     * 公司
     */
    @JSONField(name = "company")
    public String company;

//    /**
//     * 血型
//     */
//    @JSONField(name = "bloodtype")
//    public String bloodtype;

    /**
     * mv数量
     */
    @JSONField(name = "mv_total")
    public int mvTotal;

    /**
     * 地区
     */
    @JSONField(name = "area")
    public String area;

    /**
     * 姓名首字母
     */
    @JSONField(name = "firstchar")
    public String firstchar;

    /**
     * id
     */
    @JSONField(name = "artist_id")
    public String artistId;


    /**
     * 头像
     */
    @JSONField(name = "avatar_mini")
    public String avatarMini;
    @JSONField(name = "avatar_small")
    public String avatarSmall;
    @JSONField(name = "avatar_middle")
    public String avatarMiddle;
    @JSONField(name = "avatar_big")
    public String avatarBig;
    @JSONField(name = "avatar_s180")
    public String avatarS180;
    @JSONField(name = "avatar_s500")
    public String avatarS500;
    @JSONField(name = "avatar_s1000")
    public String avatarS1000;

    /**
     * 专辑数
     */
    @JSONField(name = "albums_total")
    public String albumsTotal;

    /**
     * 歌曲数
     */
    @JSONField(name = "songs_total")
    public String songsTotal;
}
