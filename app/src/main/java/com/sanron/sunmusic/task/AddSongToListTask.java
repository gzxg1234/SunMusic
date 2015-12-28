package com.sanron.sunmusic.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.DataProvider;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;

/**
 * 添加歌曲至列表
 */
public abstract class AddSongToListTask extends AsyncTask<Void, Void, Integer> {
    private PlayList playList;
    private SongInfo songInfo;

    public AddSongToListTask(PlayList playList, SongInfo songInfo) {
        this.playList = playList;
        this.songInfo = songInfo;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.TABLE_LISTSONGS);

        ContentValues values = new ContentValues(2);
        values.put(DBHelper.LISTSONGS_LISTID,playList.getId());
        values.put(DBHelper.LISTSONGS_SONGID,songInfo.getId());
        if(songInfo.getType() == SongInfo.TYPE_LOCAL) {
            //添加本地歌曲
            //检查是否已经存在于列表中
            Cursor cursor = access.query(values);
            boolean isExists = cursor.moveToFirst();
            if (isExists) {
                return -1;
            }
        }else if(songInfo.getType() == SongInfo.TYPE_WEB){

        }

        //插入
        int num = access.blukInsert(values);
        if(num > 0) {
            DataProvider.instance().notifyDataChanged(DBHelper.TABLE_PLAYLIST);
        }

        access.close();
        return num;
    }

}