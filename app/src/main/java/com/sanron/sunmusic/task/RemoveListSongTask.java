package com.sanron.sunmusic.task;

import android.content.Context;

import com.sanron.sunmusic.db.ListSongsProvider;
import com.sanron.sunmusic.db.PlayListProvider;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 删除列表歌曲
 */
public abstract class RemoveListSongTask extends DBAccessTask<Void, Void, Integer> {
    private PlayList playList;
    private List<SongInfo> deleteSongs;

    public RemoveListSongTask(PlayList playList, SongInfo songInfo) {
        this.playList = playList;
        this.deleteSongs = new ArrayList<>();
        deleteSongs.add(songInfo);
    }

    public RemoveListSongTask(PlayList playList, List<SongInfo> deleteSongs) {
        this.playList = playList;
        this.deleteSongs = deleteSongs;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        ListSongsProvider listSongsProvider = ListSongsProvider.instance();
        PlayListProvider playListProvider = PlayListProvider.instance();

        //插入
        Long[] songids = new Long[deleteSongs.size()];
        for (int i = 0; i < deleteSongs.size(); i++) {
            songids[i] = deleteSongs.get(i).getId();
        }
        int num = listSongsProvider.delete(playList.getId(), songids);
        if (num > 0) {
            //成功，更新playlist表songnum字段
            playList.setSongNum(playList.getSongNum() - num);
            playListProvider.update(playList);
        }
        return num;
    }

}