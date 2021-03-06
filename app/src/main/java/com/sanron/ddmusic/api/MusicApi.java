package com.sanron.ddmusic.api;

import android.text.TextUtils;

import com.sanron.ddmusic.api.bean.AlbumSongs;
import com.sanron.ddmusic.api.bean.AllTag;
import com.sanron.ddmusic.api.bean.BillCategoryData;
import com.sanron.ddmusic.api.bean.BillSongList;
import com.sanron.ddmusic.api.bean.FocusPicData;
import com.sanron.ddmusic.api.bean.HotSongListData;
import com.sanron.ddmusic.api.bean.HotTagData;
import com.sanron.ddmusic.api.bean.LrcPicData;
import com.sanron.ddmusic.api.bean.OfficialSongListData;
import com.sanron.ddmusic.api.bean.OfficialSongListSongs;
import com.sanron.ddmusic.api.bean.RecmdSongData;
import com.sanron.ddmusic.api.bean.Singer;
import com.sanron.ddmusic.api.bean.SingerAlbums;
import com.sanron.ddmusic.api.bean.SingerList;
import com.sanron.ddmusic.api.bean.SingerSongs;
import com.sanron.ddmusic.api.bean.SongList;
import com.sanron.ddmusic.api.bean.SongListCategory;
import com.sanron.ddmusic.api.bean.SongListData;
import com.sanron.ddmusic.api.bean.SongUrlInfo;
import com.sanron.ddmusic.api.bean.TagSongListData;
import com.sanron.ddmusic.api.bean.TagSongs;
import com.sanron.ddmusic.api.callback.JsonCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Request;


/**
 * Created by sanron on 16-3-18.
 */
public class MusicApi {

    public static final String BASE = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=android&version=5.8.0.1&format=json";
    public static boolean sIsNetAvailable = true;

    /**
     * 轮播图片
     *
     * @param num
     * @param jsonCallback
     */
    public static Call focusPic(int num, JsonCallback<FocusPicData> jsonCallback) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("method", "baidu.ting.plaza.getFocusPic");
        params.put("num", num);
        return AppHttpClient.get(createRequest(params, 600, 0), jsonCallback);
    }

    /**
     * 推荐歌曲
     *
     * @param num          　数量
     * @param jsonCallback
     */
    public static Call recmdSongs(int num, final JsonCallback<RecmdSongData> jsonCallback) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("method", "baidu.ting.song.getEditorRecommend");
        params.put("num", num);
        return AppHttpClient.get(createRequest(params, 600, 0), jsonCallback);
    }

    /**
     * 热门歌单
     *
     * @param num
     * @param jsonCallback
     */
    public static Call hotSongList(int num, final JsonCallback<HotSongListData> jsonCallback) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("method", "baidu.ting.diy.getHotGeDanAndOfficial");
        params.put("num", num);
        return AppHttpClient.get(createRequest(params, 600, 0), jsonCallback);
    }

    /**
     * 热门分类
     *
     * @param num
     * @param jsonCallback
     */
    public static Call hotTag(int num, JsonCallback<HotTagData> jsonCallback) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("method", "baidu.ting.tag.getHotTag");
        params.put("nums", num);
        return AppHttpClient.get(createRequest(params, 21600, 0), jsonCallback);
    }

    /**
     * 所有分类
     *
     * @param jsonCallback
     */
    public static Call allTag(JsonCallback<AllTag> jsonCallback) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("method", "baidu.ting.tag.getAllTag");
        return AppHttpClient.get(createRequest(params, 21600, 0), jsonCallback);
    }

    /**
     * 分类信息（分类下的歌曲）
     *
     * @param tagName      分类名
     * @param limit        获取歌曲数量
     * @param offset       偏移量
     * @param jsonCallback
     */
    public static Call tagInfo(String tagName, int limit, int offset, JsonCallback<TagSongs> jsonCallback) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("method", "baidu.ting.tag.songlist");
        params.put("tagname", tagName);
        params.put("limit", limit);
        params.put("offset", offset);
        return AppHttpClient.get(createRequest(params, 21600, 0), jsonCallback);
    }

    /**
     * 歌单信息
     *
     * @param listId
     * @param jsonCallback
     */
    public static Call songListInfo(String listId, JsonCallback<SongList> jsonCallback) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("method", "baidu.ting.diy.gedanInfo");
        params.put("listid", listId);
        return AppHttpClient.get(createRequest(params, 3600, 0), jsonCallback);
    }

    /**
     * 歌曲文件链接
     *
     * @param songid
     * @param callback
     * @return
     */
    public static Call songLink(String songid, JsonCallback<SongUrlInfo> callback) {
        HashMap<String, Object> params = new HashMap<String, Object>();
//        long currentTimeMillis = System.currentTimeMillis();
        long ts = 88888888;
        String e = ApiEncryptTool.encrypt("songid=" + songid + "&ts=" + ts);
        params.put("method", "baidu.ting.song.getInfos");
        params.put("songid", songid);
        params.put("ts", ts);
        params.put("e", e);
        return AppHttpClient.get(createRequest(params, 60, 0), callback);
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
    public static Call searchLrcPic(String word, String artist, int type, final JsonCallback<LrcPicData> callback) {
        HashMap<String, Object> params = new HashMap<>();
        //正确的姿势应该是要当前时间的,为了缓存，设置固定的值,不过没有什么影响，貌似只是为了加密检验
//        long currentTimeMillis = System.currentTimeMillis();
        long ts = 88888888;
        String query = word + "$$" + artist;
        String e = ApiEncryptTool.encrypt("query=" + query + "&ts=" + ts);
        params.put("method", "baidu.ting.search.lrcpic");
        params.put("query", query);
        params.put("ts", ts);
        params.put("type", type);
        params.put("e", e);
        return AppHttpClient.get(createRequest(params, 600,
                sIsNetAvailable ? 0 : Integer.MAX_VALUE), callback);
    }

    /**
     * 获取歌手列表
     *
     * @param offset 偏移
     * @param limit  数量
     * @param area   地区：0不分,6华语,3欧美,7韩国,60日本,5其他
     * @param sex    性别：0不分,1男,2女,3组合
     * @param order  排序：1按热门，2按艺术家id(atrist_id)
     * @param abc    艺术家名首字母：a-z,other其他
     * @return
     */
    public static Call singerList(int offset, int limit, int area, int sex,
                                  int order, String abc, JsonCallback<SingerList> callback) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("method", "baidu.ting.artist.getList");
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("area", area);
        params.put("sex", sex);
        params.put("order", order);
        if (!TextUtils.isEmpty(abc)) {
            params.put("abc", abc);
        }
        return AppHttpClient.get(createRequest(params, 6 * 3600, 0), callback);
    }

    /**
     * 热门艺术家
     *
     * @param offset 偏移量
     * @param limit  获取数量
     * @return
     */
    public static Call hotSinger(int offset, int limit, JsonCallback<SingerList> callback) {
        return singerList(offset, limit, 0, 0, 1, null, callback);
    }

    /**
     * 歌手信息
     *
     * @param artistId id
     * @param callback
     * @return
     */
    public static Call singerInfo(String artistId, JsonCallback<Singer> callback) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("method", "baidu.ting.artist.getinfo");
        params.put("artistid", artistId);
        return AppHttpClient.get(createRequest(params, 2 * 3600, 0), callback);
    }

    /**
     * 艺术家专辑
     *
     * @param artistId id
     * @param offset   偏移
     * @param limits   数量
     * @return
     */
    public static Call singerAlbums(String artistId, int offset, int limits,
                                    JsonCallback<SingerAlbums> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("method", "baidu.ting.artist.getAlbumList");
        params.put("artistid", artistId);
        params.put("offset", offset);
        params.put("limits", limits);
        params.put("order", 1);//按时间排序
        return AppHttpClient.get(createRequest(params, 2 * 3600, 0), callback);
    }

    /**
     * 歌手歌曲
     *
     * @param artistId id
     * @param offset   偏移
     * @param limits   数量
     * @return
     */
    public static Call singerSongs(String artistId, int offset, int limits,
                                   JsonCallback<SingerSongs> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("method", "baidu.ting.artist.getSongList");
        params.put("artistid", artistId);
        params.put("offset", offset);
        params.put("limits", limits);
        return AppHttpClient.get(createRequest(params, 2 * 3600, 0), callback);
    }

    /**
     * 专辑歌曲
     *
     * @param albumId
     * @param callback
     * @return
     */
    public static Call albumSongs(String albumId, JsonCallback<AlbumSongs> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("method", "baidu.ting.album.getAlbumInfo");
        params.put("album_id", albumId);
        return AppHttpClient.get(createRequest(params, 6 * 3600, 0), callback);
    }

    /**
     * 排行榜
     *
     * @param callback
     * @return
     */
    public static Call billCategory(JsonCallback<BillCategoryData> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("method", "baidu.ting.billboard.billCategory");
        params.put("kflag", 1);
        return AppHttpClient.get(createRequest(params, 12 * 3600, 0), callback);
    }

    /**
     * 排行榜歌曲
     *
     * @param type
     * @param offset
     * @param limit
     * @param callback
     * @return
     */
    public static Call billSongList(int type, int offset, int limit, JsonCallback<BillSongList> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("method", "baidu.ting.billboard.billList");
        params.put("type", type);
        params.put("offset", offset);
//        params.put("fields", "song_id,title,author,album_title,pic_big,pic_small,havehigh,all_rate,charge,has_mv_mobile");
        return AppHttpClient.get(createRequest(params, 12 * 3600, 0), callback);
    }

    /**
     * 所有歌单
     *
     * @param pageNo
     * @param pageSize
     * @param callback
     * @return
     */
    public static Call songList(int pageNo, int pageSize, JsonCallback<SongListData> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("method", "baidu.ting.diy.gedan");
        params.put("page_size", pageSize);
        params.put("page_no", pageNo);
        return AppHttpClient.get(createRequest(params, 2 * 3600, 0), callback);
    }

    /**
     * 歌单分类
     *
     * @param callback
     * @return
     */
    public static Call songListCategory(JsonCallback<SongListCategory> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("method", "baidu.ting.diy.gedanCategory");
        return AppHttpClient.get(createRequest(params, 24 * 3600, 0), callback);
    }

    /**
     * 标签下的歌单
     *
     * @param tag
     * @param page
     * @param pageSize
     * @return
     */
    public static Call songListByTag(String tag, int page, int pageSize, JsonCallback<TagSongListData> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("method", "baidu.ting.diy.search");
        params.put("page_no", page);
        params.put("page_size", pageSize);
        params.put("query", tag);
        return AppHttpClient.get(createRequest(params, 2 * 3600, 0), callback);
    }

    /**
     * 官方歌单
     *
     * @param pn       偏移量
     * @param rn       每页数量
     * @param callback
     * @return
     */
    public static Call officialSongList(int pn, int rn, JsonCallback<OfficialSongListData> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("method", "baidu.ting.diy.getOfficialDiyList");
        params.put("pn", pn);
        params.put("rn", rn);
        params.put("type", 1);
        return AppHttpClient.get(createRequest(params, 6 * 3600, 0), callback);
    }

    /**
     * 官方歌单歌曲
     *
     * @param code 歌单代码
     * @return
     */
    public static Call officialSongListSongs(String code, JsonCallback<OfficialSongListSongs> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("method", "baidu.ting.diy.getSongFromOfficalList");
        params.put("code", code);
        return AppHttpClient.get(createRequest(params, 2 * 3600, 0), callback);
    }

    private static Request createRequest(Map<String, Object> params, int maxAge, int maxStale) {
        StringBuilder sb = new StringBuilder(BASE);
        Set<String> keys = params.keySet();
        for (String name : keys) {
            String value = String.valueOf(params.get(name));
            sb.append("&").append(name).append("=").append(value);
        }
        CacheControl.Builder cacheControl = new CacheControl.Builder();
        if (maxAge != 0) {
            cacheControl.maxAge(maxAge, TimeUnit.SECONDS);
        }
        if (maxStale != 0) {
            cacheControl.maxStale(maxStale, TimeUnit.SECONDS);
        }
        return new Request.Builder()
                .addHeader("User-Agent", "android_5.8.0.1;baiduyinyue")
                .url(sb.toString())
                .cacheControl(cacheControl.build())
                .build();
    }
}
