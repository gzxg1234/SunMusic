package com.sanron.ddmusic.fragments.mymusic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.sanron.ddmusic.R;
import com.sanron.ddmusic.adapter.RecentPlayAdapter;
import com.sanron.ddmusic.db.AppDB;
import com.sanron.ddmusic.db.ResultCallback;
import com.sanron.ddmusic.db.bean.Music;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Administrator on 2015/12/21.
 */
public class RecentPlayFragment extends BaseDataFragment {


    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    RecentPlayAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new RecentPlayAdapter(getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public int getViewResId() {
        return R.layout.layout_recycler_view;
    }


    @Override
    public void loadData() {
        AppDB.get(getContext()).getRecentPlayList(new ResultCallback<List<Music>>() {
            @Override
            public void onResult(List<Music> result) {
                mAdapter.setData(result);
            }
        });
    }
}
