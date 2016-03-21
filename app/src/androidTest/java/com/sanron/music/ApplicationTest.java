package com.sanron.music;

import android.app.Application;
import android.os.SystemClock;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.sanron.music.net.ApiCallback;
import com.sanron.music.net.ApiHttpClient;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.RecommendSong;

import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    public static final String TAG = "MusicApi";

    public ApplicationTest() {
        super(Application.class);
    }

    public void testScan(){
    }


    public void testApi() {
        ApiHttpClient.init(getContext());
//        MusicApi.focusPic(10, new ApiCallback<FocusPicResult>() {
//            @Override
//            public void onFailure() {
//                Log.d(TAG,"failure");
//
//            }
//
//            @Override
//            public void onSuccess(FocusPicResult data) {
//                Log.d(TAG,data.getResultCode()+"");
//            }
//        });
//        SystemClock.sleep(2000);


        MusicApi.recmdSongs(10, new ApiCallback<List<RecommendSong>>() {
            @Override
            public void onFailure() {
                Log.d(TAG, "failure");

            }

            @Override
            public void onSuccess(List<RecommendSong> data) {
            }
        });
        SystemClock.sleep(2000);
    }
}