package com.sanron.sunmusic.task;

import android.content.Context;

import com.sanron.sunmusic.db.PlayListDao;
import com.sanron.sunmusic.db.SongInfoDao;
import com.sanron.sunmusic.model.SongInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/22.
 */
public abstract class GetPlayListSongsTask extends DBAccessTask<Long, Void, List<SongInfo>> {


    public GetPlayListSongsTask(Context context) {
        super(context);
    }

    @Override
    protected List<SongInfo> doInBackground(Long... params) {
        Long listid = params[0];
        List<SongInfo> listSongs = new ArrayList<>();
        PlayListDao playListDao = new PlayListDao(mContextRef.get());
        SongInfoDao songInfoDao = new SongInfoDao(mContextRef.get());
        List<Long> songIds = playListDao.queryListSongs(listid);
        for (int i = 0; i < songIds.size(); i++) {
            long sonid = songIds.get(i);
            SongInfo songInfo = songInfoDao.queryById(sonid);
            if (songInfo != null) {
                listSongs.add(songInfo);
            }
        }
        playListDao.closeDatabase();
        songInfoDao.closeDatabase();
        return listSongs;
    }

}
