package com.sanron.music.net.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("title")
    private String title;

    /**
     * 歌曲id
     */
    @JsonProperty("song_id")
    private String songId;

    /**
     * 所有歌手名
     */
    @JsonProperty("author")
    private String allArtistName;

    /**
     * 所有歌手id
     */
    @JsonProperty("all_artist_id")
    private String allArtistId;

    /**
     * 主要歌手id
     */
    @JsonProperty("artist_id")
    private String artistId;

    /**
     * 专辑名
     */
    @JsonProperty("album_title")
    private String albumName;

    /**
     * 专辑id
     */
    @JsonProperty("album_id")
    private String albumId;

    /**
     * 是否有mv
     */
    @JsonProperty("has_mv")
    private int hasMv;

    /**
     * 小图,90x90
     */
    @JsonProperty("pic_small")
    private String smallPic;

    /**
     * 大图,150x150
     */
    @JsonProperty("pic_big")
    private String bigPic;

    /**
     * 更大图,500x500
     */
    @JsonProperty("pic_premium")
    private String premiumPic;

    /**
     * 巨大图,1000x1000
     */
    @JsonProperty("pic_huge")
    private String hugePic;

    @Override
    public String toString() {
        return "title:" + title
                + " id:" + songId
                + " album:" + albumName
                + " albumId:" + albumId
                + " artist:" + albumId
                + " artistId:" + artistId
                + " artistIds:" + allArtistId
                + " picUrl:" + bigPic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getHugePic() {
        return hugePic;
    }

    public void setHugePic(String hugePic) {
        this.hugePic = hugePic;
    }


    public String getSmallPic() {
        return smallPic;
    }

    public void setSmallPic(String smallPic) {
        this.smallPic = smallPic;
    }

    public String getBigPic() {
        return bigPic;
    }

    public void setBigPic(String bigPic) {
        this.bigPic = bigPic;
    }

    public String getPremiumPic() {
        return premiumPic;
    }

    public void setPremiumPic(String premiumPic) {
        this.premiumPic = premiumPic;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getAllArtistName() {
        return allArtistName;
    }

    public void setAllArtistName(String allArtistName) {
        this.allArtistName = allArtistName;
    }

    public String getAllArtistId() {
        return allArtistId;
    }

    public void setAllArtistId(String allArtistId) {
        this.allArtistId = allArtistId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public int getHasMv() {
        return hasMv;
    }

    public void setHasMv(int hasMv) {
        this.hasMv = hasMv;
    }

    public Music toMusic() {
        Music music = new Music();
        music.setTitle(title);
        music.setArtist(allArtistName);
        music.setAlbum(albumName);
        String titleKey = (title == null ?
                null : PinyinHelper.convertToPinyinString(title, "", PinyinFormat.WITHOUT_TONE));
        music.setTitleKey(titleKey);
        music.setSongId(songId);
        return music;
    }
}
