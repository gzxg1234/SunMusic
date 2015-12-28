package com.sanron.sunmusic.task;

import android.content.ContentValues;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.DataProvider;

/**
 * 删除播放列表
 */
public abstract class DelPlayListTask extends AsyncTask<Long, Void, Integer> {


    @Override
    protected Integer doInBackground(Long... params) {
        long listid = params[0];
        DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.TABLE_PLAYLIST);
        ContentValues values = new ContentValues();
        values.put(DBHelper.ID,listid);
        int num = access.delete(values);
        access.close();
        return num;
    }

}