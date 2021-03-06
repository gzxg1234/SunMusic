package com.sanron.ddmusic.fragments.web;

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
import com.sanron.ddmusic.R;
import com.sanron.ddmusic.adapter.CommonItemViewHolder;
import com.sanron.ddmusic.adapter.SongAdapter;
import com.sanron.ddmusic.api.callback.JsonCallback;
import com.sanron.ddmusic.api.MusicApi;
import com.sanron.ddmusic.api.bean.Album;
import com.sanron.ddmusic.api.bean.Singer;
import com.sanron.ddmusic.api.bean.SingerAlbums;
import com.sanron.ddmusic.api.bean.SingerSongs;
import com.sanron.ddmusic.api.bean.Song;
import com.sanron.ddmusic.fragments.base.PullFragment;
import com.sanron.ddmusic.playback.Player;
import com.sanron.ddmusic.service.PlayUtil;
import com.sanron.ddmusic.view.DDPullListView;
import com.sanron.ddmusic.view.SingerDetailDialog;

import java.util.List;

import butterknife.BindView;
import okhttp3.Call;

/**
 * Created by sanron on 16-4-18.
 */
public class SingerInfoFragment extends PullFragment implements DDPullListView.OnLoadMoreListener, Player.OnPlayStateChangeListener, RadioGroup.OnCheckedChangeListener {

    @BindView(R.id.top_board)
    ImageView mIvSingerAvatar;
    @BindView(R.id.tv_singer_name)
    TextView mTvSingerName;
    @BindView(R.id.tv_singer_country)
    TextView mTvSingerCountry;
    @BindView(R.id.radio_group)
    RadioGroup mRadioGroup;

    private String mArtistId;
    private Singer mSinger;
    private SongAdapter mSongAdapter;
    private AlbumItemAdapter mAlbumAdapter;
    private boolean mHasMoreSong = true;
    private boolean mHasMoreAlbum = true;

    public static final int LOAD_LIMIT = 50;//每页加载数量
    public static final String ARG_ARTIST_ID = "artist_id";

    public static SingerInfoFragment newInstance(String artistId) {
        SingerInfoFragment fragment = new SingerInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ARTIST_ID, artistId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mArtistId = args.getString(ARG_ARTIST_ID);
        }
    }

    @Override
    public int getViewResId() {
        return R.layout.web_frag_singer_info;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSongAdapter = new SongAdapter(getContext()) {
            @Override
            protected String onBindText2(Song song) {
                return song.albumTitle;
            }
        };
        mAlbumAdapter = new AlbumItemAdapter();
        mPullListView.setAdapter(mSongAdapter);
        mPullListView.setOnLoadMoreListener(this);
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
    }

    @Override
    public void onPlayerReady() {
        PlayUtil.addPlayStateChangeListener(this);
        if (mSongAdapter != null) {
            mSongAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PlayUtil.removePlayStateChangeListener(this);
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
                }
                hideLoadingView();
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
    public void onLoadMore() {
        switch (mRadioGroup.getCheckedRadioButtonId()) {
            case R.id.rb_singer_song: {
                loadSongs();
            }
            break;

            case R.id.rb_singer_album: {
                loadAlbums();
            }
            break;
        }
    }

    @Override
    public void onPlayStateChange(int state) {
        if (state == Player.STATE_PREPARING
                || state == Player.STATE_IDLE) {
            mSongAdapter.notifyDataSetChanged();
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
            CommonItemViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.list_common_item, parent, false);
            } else {
                holder = (CommonItemViewHolder) convertView.getTag();
            }

            if (holder == null) {
                holder = new CommonItemViewHolder(convertView);
                holder.ivMenu.setImageResource(R.mipmap.ic_chevron_right_black_90_24dp);
                convertView.setTag(holder);
            }

            final Album album = data.get(position);
            //取消之前的加载任务
            ImageLoader.getInstance()
                    .cancelDisplayTask(holder.ivPicture);
            holder.ivPicture.setImageBitmap(null);
            ImageLoader.getInstance()
                    .displayImage(album.picS180, holder.ivPicture);
            holder.tvText1.setText(album.title);
            holder.tvText2.setText(album.publishtime);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getMainActivity().showAlbumSongs(album.albumId);
                }
            });
            return convertView;
        }

    }
}
