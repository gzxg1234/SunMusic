package com.sanron.music.net.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by sanron on 16-4-10.
 */
public class LrcPicResult {

    @JSONField(name = "songinfo")
    private List<LrcPic> lrcPics;

    public List<LrcPic> getLrcPics() {
        return lrcPics;
    }

    public void setLrcPics(List<LrcPic> lrcPics) {
        this.lrcPics = lrcPics;
    }

    public static class LrcPic {
        /**
         * 歌词
         */
        @JSONField(name = "lrclink")
        private String lrc;

        @JSONField(name = "song_id")
        private String songId;

        @JSONField(name = "author")
        private String author;

        @JSONField(name = "song_title")
        private String title;

        /**
         * 艺术家图片
         */
        @JSONField(name = "artist_480_480")
        private String artist480x480;
        @JSONField(name = "artist_640_1136")
        private String artist640x1136;
        @JSONField(name = "artist_1000_1000")
        private String artist1000x1000;

        /**
         * 歌曲相关图片
         */
        @JSONField(name = "pic_s180")
        private String pic180x180;
        @JSONField(name = "pic_s500")
        private String pic500x500;
        @JSONField(name = "pic_s1000")
        private String pic1000x1000;

        /**
         * 头像
         */
        @JSONField(name = "avatar_s180")
        private String avatar180x180;
        @JSONField(name = "avatar_s500")
        private String avatar500x500;

        public String getLrc() {
            return lrc;
        }

        public void setLrc(String lrc) {
            this.lrc = lrc;
        }

        public String getSongId() {
            return songId;
        }

        public void setSongId(String songId) {
            this.songId = songId;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getArtist480x480() {
            return artist480x480;
        }

        public void setArtist480x480(String artist480x480) {
            this.artist480x480 = artist480x480;
        }

        public String getArtist640x1136() {
            return artist640x1136;
        }

        public void setArtist640x1136(String artist640x1136) {
            this.artist640x1136 = artist640x1136;
        }

        public String getArtist1000x1000() {
            return artist1000x1000;
        }

        public void setArtist1000x1000(String artist1000x1000) {
            this.artist1000x1000 = artist1000x1000;
        }

        public String getPic180x180() {
            return pic180x180;
        }

        public void setPic180x180(String pic180x180) {
            this.pic180x180 = pic180x180;
        }

        public String getPic500x500() {
            return pic500x500;
        }

        public void setPic500x500(String pic500x500) {
            this.pic500x500 = pic500x500;
        }

        public String getPic1000x1000() {
            return pic1000x1000;
        }

        public void setPic1000x1000(String pic1000x1000) {
            this.pic1000x1000 = pic1000x1000;
        }

        public String getAvatar180x180() {
            return avatar180x180;
        }

        public void setAvatar180x180(String avatar180x180) {
            this.avatar180x180 = avatar180x180;
        }

        public String getAvatar500x500() {
            return avatar500x500;
        }

        public void setAvatar500x500(String avatar500x500) {
            this.avatar500x500 = avatar500x500;
        }
    }
}
