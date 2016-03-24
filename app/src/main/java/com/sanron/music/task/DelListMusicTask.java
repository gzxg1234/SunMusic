package com.sanron.music.task;

import android.content.ContentValues;
import android.os.AsyncTask;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.model.PlayList;
import com.sanron.music.db.model.Music;

import java.util.List;

/**
 * 删除列表歌曲
 */
public abstract class DelListMusicTask extends AsyncTask<Void, Void, Integer> {
    private PlayList playList;
    private List<Music> deleteSongs;

    public DelListMusicTask(PlayList playList, List<Music> deleteSongs) {
        this.playList = playList;
        this.deleteSongs = deleteSongs;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.TABLE_LISTMUSIC);

        ContentValues values = new ContentValues();
        int delNum = 0;
        values.put(DBHelper.LISTMUSIC_LISTID,playList.getId());
        for (int i = 0; i < deleteSongs.size(); i++) {
            values.put(DBHelper.LISTMUSIC_MUSICID,deleteSongs.get(i).getId());
            delNum += access.delete(values);
        }
        access.close();
        return delNum;
    }

}