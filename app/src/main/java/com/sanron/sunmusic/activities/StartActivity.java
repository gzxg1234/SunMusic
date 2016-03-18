package com.sanron.sunmusic.activities;

import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Administrator on 2016/3/5.
 */
public class StartActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
        Intent intent = new Intent(StartActivity.this,MainActivity.class);
        startActivity(intent);

    }
}
