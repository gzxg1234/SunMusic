package com.sanron.ddmusic;

import com.sanron.ddmusic.bdmusic.BMA;

import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void testApi() throws Exception {
        System.out.println(BMA.GeDan.geDanByTag("日语",1,20));

        System.out.println(BMA.GeDan.geDanCategory());
        System.out.println(BMA.GeDan.geDan(1, 20));
        System.out.println(BMA.Billboard.billSongList(1, 0, 10));
        System.out.println(BMA.Billboard.billCategory());
        System.out.println(BMA.Album.albumInfo("257122561"));
        System.out.println(BMA.Artist.artistInfo("1746"));
        System.out.println(BMA.Artist.artistAlbums("1746", 0, 10));
        System.out.println(BMA.Artist.artistSongList("1746", 0, 100));
        System.out.println(BMA.Artist.hotArtist(0, 100));
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
    public void testJson() throws IOException, ParseException {
        String json = "{\"date\":\"2000-00-00\"}";
//        String json = "{\"mBillCategory\":\"\"}";
//        String json = "{\"mBillCategory\":}";
//        JsonUtil.fromJson(json, RecommendSongResult.class);
    }
}