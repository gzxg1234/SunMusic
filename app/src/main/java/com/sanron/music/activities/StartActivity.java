package com.sanron.music.activities;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Bundle;

/**
 * Created by Administrator on 2016/3/5.
 */
public class StartActivity extends BaseActivity {

    public static MediaScannerConnection connection = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        final File root = Environment.getExternalStorageDirectory();
//
//        MediaScannerConnection.MediaScannerConnectionClient mediaScannerConnectionClient = new MediaScannerConnection.MediaScannerConnectionClient() {
//            @Override
//            public void onMediaScannerConnected() {
//                connection.scanFile(root.getPath(),"audio/*");
//            }
//
//            @Override
//            public void onScanCompleted(String s, Uri uri) {
//
//            }
//        };
//        connection = new MediaScannerConnection(this,mediaScannerConnectionClient);
//        connection.connect();
//        　MediaScannerConnection.scanFile();
        finish();
        Intent intent = new Intent(StartActivity.this,MainActivity.class);
        startActivity(intent);

    }
}
