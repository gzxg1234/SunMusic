package com.sanron.music.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;

/**
 * Created by Administrator on 2016/3/5.
 */
public class StartActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String s = Environment.getExternalStorageDirectory().getAbsolutePath()+"/pic/";
        ContentValues values = new ContentValues();
        DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.TABLE_MUSIC);
        values.put(DBHelper.MUSIC_PIC,s+"b.jpg");
        access.update(values,DBHelper.ID+"=?",""+2);
        values.put(DBHelper.MUSIC_PIC,s+"c.png");
        access.update(values,DBHelper.ID+"=?",""+3);
        values.put(DBHelper.MUSIC_PIC,s+"d.jpg");
        access.update(values,DBHelper.ID+"=?",""+4);
        values.put(DBHelper.MUSIC_PIC,s+"e.jpg");
        access.update(values,DBHelper.ID+"=?",""+5);
        values.put(DBHelper.MUSIC_PIC,s+"f.jpg");
        access.update(values,DBHelper.ID+"=?",""+6);
        values.put(DBHelper.MUSIC_PIC,s+"g.jpg");
        access.update(values,DBHelper.ID+"=?",""+7);
        values.put(DBHelper.MUSIC_PIC,s+"h.png");
        access.update(values,DBHelper.ID+"=?",""+8);
        values.put(DBHelper.MUSIC_PIC,s+"i.jpg");
        access.update(values,DBHelper.ID+"=?",""+9);
        access.close();

        finish();
        Intent intent = new Intent(StartActivity.this,MainActivity.class);
        startActivity(intent);
//        Intent intent = new Intent(StartActivity.this,ScanActivity.class);
//        startActivity(intent);
//        Intent intent = new Intent(StartActivity.this,ScanDiyActivity.class);
//        startActivity(intent);
    }

}
