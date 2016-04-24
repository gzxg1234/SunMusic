package com.sanron.music.fragments.web;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.adapter.RankSongItemAdapter;
import com.sanron.music.adapter.SongItemAdapter;
import com.sanron.music.api.JsonCallback;
import com.sanron.music.api.MusicApi;
import com.sanron.music.api.bean.BillSongList;
import com.sanron.music.service.IPlayer;
import com.sanron.music.view.DDPullListView;

import okhttp3.Call;

/**
 * Created by sanron on 16-4-24.
 */
public class BillboardInfoFragment extends CommonSongPullFragment implements View.OnClickListener, DDPullListView.OnLoadListener, IPlayer.OnPlayStateChangeListener {

    private int mBillType;

    public static final String ARG_TYPE = "type";
//    public static final int LOAD_LIMIT = 50;

    public static BillboardInfoFragment newInstance(int type) {
        BillboardInfoFragment fragment = new BillboardInfoFragment();
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
    protected SongItemAdapter createAdapter() {
        return new RankSongItemAdapter(getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mIbtnFavorite.setVisibility(View.GONE);
        mIbtnDownload.setOnClickListener(this);
        mIbtnShare.setOnClickListener(this);

        mPullListView.setOnLoadListener(this);
    }


    @Override
    protected void loadData() {
        loadMore(true);
    }

    @Override
    public void onLoad() {
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
