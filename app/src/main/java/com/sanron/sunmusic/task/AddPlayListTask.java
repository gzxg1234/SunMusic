package com.sanron.sunmusic.task;

import android.os.AsyncTask;

import com.sanron.sunmusic.db.PlayListProvider;
import com.sanron.sunmusic.model.PlayList;

/**
 * 新建列表
 * Created by Administrator on 2015/12/24.
 */
public abstract class AddPlayListTask extends AsyncTask<String,Void,Integer> {

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
        int num = listProvider.insert(playList);
        listProvider.notifyObservers();
        return num;
    }
}
