package com.sanron.sunmusic.task;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.PlayListProvider;
import com.sanron.sunmusic.model.PlayList;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取播放列表
 */
public abstract class GetPlayListsTask extends AsyncTask<Void, Void, List<PlayList>> {


    @Override
    protected List<PlayList> doInBackground(Void... params) {
        PlayListProvider playListProvider = PlayListProvider.instance();
        List<PlayList> playLists = new ArrayList<>();
        ContentValues values = new ContentValues(1);
        Cursor cursor = null;

        //我喜欢
        values.put(DBHelper.PLAYLIST_TYPE,PlayList.TYPE_FAVORITE);
        cursor = playListProvider.query(values);
        while(cursor.moveToNext()){
            playLists.add(PlayList.fromCursor(cursor));
        }
        cursor.close();

        //用户列表
        values.put(DBHelper.PLAYLIST_TYPE,PlayList.TYPE_USER);
        cursor = playListProvider.query(values);
        while(cursor.moveToNext()){
            playLists.add(PlayList.fromCursor(cursor));
        }
        cursor.close();

        return playLists;
    }

}