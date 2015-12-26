package com.sanron.sunmusic.task;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.PlayListProvider;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;

import java.util.List;

/**
 * 修改播放列表名
 * Created by Administrator on 2015/12/24.
 */
public abstract class UpdatePlayListNameTask extends AsyncTask<Void,Void,Integer> {

    private PlayList mPlayList;
    public UpdatePlayListNameTask(PlayList playList){
        this.mPlayList = playList;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        PlayListProvider listProvider = PlayListProvider.instance();
        int num = 0;
        //检查列表名是否已存在
        ContentValues values = new ContentValues(1);
        values.put(DBHelper.PLAYLIST_NAME,mPlayList.getName());
        Cursor cursor = listProvider.query(values);
        if(cursor.moveToFirst()
                && PlayList.fromCursor(cursor).getId()!= mPlayList.getId()){
            //列表名已存在
            num = -1;
        }else{
            num = listProvider.update(values,DBHelper.ID+"=?",mPlayList.getId()+"");
        }
        cursor.close();
        listProvider.notifyObservers();
        return num;
    }
}
