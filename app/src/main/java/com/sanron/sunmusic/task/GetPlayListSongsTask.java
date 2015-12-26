package com.sanron.sunmusic.task;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.ListSongsProvider;
import com.sanron.sunmusic.db.SongInfoProvider;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;

import java.util.ArrayList;
import java.util.List;

/**获取播放列表歌曲
 * Created by Administrator on 2015/12/22.
 */
public abstract class GetPlayListSongsTask extends AsyncTask<Void, Void, List<SongInfo>> {

    private PlayList mPlayList;
    public GetPlayListSongsTask(PlayList playList){
        this.mPlayList = playList;
    }

    @Override
    protected List<SongInfo> doInBackground(Void... params) {
        long listid = mPlayList.getId();
        List<SongInfo> listSongs = new ArrayList<>();
        SongInfoProvider songProvider = SongInfoProvider.instance();
        ListSongsProvider listSongsProvider = ListSongsProvider.instance();

        ContentValues values = new ContentValues(1);
        values.put(DBHelper.LISTSONGS_LISTID,listid);
        Cursor cursor = listSongsProvider.query(values);
        values.clear();
        while(cursor.moveToNext()){
            long songid = cursor.getLong(cursor.getColumnIndex(DBHelper.LISTSONGS_SONGID));
            values.put(DBHelper.ID,songid);

            //查歌曲信息
            Cursor c2 = songProvider.query(values);
            if(c2.moveToFirst()){
                listSongs.add(SongInfo.fromCursor(c2));
            }
            c2.close();
        }
        cursor.close();
        return listSongs;
    }
}
