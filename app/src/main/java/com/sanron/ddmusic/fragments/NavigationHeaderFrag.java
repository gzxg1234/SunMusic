package com.sanron.ddmusic.fragments;

import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.ddmusic.R;
import com.sanron.ddmusic.activities.MainActivity;
import com.sanron.ddmusic.api.LrcPicProvider;
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.playback.Player;
import com.sanron.ddmusic.service.PlayUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 管理导航头部
 * Created by sanron on 16-4-18.
 */
public class NavigationHeaderFrag extends Fragment implements Player.OnPlayStateChangeListener, MainActivity.PlayerReadyCallback, LrcPicProvider.OnLrcPicChangeCallback {

    @BindView(R.id.civ_music_pic)
    ImageView mIvMusicPic;
    @BindView(R.id.tv_music_title)
    TextView mTvMusicTitle;
    @BindView(R.id.tv_music_artist)
    TextView mTvMusicArtist;

    public void setHeader(View header) {
        ButterKnife.bind(this, header);
    }

    @Override
    public void onPlayerReady() {
        PlayUtil.addPlayStateChangeListener(this);
        LrcPicProvider.get().addOnLrcPicChangeCallback(this);
        if (PlayUtil.getState() != Player.STATE_IDLE) {
            onPlayStateChange(Player.STATE_PREPARING);
        }
        if (PlayUtil.getState() >= Player.STATE_PREPARED) {
            onPlayStateChange(Player.STATE_PREPARED);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MainActivity) getActivity()).removePlayerReadyCallback(this);
        PlayUtil.removePlayStateChangeListener(this);
        LrcPicProvider.get().removeOnLrcPicChangeCallback(this);
    }

    @Override
    public void onPlayStateChange(int state) {
        switch (state) {
            case Player.STATE_IDLE: {
                mTvMusicTitle.setText("叮咚音乐");
                mTvMusicArtist.setText("");
                mIvMusicPic.setImageResource(R.mipmap.default_small_song_pic);
            }
            break;

            case Player.STATE_PREPARING: {
                mIvMusicPic.setImageResource(R.mipmap.default_small_song_pic);
                Music music = PlayUtil.getCurrentMusic();
                mTvMusicTitle.setText(music.getTitle());
                String artist = music.getArtist();
                if ("<unknown>".equals(artist)) {
                    artist = "未知歌手";
                }
                mTvMusicArtist.setText(artist);
            }
            break;
        }
    }

    @Override
    public void onLrcPicChange() {
        ImageLoader.getInstance().cancelDisplayTask(mIvMusicPic);
        String artistPicture = LrcPicProvider.get().getArtistPictureLink();
        if (!TextUtils.isEmpty(artistPicture)) {
            ImageLoader
                    .getInstance()
                    .displayImage(
                            artistPicture, mIvMusicPic);
        }
    }
}
