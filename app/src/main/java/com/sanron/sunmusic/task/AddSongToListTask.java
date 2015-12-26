package com.sanron.sunmusic.task;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.ListSongsProvider;
import com.sanron.sunmusic.db.PlayListProvider;
import com.sanron.sunmusic.db.SongInfoProvider;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;

import java.util.List;

/**
 * 添加歌曲至列表
 */
public abstract class AddSongToListTask extends AsyncTask<Void, Void, Integer> {
    private PlayList playList;
    private SongInfo songInfo;

    public AddSongToListTask(PlayList playList, SongInfo songInfo) {
        this.playList = playList;
        this.songInfo = songInfo;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        ListSongsProvider listSongsProvider = ListSongsProvider.instance();
        PlayListProvider playListProvider = PlayListProvider.instance();
        SongInfoProvider songInfoProvider = SongInfoProvider.instance();

        ContentValues values = new ContentValues(2);
        values.put(DBHelper.LISTSONGS_LISTID,playList.getId());
        values.put(DBHelper.LISTSONGS_SONGID,songInfo.getId());
        if(songInfo.getType() == SongInfo.TYPE_LOCAL) {
            //添加本地歌曲
            //检查是否已经存在于列表中
            Cursor cursor = listSongsProvider.query(values);
            boolean isExists = cursor.moveToFirst();
            cursor.close();
            if (isExists) {
                return -1;
            }
        }else if(songInfo.getType() == SongInfo.TYPE_WEB){

        }

        //插入
        int num = listSongsProvider.blukInsert(values);
        if(num > 0) {
            playListProvider.notifyDataChanged();
        }

        listSongsProvider.notifyObservers();
        return num;
    }

}