package com.sanron.sunmusic.task;

import android.content.Context;

import com.sanron.sunmusic.db.ListSongsProvider;
import com.sanron.sunmusic.db.PlayListProvider;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;

public abstract class AddSongToListTask extends DBAccessTask<Void, Void, Integer> {
    private PlayList playList;
    private SongInfo songInfo;

    public AddSongToListTask(PlayList playList, SongInfo songInfo) {
        this.playList = playList;
        this.songInfo = songInfo;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        ListSongsProvider listSongsProvider = ListSongsProvider.instance();
        PlayListProvider playListProvider = PlayListProvider.instance();

        //是否已经存在于列表中
        Long[] songids = listSongsProvider.query(playList.getId(), songInfo.getId());
        if (songids.length>0) {
            return -1;
        }

        //插入
        int num = listSongsProvider.insert(playList.getId(), songInfo.getId());
        if (num > 0) {
            //成功，更新playlist表songnum字段
            playList.setSongNum(playList.getSongNum() + num);
            playListProvider.update(playList);
        }
        return num;
    }

}