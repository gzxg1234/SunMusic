package com.sanron.ddmusic.api.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by sanron on 16-3-29.
 */
public class SongUrlInfo {

    @JSONField(name = "songurl")
    public SongUrl songUrl;

    public static class SongUrl {

        @JSONField(name = "url")
        public List<Url> urls;

        public static class Url {
            /**
             * 比特率
             */
            @JSONField(name = "file_bitrate")
            public int fileBitrate;

            /**
             * 下载链接
             */
            @JSONField(name = "file_link")
            public String fileLink;

            @JSONField(name = "show_link")
            public String showLink;

            /**
             * 是否试听地址
             */
            @JSONField(name = "is_udition_url")
            public int isAudition;

            @JSONField(name = "file_size")
            public int fileSize;

        }
    }
}
