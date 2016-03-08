package com.sanron.sunmusic.fragments.MyMusic;

import com.sanron.sunmusic.adapter.DataListAdapter;
import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.model.Music;

/**
 * Created by Administrator on 2015/12/21.
 */
public class RecentPlayFrag extends BaseListFrag<Music> {


    public RecentPlayFrag(int layout, String[] subscribes) {
        super(layout, subscribes);
    }

    @Override
    protected void bindViewHolder(DataListAdapter.ItemHolder holder, int position) {

    }

    public static RecentPlayFrag newInstance(){
        return new RecentPlayFrag(LAYOUT_LINEAR,new String[]{DBHelper.TABLE_MUSIC,DBHelper.TABLE_LISTMUSIC});
    }

    @Override
    public void refreshData() {

    }
}
