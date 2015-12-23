package com.sanron.sunmusic.task;

import android.content.Context;

import com.sanron.sunmusic.db.PlayListProvider;
import com.sanron.sunmusic.model.PlayList;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取播放列表
 */
public abstract class GetPlayListsTask extends DBAccessTask<Void, Void, List<PlayList>> {


    @Override
    protected List<PlayList> doInBackground(Void... params) {
        PlayListProvider playListProvider = PlayListProvider.instance();
        List<PlayList> playLists = new ArrayList<>();
        PlayList query = new PlayList();
        query.setType(PlayList.TYPE_FAVORITE);
        playLists.addAll(playListProvider.query(query));
        query.setType(PlayList.TYPE_USER);
        playLists.addAll(playListProvider.query(query));
        return playLists;
    }
}