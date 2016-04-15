package com.sanron.music;

import android.content.ContentValues;
import android.os.Environment;
import android.os.SystemClock;

import com.sanron.music.bdmusic.BMA;
import com.sanron.music.db.model.Music;
import com.sanron.music.net.ApiCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.LrcPicResult;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void testApi() throws Exception {

        System.out.println(BMA.Tag.allTag());
        System.out.println(BMA.Tag.tagSongList("劲爆", 100, 100));
        System.out.println(BMA.FocusPic.focusPic(10));
        System.out.println(BMA.GeDan.hotGeDan(3));
        System.out.println(BMA.Song.songInfo("7313983"));
        System.out.println(BMA.Search.searchLrcPic("天黑黑", "胡彦斌", 2));
        System.out.println(BMA.Song.baseInfo("7313983"));
        System.out.println(BMA.GeDan.geDanInfo("6432"));
        System.out.println(BMA.GeDan.hotGeDan(3));
        System.out.println(BMA.GeDan.geDan(1, 2));
        System.out.println(BMA.Album.recommendAlbum(0, 10));
        System.out.println(BMA.Tag.hotTag(4));
        System.out.println(BMA.Tag.allTag());
        System.out.println(BMA.Song.recommendSong(10));
        System.out.println(BMA.Artist.artistSongList("54505", 0, 10));
    }

    @Test
    public void testA() {
        System.out.println(BMA.Song.songInfo("7313983"));
        System.out.println(BMA.Search.searchLrcPic("摩羯座", "周杰伦", 1));
        System.out.println(BMA.Search.searchSugestion("不得不爱"));
        System.out.println(BMA.Scene.constantScene());
    }

    @Test
    public void testJson() throws IOException {
        String json = "{\"content\":[{\"title\":434}]}";
//        String json = "{\"content\":\"\"}";
//        String json = "{\"content\":}";
//        JsonUtil.fromJson(json, RecommendSongResult.class);
    }
}