package com.sanron.music.net;

import android.content.ContentValues;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sanron.music.net.bean.DetailSongInfo;
import com.sanron.music.net.bean.FocusPicResult;
import com.sanron.music.net.bean.HotTagResult;
import com.sanron.music.net.bean.RecommendSong;
import com.sanron.music.net.bean.SongList;
import com.sanron.music.utils.JsonUtils;

import java.io.IOException;
import java.util.List;
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
    public static void focusPic(int num, ApiCallback<FocusPicResult> apiCallback) {
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
    public static void recmdSongs(int num, final ApiCallback<List<RecommendSong>> apiCallback) {
        ContentValues params = new ContentValues();
        params.put("method", "baidu.ting.song.getEditorRecommend");
        params.put("num", num);
        ApiHttpClient.get(url(params), 600, new ApiCallback<JsonNode>() {
            @Override
            public void onFailure(Call call, IOException e) {
                apiCallback.onFailure(call, e);
            }

            @Override
            public void onSuccess(Call call, JsonNode node) {
                ArrayNode arrayNode = (ArrayNode) node.get("content");
                if (arrayNode != null
                        && arrayNode.size() > 0) {
                    try {
                        String json = arrayNode.get(0).get("song_list").toString();
                        List<RecommendSong> recommendSongs = JsonUtils.fromJson(json,
                                new TypeReference<List<RecommendSong>>() {
                                });
                        apiCallback.onSuccess(call, recommendSongs);
                    } catch (IOException e) {
                        e.printStackTrace();
                        apiCallback.onFailure(call, e);
                    }
                }
            }
        });
    }

    /**
     * 热门歌单
     *
     * @param num
     * @param apiCallback
     */
    public static void hotSongList(int num, final ApiCallback<List<SongList>> apiCallback) {
        ContentValues params = new ContentValues();
        params.put("method", "baidu.ting.diy.getHotGeDanAndOfficial");
        params.put("num", num);
        ApiHttpClient.get(url(params), 600, new ApiCallback<JsonNode>() {
            @Override
            public void onFailure(Call call, IOException e) {
                apiCallback.onFailure(call, e);
            }

            @Override
            public void onSuccess(Call call, JsonNode node) {
                JsonNode content = node.get("content");
                if (content != null) {
                    String json = content.get("list").toString();
                    try {
                        List<SongList> hotSongLists = JsonUtils.fromJson(json,
                                new TypeReference<List<SongList>>() {
                                });
                        apiCallback.onSuccess(call, hotSongLists);
                    } catch (IOException e) {
                        e.printStackTrace();
                        apiCallback.onFailure(call, e);
                    }
                }
            }
        });
    }

    /**
     * 热门分类
     *
     * @param num
     * @param apiCallback
     */
    public static void hotTag(int num, ApiCallback<HotTagResult> apiCallback) {
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
     * @param songid
     * @param callback
     * @return
     */
    public static Call songLink(String songid, ApiCallback<DetailSongInfo> callback) {
        ContentValues params = new ContentValues();
        long currentTimeMillis = System.currentTimeMillis();
        String str = "songid=" + songid + "&ts=" + currentTimeMillis;
        String e = AESTools.encrpty(str);
        params.put("method", "baidu.ting.song.getInfos");
        params.put("songid", songid);
        params.put("ts", currentTimeMillis);
        params.put("e", e);
        return ApiHttpClient.get(url(params), callback);
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
}
