package com.sanron.music.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.db.model.Music;
import com.sanron.music.service.IPlayer;

/**
 * Created by sanron on 16-3-24.
 */
public class NavigationHeader extends FrameLayout implements IPlayer.Callback {

    private IPlayer player;
    private ImageView ivMusicPic;
    private TextView tvMusicTitle;
    private TextView tvMusicArtist;


    public NavigationHeader(Context context, IPlayer player) {
        super(context, null);
        this.player = player;
        LayoutInflater.from(context).inflate(R.layout.navigation_header, this);
        ivMusicPic = (ImageView) findViewById(R.id.civ_music_pic);
        tvMusicTitle = (TextView) findViewById(R.id.tv_music_title);
        tvMusicArtist = (TextView) findViewById(R.id.tv_music_artist);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        player.addCallback(this);
        if(player.getState() != player.STATE_STOP){
            onStateChange(IPlayer.STATE_PREPARING);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        player.removeCallback(this);
    }

    @Override
    public void onLoadedPicture(Bitmap musicPic) {
        if (musicPic == null) {
            ivMusicPic.setImageResource(R.mipmap.default_song_pic);
        } else {
            ivMusicPic.setImageBitmap(musicPic);
        }
    }

    @Override
    public void onStateChange(int state) {
        switch (state) {
            case IPlayer.STATE_STOP: {
                tvMusicTitle.setText("叮咚音乐");
                tvMusicArtist.setText("");
            }
            break;

            case IPlayer.STATE_PREPARING: {
                Music music = player.getCurrentMusic();
                tvMusicTitle.setText(music.getTitle());
                String artist = music.getArtist();
                if (artist == null
                        || artist.equals("<unknown>")) {
                    artist = "未知歌手";
                }
                tvMusicArtist.setText(artist);
            }
            break;
        }
    }


    @Override
    public void onBufferingUpdate(int bufferedPosition) {

    }
}
