package com.sanron.music.fragments.MyMusic;

import com.sanron.music.adapter.DataListAdapter;
import com.sanron.music.db.Music;

/**
 * Created by Administrator on 2015/12/21.
 */
public class RecentPlayFrag extends BaseDataFrag<Music> {


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
