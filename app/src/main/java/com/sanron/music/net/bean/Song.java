package com.sanron.music.net.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sanron.music.net.converter.SplitConverter;

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
     * 之所以为数组是因为合唱的歌有多名歌手,下同
     */
    @JsonProperty("author")
    @JsonDeserialize(converter = SplitConverter.class)
    private String[] allArtistName;

    /**
     * 所有歌手id
     */
    @JsonProperty("all_artist_id")
    @JsonDeserialize(converter = SplitConverter.class)
    private String[] allArtistId;

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
        return "title:"+title
                +" id:"+songId
                +" album:"+albumName
                +" albumId:"+albumId
                +" artist:"+albumId
                +" artistId:"+artistId
                +" artistIds:"+allArtistId
                +" picUrl:"+bigPic;
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

    public String[] getAllArtistName() {
        return allArtistName;
    }

    public void setAllArtistName(String[] allArtistName) {
        this.allArtistName = allArtistName;
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

    public String[] getAllArtistId() {
        return allArtistId;
    }

    public void setAllArtistId(String[] allArtistId) {
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

}
