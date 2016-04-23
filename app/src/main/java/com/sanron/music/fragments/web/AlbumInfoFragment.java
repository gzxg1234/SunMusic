package com.sanron.music.fragments.web;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.R;
import com.sanron.music.common.T;
import com.sanron.music.net.JsonCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.AlbumSongs;
import com.sanron.music.service.IPlayer;
import com.sanron.music.task.AddCollectPlayListTask;

import okhttp3.Call;

/**
 * Created by sanron on 16-4-23.
 */
public class AlbumInfoFragment extends SongListInfoFragment implements View.OnClickListener, IPlayer.OnPlayStateChangeListener {

    private String mAlbumId;
    private AlbumSongs mData;

    public static final String ARG_ALBUM_ID = "album_id";

    public static AlbumInfoFragment newInstance(String albumId) {
        AlbumInfoFragment albumInfoFragment = new AlbumInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_ALBUM_ID, albumId);
        albumInfoFragment.setArguments(bundle);
        return albumInfoFragment;
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
    protected void loadData() {
        Call call = MusicApi.albumSongs(mAlbumId, new JsonCallback<AlbumSongs>() {
            @Override
            public void onSuccess(AlbumSongs albumSongs) {
                setData(albumSongs);
                hideLoadingView();
            }

            @Override
            public void onFailure(Exception e) {
                showLoadFailedView();
            }
        });
        addCall(call);
    }

    private void setData(AlbumSongs data) {
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
        mIsCollected = checkIsCollected("2" + mAlbumId);
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
                    String pic = mData.albumInfo.pic300;
                    if (TextUtils.isEmpty(pic)) {
                        pic = mData.albumInfo.picRadio;
                        if (TextUtils.isEmpty(pic)) {
                            pic = mData.albumInfo.picS180;
                        }
                    }
                    new AddCollectPlayListTask(mData.songs,
                            mData.albumInfo.title,
                            "2" + mData.albumInfo.albumId,
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

}
