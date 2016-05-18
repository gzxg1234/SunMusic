package com.sanron.music.fragments;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.sanron.music.R;
import com.sanron.music.activities.MainActivity;
import com.sanron.music.db.bean.Music;
import com.sanron.music.api.JsonCallback;
import com.sanron.music.api.MusicApi;
import com.sanron.music.api.bean.LrcPicData;
import com.sanron.music.playback.Player;
import com.sanron.music.service.PlayerUtil;

import java.util.List;

import okhttp3.Call;

/**
 * 管理导航头部
 * Created by sanron on 16-4-18.
 */
public class NavigationHeaderFrag extends Fragment implements Player.OnPlayStateChangeListener, MainActivity.PlayerReadyCallback {

    private View mHeader;
    private ImageView mIvMusicPic;
    private TextView mTvMusicTitle;
    private TextView mTvMusicArtist;
    private Call mGetAvatarCall;

    public void setHeader(View header) {
        this.mHeader = header;
        mIvMusicPic = (ImageView) header.findViewById(R.id.civ_music_pic);
        mTvMusicTitle = (TextView) header.findViewById(R.id.tv_music_title);
        mTvMusicArtist = (TextView) header.findViewById(R.id.tv_music_artist);
    }

    @Override
    public void onPlayerReady() {
        PlayerUtil.addPlayStateChangeListener(this);
        if (PlayerUtil.getState() != Player.STATE_STOP) {
            onPlayStateChange(Player.STATE_PREPARING);
        }
        if (PlayerUtil.getState() >= Player.STATE_PREPARED) {
            onPlayStateChange(Player.STATE_PREPARED);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MainActivity) getActivity()).removePlayerReadyCallback(this);
        PlayerUtil.removePlayStateChangeListener(this);
    }

    @Override
    public void onPlayStateChange(int state) {
        switch (state) {
            case Player.STATE_STOP: {
                mTvMusicTitle.setText("叮咚音乐");
                mTvMusicArtist.setText("");
            }
            break;

            case Player.STATE_PREPARING: {
                Music music = PlayerUtil.getCurrentMusic();
                mTvMusicTitle.setText(music.getTitle());
                String artist = music.getArtist();
                if ("<unknown>".equals(artist)) {
                    artist = "未知歌手";
                }
                mTvMusicArtist.setText(artist);
            }
            break;

            case Player.STATE_PREPARED: {
                mIvMusicPic.setImageResource(R.mipmap.default_small_song_pic);
                Music music = PlayerUtil.getCurrentMusic();
                final int currentIndex = PlayerUtil.getCurrentIndex();
                String artist = music.getArtist();
                if (mGetAvatarCall != null) {
                    mGetAvatarCall.cancel();
                }
                mGetAvatarCall = MusicApi.searchLrcPic(music.getTitle(),
                        "<unknown>".equals(artist) ? "" : artist,
                        2,
                        new JsonCallback<LrcPicData>() {
                            final int requestIndex = currentIndex;

                            @Override
                            public void onFailure(Exception e) {
                            }

                            @Override
                            public void onSuccess(LrcPicData data) {
                                List<LrcPicData.LrcPic> lrcPics = data.lrcPics;
                                String avatar = null;
                                if (lrcPics != null) {
                                    for (LrcPicData.LrcPic lrcPic : lrcPics) {
                                        avatar = lrcPic.avatar180x180;
                                        if (TextUtils.isEmpty(avatar)) {
                                            avatar = lrcPic.avatar500x500;
                                        }
                                        if (!TextUtils.isEmpty(avatar)) {
                                            break;
                                        }
                                    }
                                }
                                if (!TextUtils.isEmpty(avatar)) {
                                    ImageLoader.getInstance()
                                            .loadImage(avatar, new SimpleImageLoadingListener() {
                                                @Override
                                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                                    if (PlayerUtil.getCurrentIndex() == requestIndex) {
                                                        mIvMusicPic.setImageBitmap(loadedImage);
                                                    }
                                                }
                                            });
                                }
                            }
                        });
            }
            break;
        }
    }

}
