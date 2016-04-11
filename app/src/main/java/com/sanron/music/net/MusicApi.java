package com.sanron.music.net;

import android.content.ContentValues;

import com.sanron.music.net.bean.SongUrlInfo;
import com.sanron.music.net.bean.FocusPicData;
import com.sanron.music.net.bean.HotSongListData;
import com.sanron.music.net.bean.HotTagData;
import com.sanron.music.net.bean.LrcPicResult;
import com.sanron.music.net.bean.RecmdSongData;
import com.sanron.music.net.bean.SongList;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;

import okhttp3.Call;

/**
 * Created by sanron on 16-3-18.
 */
public class MusicApi {

    public static final String BASE = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=android&version=5.6.5.6&format=json";

    /**
     * 轮播图片
     *
     * @param num
     * @param apiCallback
     */
    public static void focusPic(int num, ApiCallback<FocusPicData> apiCallback) {
        ContentValues params = new ContentValues();
        params.put("method", "baidu.ting.plaza.getFocusPic");
        params.put("num", num);
        ApiHttpClient.get(url(params), 600, apiCallback);
    }

    /**
     * 推荐歌曲
     *
     * @param num         　数量
     * @param apiCallback
     */
    public static void recmdSongs(int num, final ApiCallback<RecmdSongData> apiCallback) {
        ContentValues params = new ContentValues();
        params.put("method", "baidu.ting.song.getEditorRecommend");
        params.put("num", num);
        ApiHttpClient.get(url(params), 600,apiCallback);
    }

    /**
     * 热门歌单
     *
     * @param num
     * @param apiCallback
     */
    public static void hotSongList(int num, final ApiCallback<HotSongListData> apiCallback) {
        ContentValues params = new ContentValues();
        params.put("method", "baidu.ting.diy.getHotGeDanAndOfficial");
        params.put("num", num);
        ApiHttpClient.get(url(params), 600,apiCallback);
    }

    /**
     * 热门分类
     *
     * @param num
     * @param apiCallback
     */
    public static void hotTag(int num, ApiCallback<HotTagData> apiCallback) {
        ContentValues params = new ContentValues();
        params.put("method", "baidu.ting.tag.getHotTag");
        params.put("nums", num);
        ApiHttpClient.get(url(params), 600, apiCallback);
    }

    /**
     * 歌单信息
     *
     * @param listId
     * @param apiCallback
     */
    public static Call songListInfo(String listId, ApiCallback<SongList> apiCallback) {
        ContentValues params = new ContentValues();
        params.put("method", "baidu.ting.diy.gedanInfo");
        params.put("listid", listId);
        return ApiHttpClient.get(url(params), 3600, apiCallback);
    }

    /**
     * 歌曲文件链接
     *
     * @param songid
     * @param callback
     * @return
     */
    public static Call songLink(String songid, ApiCallback<SongUrlInfo> callback) {
        ContentValues params = new ContentValues();
        long currentTimeMillis = System.currentTimeMillis();
        String str = "songid=" + songid + "&ts=" + currentTimeMillis;
        String e = EncrptyTool.encrpty(str);
        params.put("method", "baidu.ting.song.getInfos");
        params.put("songid", songid);
        params.put("ts", currentTimeMillis);
        params.put("e", e);
        return ApiHttpClient.get(url(params), callback);
    }

    /**
     * 搜图词
     *
     * @param word
     * @param artist
     * @param type
     * @param callback
     * @return
     */
    public static Call searchLrcPic(String word, String artist, int type, ApiCallback<LrcPicResult> callback) {
        ContentValues params = new ContentValues();
        String ts = Long.toString(System.currentTimeMillis());
        String query = word + "$$" + artist;
        String e = com.sanron.music.bdmusic.AESTools.encrpty("query=" + query + "&ts=" + ts);
        params.put("method", "baidu.ting.search.lrcpic");
        params.put("query", query);
        params.put("ts", ts);
        params.put("type", type);
        params.put("e", e);
        return ApiHttpClient.get(url(params), 10, callback);
    }

    private static String url(ContentValues params) {
        StringBuffer sb = new StringBuffer(BASE);
        Set<String> keys = params.keySet();
        for (String name : keys) {
            String value = String.valueOf(params.get(name));
            sb.append("&").append(name).append("=").append(value);
        }
        return sb.toString();
    }

    public static String encode(String str) {
        if (str == null) return "";

        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }
}
