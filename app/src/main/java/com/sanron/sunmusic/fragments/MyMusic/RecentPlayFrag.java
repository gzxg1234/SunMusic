package com.sanron.sunmusic.fragments.MyMusic;

import com.sanron.sunmusic.adapter.DataListAdapter;
import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.model.Music;

/**
 * Created by Administrator on 2015/12/21.
 */
public class RecentPlayFrag extends BaseListFrag<Music> {


    public RecentPlayFrag() {
        super(LAYOUT_LINEAR,null);
    }

    @Override
    protected void bindViewHolder(DataListAdapter.ItemHolder holder, int position) {

    }

    @Override
    public void refreshData() {

    }
}
