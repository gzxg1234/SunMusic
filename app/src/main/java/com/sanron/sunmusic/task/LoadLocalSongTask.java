package com.sanron.sunmusic.task;

import android.content.Context;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.SongInfoDao;
import com.sanron.sunmusic.fragments.MySongFrag.LocalSongFrag;
import com.sanron.sunmusic.model.SongInfo;

import java.util.List;

/**
 * Created by Administrator on 2015/12/21.
 */
public class LoadLocalSongTask extends AsyncTask<Void, Void, List<SongInfo>> {

    private Context context;
    private LocalSongFrag localSongFrag;

    public LoadLocalSongTask(Context context, LocalSongFrag localSongFrag) {
        this.context = context;
        this.localSongFrag = localSongFrag;
    }

    @Override
    protected List<SongInfo> doInBackground(Void... params) {
        SongInfoDao songInfoDao = new SongInfoDao(context);
        List<SongInfo> songInfos = songInfoDao.queryByType(SongInfo.TYPE_LOCAL);
        return songInfos;
    }

    @Override
    protected void onPostExecute(List<SongInfo> songInfos) {
        localSongFrag.refreshData(songInfos);
    }
}
