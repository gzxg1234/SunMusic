package com.sanron.music.ui.web;

import android.database.Cursor;
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

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.sanron.music.R;
import com.sanron.music.adapter.SongItemAdapter;
import com.sanron.music.common.T;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.bean.Music;
import com.sanron.music.net.JsonCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.Song;
import com.sanron.music.net.bean.SongList;
import com.sanron.music.service.IPlayer;
import com.sanron.music.service.PlayerUtil;
import com.sanron.music.task.AddCollectSongListTask;
import com.sanron.music.ui.PullFrag;

import java.util.List;

import okhttp3.Call;

/**
 * Created by sanron on 16-4-1.
 */
public class SongListInfoFrag extends PullFrag implements View.OnClickListener, IPlayer.OnPlayStateChangeListener {

    public static final String ARG_LIST_ID = "list_id";

    private String mListId;
    private SongList mData;
    private boolean mIsCollected;

    private TextView mTvSongListTitle;
    private TextView mTvSongListTag;

    private TextView mTvSongNum;
    private ImageButton mIbtnFavorite;
    private ImageButton mIbtnDownload;
    private ImageButton mIbtnShare;
    private ImageView mIvPicture;

    private SongItemAdapter mAdapter;
    private DisplayImageOptions mImageOptions;


    public static SongListInfoFrag newInstance(String songListId) {
        SongListInfoFrag songListInfoFrag = new SongListInfoFrag();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_LIST_ID, songListId);
        songListInfoFrag.setArguments(bundle);
        return songListInfoFrag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new SongItemAdapter(getContext());
        mImageOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheOnDisk(true)
                .build();
        Bundle args = getArguments();
        if (args != null) {
            mListId = args.getString(ARG_LIST_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.web_frag_songlist, container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvSongNum = $(R.id.tv_song_num);
        mIbtnDownload = $(R.id.ibtn_download);
        mIbtnFavorite = $(R.id.ibtn_favorite);
        mIbtnShare = $(R.id.ibtn_share);
        mTvSongListTag = $(R.id.tv_list_tag);
        mTvSongListTitle = $(R.id.tv_list_title);
        mIvPicture = (ImageView) mTopBoard;

        mPullListView.setAdapter(mAdapter);
        mIbtnDownload.setOnClickListener(this);
        mIbtnShare.setOnClickListener(this);
        mIbtnFavorite.setOnClickListener(this);
        mPullListView.setHasMore(false);

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
        getMainActivity().addPlayerReadyCallback(this);
    }

    @Override
    public void onPlayerReady() {
        PlayerUtil.addPlayStateChangeListener(this);
        onPlayStateChange(IPlayer.STATE_PREPARING);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getMainActivity().removePlayerReadyCallback(this);
        PlayerUtil.removePlayStateChangeListener(this);
    }

    @Override
    protected void loadData() {
        System.out.println("getData");
        Call call = MusicApi.songListInfo(mListId, new JsonCallback<SongList>() {
            @Override
            public void onFailure(Exception e) {
                showLoadFailedView();
                mPullListView.onLoadCompleted();
            }

            @Override
            public void onSuccess(SongList data) {
                setData(data);
                mPullListView.onLoadCompleted();
                hideLoadingView();
            }
        });
        addCall(call);
    }


    private void setData(SongList data) {
        this.mData = data;
        if (data == null) {
            return;
        }
        String pic = data.pic700;
        if (TextUtils.isEmpty(pic)) {
            pic = data.pic500;
            if (TextUtils.isEmpty(pic)) {
                pic = data.pic500;
            }
        }
        ImageLoader.getInstance().displayImage(pic, mIvPicture, mImageOptions);
        mTvSongListTag.setText(data.tag);
        mTvSongListTitle.setText(data.title);
        if (data.songs != null) {
            mTvSongNum.setText("共" + data.songs.size() + "首歌");
        }
        mAdapter.setData(data.songs);
        onPlayStateChange(IPlayer.STATE_PREPARING);
        setTitle(data.title);
        mIsCollected = checkIsCollected(mListId);
        if (mIsCollected) {
            mIbtnFavorite.setImageResource(R.mipmap.ic_favorite_black_24dp);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_download: {

            }
            break;

            case R.id.ibtn_favorite: {
                if (mIsCollected) {
                    T.show("已收藏过此歌单");
                } else {
                    new AddCollectSongListTask(mData) {
                        @Override
                        protected void onPostExecute(Integer result) {
                            switch (result) {

                                case SUCCESS: {
                                    T.show("收藏成功");
                                    mIsCollected = true;
                                    mIbtnFavorite.setImageResource(R.mipmap.ic_favorite_black_24dp);
                                }
                                break;

                                case FAILED: {
                                    T.show("收藏歌单失败");
                                }
                                break;
                            }
                        }
                    }.execute();
                }
            }
            break;

            case R.id.ibtn_share: {

            }
            break;
        }
    }

    private boolean checkIsCollected(String listId) {
        DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.List.TABLE);
        Cursor c = access.query(new String[]{DBHelper.ID},
                DBHelper.List.LIST_ID + "=?",
                new String[]{listId});
        boolean result = c.moveToFirst();
        access.close();
        return result;
    }

    @Override
    public void onPlayStateChange(int state) {
        if (state == IPlayer.STATE_PREPARING) {
            //列表中是否有播放中的歌曲
            Music currentMusic = PlayerUtil.getCurrentMusic();
            List<Song> listData = mAdapter.getData();
            if (listData != null
                    && currentMusic != null) {
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
