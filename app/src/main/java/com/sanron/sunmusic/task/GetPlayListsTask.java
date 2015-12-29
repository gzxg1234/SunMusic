package com.sanron.sunmusic.task;

import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.DataProvider;
import com.sanron.sunmusic.model.PlayList;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取播放列表
 */
public abstract class GetPlayListsTask extends AsyncTask<Void, Void, List<PlayList>> {


    @Override
    protected List<PlayList> doInBackground(Void... params) {
        DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.TABLE_PLAYLIST);
        List<PlayList> playLists = new ArrayList<>();
        Cursor cursor = access.query(DBHelper.PLAYLIST_TYPE+"=? or "+DBHelper.PLAYLIST_TYPE+"=?",
                PlayList.TYPE_FAVORITE+"",PlayList.TYPE_USER+"");
        while(cursor.moveToNext()){
            playLists.add(PlayList.fromCursor(cursor));
        }
        access.close();
        return playLists;
    }

}