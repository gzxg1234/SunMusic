package com.sanron.music.fragments.web;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.adapter.SongAdapter;
import com.sanron.music.api.JsonCallback;
import com.sanron.music.api.MusicApi;
import com.sanron.music.api.bean.Song;
import com.sanron.music.api.bean.TagSongs;
import com.sanron.music.db.bean.Music;
import com.sanron.music.fragments.base.PullFragment;
import com.sanron.music.playback.Player;
import com.sanron.music.service.PlayerUtil;
import com.sanron.music.view.DDPullListView;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Call;

/**
 * 分类歌曲
 * Created by sanron on 16-4-12.
 */
public class TagInfoFragment extends PullFragment implements Player.OnPlayStateChangeListener, View.OnClickListener {

    @BindView(R.id.tv_play)
    TextView mTvPlay;
    @BindView(R.id.ibtn_download)
    ImageButton mIbtnDownload;
    @BindView(R.id.tv_tag_name)
    TextView mTvTagName;

    private String mTag;
    private SongAdapter mAdapter;
    public static final int LOAD_LIMIT = 50;
    public static final String ARG_TAG = "tag";

    public static TagInfoFragment newInstance(String tag) {
        TagInfoFragment fragment = new TagInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new SongAdapter(getContext());
        Bundle args = getArguments();
        if (args != null) {
            mTag = args.getString(ARG_TAG);
        }
    }

    @Override
    public int getViewResId() {
        return R.layout.web_frag_tag_song;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(mTag);
        mTvTagName.setText(mTag);
        mPullListView.setAdapter(mAdapter);
        mTvPlay.setOnClickListener(this);
        mPullListView.setOnLoadMoreListener(new DDPullListView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadData();
            }
        });
        mTopBoard.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mTopBoard.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int height = mTopBoard.getHeight();
                mPullListView.setmMaxHeaderHeight(height + 200);
                mPullListView.setNormalHeaderHeight(height);
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
    protected void loadData() {
        loadMore(true);
    }

    private void loadMore(final boolean first) {
        Call call = MusicApi.tagInfo(mTag, LOAD_LIMIT, mAdapter == null ? 0 : mAdapter.getCount(), new JsonCallback<TagSongs>() {
            @Override
            public void onSuccess(final TagSongs data) {
                if (data != null
                        && data.taginfo != null) {
                    mAdapter.addData(data.taginfo.songs);
                    onPlayStateChange(Player.STATE_PREPARING);
                    if (data.taginfo.havemore == 0) {
                        mPullListView.setHasMore(false);
                    }
                }
                if (first) {
                    hideLoadingView();
                }
                mPullListView.onLoadCompleted();
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
    public void onPlayStateChange(int state) {
        if (state == Player.STATE_PREPARING
                || state == Player.STATE_STOP) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_play: {
                PlayerUtil.clearQueue();
                List<Music> musics = new LinkedList<>();
                for (Song song : mAdapter.getData()) {
                    musics.add(song.toMusic());
                }
                PlayerUtil.enqueue(musics);
                PlayerUtil.play(0);
            }
            break;
        }
    }
}
