package com.sanron.music;

import com.sanron.music.bdmusic.BMA;

import org.junit.Test;

import java.io.IOException;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void testApi() throws Exception {
        System.out.println(BMA.GeDan.hotGeDan(3));
        System.out.println(BMA.GeDan.geDan(1,2));
        System.out.println(BMA.Album.recommendAlbum(0,10));
        System.out.println(BMA.Tag.hotTag(4));
        System.out.println(BMA.Tag.allTag());
        System.out.println(BMA.Song.recommendSong(10));
        System.out.println(BMA.Song.songInfo("7313983"));
        System.out.println(BMA.GeDan.geDanInfo("6432"));
        System.out.println(BMA.Artist.artistSongList("54505", 0, 10));
        System.out.println(BMA.Tag.tagSongList("伤感",10));
    }

    @Test
    public void testA(){
        System.out.println(BMA.Search.searchLrcPic("摩羯座","周杰伦",1));
        System.out.println(BMA.Search.searchSugestion("不得不爱"));
        System.out.println(BMA.Song.songInfo("293547"));
        System.out.println(BMA.Scene.constantScene());
    }

    @Test
    public void testJson() throws IOException {
        String json = "{\"content\":[{\"title\":434}]}";
//        String json = "{\"content\":\"\"}";
//        String json = "{\"content\":}";
//        JsonUtils.fromJson(json, RecommendSongResult.class);
    }
}