package com.sanron.music.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.sanron.music.AppContext;
import com.sanron.music.R;
import com.sanron.music.db.model.Music;
import com.sanron.music.net.ApiCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.LrcPicData;
import com.sanron.music.service.IPlayer;

import java.util.List;

import okhttp3.Call;

/**
 * Created by sanron on 16-3-24.
 */
public class NavigationHeader extends FrameLayout implements IPlayer.OnPlayStateChangeListener {

    private IPlayer mPlayer;
    private LinearLayout mLinearLayout;
    private ImageView mIvMusicPic;
    private TextView mTvMusicTitle;
    private TextView mTvMusicArtist;
    private Call mGetAvatarCall;

    public NavigationHeader(Context context, IPlayer player) {
        super(context, null);
        this.mPlayer = player;
        LayoutInflater.from(context).inflate(R.layout.navigation_header, this);
        mLinearLayout = (LinearLayout) getChildAt(0);
        mIvMusicPic = (ImageView) findViewById(R.id.civ_music_pic);
        mTvMusicTitle = (TextView) findViewById(R.id.tv_music_title);
        mTvMusicArtist = (TextView) findViewById(R.id.tv_music_artist);
        ((AppContext) context.getApplicationContext()).setViewFitsStatusBar(mLinearLayout);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mPlayer.addPlayStateChangeListener(this);
        if (mPlayer.getState() != mPlayer.STATE_STOP) {
            onPlayStateChange(IPlayer.STATE_PREPARING);
        }
        if (mPlayer.getState() >= IPlayer.STATE_PREPARED) {
            onPlayStateChange(IPlayer.STATE_PREPARED);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPlayer.removePlayStateChangeListener(this);
    }

    @Override

    public void onPlayStateChange(int state) {
        switch (state) {
            case IPlayer.STATE_STOP: {
                mTvMusicTitle.setText("叮咚音乐");
                mTvMusicArtist.setText("");
            }
            break;

            case IPlayer.STATE_PREPARING: {
                Music music = mPlayer.getCurrentMusic();
                mTvMusicTitle.setText(music.getTitle());
                String artist = music.getArtist();
                if ("<unknown>".equals(artist)) {
                    artist = "未知歌手";
                }
                mTvMusicArtist.setText(artist);
            }
            break;

            case IPlayer.STATE_PREPARED: {
                mIvMusicPic.setImageResource(R.mipmap.default_small_song_pic);
                Music music = mPlayer.getCurrentMusic();
                final int currentIndex = mPlayer.getCurrentIndex();
                String artist = music.getArtist();
                if (mGetAvatarCall != null) {
                    mGetAvatarCall.cancel();
                }
                mGetAvatarCall = MusicApi.searchLrcPic(music.getTitle(),
                        "<unknown>".equals(artist) ? "" : artist,
                        2,
                        new ApiCallback<LrcPicData>() {
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
                                                    if (mPlayer.getCurrentIndex() == requestIndex) {
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