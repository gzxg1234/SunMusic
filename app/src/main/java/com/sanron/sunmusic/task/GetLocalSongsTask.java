package com.sanron.sunmusic.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.DataProvider;
import com.sanron.sunmusic.model.SongInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取本地歌曲
 * Created by Administrator on 2015/12/21.
 */
public abstract class GetLocalSongsTask extends AsyncTask<Void, Void, List<SongInfo>> {


    @Override
    protected List<SongInfo> doInBackground(Void... params) {
        DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.TABLE_SONG);
        List<SongInfo> songInfos = new ArrayList<>();
        ContentValues values = new ContentValues(1);
        values.put(DBHelper.SONG_TYPE, SongInfo.TYPE_LOCAL);
        Cursor cursor = access.query(values);
        while(cursor.moveToNext()){
            songInfos.add(SongInfo.fromCursor(cursor));
        }
        access.close();
        return songInfos;
    }

}
