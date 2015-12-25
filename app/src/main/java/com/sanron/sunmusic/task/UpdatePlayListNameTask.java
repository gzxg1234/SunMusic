package com.sanron.sunmusic.task;

import android.content.Context;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.PlayListProvider;
import com.sanron.sunmusic.model.PlayList;

import java.util.List;

/**
 * 修改播放列表名
 * Created by Administrator on 2015/12/24.
 */
public abstract class UpdatePlayListNameTask extends AsyncTask<Void,Void,Integer> {

    private PlayList mPlayList;
    public UpdatePlayListNameTask(PlayList playList){
        this.mPlayList = playList;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        PlayListProvider listProvider = PlayListProvider.instance();
        int num = 0;
        //检查列表名是否已存在
        PlayList query = new PlayList();
        query.setName(mPlayList.getName());
        List<PlayList> result = listProvider.query(query);
        if(result.size()>0 && result.get(0).getId() != mPlayList.getId()){
            //列表名已存在
            num = -1;
        }else{
            num = listProvider.update(mPlayList);
        }
        listProvider.notifyObservers();
        return num;
    }
}
