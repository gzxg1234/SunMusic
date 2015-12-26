package com.sanron.sunmusic.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.ArtistProvider;
import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.SongInfoProvider;
import com.sanron.sunmusic.model.Artist;
import com.sanron.sunmusic.model.SongInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取本地歌曲
 * Created by Administrator on 2015/12/21.
 */
public abstract class GetArtistsTask extends AsyncTask<Void, Void, List<Artist>> {


    @Override
    protected List<Artist> doInBackground(Void... params) {
        ArtistProvider artistProvider = ArtistProvider.instance();
        List<Artist> artists = new ArrayList<>();
        Cursor cursor = artistProvider.query(null,null,null);
        while(cursor.moveToNext()){
            artists.add(Artist.fromCursor(cursor));
        }
        cursor.close();
        return artists;
    }

}
