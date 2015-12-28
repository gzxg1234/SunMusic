package com.sanron.sunmusic.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.DataProvider;
import com.sanron.sunmusic.model.PlayList;

/**
 * 修改播放列表名
 * Created by Administrator on 2015/12/24.
 */
public abstract class UpdatePlayListNameTask extends AsyncTask<Void, Void, Integer> {

    private PlayList mPlayList;

    public UpdatePlayListNameTask(PlayList playList) {
        this.mPlayList = playList;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        DataProvider.Access listAccess = DataProvider.instance().getAccess(DBHelper.TABLE_PLAYLIST);
        int num;
        //检查列表名是否已存在
        ContentValues values = new ContentValues(1);
        values.put(DBHelper.PLAYLIST_NAME, mPlayList.getName());
        Cursor cursor = listAccess.query(values);
        if (cursor.moveToFirst()
                && PlayList.fromCursor(cursor).getId() != mPlayList.getId()) {
            //列表名已存在
            num = -1;
        } else {
            num = listAccess.update(values, DBHelper.ID + "=?", mPlayList.getId() + "");
        }
        listAccess.close();
        return num;
    }
}
