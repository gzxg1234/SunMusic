package com.sanron.music.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.model.PlayList;
import com.sanron.music.db.model.Music;

import java.util.ArrayList;
import java.util.List;

/**获取播放列表歌曲
 * Created by Administrator on 2015/12/22.
 */
public abstract class GetListMusicTask extends AsyncTask<Void, Void, List<Music>> {

    private PlayList mPlayList;
    public GetListMusicTask(PlayList playList){
        this.mPlayList = playList;
    }

    @Override
    protected List<Music> doInBackground(Void... params) {
        long listid = mPlayList.getId();
        List<Music> listSongs = new ArrayList<>();
        DataProvider.Access songAccess = DataProvider.instance().getAccess(DBHelper.TABLE_MUSIC);
        DataProvider.Access listSongsAccess = DataProvider.instance().getAccess(DBHelper.TABLE_LISTMUSIC);

        ContentValues values = new ContentValues(1);
        values.put(DBHelper.LISTMUSIC_LISTID,listid);
        Cursor cursor = listSongsAccess.query(values);
        values.clear();
        while(cursor.moveToNext()){
            long songid = cursor.getLong(cursor.getColumnIndex(DBHelper.LISTMUSIC_MUSICID));
            values.put(DBHelper.ID,songid);

            //查歌曲信息
            Cursor c2 = songAccess.query(values);
            if(c2.moveToFirst()){
                listSongs.add(Music.fromCursor(c2));
            }
        }
        songAccess.close();
        listSongsAccess.close();
        return listSongs;
    }
}
