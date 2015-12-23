package com.sanron.sunmusic.task;

import android.content.Context;

import com.sanron.sunmusic.db.ListSongsProvider;
import com.sanron.sunmusic.db.SongInfoProvider;
import com.sanron.sunmusic.model.SongInfo;

import java.util.ArrayList;
import java.util.List;

/**获取播放列表歌曲
 * Created by Administrator on 2015/12/22.
 */
public abstract class GetPlayListSongsTask extends DBAccessTask<Long, Void, List<SongInfo>> {



    @Override
    protected List<SongInfo> doInBackground(Long... params) {
        Long listid = params[0];
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
