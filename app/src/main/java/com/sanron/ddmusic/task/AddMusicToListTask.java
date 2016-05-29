package com.sanron.ddmusic.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.ddmusic.db.DBHelper;
import com.sanron.ddmusic.db.DataProvider;
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.db.bean.PlayList;

import java.util.Date;
import java.util.List;

/**
 * 添加歌曲至列表
 */
public abstract class AddMusicToListTask extends AsyncTask<Void, Void, Integer> {
    private PlayList mPlayList;
    private List<Music> mAddMusics;

    public AddMusicToListTask(PlayList playList, List<Music> musics) {
        this.mPlayList = playList;
        this.mAddMusics = musics;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        DataProvider.Access access = DataProvider.get().newAccess(DBHelper.ListMember.TABLE);
        DataProvider.get().beginTransaction();
        int insertNum = 0;//添加成功数量
        long time = new Date().getTime();
        ContentValues values = new ContentValues(3);
        values.put(DBHelper.ListMember.ADD_TIME, time);
        for (int i = 0; i < mAddMusics.size(); i++) {
            Music music = mAddMusics.get(i);
            //检查是否已经存在于列表中
            String sql = "select 1 from " + DBHelper.ListMember.TABLE
                    + " where " + DBHelper.ListMember.LIST_ID + "=?"
                    + " and " + DBHelper.ListMember.MUSIC_ID + "=?";
            Cursor cursor = access.rawQuery(sql, String.valueOf(mPlayList.getId()),
                    String.valueOf(music.getId()));
            if (!cursor.moveToFirst()) {
                values.put(DBHelper.ListMember.LIST_ID, mPlayList.getId());
                values.put(DBHelper.ListMember.MUSIC_ID, music.getId());
                if (access.insert(null, values) != -1) {
                    insertNum++;
                }
            }
        }
        DataProvider.get().setTransactionSuccessful();
        DataProvider.get().endTransaction();
        access.close();
        return insertNum;
    }

}