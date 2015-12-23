package com.sanron.sunmusic.task;

import android.content.Context;

import com.sanron.sunmusic.db.SongInfoProvider;
import com.sanron.sunmusic.model.SongInfo;

import java.util.List;

/**
 * 获取本地歌曲
 * Created by Administrator on 2015/12/21.
 */
public abstract class GetLocalSongsTask extends DBAccessTask<Void, Void, List<SongInfo>> {


    @Override
    protected List<SongInfo> doInBackground(Void... params) {
        SongInfoProvider songProvider = SongInfoProvider.instance();
        SongInfo query = new SongInfo();
        query.setType(SongInfo.TYPE_LOCAL);
        List<SongInfo> songInfos = songProvider.query(query);
        return songInfos;
    }

}
