package com.sanron.music.fragments.web;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.R;
import com.sanron.music.adapter.SongAdapter;
import com.sanron.music.api.JsonCallback;
import com.sanron.music.api.MusicApi;
import com.sanron.music.api.bean.OfficialSongListSongs;
import com.sanron.music.api.bean.Song;
import com.sanron.music.common.ViewTool;
import com.sanron.music.task.AddCollectPlayListTask;

import java.util.Iterator;
import java.util.List;

import okhttp3.Call;

/**
 * Created by sanron on 16-4-1.
 */
public class OfficialSongListInfoFragment extends CommonSongPullFragment implements View.OnClickListener {

    public static final String ARG_CODE = "code";

    private String mCode;
    private OfficialSongListSongs mData;
    protected boolean mIsCollected;

    public static final String LIST_ID_PREFIX = "3";

    public static OfficialSongListInfoFragment newInstance(String songListId) {
        OfficialSongListInfoFragment fragment = new OfficialSongListInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_CODE, songListId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mCode = args.getString(ARG_CODE);
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
        Call call = MusicApi.officialSongListSongs(mCode, new JsonCallback<OfficialSongListSongs>() {
            @Override
            public void onFailure(Exception e) {
                showLoadFailedView();
            }

            @Override
            public void onSuccess(OfficialSongListSongs data) {
                updateUI(data);
                hideLoadingView();
            }
        });
        addCall(call);
    }

    private void updateUI(OfficialSongListSongs data) {
        this.mData = data;
        if (data == null) {
            return;
        }
        String pic = data.pic;
        ImageLoader.getInstance().displayImage(pic, mIvPicture);
        mTvText1.setText(data.name);
        mTvText2.setText("创建时间:" + data.createTime);
        if (data.songs != null) {
            filterInvalidSong(data.songs);
            mTvSongNum.setText("共" + data.songs.size() + "首歌");
        }
        mAdapter.setData(data.songs);
        setTitle(data.name);
        mIsCollected = checkIsCollected(LIST_ID_PREFIX + mCode);
        if (mIsCollected) {
            mIbtnFavorite.setImageResource(R.mipmap.ic_favorite_black_24dp);
        }
    }

    /**
     * 过滤掉无效歌曲数据
     *
     * @param songs
     */
    private void filterInvalidSong(List<Song> songs) {
        Iterator<Song> iterator = songs.iterator();
        while (iterator.hasNext()) {
            Song song = iterator.next();
            if ("0".equals(song.songId)) {
                iterator.remove();
            }
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
                    new AddCollectPlayListTask(mData.songs,
                            mData.name,
                            LIST_ID_PREFIX + mData.code,
                            mData.pic) {
                        @Override
                        protected void onPostExecute(Integer result) {
                            switch (result) {

                                case SUCCESS: {
                                    ViewTool.show("收藏成功");
                                    mIsCollected = true;
                                    mIbtnFavorite.setImageResource(R.mipmap.ic_favorite_black_24dp);
                                }
                                break;

                                case FAILED: {
                                    ViewTool.show("收藏失败");
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
