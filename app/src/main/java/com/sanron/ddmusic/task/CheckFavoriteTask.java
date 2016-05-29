package com.sanron.ddmusic.task;

import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.ddmusic.db.DBHelper;
import com.sanron.ddmusic.db.DataProvider;
import com.sanron.ddmusic.db.bean.Music;

/**
 * Created by sanron on 16-5-29.
 */
public class CheckFavoriteTask extends AsyncTask<Void, Void, Boolean> {

    private Music mMusic;

    public CheckFavoriteTask(Music music) {
        mMusic = music;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Boolean favorite = false;
        DataProvider.Access listMemeberAccess = DataProvider.get().newAccess(DBHelper.ListMember.TABLE);
        DataProvider.Access musicAccess = DataProvider.get().newAccess(DBHelper.Music.TABLE);
        long id = mMusic.getId();
        if (mMusic.getType() == DBHelper.Music.TYPE_WEB) {
            //网络歌曲,先查在数据库中的id
            Cursor cursor = musicAccess.query(new String[]{DBHelper.ID},
                    DBHelper.Music.SONG_ID + "=?",
                    new String[]{mMusic.getSongId()});
            if (cursor.moveToFirst()) {
                id = cursor.getInt(0);
            }
        }
        if (id != 0) {
            Cursor c = listMemeberAccess.query(new String[]{DBHelper.ID},
                    DBHelper.ListMember.MUSIC_ID + "=? and " + DBHelper.ListMember.LIST_ID + "=?",
                    new String[]{id + "", DBHelper.List.TYPE_FAVORITE_ID + ""});
            favorite = c.moveToFirst();
        }
        listMemeberAccess.close();
        musicAccess.close();
        return favorite;
    }
}
