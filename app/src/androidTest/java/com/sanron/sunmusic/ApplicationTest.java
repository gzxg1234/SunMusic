package com.sanron.sunmusic;

import android.app.Application;
import android.os.SystemClock;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.sanron.sunmusic.net.ApiCallback;
import com.sanron.sunmusic.net.ApiHttpClient;
import com.sanron.sunmusic.net.MusicApi;
import com.sanron.sunmusic.net.bean.Base;
import com.sanron.sunmusic.net.bean.FocusPic;
import com.sanron.sunmusic.net.bean.FocusPicResult;

import java.io.IOException;
import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testApi(){
        MusicApi.focusPic(10, new ApiCallback<FocusPicResult>() {
            @Override
            public void onFailure() {
                System.out.println("failure");
            }

            @Override
            public void onSuccess(FocusPicResult data) {
                List<FocusPic> focusPicList = data.getFocusPicList();
                for(FocusPic focusPic:focusPicList){
                    System.out.println(focusPic.getDesc());
                }
            }
        });
    }
}