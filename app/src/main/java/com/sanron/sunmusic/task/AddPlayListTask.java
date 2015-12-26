package com.sanron.sunmusic.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.PlayListProvider;
import com.sanron.sunmusic.model.PlayList;

/**
 * 新建列表
 * Created by Administrator on 2015/12/24.
 */
public abstract class AddPlayListTask extends AsyncTask<String,Void,Integer> {

    @Override
    protected Integer doInBackground(String... params) {
        String listName = params[0];
        PlayListProvider listProvider = PlayListProvider.instance();
        ContentValues values = new ContentValues(2);
        values.put(DBHelper.PLAYLIST_NAME,listName);

        //检查是否重名
        Cursor cursor = listProvider.query(values);
        boolean isExists = cursor.moveToFirst();
        cursor.close();
        if(isExists){
            return -1;
        }

        //插入
        values.put(DBHelper.PLAYLIST_TYPE,PlayList.TYPE_USER);
        int num = listProvider.blukInsert(values);
        listProvider.notifyObservers();
        return num;
    }

}
