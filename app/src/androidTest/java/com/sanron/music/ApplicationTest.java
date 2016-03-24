package com.sanron.music;

import android.app.Application;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.test.ApplicationTestCase;

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
        File file = new File(Environment.getExternalStorageDirectory(), "/kgmusic/download/弦子 - 舍不得.mp3");
        System.out.println(file.exists());
        System.out.println(file.lastModified() / 1000);
    }
}