package com.sanron.music.ui.web;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.R;
import com.sanron.music.adapter.SongItemAdapter;
import com.sanron.music.db.bean.Music;
import com.sanron.music.net.JsonCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.Album;
import com.sanron.music.net.bean.Singer;
import com.sanron.music.net.bean.SingerAlbums;
import com.sanron.music.net.bean.SingerSongs;
import com.sanron.music.net.bean.Song;
import com.sanron.music.service.IPlayer;
import com.sanron.music.service.PlayerUtil;
import com.sanron.music.ui.PullFrag;
import com.sanron.music.view.DDPullListView;
import com.sanron.music.view.SingerDetailDialog;

import java.util.List;

import okhttp3.Call;

/**
 * Created by sanron on 16-4-18.
 */
public class SingerInfoFrag extends PullFrag implements DDPullListView.OnLoadListener, IPlayer.OnPlayStateChangeListener, RadioGroup.OnCheckedChangeListener {

    private String mArtistId;
    private ImageView mIvSingerAvatar;
    private TextView mTvSingerName;
    private TextView mTvSingerCountry;
    private RadioGroup mRadioGroup;
    private Singer mSinger;
    private SongItemAdapter mSongAdapter;
    private AlbumItemAdapter mAlbumAdapter;
    private boolean mHasMoreSong = true;
    private boolean mHasMoreAlbum = true;

    public static final int LOAD_LIMIT = 50;//每页加载数量
    public static final String ARG_ARTIST_ID = "artist_id";

    public static SingerInfoFrag newInstance(String artistId) {
        SingerInfoFrag frag = new SingerInfoFrag();
        Bundle args = new Bundle();
        args.putString(ARG_ARTIST_ID, artistId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mArtistId = args.getString(ARG_ARTIST_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.web_frag_singer_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRadioGroup = $(R.id.radio_group);
        mTvSingerCountry = $(R.id.tv_singer_country);
        mTvSingerName = $(R.id.tv_singer_name);
        mIvSingerAvatar = (ImageView) mTopBoard;

        mSongAdapter = new SongItemAdapter(getContext()) {
            @Override
            protected String onBindText2(Song song) {
                return song.albumTitle;
            }
        };
        mAlbumAdapter = new AlbumItemAdapter();
        mPullListView.setAdapter(mSongAdapter);
        mPullListView.setOnLoadListener(this);
        mRadioGroup.setOnCheckedChangeListener(this);
        mIvSingerAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSinger == null) {
                    return;
                }

                SingerDetailDialog detailDialog = new SingerDetailDialog(getContext(), mSinger);
                BitmapDrawable bd = (BitmapDrawable) mIvSingerAvatar.getDrawable();
                if (bd != null) {
                    detailDialog.setAvatar(bd.getBitmap());
                }
                detailDialog.show();
            }
        });
        mIvSingerAvatar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mIvSingerAvatar.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int width = mIvSingerAvatar.getWidth();
                int normalHeaderHeight = mIvSingerAvatar.getHeight() + mViewOperator.getHeight();
                mPullListView.setmMaxHeaderHeight(width + mViewOperator.getHeight());
                mPullListView.setNormalHeaderHeight(normalHeaderHeight);
            }
        });


        getMainActivity().addPlayerReadyCallback(this);
    }

    @Override
    public void onPlayerReady() {
        super.onPlayerReady();
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
        Call call = MusicApi.singerInfo(mArtistId, new JsonCallback<Singer>() {
            @Override
            public void onSuccess(Singer data) {
                mSinger = data;
                if (mSinger != null) {
                    setTitle(mSinger.name);
                    mTvSingerName.setText(mSinger.name);
                    mTvSingerCountry.setText(mSinger.country);
                    ImageLoader.getInstance()
                            .displayImage(mSinger.avatarS1000,
                                    mIvSingerAvatar);
                    mPullListView.load();
                    hideLoadingView();
                } else {
                    showLoadFailedView();
                }
            }

            @Override
            public void onFailure(Exception e) {
                showLoadFailedView();
            }
        });
        addCall(call);
    }

    private void loadAlbums() {
        Call call = MusicApi.singerAlbums(mArtistId,
                mAlbumAdapter == null ? 0 : mAlbumAdapter.getCount(),
                LOAD_LIMIT,
                new JsonCallback<SingerAlbums>() {

                    @Override
                    public void onSuccess(SingerAlbums singerAlbums) {
                        if (singerAlbums != null) {
                            mHasMoreAlbum = singerAlbums.havemore == 1;
                            mPullListView.setHasMore(mHasMoreAlbum);
                            mAlbumAdapter.addData(singerAlbums.albums);
                        }
                        mPullListView.onLoadCompleted();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mPullListView.onLoadCompleted();
                    }
                });
        addCall(call);
    }


    private void loadSongs() {
        Call call = MusicApi.singerSongs(mArtistId,
                mSongAdapter == null ? 0 : mSongAdapter.getCount(),
                LOAD_LIMIT,
                new JsonCallback<SingerSongs>() {
                    @Override
                    public void onSuccess(SingerSongs data) {
                        if (data != null) {
                            mSongAdapter.addData(data.songs);
                            mHasMoreSong = data.havemore == 1;
                            mPullListView.setHasMore(mHasMoreSong);
                            onPlayStateChange(IPlayer.STATE_PREPARING);
                        }
                        mPullListView.onLoadCompleted();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mPullListView.onLoadCompleted();
                    }
                });
        addCall(call);
    }

    @Override
    public void onLoad() {
        if (mRadioGroup.getCheckedRadioButtonId() == R.id.rb_singer_song) {
            loadSongs();
        } else {
            loadAlbums();
        }
    }

    @Override
    public void onPlayStateChange(int state) {
        if (state == IPlayer.STATE_PREPARING) {
            //列表中是否有播放中的歌曲
            Music currentMusic = PlayerUtil.getCurrentMusic();
            List<Song> listData = mSongAdapter.getData();
            if (listData != null
                    && currentMusic != null) {
                for (int i = 0; i < listData.size(); i++) {
                    if (listData.get(i).songId.equals(currentMusic.getSongId())) {
                        mSongAdapter.setPlayingPosition(i);
                        break;
                    }
                }
            }
        } else if (state == IPlayer.STATE_STOP) {
            mSongAdapter.setPlayingPosition(-1);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_singer_song: {
                mPullListView.setAdapter(mSongAdapter);
                mPullListView.setHasMore(mHasMoreSong);
                if (mSongAdapter.isEmpty()) {
                    mPullListView.load();
                }
            }
            break;

            case R.id.rb_singer_album: {
                mPullListView.setAdapter(mAlbumAdapter);
                mPullListView.setHasMore(mHasMoreAlbum);
                if (mAlbumAdapter.isEmpty()) {
                    mPullListView.load();
                }
            }
            break;
        }
    }

    private class AlbumItemAdapter extends BaseAdapter {

        private List<Album> data;

        public void setData(List<Album> data) {
            this.data = data;
        }

        public void addData(List<Album> albums) {
            if (data == null) {
                data = albums;
            } else {
                data.addAll(albums);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.list_singer_album_item, parent, false);
            }
            Album album = data.get(position);
            ImageView ivPic = (ImageView) convertView.findViewById(R.id.iv_album_pic);
            TextView tvTitle = (TextView) convertView.findViewById(R.id.tv_singer_album_title);
            TextView tvDate = (TextView) convertView.findViewById(R.id.tv_singer_album_publish);
            ivPic.setImageBitmap(null);
            ImageLoader.getInstance()
                    .displayImage(album.picS180, ivPic);
            tvTitle.setText(album.title);
            tvDate.setText(album.publishtime);
            return convertView;
        }
    }
}
