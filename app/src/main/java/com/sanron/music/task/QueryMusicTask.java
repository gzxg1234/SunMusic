package com.sanron.music.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.model.Music;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取本地歌曲
 * Created by Administrator on 2015/12/21.
 */
public abstract class QueryMusicTask extends AsyncTask<Void, Void, List<Music>> {

    private ContentValues query;

    public QueryMusicTask(ContentValues values){
        query = values;
    }

    @Override
    protected List<Music> doInBackground(Void... params) {
        DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.Music.TABLE);
        List<Music> musics = new ArrayList<>();
        Cursor cursor = access.query(query);
        while(cursor.moveToNext()){
            Music music = Music.fromCursor(cursor);
            musics.add(music);
        }
        access.close();
        return musics;
    }

}
