package com.sanron.music.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;

/**
 * 新建列表
 * Created by Administrator on 2015/12/24.
 */
public abstract class AddPlayListTask extends AsyncTask<String, Void, Integer> {

    public static final int EXISTS = -1;
    public static final int FAILED = 0;
    public static final int SUCCESS = 1;

    @Override
    protected Integer doInBackground(String... params) {
        int result = FAILED;
        String listName = params[0];
        DataProvider.Access listAccess = DataProvider.instance().getAccess(DBHelper.List.TABLE);

        //检查是否重名
        Cursor cursor = listAccess.query(new String[]{DBHelper.ID},
                DBHelper.List.TITLE + "=? and " + DBHelper.List.TYPE + "=?",
                new String[]{listName, String.valueOf(DBHelper.List.TYPE_USER)});
        boolean isExists = cursor.moveToFirst();
        cursor.close();
        if (isExists) {
            result = EXISTS;
        } else {

            ContentValues values = new ContentValues(2);
            values.put(DBHelper.List.TITLE, listName);
            values.put(DBHelper.List.TYPE, DBHelper.List.TYPE_USER);
            //插入
            values.put(DBHelper.List.TYPE, DBHelper.List.TYPE_USER);
            values.put(DBHelper.List.ADD_TIME, System.currentTimeMillis());
            if (listAccess.insert(null, values) != -1) {
                result = SUCCESS;
            }
        }
        listAccess.close();
        return result;
    }

}
