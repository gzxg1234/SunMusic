package com.sanron.sunmusic;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.sanron.sunmusic.db.SongInfoDao;
import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.music.SongLoader;

import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testSearchLocalMusic(){
        SongInfoDao songInfoDao = new SongInfoDao(getContext());
        List<SongInfo> songInfos = SongLoader.load(getContext());
        songInfoDao.add(songInfos);
    }
}