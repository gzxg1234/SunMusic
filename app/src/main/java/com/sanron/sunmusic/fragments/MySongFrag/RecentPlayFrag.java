package com.sanron.sunmusic.fragments.MySongFrag;

import com.sanron.sunmusic.adapter.DataListAdapter;
import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.fragments.BaseListFrag;
import com.sanron.sunmusic.model.SongInfo;

/**
 * Created by Administrator on 2015/12/21.
 */
public class RecentPlayFrag extends BaseListFrag<SongInfo> {


    public RecentPlayFrag(int layout, String[] subscribes) {
        super(layout, subscribes);
    }

    @Override
    protected void bindViewHolder(DataListAdapter.ItemHolder holder, int position) {

    }

    public static RecentPlayFrag newInstance(){
        return new RecentPlayFrag(LAYOUT_LINEAR,new String[]{DBHelper.TABLE_SONG,DBHelper.TABLE_LISTSONGS});
    }

    @Override
    public void refreshData() {

    }
}
