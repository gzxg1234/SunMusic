package com.sanron.ddmusic;

import android.app.Application;
import android.media.MediaScannerConnection;
import android.os.SystemClock;
import android.test.ApplicationTestCase;

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