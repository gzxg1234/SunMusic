package com.sanron.ddmusic.fragments.web;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.ddmusic.R;
import com.sanron.ddmusic.adapter.SongAdapter;
import com.sanron.ddmusic.api.MusicApi;
import com.sanron.ddmusic.api.bean.AlbumSongs;
import com.sanron.ddmusic.api.callback.JsonCallback;
import com.sanron.ddmusic.common.ViewTool;
import com.sanron.ddmusic.db.AppDB;
import com.sanron.ddmusic.db.ResultCallback;
import com.sanron.ddmusic.playback.Player;

import butterknife.OnClick;
import okhttp3.Call;

/**
 * Created by sanron on 16-4-23.
 */
public class AlbumSongsFragment extends CommonSongPullFragment implements Player.OnPlayStateChangeListener {

    private String mAlbumId;
    private AlbumSongs mData;
    private boolean mIsCollected;

    public static final String LIST_ID_PREFIX = "2";
    public static final String ARG_ALBUM_ID = "album_id";

    public static AlbumSongsFragment newInstance(String albumId) {
        AlbumSongsFragment fragment = new AlbumSongsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_ALBUM_ID, albumId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mAlbumId = args.getString(ARG_ALBUM_ID);
        }
    }

    @Override
    protected SongAdapter createAdapter() {
        return new SongAdapter(getContext());
    }

    @Override
    protected void loadData() {
        Call call = MusicApi.albumSongs(mAlbumId, new JsonCallback<AlbumSongs>() {
            @Override
            public void onSuccess(AlbumSongs albumSongs) {
                mPullListView.setHasMore(false);
                updateUI(albumSongs);
                hideLoadingView();
            }

            @Override
            public void onFailure(Exception e) {
                showLoadFailedView();
            }
        });
        addCall(call);
    }

    private void updateUI(AlbumSongs data) {
        this.mData = data;
        if (data == null) {
            return;
        }

        String pic = data.albumInfo.picS1000;
        if (TextUtils.isEmpty(pic)) {
            pic = data.albumInfo.picW700;
            if (TextUtils.isEmpty(pic)) {
                pic = data.albumInfo.picS500;
            }
        }
        ImageLoader.getInstance().displayImage(pic, mIvPicture);

        setTitle(data.albumInfo.title);
        mTvText1.setText(data.albumInfo.title);
        mTvText2.setText(data.albumInfo.author);
        if (data.songs != null) {
            mTvSongNum.setText("共" + data.songs.size() + "首歌");
        }
        mAdapter.setData(data.songs);
        mIsCollected = checkIsCollected(LIST_ID_PREFIX + mAlbumId);
        if (mIsCollected) {
            mIbtnFavorite.setImageResource(R.mipmap.ic_favorite_black_24dp);
        }
    }

    @OnClick(R.id.ibtn_favorite)
    public void favorite(){
        if (mIsCollected) {
            ViewTool.show("已收藏过此歌单");
        } else {
            String pic = mData.albumInfo.pic300;
            if (TextUtils.isEmpty(pic)) {
                pic = mData.albumInfo.picRadio;
                if (TextUtils.isEmpty(pic)) {
                    pic = mData.albumInfo.picS180;
                }
            }
            AppDB.get(getContext()).addCollectList(
                    mData.songs,
                    mData.albumInfo.title,
                    LIST_ID_PREFIX + mData.albumInfo.albumId,
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

}
