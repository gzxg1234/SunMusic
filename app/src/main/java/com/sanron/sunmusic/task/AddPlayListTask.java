package com.sanron.sunmusic.task;

import android.content.Context;

import com.sanron.sunmusic.db.PlayListProvider;
import com.sanron.sunmusic.model.PlayList;

/**
 * Created by Administrator on 2015/12/24.
 */
public abstract class AddPlayListTask extends DBAccessTask<String,Void,Integer> {

    @Override
    protected Integer doInBackground(String... params) {
        String listName = params[0];
        PlayListProvider listProvider = PlayListProvider.instance();
        PlayList playList = new PlayList();
        playList.setName(listName);
        //检查是否重名
        if(listProvider.query(playList).size()>0){
            return -1;
        }
        playList.setType(PlayList.TYPE_USER);
        return listProvider.insert(playList);
    }
}
