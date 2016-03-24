package com.sanron.music.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.sanron.music.AppContext;
import com.sanron.music.AppManager;

/**
 * Created by Sanron on 2015/12/15.
 */
public class BaseActivity extends AppCompatActivity {

    protected AppContext appContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.instance().addActivity(this);
        appContext = (AppContext) getApplicationContext();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.instance().removeActivity(this);
    }


    protected <T extends View> T $(int id) {
        return (T) findViewById(id);
    }
}
