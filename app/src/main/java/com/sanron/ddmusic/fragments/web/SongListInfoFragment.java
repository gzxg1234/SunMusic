package com.sanron.ddmusic.fragments.web;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.ddmusic.R;
import com.sanron.ddmusic.adapter.SongAdapter;
import com.sanron.ddmusic.api.JsonCallback;
import com.sanron.ddmusic.api.MusicApi;
import com.sanron.ddmusic.api.bean.SongList;
import com.sanron.ddmusic.common.ViewTool;
import com.sanron.ddmusic.db.AppDB;
import com.sanron.ddmusic.db.ResultCallback;

import okhttp3.Call;

/**
 * Created by sanron on 16-4-1.
 */
public class SongListInfoFragment extends CommonSongPullFragment implements View.OnClickListener {


    private String mListId;
    private SongList mData;
    protected boolean mIsCollected;

    public static final String LIST_ID_PREFIX = "1";
    public static final String ARG_LIST_ID = "list_id";

    public static SongListInfoFragment newInstance(String songListId) {
        SongListInfoFragment fragment = new SongListInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_LIST_ID, songListId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mListId = args.getString(ARG_LIST_ID);
        }
    }

    @Override
    protected SongAdapter createAdapter() {
        return new SongAdapter(getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIbtnDownload.setOnClickListener(this);
        mIbtnShare.setOnClickListener(this);
        mIbtnFavorite.setOnClickListener(this);
        mPullListView.setHasMore(false);
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
                updateUI(data);
                hideLoadingView();
            }
        });
        addCall(call);
    }


    private void updateUI(SongList data) {
        this.mData = data;
        if (data == null) {
            return;
        }

        if (!TextUtils.isEmpty(data.pic500)) {
            ImageLoader.getInstance().displayImage(data.pic500, mIvPicture);
        }
        mTvText1.setText(data.title);
        mTvText2.setText(data.tag);
        if (data.songs != null) {
            mTvSongNum.setText("共" + data.songs.size() + "首歌");
        }
        mAdapter.setData(data.songs);
        setTitle(data.title);
        mIsCollected = checkIsCollected(LIST_ID_PREFIX + mListId);
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
                    ViewTool.show("已收藏过此歌单");
                } else {
                    String pic = mData.pic300;
                    if (TextUtils.isEmpty(pic)) {
                        pic = mData.pic500;
                        if (TextUtils.isEmpty(pic)) {
                            pic = mData.pic;
                        }
                    }

                    AppDB.get(getContext()).addCollectList(
                            mData.songs,
                            mData.title,
                            LIST_ID_PREFIX + mData.listId,
                            pic,
                            new ResultCallback<Integer>() {
                                @Override
                                public void onResult(Integer result) {
                                    if (result == 1) {
                                        ViewTool.show("收藏成功");
                                        mIsCollected = true;
                                        mIbtnFavorite.setImageResource(R.mipmap.ic_favorite_black_24dp);
                                    } else {
                                        ViewTool.show("收藏失败");
                                    }
                                }
                            });
                }
            }
            break;

            case R.id.ibtn_share: {

            }
            break;
        }
    }

}
