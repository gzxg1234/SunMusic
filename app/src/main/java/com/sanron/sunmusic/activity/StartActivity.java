package com.sanron.sunmusic.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.service.PlayerUtils;

/**
 * Created by Administrator on 2016/3/5.
 */
public class StartActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlayerUtils.bindService(getApplicationContext(), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                StartActivity.this.finish();
                Intent intent = new Intent(StartActivity.this,MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        });
    }
}
