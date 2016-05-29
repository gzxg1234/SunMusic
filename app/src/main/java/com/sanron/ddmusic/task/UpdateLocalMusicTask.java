package com.sanron.ddmusic.task;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.sanron.ddmusic.db.AppDB;
import com.sanron.ddmusic.db.BaseHelper;
import com.sanron.ddmusic.db.ListMemberHelper;
import com.sanron.ddmusic.db.MusicHelper;
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.db.bean.PlayList;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * 更新本地歌曲
 * Created by sanron on 16-3-27.
 */
public class UpdateLocalMusicTask extends AsyncTask<Void, Void, Void> {


    private Context mContext;
    private List<Music> musics;
    private boolean isFullScan;

    public UpdateLocalMusicTask(Context context, List<Music> musics, boolean isFullScan) {
        this.musics = musics;
        this.isFullScan = isFullScan;
        mContext = context.getApplicationContext();
    }

    @Override
    protected Void doInBackground(Void... params) {
        long time = System.currentTimeMillis();
        SQLiteDatabase db = AppDB.get(mContext).getWritableDatabase();
        db.beginTransaction();

        //插入数据
        List<Long> ids = new LinkedList<>();
        for (Music music : musics) {
            //检查是否已经存在,对比文件路径和修改时间
            Cursor c = db.query(
                    MusicHelper.Columns.TABLE,
                    new String[]{BaseHelper.ID, MusicHelper.Columns.DATE_MODIFIED},
                    MusicHelper.Columns.DATA + "=?",
                    new String[]{music.getData()}, null, null, null);
            if (c.moveToFirst()) {
                long id = c.getLong(0);
                long dateModified = c.getLong(1);
                if (music.getModifiedDate() == dateModified) {
                    ids.add(id);
                    continue;
                }
            }
            c.close();
            long id = MusicHelper.addMusic(db, music);
            ids.add(id);
        }

        if (isFullScan) {
            //全盘扫描，更新全部
            ListMemberHelper.deleteByListId(db, PlayList.TYPE_LOCAL_ID);

            List<Music> locals = MusicHelper.getMusicByType(db, Music.TYPE_LOCAL);
            for (Music localMusic : locals) {
                long musicid = localMusic.getId();
                String dataPath = localMusic.getData();
                File file = new File(dataPath);
                if (file.exists()) {
                    ListMemberHelper.addMusicToList(db, musicid, PlayList.TYPE_LOCAL_ID, time);
                }
            }
        } else {
            for (Long id : ids) {
                if (ListMemberHelper.isExistByMusicIdAndListId(db, PlayList.TYPE_LOCAL_ID, id)) {
                    continue;
                }
                ListMemberHelper.addMusicToList(db, id, PlayList.TYPE_LOCAL_ID, time);
            }
        }

        db.setTransactionSuccessful();
        db.endTransaction();

        LocalBroadcastManager.getInstance(mContext)
                .sendBroadcast(new Intent("LocalMusicUpdate"));
        return null;
    }

}
