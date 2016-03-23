package com.sanron.music.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

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
        setTranslucentStatusBar();
        AppManager.instance().addActivity(this);
        appContext = (AppContext) getApplicationContext();
    }

    private void setTranslucentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
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
