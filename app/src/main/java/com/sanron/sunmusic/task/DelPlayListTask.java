package com.sanron.sunmusic.task;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.ListSongsProvider;
import com.sanron.sunmusic.db.PlayListProvider;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;

/**
 * 删除播放列表
 */
public abstract class DelPlayListTask extends AsyncTask<Long, Void, Integer> {


    @Override
    protected Integer doInBackground(Long... params) {
        long listid = params[0];
        ListSongsProvider listSongsProvider = ListSongsProvider.instance();
        PlayListProvider playListProvider = PlayListProvider.instance();

        ContentValues values = new ContentValues();
        values.put(DBHelper.ID,listid);
        int num = playListProvider.delete(values);
        playListProvider.notifyObservers();
        listSongsProvider.notifyObservers();
        return num;
    }

}