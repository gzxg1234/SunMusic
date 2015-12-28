package com.sanron.sunmusic.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.DataProvider;
import com.sanron.sunmusic.model.PlayList;

/**
 * 新建列表
 * Created by Administrator on 2015/12/24.
 */
public abstract class AddPlayListTask extends AsyncTask<String,Void,Integer> {

    @Override
    protected Integer doInBackground(String... params) {
        String listName = params[0];
        DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.TABLE_PLAYLIST);
        ContentValues values = new ContentValues(2);
        values.put(DBHelper.PLAYLIST_NAME,listName);

        //检查是否重名
        Cursor cursor = access.query(values);
        boolean isExists = cursor.moveToFirst();
        cursor.close();
        if(isExists){
            return -1;
        }

        //插入
        values.put(DBHelper.PLAYLIST_TYPE,PlayList.TYPE_USER);
        int num = access.blukInsert(values);
        access.close();
        return num;
    }

}
