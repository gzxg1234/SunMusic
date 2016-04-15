package com.sanron.music.net;

import com.sanron.music.net.bean.AllTag;
import com.sanron.music.net.bean.FocusPicData;
import com.sanron.music.net.bean.HotSongListData;
import com.sanron.music.net.bean.HotTagData;
import com.sanron.music.net.bean.LrcPicResult;
import com.sanron.music.net.bean.RecmdSongData;
import com.sanron.music.net.bean.SongList;
import com.sanron.music.net.bean.SongUrlInfo;
import com.sanron.music.net.bean.TagData;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
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
    public static Call focusPic(int num, ApiCallback<FocusPicData> apiCallback) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("method", "baidu.ting.plaza.getFocusPic");
        params.put("num", num);
        return ApiHttpClient.get(url(params), 600, apiCallback);
    }

    /**
     * 推荐歌曲
     *
     * @param num         　数量
     * @param apiCallback
     */
    public static Call recmdSongs(int num, final ApiCallback<RecmdSongData> apiCallback) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("method", "baidu.ting.song.getEditorRecommend");
        params.put("num", num);
        return ApiHttpClient.get(url(params), 600, apiCallback);
    }

    /**
     * 热门歌单
     *
     * @param num
     * @param apiCallback
     */
    public static Call hotSongList(int num, final ApiCallback<HotSongListData> apiCallback) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("method", "baidu.ting.diy.getHotGeDanAndOfficial");
        params.put("num", num);
        return ApiHttpClient.get(url(params), 600, apiCallback);
    }

    /**
     * 热门分类
     *
     * @param num
     * @param apiCallback
     */
    public static Call hotTag(int num, ApiCallback<HotTagData> apiCallback) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("method", "baidu.ting.tag.getHotTag");
        params.put("nums", num);
        return ApiHttpClient.get(url(params), 21600, apiCallback);
    }

    /**
     * 所有分类
     *
     * @param apiCallback
     */
    public static Call allTag(ApiCallback<AllTag> apiCallback) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("method", "baidu.ting.tag.getAllTag");
        return ApiHttpClient.get(url(params), 21600, apiCallback);
    }

    /**
     * 分类信息（分类下的歌曲）
     *
     * @param tagName     分类名
     * @param limit       获取歌曲数量
     * @param offset      偏移量
     * @param apiCallback
     */
    public static Call tagInfo(String tagName, int limit, int offset, ApiCallback<TagData> apiCallback) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("method", "baidu.ting.tag.songlist");
        params.put("tagname", tagName);
        params.put("limit", limit);
        params.put("offset", offset);
        return ApiHttpClient.get(url(params), 21600, apiCallback);
    }

    /**
     * 歌单信息
     *
     * @param listId
     * @param apiCallback
     */
    public static Call songListInfo(String listId, ApiCallback<SongList> apiCallback) {
        HashMap<String, Object> params = new HashMap<String, Object>();
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
        HashMap<String, Object> params = new HashMap<String, Object>();
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
        HashMap<String, Object> params = new HashMap<>();
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


    private static String url(HashMap<String, Object> params) {
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
