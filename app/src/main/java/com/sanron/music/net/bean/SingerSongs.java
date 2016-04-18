package com.sanron.music.net.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.List;

/**
 * Created by sanron on 16-4-18.
 */
public class SingerSongs {

    /**
     * 歌曲总数
     */
    @JSONField(name = "songnums")
    public String songnums;

    @JSONField(name = "havemore")
    public int havemore;

    @JSONField(name = "error_code")
    public int errorCode;

    @JSONField(name = "songlist")
    public List<Song> songs;
//
//    public static class Songlist {
//        @JSONField(name = "artist_id")
//        public String artistId;
//        @JSONField(name = "all_artist_ting_uid")
//        public String allArtistTingUid;
//        @JSONField(name = "all_artist_id")
//        public String allArtistId;
//        @JSONField(name = "language")
//        public String language;
//        @JSONField(name = "publishtime")
//        public String publishtime;
//        @JSONField(name = "album_no")
//        public String albumNo;
//        @JSONField(name = "versions")
//        public String versions;
//        @JSONField(name = "pic_big")
//        public String picBig;
//        @JSONField(name = "pic_small")
//        public String picSmall;
//        @JSONField(name = "country")
//        public String country;
//        @JSONField(name = "area")
//        public String area;
//        @JSONField(name = "lrclink")
//        public String lrclink;
//        @JSONField(name = "hot")
//        public String hot;
//        @JSONField(name = "file_duration")
//        public String fileDuration;
//        @JSONField(name = "del_status")
//        public String delStatus;
//        @JSONField(name = "resource_type")
//        public String resourceType;
//        @JSONField(name = "copy_type")
//        public String copyType;
//        @JSONField(name = "relate_status")
//        public String relateStatus;
//        @JSONField(name = "all_rate")
//        public String allRate;
//        @JSONField(name = "has_mv_mobile")
//        public int hasMvMobile;
//        @JSONField(name = "toneid")
//        public String toneid;
//        @JSONField(name = "song_id")
//        public String songId;
//        @JSONField(name = "title")
//        public String title;
//        @JSONField(name = "ting_uid")
//        public String tingUid;
//        @JSONField(name = "author")
//        public String author;
//        @JSONField(name = "album_id")
//        public String albumId;
//        @JSONField(name = "album_title")
//        public String albumTitle;
//        @JSONField(name = "is_first_publish")
//        public int isFirstPublish;
//        @JSONField(name = "havehigh")
//        public int havehigh;
//        @JSONField(name = "charge")
//        public int charge;
//        @JSONField(name = "has_mv")
//        public int hasMv;
//        @JSONField(name = "learn")
//        public int learn;
//        @JSONField(name = "song_source")
//        public String songSource;
//        @JSONField(name = "piao_id")
//        public String piaoId;
//        @JSONField(name = "korean_bb_song")
//        public String koreanBbSong;
//        @JSONField(name = "resource_type_ext")
//        public String resourceTypeExt;
//        @JSONField(name = "mv_provider")
//        public String mvProvider;
//        @JSONField(name = "listen_total")
//        public String listenTotal;
//    }
}
