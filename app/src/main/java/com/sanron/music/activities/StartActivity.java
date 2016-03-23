package com.sanron.music.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

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
//        Intent intent = new Intent(StartActivity.this,ScanActivity.class);
//        startActivity(intent);
//        Intent intent = new Intent(StartActivity.this,ScanDiyActivity.class);
//        startActivity(intent);
    }

}
