package com.sanron.sunmusic.task;

import android.content.Context;
import android.os.AsyncTask;

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
        Long[] songIds = listSongsProvider.query(listid,-1);
        SongInfo query = new SongInfo();
        for (int i = 0; i < songIds.length; i++) {
            long songid = songIds[i];
            query.setId(songid);
            List<SongInfo> songInfos = songProvider.query(query);
            listSongs.addAll(songInfos);
        }
        return listSongs;
    }

}
