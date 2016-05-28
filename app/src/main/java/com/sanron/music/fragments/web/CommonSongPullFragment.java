package com.sanron.music.fragments.web;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.adapter.SongAdapter;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.fragments.base.PullFragment;
import com.sanron.music.playback.Player;
import com.sanron.music.service.PlayerUtil;

import butterknife.BindView;

/**
 * Created by sanron on 16-4-24.
 */
public abstract class CommonSongPullFragment extends PullFragment implements Player.OnPlayStateChangeListener {

    @BindView(R.id.top_board)
    protected ImageView mIvPicture;
    @BindView(R.id.tv_text1)
    protected TextView mTvText1;
    @BindView(R.id.tv_text2)
    protected TextView mTvText2;
    @BindView(R.id.tv_song_num)
    protected TextView mTvSongNum;
    @BindView(R.id.ibtn_favorite)
    protected ImageButton mIbtnFavorite;
    @BindView(R.id.ibtn_download)
    protected ImageButton mIbtnDownload;
    @BindView(R.id.ibtn_share)
    protected ImageButton mIbtnShare;
    protected SongAdapter mAdapter;

    @Override
    public int getViewResId() {
        return R.layout.web_frag_common_song;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = createAdapter();
    }

    protected abstract SongAdapter createAdapter();

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        if (state == Player.STATE_PREPARING
                || state == Player.STATE_STOP) {
            mAdapter.notifyDataSetChanged();
        }
    }

    protected boolean checkIsCollected(String listId) {
        DataProvider.Access access = DataProvider.get().newAccess(DBHelper.List.TABLE);
        Cursor c = access.query(new String[]{DBHelper.ID},
                DBHelper.List.LIST_ID + "=?",
                new String[]{listId});
        boolean result = c.moveToFirst();
        access.close();
        return result;
    }
}
