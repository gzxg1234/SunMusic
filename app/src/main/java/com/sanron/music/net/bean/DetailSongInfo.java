package com.sanron.music.net.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by sanron on 16-3-29.
 */
public class DetailSongInfo {

    @JsonProperty("songurl")
    private SongUrl songUrl;

    public SongUrl getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(SongUrl songUrl) {
        this.songUrl = songUrl;
    }

    public static class SongUrl {

        @JsonProperty("url")
        private List<FileInfo> fileInfos;

        public List<FileInfo> getFileInfos() {
            return fileInfos;
        }

        public void setFileInfos(List<FileInfo> fileInfos) {
            this.fileInfos = fileInfos;
        }

        public static class FileInfo {
            /**
             * 比特率
             */
            @JsonProperty("file_bitrate")
            private int fileBitrate;

            /**
             * 下载链接
             */
            @JsonProperty("show_link")
            private String fileLink;

            /**
             * 是否试听地址
             */
            @JsonProperty("is_udition_url")
            private int isAudition;

            @JsonProperty("file_size")
            private int fileSize;

            public int getIsAudition() {
                return isAudition;
            }

            public void setIsAudition(int isAudition) {
                this.isAudition = isAudition;
            }

            public int getFileBitrate() {
                return fileBitrate;
            }

            public void setFileBitrate(int fileBitrate) {
                this.fileBitrate = fileBitrate;
            }

            public String getFileLink() {
                return fileLink;
            }

            public void setFileLink(String fileLink) {
                this.fileLink = fileLink;
            }

            public int getFileSize() {
                return fileSize;
            }

            public void setFileSize(int fileSize) {
                this.fileSize = fileSize;
            }
        }
    }
}
