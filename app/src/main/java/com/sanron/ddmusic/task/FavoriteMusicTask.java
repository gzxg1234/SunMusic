package com.sanron.ddmusic.task;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.sanron.ddmusic.db.AppDB;
import com.sanron.ddmusic.db.ListMemberHelper;
import com.sanron.ddmusic.db.MusicHelper;
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.db.bean.PlayList;

/**
 * 收藏音乐
 * Created by sanron on 16-5-29.
 */
public class FavoriteMusicTask extends AsyncTask<Void, Void, Boolean> {

    private Music mMusic;
    private Context mContext;

    public FavoriteMusicTask(Context context, Music music) {
        mContext = context.getApplicationContext();
        mMusic = music;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success = false;
        SQLiteDatabase db = AppDB.get(mContext).getWritableDatabase();
        db.beginTransaction();
        try {
            long id = mMusic.getId();
            if (mMusic.getType() == Music.TYPE_WEB) {
                //网络歌曲,先查在数据库中的id
                Music q = MusicHelper.getMusicBySongId(db, mMusic.getSongId());
                if (q != null) {
                    id = q.getId();
                }
            }
            if (id == 0) {
                id = MusicHelper.addMusic(db, mMusic);
            }
            if (id > 0) {
                long time = System.currentTimeMillis();
                long insertId = ListMemberHelper.addMusicToList(db, id, PlayList.TYPE_FAVORITE_ID, time);
                if (insertId != -1) {
                    success = true;
                }
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {

        } finally {
            db.endTransaction();
        }
        if (success) {
            LocalBroadcastManager.getInstance(mContext)
                    .sendBroadcast(new Intent("PlayListUpdate"));
        }
        return success;
    }
}
