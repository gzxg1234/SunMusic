package com.sanron.music.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.bean.PlayList;

/**
 * 修改播放列表名
 * Created by Administrator on 2015/12/24.
 */
public abstract class UpdateListNameTask extends AsyncTask<Void, Void, Integer> {
    public static final int EXISTS = -1;
    public static final int FAILED = 0;
    public static final int SUCCESS = 1;
    private PlayList updatePlayList;

    public UpdateListNameTask(PlayList playList) {
        this.updatePlayList = playList;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        DataProvider.Access listAccess = DataProvider.get().newAccess(DBHelper.List.TABLE);
        int result = FAILED;
        //检查列表名是否已存在
        Cursor cursor = listAccess.query(new String[]{DBHelper.ID},
                DBHelper.List.TITLE + "=? and " + DBHelper.List.TYPE + "=?",
                new String[]{updatePlayList.getTitle(), String.valueOf(DBHelper.List.TYPE_USER)});
        if (cursor.moveToFirst()) {
            //列表名已存在
            result = EXISTS;
        } else {
            ContentValues values = new ContentValues(1);
            values.put(DBHelper.List.TITLE, updatePlayList.getTitle());
            int num = listAccess.update(values,
                    DBHelper.ID + "=?",
                    String.valueOf(updatePlayList.getId()));
            if (num > 0) {
                result = SUCCESS;
            }
        }
        listAccess.close();
        return result;
    }
}
