package com.sanron.sunmusic.task;

import android.content.Context;

import com.sanron.sunmusic.db.PlayListDao;
import com.sanron.sunmusic.model.PlayList;

import java.util.ArrayList;
import java.util.List;

public abstract class GetPlayListsTask extends DBAccessTask<Void, Void, List<PlayList>> {

    public GetPlayListsTask(Context context) {
        super(context);
    }

    @Override
    protected List<PlayList> doInBackground(Void... params) {
        PlayListDao playListDao = new PlayListDao(mContextRef.get());
        List<PlayList> playLists = new ArrayList<>();
        playLists.addAll(playListDao.queryByType(PlayList.TYPE_DEFAULT));
        playLists.addAll(playListDao.queryByType(PlayList.TYPE_USER));
        return playLists;
    }
}