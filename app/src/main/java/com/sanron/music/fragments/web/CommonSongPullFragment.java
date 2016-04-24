package com.sanron.music.fragments.web;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.adapter.SongItemAdapter;
import com.sanron.music.fragments.base.PullFragment;
import com.sanron.music.service.IPlayer;
import com.sanron.music.service.PlayerUtil;

/**
 * Created by sanron on 16-4-24.
 */
public abstract class CommonSongPullFragment extends PullFragment implements IPlayer.OnPlayStateChangeListener {


    protected ImageView mIvPicture;
    protected TextView mTvText1;
    protected TextView mTvText2;
    protected TextView mTvSongNum;
    protected ImageButton mIbtnFavorite;
    protected ImageButton mIbtnDownload;
    protected ImageButton mIbtnShare;
    protected SongItemAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.web_frag_common_song, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = createAdapter();
    }

    protected abstract SongItemAdapter createAdapter();

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
    }

    @Override
    public void onPlayerReady() {
        PlayerUtil.addPlayStateChangeListener(this);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PlayerUtil.removePlayStateChangeListener(this);
    }


    @Override
    public void onPlayStateChange(int state) {
        if (state == IPlayer.STATE_PREPARING
                || state == IPlayer.STATE_STOP) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
