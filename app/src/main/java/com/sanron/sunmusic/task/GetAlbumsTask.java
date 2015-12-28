package com.sanron.sunmusic.task;

import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.DataProvider;
import com.sanron.sunmusic.model.Album;
import com.sanron.sunmusic.model.Artist;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取本地歌曲
 * Created by Administrator on 2015/12/21.
 */
public abstract class GetAlbumsTask extends AsyncTask<Void, Void, List<Album>> {


    @Override
    protected List<Album> doInBackground(Void... params) {
        DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.TABLE_ALBUM);
        List<Album> albums = new ArrayList<>();
        Cursor cursor = access.query(null,null,null);
        while(cursor.moveToNext()){
            albums.add(Album.fromCursor(cursor));
        }
        access.close();
        return albums;
    }

}
