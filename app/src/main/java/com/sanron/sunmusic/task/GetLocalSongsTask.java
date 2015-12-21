package com.sanron.sunmusic.task;

import android.content.Context;

import com.sanron.sunmusic.db.SongInfoDao;
import com.sanron.sunmusic.model.SongInfo;

import java.util.List;

/**
 * 读取本地歌曲
 * Created by Administrator on 2015/12/21.
 */
public abstract class GetLocalSongsTask extends DBAccessTask<Void, Void, List<SongInfo>> {

    public GetLocalSongsTask(Context context) {
        super(context);
    }

    @Override
    protected List<SongInfo> doInBackground(Void... params) {
        SongInfoDao songInfoDao = new SongInfoDao(mContextRef.get());
        List<SongInfo> songInfos = songInfoDao.queryByType(SongInfo.TYPE_LOCAL);
        songInfoDao.closeDatabase();
        return songInfos;
    }

}
