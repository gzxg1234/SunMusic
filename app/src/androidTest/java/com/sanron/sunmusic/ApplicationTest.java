package com.sanron.sunmusic;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.sanron.sunmusic.music.LocalMusicLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testSearchLocalMusic() throws IOException {
        long startTime = System.currentTimeMillis();
        List<File> files = LocalMusicLoader.load();
        long endTime = System.currentTimeMillis();
        Log.d("spent time", (endTime - startTime) + "");
        for (File file : files) {
            Log.d("search music", "filepath=" + file.getAbsolutePath() + ",size=" + file.length());
        }
    }

}