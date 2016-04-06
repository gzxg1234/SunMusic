package com.sanron.music;

import android.app.Application;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.SystemClock;
import android.test.ApplicationTestCase;

import com.sanron.music.net.ApiCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.DetailSongInfo;

import java.io.File;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    public static final String TAG = "MusicApi";

    public ApplicationTest() {
        super(Application.class);
    }

    public MediaScannerConnection connection;


    public void testApi() {
        MusicApi.songLink("7313983", new ApiCallback<DetailSongInfo>() {
            @Override
            public void onFailure() {

            }

            @Override
            public void onSuccess(DetailSongInfo data) {
                System.out.println(data);
            }
        });
        SystemClock.sleep(100000);
    }
}