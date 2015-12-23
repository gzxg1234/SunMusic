package com.sanron.sunmusic.task;

import android.content.Context;

import com.sanron.sunmusic.db.ListSongsProvider;
import com.sanron.sunmusic.db.PlayListProvider;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;

public abstract class DeletePlayListTask extends DBAccessTask<Long, Void, Integer> {


    @Override
    protected Integer doInBackground(Long... params) {
        long listid = params[0];
        ListSongsProvider listSongsProvider = ListSongsProvider.instance();
        PlayListProvider playListProvider = PlayListProvider.instance();

        PlayList delete = new PlayList();
        delete.setId(listid);
        int num = playListProvider.delete(delete);
        //删除播放列表成功，同时删除ListSongs表的相关列
        if(num > 0){
            listSongsProvider.delete(listid);
        }
        return num;
    }

}