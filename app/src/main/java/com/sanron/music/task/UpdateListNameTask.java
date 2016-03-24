package com.sanron.music.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.model.PlayList;

/**
 * 修改播放列表名
 * Created by Administrator on 2015/12/24.
 */
public abstract class UpdateListNameTask extends AsyncTask<Void, Void, Integer> {

    private PlayList updatePlayList;

    public UpdateListNameTask(PlayList playList) {
        this.updatePlayList = playList;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        DataProvider.Access listAccess = DataProvider.instance().getAccess(DBHelper.List.TABLE);
        int result;
        //检查列表名是否已存在
        ContentValues values = new ContentValues(1);
        values.put(DBHelper.List.NAME, updatePlayList.getName());
        String sql = "select 1 from " + DBHelper.List.TABLE
                + " where " + DBHelper.List.NAME + "=?"
                + " and " + DBHelper.ID + "!=?";
        Cursor cursor = listAccess.rawQuery(sql,
                updatePlayList.getName(),
                String.valueOf(updatePlayList.getId()));
        if (cursor.moveToFirst()) {
            //列表名已存在
            result = -1;
        } else {
            result = listAccess.update(values,
                    DBHelper.ID + "=?",
                    String.valueOf(updatePlayList.getId()));
        }
        listAccess.close();
        return result;
    }
}
