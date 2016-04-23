package com.sanron.music.fragments.web;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.adapter.SongItemAdapter;
import com.sanron.music.db.bean.Music;
import com.sanron.music.fragments.base.PullFragment;
import com.sanron.music.net.JsonCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.Song;
import com.sanron.music.net.bean.TagSongs;
import com.sanron.music.service.IPlayer;
import com.sanron.music.service.PlayerUtil;
import com.sanron.music.view.DDPullListView;

import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;

/**
 * 分类歌曲
 * Created by sanron on 16-4-12.
 */
public class TagInfoFragment extends PullFragment implements IPlayer.OnPlayStateChangeListener, View.OnClickListener {

    private String mTag;
    private TextView mTvPlay;
    private ImageButton mIbtnDownload;
    private TextView mTvTagName;
    private SongItemAdapter mAdapter;
    public static final int LOAD_LIMIT = 50;

    public static TagInfoFragment newInstance(String tag) {
        Bundle args = new Bundle();
        args.putString("tag", tag);
        TagInfoFragment tagSongFrag = new TagInfoFragment();
        tagSongFrag.setArguments(args);
        return tagSongFrag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new SongItemAdapter(getContext());
        Bundle args = getArguments();
        if (args != null) {
            mTag = args.getString("tag");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.web_frag_tag_song, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvPlay = $(R.id.tv_play);
        mIbtnDownload = $(R.id.ibtn_download);
        mTvTagName = $(R.id.tv_tag_name);

        setTitle(mTag);
        mTvTagName.setText(mTag);
        mPullListView.setAdapter(mAdapter);
        mTvPlay.setOnClickListener(this);
        mPullListView.setOnLoadListener(new DDPullListView.OnLoadListener() {
            @Override
            public void onLoad() {
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
                    onPlayStateChange(IPlayer.STATE_PREPARING);
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
        if (state == IPlayer.STATE_PREPARING
                || state == IPlayer.STATE_STOP) {
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
