package com.sanron.sunmusic.task;

import android.content.ContentValues;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.DataProvider;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;

import java.util.List;

/**
 * 删除列表歌曲
 */
public abstract class DelListSongTask extends AsyncTask<Void, Void, Integer> {
    private PlayList playList;
    private List<SongInfo> deleteSongs;

    public DelListSongTask(PlayList playList, List<SongInfo> deleteSongs) {
        this.playList = playList;
        this.deleteSongs = deleteSongs;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.TABLE_LISTSONGS);

        ContentValues values = new ContentValues();
        int delNum = 0;
        values.put(DBHelper.LISTSONGS_LISTID,playList.getId());
        for (int i = 0; i < deleteSongs.size(); i++) {
            values.put(DBHelper.LISTSONGS_SONGID,deleteSongs.get(i).getId());
            delNum += access.delete(values);
        }
        access.close();
        return delNum;
    }

}