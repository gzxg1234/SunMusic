package com.sanron.music.fragments;

import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.R;
import com.sanron.music.activities.MainActivity;
import com.sanron.music.api.LrcPicProvider;
import com.sanron.music.db.bean.Music;
import com.sanron.music.playback.Player;
import com.sanron.music.service.PlayerUtil;

/**
 * 管理导航头部
 * Created by sanron on 16-4-18.
 */
public class NavigationHeaderFrag extends Fragment implements Player.OnPlayStateChangeListener, MainActivity.PlayerReadyCallback, LrcPicProvider.OnLrcPicChangeCallback {

    private View mHeader;
    private ImageView mIvMusicPic;
    private TextView mTvMusicTitle;
    private TextView mTvMusicArtist;

    public void setHeader(View header) {
        this.mHeader = header;
        mIvMusicPic = (ImageView) header.findViewById(R.id.civ_music_pic);
        mTvMusicTitle = (TextView) header.findViewById(R.id.tv_music_title);
        mTvMusicArtist = (TextView) header.findViewById(R.id.tv_music_artist);
    }

    @Override
    public void onPlayerReady() {
        PlayerUtil.addPlayStateChangeListener(this);
        LrcPicProvider.get().addOnLrcPicChangeCallback(this);
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
        LrcPicProvider.get().removeOnLrcPicChangeCallback(this);
    }

    @Override
    public void onPlayStateChange(int state) {
        switch (state) {
            case Player.STATE_STOP: {
                mTvMusicTitle.setText("叮咚音乐");
                mTvMusicArtist.setText("");
                mIvMusicPic.setImageResource(R.mipmap.default_small_song_pic);
            }
            break;

            case Player.STATE_PREPARING: {
                mIvMusicPic.setImageResource(R.mipmap.default_small_song_pic);
                Music music = PlayerUtil.getCurrentMusic();
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
