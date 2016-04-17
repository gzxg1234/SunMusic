package com.sanron.music.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.model.Music;
import com.sanron.music.db.model.PlayList;

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
        DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.ListMember.TABLE);
        DataProvider.instance().beginTransaction();
        int insertNum = 0;//添加成功数量
        ContentValues values = new ContentValues(2);
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
        DataProvider.instance().setTransactionSuccessful();
        DataProvider.instance().endTransaction();
        access.close();
        return insertNum;
    }

}