package com.sanron.music.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by sanron on 16-4-19.
 */
public class Album {

    /**
     * 名
     */
    @JSONField(name = "title")
    public String title;

    /**
     * id
     */
    @JSONField(name = "album_id")
    public String albumId;

    /**
     * 歌手
     */
    @JSONField(name = "author")
    public String author;

    /**
     * 歌手Id
     */
    @JSONField(name = "artist_id")
    public String artistId;

    /**
     * 所有歌手id
     */
    @JSONField(name = "all_artist_id")
    public String allArtistId;

    /**
     * 发行公司
     */
    @JSONField(name = "publishcompany")
    public String publishcompany;

    @JSONField(name = "prodcompany")
    public String prodcompany;

    /**
     * 国家
     */
    @JSONField(name = "country")
    public String country;

    /**
     * 语言
     */
    @JSONField(name = "language")
    public String language;

    /**
     * 歌曲数
     */
    @JSONField(name = "songs_total")
    public String songsTotal;

    @JSONField(name = "info")
    public String info;

    /**
     * 专辑风格
     */
    @JSONField(name = "styles")
    public String styles;

    /**
     * 发行时间
     * 不用date,因为有时会返回2000-00-00类似的数据,解析会出错
     */
    @JSONField(name = "publishtime")
    public String publishtime;

    /**
     * 应该是热度
     */
    @JSONField(name = "hot")
    public String hot;

    /**
     * 90x90
     */
    @JSONField(name = "pic_small")
    public String picSmall;
    /**
     * 150x150
     */
    @JSONField(name = "pic_big")
    public String picBig;
    /**
     * 300x300
     */
    @JSONField(name = "pic_radio")
    public String picRadio;
    /**
     * 180x180
     */
    @JSONField(name = "pic_s180")
    public String picS180;
    @JSONField(name = "pic_300")
    public String pic300;
    @JSONField(name = "pic_s500")
    public String picS500;
    @JSONField(name = "pic_w700")
    public String picW700;
    @JSONField(name = "pic_s1000")
    public String picS1000;
}
