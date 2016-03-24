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
    private PlayList playlist;
    private List<Music> addSongs;

    public AddMusicToListTask(PlayList playList, List<Music> musics) {
        this.playlist = playList;
        this.addSongs = musics;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.ListData.TABLE);
        access.beginTransaction();
        int insertNum = 0;//添加成功数量
        ContentValues values = new ContentValues(2);
        for (int i = 0; i < addSongs.size(); i++) {
            Music music = addSongs.get(i);
            //检查是否已经存在于列表中
            String sql = "select 1 from " + DBHelper.ListData.TABLE
                    + " where " + DBHelper.ListData.LIST_ID + "=?"
                    + " and " + DBHelper.ListData.MUSIC_ID + "=?";
            Cursor cursor = access.rawQuery(sql, String.valueOf(playlist.getId()),
                    String.valueOf(music.getId()));
            if (!cursor.moveToFirst()) {
                values.put(DBHelper.ListData.LIST_ID, playlist.getId());
                values.put(DBHelper.ListData.MUSIC_ID, music.getId());
                if (access.insert(null, values) != -1) {
                    insertNum++;
                }
            }
        }
        access.setTransactionSuccessful();
        access.endTransaction();
        access.close();
        return insertNum;
    }

}