package com.sanron.ddmusic.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.ddmusic.db.DBHelper;
import com.sanron.ddmusic.db.DataProvider;
import com.sanron.ddmusic.db.bean.Music;

import java.util.Date;

/**
 * Created by sanron on 16-5-29.
 */
public class FavoriteMusicTask extends AsyncTask<Void, Void, Boolean> {

    private Music mMusic;

    public FavoriteMusicTask(Music music) {
        mMusic = music;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success = false;
        DataProvider.Access listMemeberAccess = DataProvider.get().newAccess(DBHelper.ListMember.TABLE);
        DataProvider.Access musicAccess = DataProvider.get().newAccess(DBHelper.Music.TABLE);
        DataProvider.get().beginTransaction();
        try {
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
            if (id == 0) {
                id = musicAccess.insert(null, mMusic.toContentValues());
            }
            if (id > 0) {
                long time = new Date().getTime();
                ContentValues values = new ContentValues(3);
                values.put(DBHelper.ListMember.ADD_TIME, time);
                values.put(DBHelper.ListMember.LIST_ID, DBHelper.List.TYPE_FAVORITE_ID);
                values.put(DBHelper.ListMember.MUSIC_ID, id);
                if (musicAccess.insert(null, values) != -1) {
                    success = true;
                }
                DataProvider.get().setTransactionSuccessful();
            }
        } catch (Exception e) {

        } finally {
            listMemeberAccess.close();
            musicAccess.close();
            DataProvider.get().endTransaction();
        }
        return success;
    }
}
