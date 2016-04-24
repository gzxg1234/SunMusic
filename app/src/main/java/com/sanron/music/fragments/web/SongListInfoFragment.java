package com.sanron.music.fragments.web;

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

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.R;
import com.sanron.music.adapter.SongItemAdapter;
import com.sanron.music.common.T;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.fragments.base.PullFragment;
import com.sanron.music.api.JsonCallback;
import com.sanron.music.api.MusicApi;
import com.sanron.music.api.bean.SongList;
import com.sanron.music.service.IPlayer;
import com.sanron.music.service.PlayerUtil;
import com.sanron.music.task.AddCollectPlayListTask;

import okhttp3.Call;

/**
 * Created by sanron on 16-4-1.
 */
public class SongListInfoFragment extends PullFragment implements View.OnClickListener, IPlayer.OnPlayStateChangeListener {

    public static final String ARG_LIST_ID = "list_id";

    private String mListId;
    private SongList mData;
    protected boolean mIsCollected;

    protected TextView mTvText1;
    protected TextView mTvText2;

    protected TextView mTvSongNum;
    protected ImageButton mIbtnFavorite;
    protected ImageButton mIbtnDownload;
    protected ImageButton mIbtnShare;
    protected ImageView mIvPicture;

    protected SongItemAdapter mAdapter;

    public static SongListInfoFragment newInstance(String songListId) {
        SongListInfoFragment songListInfoFrag = new SongListInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_LIST_ID, songListId);
        songListInfoFrag.setArguments(bundle);
        return songListInfoFrag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new SongItemAdapter(getContext());
        Bundle args = getArguments();
        if (args != null) {
            mListId = args.getString(ARG_LIST_ID);
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
        Call call = MusicApi.songListInfo(mListId, new JsonCallback<SongList>() {
            @Override
            public void onFailure(Exception e) {
                showLoadFailedView();
            }

            @Override
            public void onSuccess(SongList data) {
                setData(data);
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
        String pic = data.picW700;
        if (TextUtils.isEmpty(pic)) {
            pic = data.pic500;
            if (TextUtils.isEmpty(pic)) {
                pic = data.pic500;
            }
        }
        ImageLoader.getInstance().displayImage(pic, mIvPicture);
        mTvText2.setText(data.tag);
        mTvText1.setText(data.title);
        if (data.songs != null) {
            mTvSongNum.setText("共" + data.songs.size() + "首歌");
        }
        mAdapter.setData(data.songs);
        setTitle(data.title);
        mIsCollected = checkIsCollected("1" + mListId);
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
                    String pic = mData.pic300;
                    if (TextUtils.isEmpty(pic)) {
                        pic = mData.pic500;
                        if (TextUtils.isEmpty(pic)) {
                            pic = mData.pic;
                        }
                    }
                    new AddCollectPlayListTask(mData.songs,
                            mData.title,
                            "1" + mData.listId,
                            pic) {
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
                                    T.show("收藏失败");
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

    protected boolean checkIsCollected(String listId) {
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
        if (state == IPlayer.STATE_PREPARING
                || state == IPlayer.STATE_STOP) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
