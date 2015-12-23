package com.sanron.sunmusic.task;

import android.content.Context;

import com.sanron.sunmusic.db.PlayListProvider;
import com.sanron.sunmusic.model.PlayList;

import java.util.List;

/**
 * Created by Administrator on 2015/12/24.
 */
public abstract class UpdatePlayListNameTask extends DBAccessTask<PlayList,Void,Integer> {


    @Override
    protected Integer doInBackground(PlayList... params) {
        PlayList update = params[0];
        PlayListProvider listProvider = PlayListProvider.instance();

        //检查列表名是否已存在
        PlayList query = new PlayList();
        query.setName(update.getName());
        List<PlayList> result = listProvider.query(query);
        if(result.size()>0){
            //列表名已存在
            if(result.get(0).getId() != update.getId()) {
                return -1;
            }
        }
        return listProvider.update(update);
    }
}
