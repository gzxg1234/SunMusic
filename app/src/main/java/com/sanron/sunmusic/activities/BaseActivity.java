package com.sanron.sunmusic.activities;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Sanron on 2015/12/15.
 */
public class BaseActivity extends AppCompatActivity {

    protected <T extends View> T $(int id){
        return (T) findViewById(id);
    }
}
