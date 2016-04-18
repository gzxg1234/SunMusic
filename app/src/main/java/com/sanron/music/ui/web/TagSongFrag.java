package com.sanron.music.ui.web;

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
import com.sanron.music.net.JsonCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.Song;
import com.sanron.music.net.bean.TagSongsData;
import com.sanron.music.service.IPlayer;
import com.sanron.music.service.PlayerUtil;
import com.sanron.music.ui.PullFrag;
import com.sanron.music.view.DDPullListView;

import java.util.List;

import okhttp3.Call;

/**
 * 分类歌曲
 * Created by sanron on 16-4-12.
 */
public class TagSongFrag extends PullFrag implements IPlayer.OnPlayStateChangeListener {

    private String mTag;
    private TextView mTvPlay;
    private ImageButton mIbtnDownload;
    private TagSongsData mTagSongsData;
    private TextView mTvTagName;
    private SongItemAdapter mAdapter;
    public static final int LOAD_LIMIT = 50;

    public static TagSongFrag newInstance(String tag) {
        Bundle args = new Bundle();
        args.putString("tag", tag);
        TagSongFrag tagSongFrag = new TagSongFrag();
        tagSongFrag.setArguments(args);
        return tagSongFrag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mTag = args.getString("tag");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.web_frag_tag_song, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvPlay = $(R.id.tv_play);
        mIbtnDownload = $(R.id.ibtn_download);
        mTvTagName = $(R.id.tv_tag_name);

        setTitle(mTag);
        mTvTagName.setText(mTag);
        mAdapter = new SongItemAdapter(getContext());
        mPullListView.setAdapter(mAdapter);
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

        getMainActivity().addPlayerReadyCallback(this);
    }

    @Override
    public void onPlayerReady() {
        PlayerUtil.addPlayStateChangeListener(this);
        onPlayStateChange(IPlayer.STATE_PREPARED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getMainActivity().removePlayerReadyCallback(this);
        PlayerUtil.removePlayStateChangeListener(this);
    }


    @Override
    protected void loadData() {
        Call call = MusicApi.tagInfo(mTag, LOAD_LIMIT, mAdapter == null ? 0 : mAdapter.getCount(), new JsonCallback<TagSongsData>() {
            @Override
            public void onSuccess(final TagSongsData data) {
                setData(data);
                mPullListView.onLoadCompleted();
            }

            @Override
            public void onFailure(Exception e) {
                showLoadFailedView();
                mPullListView.onLoadCompleted();
            }
        });
        addCall(call);
    }


    private void setData(TagSongsData tagSongsData) {
        this.mTagSongsData = tagSongsData;
        if (tagSongsData != null
                && tagSongsData.taginfo != null) {
            mAdapter.addData(tagSongsData.taginfo.songs);
            onPlayStateChange(IPlayer.STATE_PREPARING);
            if (tagSongsData.taginfo.havemore == 0) {
                mPullListView.setHasMore(false);
            }
        }
        hideLoadingView();
    }

    @Override
    public void onPlayStateChange(int state) {
        if (state == IPlayer.STATE_PREPARING) {
            //列表中是否有播放中的歌曲
            Music currentMusic = PlayerUtil.getCurrentMusic();
            List<Song> listData = mAdapter.getData();
            if (listData != null) {
                for (int i = 0; i < listData.size(); i++) {
                    if (listData.get(i).songId.equals(currentMusic.getSongId())) {
                        mAdapter.setPlayingPosition(i);
                        break;
                    }
                }
            }
        } else if (state == IPlayer.STATE_STOP) {
            mAdapter.setPlayingPosition(-1);
        }
    }
}
