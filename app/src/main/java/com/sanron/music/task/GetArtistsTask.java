package com.sanron.music.task;

import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.model.Artist;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取本地歌曲
 * Created by Administrator on 2015/12/21.
 */
public abstract class GetArtistsTask extends AsyncTask<Void, Void, List<Artist>> {


    @Override
    protected List<Artist> doInBackground(Void... params) {
        DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.TABLE_ARTIST);
        List<Artist> artists = new ArrayList<>();
        Cursor cursor = access.query(null,null,null);
        while(cursor.moveToNext()){
            artists.add(Artist.fromCursor(cursor));
        }
        access.close();
        return artists;
    }

}
