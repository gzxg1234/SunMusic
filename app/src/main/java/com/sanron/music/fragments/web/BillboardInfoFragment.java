package com.sanron.music.fragments.web;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.R;
import com.sanron.music.adapter.RankSongItemAdapter;
import com.sanron.music.api.JsonCallback;
import com.sanron.music.api.MusicApi;
import com.sanron.music.api.bean.BillSongList;
import com.sanron.music.fragments.base.PullFragment;
import com.sanron.music.service.IPlayer;
import com.sanron.music.service.PlayerUtil;
import com.sanron.music.view.DDPullListView;

import okhttp3.Call;

/**
 * Created by sanron on 16-4-24.
 */
public class BillboardInfoFragment extends PullFragment implements View.OnClickListener, DDPullListView.OnLoadListener, IPlayer.OnPlayStateChangeListener {

    private int mBillType;
    private RankSongItemAdapter mAdapter;
    protected TextView mTvText1;
    protected TextView mTvText2;

    protected TextView mTvSongNum;
    protected ImageButton mIbtnFavorite;
    protected ImageButton mIbtnDownload;
    protected ImageButton mIbtnShare;
    protected ImageView mIvPicture;

    public static final String ARG_TYPE = "type";
//    public static final int LOAD_LIMIT = 50;

    public static BillboardInfoFragment newInstance(int type) {
        BillboardInfoFragment billboardInfoFragment = new BillboardInfoFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        billboardInfoFragment.setArguments(args);
        return billboardInfoFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new RankSongItemAdapter(getContext());
        Bundle args = getArguments();
        if (args != null) {
            mBillType = args.getInt(ARG_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.web_frag_songlist, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvSongNum = $(R.id.tv_song_num);
        mIbtnDownload = $(R.id.ibtn_download);
        mIbtnFavorite = $(R.id.ibtn_favorite);
        mIbtnShare = $(R.id.ibtn_share);
        mTvText2 = $(R.id.tv_text2);
        mTvText1 = $(R.id.tv_text1);
        mIvPicture = (ImageView) mTopBoard;

        mIbtnFavorite.setVisibility(View.GONE);
        mIbtnDownload.setOnClickListener(this);
        mIbtnShare.setOnClickListener(this);
        mPullListView.setAdapter(mAdapter);

        mIvPicture.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mIvPicture.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int width = mIvPicture.getWidth();
                int normalHeaderHeight = mIvPicture.getHeight() + mViewOperator.getHeight();
                mPullListView.setmMaxHeaderHeight(width + mViewOperator.getHeight());
                mPullListView.setNormalHeaderHeight(normalHeaderHeight);
            }
        });

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

    @Override
    public void onPlayerReady() {
        super.onPlayerReady();
        PlayerUtil.addPlayStateChangeListener(this);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayStateChange(int state) {
        if (state == IPlayer.STATE_PREPARING
                || state == IPlayer.STATE_STOP) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
