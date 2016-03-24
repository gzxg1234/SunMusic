package com.sanron.music.activities;

import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.sanron.music.R;
import com.sanron.music.db.DBAccess;
import com.sanron.music.db.model.Music;
import com.sanron.music.utils.AudioTool;
import com.sanron.music.utils.MusicScanner;
import com.sanron.music.utils.MyLog;
import com.sanron.music.utils.TUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by sanron on 16-3-22.
 */
public class ScanActivity extends BaseActivity implements View.OnClickListener {


    private MusicScanner scanner;

    private Toolbar toolbar;
    private Button btnStart;
    private LinearLayout llFindSongNum;
    private TextView tvFindNum;
    private TextView tvFileName;
    private CheckBox cbIgnore;
    private List<Music> scanResult;

    public static final String[] PROJECTIONS = new String[]{
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DISPLAY_NAME
    };
    public static final String DURATION_SELECTION = MediaStore.Audio.Media.DURATION + ">60000";

    public static final int MENU_DIY_SCAN = 1;
    public static final int REQUEST_CODE_DIY = 1;
    public static final String TEXT_START_SCAN = "开始扫描";
    public static final String TEXT_STOP_SCAN = "停止扫描";
    public static final String TEXT_FINISH = "完成";
    public static final String TAG = ScanActivity.class.getSimpleName();

    private MusicScanner.OnScanMediaListener listener = new MusicScanner.OnScanMediaListener() {
        @Override
        public void onStart() {
            scanResult.clear();
            tvFindNum.setText("0");
            tvFileName.setVisibility(View.VISIBLE);
            llFindSongNum.setVisibility(View.VISIBLE);
            cbIgnore.setVisibility(View.INVISIBLE);
            btnStart.setText(TEXT_STOP_SCAN);
            tvFileName.setText("正在扫描...");
        }

        @Override
        public void onProgress(final String filePath, Uri uri) {
            Cursor cursor = getContentResolver().query(uri,
                    PROJECTIONS,
                    cbIgnore.isChecked() ? DURATION_SELECTION : null,
                    null,
                    MediaStore.Audio.Media.TITLE_KEY);

            if (cursor.moveToFirst()) {
                String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String titleKey = (title == null ?
                        null : PinyinHelper.convertToPinyinString(title, "", PinyinFormat.WITHOUT_TONE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String albumKey = (album == null ?
                        null : PinyinHelper.convertToPinyinString(album, "", PinyinFormat.WITHOUT_TONE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String artistKey = (artist == null ?
                        null : PinyinHelper.convertToPinyinString(artist, "", PinyinFormat.WITHOUT_TONE));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                int bitrate = AudioTool.readBitrate(path);
                Music music = new Music();
                music.setType(Music.TYPE_LOCAL);
                music.setDisplayName(displayName);
                music.setTitle(title);
                music.setTitleKey(titleKey);
                music.setAlbum(album);
                music.setAlbumKey(albumKey);
                music.setArtist(artist);
                music.setArtistKey(artistKey);
                music.setPath(path);
                music.setBitrate(bitrate);
                music.setDuration(duration);
                scanResult.add(music);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MyLog.i(TAG, "扫描到:" + filePath);
                        tvFileName.setText(filePath);
                        tvFindNum.setText(String.valueOf(scanResult.size()));
                    }
                });
            }
            cursor.close();
        }

        @Override
        public void onCompleted(final boolean fromUser) {
            MyLog.d(TAG, "停止扫描");
            MyLog.d(TAG, "扫描到" + scanResult.size() + "首歌曲");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvFileName.setVisibility(View.INVISIBLE);
                    if (fromUser) {
                        btnStart.setText(TEXT_START_SCAN);
                        cbIgnore.setVisibility(View.VISIBLE);
                        llFindSongNum.setVisibility(View.INVISIBLE);
                    } else {
                        btnStart.setText(TEXT_FINISH);
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_music);
        initView();
    }

    private void initView() {
        toolbar = $(R.id.toolbar);
        btnStart = $(R.id.btn_start_scan);
        cbIgnore = $(R.id.cb_ignore_60);
        tvFindNum = $(R.id.tv_find_song_num);
        tvFileName = $(R.id.tv_filename);
        llFindSongNum = $(R.id.linear1);
        scanResult = new LinkedList<>();
        scanner = new MusicScanner(this);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnStart.setTag("start");
        btnStart.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_DIY_SCAN, Menu.NONE, "自定义扫描")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_DIY_SCAN: {
                startActivityForResult(new Intent(this, ScanDiyActivity.class), REQUEST_CODE_DIY);
            }
            break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_DIY: {
                switch (resultCode) {
                    case RESULT_OK: {
                        String[] paths = data.getStringArrayExtra(ScanDiyActivity.EXTRA_SELECT_PATHS);
                        if (paths != null) {
                            //自定义扫描开始
                            scanner.scan(listener, paths);
                        }
                    }
                    break;
                }
            }
            break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start_scan: {
                String text = ((TextView) view).getText().toString();
                if (TEXT_START_SCAN.equals(text)) {
                    if (scanner.isScanning()) {
                        TUtils.show(this, "正在扫描，请稍后操作");
                    } else {
                        MyLog.d(TAG, "开始扫描");
                        scanner.scan(listener, Environment.getExternalStorageDirectory().getAbsolutePath());
                    }
                } else if (TEXT_STOP_SCAN.equals(text)) {
                    scanner.stopScan();
                } else if (TEXT_FINISH.equals(text)) {
                    //完成扫描，更新数据
                    final ProgressDialog pd = new ProgressDialog(this);
                    pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    new AsyncTask<Void,Void,Void>(){
                        @Override
                        protected void onPreExecute() {
                            pd.show();
                        }

                        @Override
                        protected Void doInBackground(Void... voids) {
                            DBAccess.instance().updateLocalMusic(scanResult);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            pd.dismiss();
                            finish();
                        }
                    }.execute();
                }
            }
            break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanner.stopScan();
    }
}
