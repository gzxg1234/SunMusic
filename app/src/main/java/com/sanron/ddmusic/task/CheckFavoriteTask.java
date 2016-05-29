package com.sanron.ddmusic.task;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.sanron.ddmusic.db.AppDB;
import com.sanron.ddmusic.db.ListMemberHelper;
import com.sanron.ddmusic.db.MusicHelper;
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.db.bean.PlayList;

/**
 * Created by sanron on 16-5-29.
 */
public class CheckFavoriteTask extends AsyncTask<Void, Void, Boolean> {

    private Music mMusic;
    private Context mContext;

    public CheckFavoriteTask(Context context, Music music) {
        mContext = context.getApplicationContext();
        mMusic = music;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Boolean favorite = false;
        SQLiteDatabase db = AppDB.get(mContext).getWritableDatabase();
        long id = mMusic.getId();
        if (mMusic.getType() == Music.TYPE_WEB) {
            //网络歌曲,先查在数据库中的id
            Music music = MusicHelper.getMusicBySongId(db, mMusic.getSongId());
            if (music != null) {
                id = music.getId();
            }
        }
        if (id != 0) {
            favorite = ListMemberHelper.isExistByMusicIdAndListId(db, PlayList.TYPE_FAVORITE_ID, id);
        }
        return favorite;
    }
}
