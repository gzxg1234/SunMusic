package com.sanron.music.net.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.sanron.music.db.model.Music;

/**
 * Created by sanron on 16-3-19.
 */
public class Song {

    /**
     * 歌名
     */
    @JSONField(name = "title")
    public String title;

    /**
     * 歌曲id
     */
    @JSONField(name = "song_id")
    public String songId;

    /**
     * 所有歌手名
     */
    @JSONField(name = "author")
    public String author;

    /**
     * 所有歌手id
     */
    @JSONField(name = "all_artist_id")
    public String allArtistId;

    /**
     * 主要歌手id
     */
    @JSONField(name = "artist_id")
    public String artistId;

    /**
     * 专辑名
     */
    @JSONField(name = "album_title")
    public String albumTitle;

    /**
     * 专辑id
     */
    @JSONField(name = "album_id")
    public String albumId;

    /**
     * 是否有mv
     */
    @JSONField(name = "has_mv")
    public int hasMv;

    /**
     * 小图,90x90
     */
    @JSONField(name = "pic_small")
    public String smallPic;

    /**
     * 大图,150x150
     */
    @JSONField(name = "pic_big")
    public String bigPic;

    /**
     * 更大图,500x500
     */
    @JSONField(name = "pic_premium")
    public String premiumPic;

    /**
     * 巨大图,1000x1000
     */
    @JSONField(name = "pic_huge")
    public String hugePic;

    @Override
    public String toString() {
        return "title:" + title
                + " id:" + songId
                + " album:" + albumTitle
                + " albumId:" + albumId
                + " artist:" + albumId
                + " artistId:" + artistId
                + " artistIds:" + allArtistId
                + " picUrl:" + bigPic;
    }


    public Music toMusic() {
        Music music = new Music();
        music.setTitle(title);
        music.setArtist(author);
        music.setAlbum(albumTitle);
        String titleKey = (title == null ?
                null : PinyinHelper.convertToPinyinString(title, "", PinyinFormat.WITHOUT_TONE));
        music.setTitleKey(titleKey);
        music.setSongId(songId);
        return music;
    }
}
