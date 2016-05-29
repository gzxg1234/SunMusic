package com.sanron.ddmusic.fragments.web;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.ddmusic.adapter.RankSongAdapter;
import com.sanron.ddmusic.adapter.SongAdapter;
import com.sanron.ddmusic.api.JsonCallback;
import com.sanron.ddmusic.api.MusicApi;
import com.sanron.ddmusic.api.bean.BillSongList;
import com.sanron.ddmusic.playback.Player;
import com.sanron.ddmusic.view.DDPullListView;

import okhttp3.Call;

/**
 * Created by sanron on 16-4-24.
 */
public class BillboardSongsFragment extends CommonSongPullFragment implements View.OnClickListener, DDPullListView.OnLoadMoreListener, Player.OnPlayStateChangeListener {

    private int mBillType;

    public static final String ARG_TYPE = "type";
//    public static final int LOAD_LIMIT = 50;

    public static BillboardSongsFragment newInstance(int type) {
        BillboardSongsFragment fragment = new BillboardSongsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mBillType = args.getInt(ARG_TYPE);
        }
    }

    @Override
    protected SongAdapter createAdapter() {
        return new RankSongAdapter(getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mIbtnFavorite.setVisibility(View.GONE);
        mIbtnDownload.setOnClickListener(this);
        mIbtnShare.setOnClickListener(this);

        mPullListView.setOnLoadMoreListener(this);
    }


    @Override
    protected void loadData() {
        loadMore(true);
    }

    @Override
    public void onLoadMore() {
        loadMore(false);
    }

    public void loadMore(final boolean first) {
        final Call call = MusicApi.billSongList(mBillType,
                0,
                100, //百度音乐是直接取最多100首歌
                new JsonCallback<BillSongList>() {
                    @Override
                    public void onSuccess(BillSongList billSongList) {
                        if (billSongList != null) {
                            mAdapter.addData(billSongList.songs);
                            ImageLoader.getInstance()
                                    .displayImage(billSongList.billboard.picS640,
                                            mIvPicture);
                            setTitle(billSongList.billboard.name);
                            mTvText1.setText(billSongList.billboard.name);
                            if (!TextUtils.isEmpty(billSongList.billboard.updateDate)) {
                                mTvText2.setText("更新时间:" + billSongList.billboard.updateDate);
                            }
                            mTvSongNum.setText("共" + billSongList.songs.size() + "首歌");
//                            经测试，返回的hasmore是错误的,
//                            mPullListView.setHasMore(billSongList.billboard.havemore == 1);
                            mPullListView.setHasMore(false);
                        }
                        mPullListView.onLoadCompleted();
                        if (first) {
                            hideLoadingView();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (first) {
                            showLoadFailedView();
                        }
                        mPullListView.onLoadCompleted();
                    }
                });
        addCall(call);
    }

    @Override
    public void onClick(View v) {

    }
}
