package com.sanron.music;

import android.app.Application;
import android.media.MediaScannerConnection;
import android.os.SystemClock;
import android.test.ApplicationTestCase;

import com.sanron.music.net.ApiCallback;
import com.sanron.music.net.ApiHttpClient;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.AllTag;

import java.io.IOException;

import okhttp3.Call;

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
        SystemClock.sleep(100000);
    }
}