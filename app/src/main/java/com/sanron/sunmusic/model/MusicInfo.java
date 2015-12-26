package com.sanron.sunmusic.model;

/**
 * Created by Administrator on 2015/12/19.
 */
public class MusicInfo {

    public static final int TYPE_LOCAL = 0; //本地
    public static final int TYPE_WEB = 1;   //网络

    private int type;
    private String path;
    private String displayName;
    private String title;
    private String album;
    private String artist;
    private String pinyin;
}
