package com.sanron.music.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.service.IPlayer;
import com.sanron.music.service.Playable;

/**
 * Created by sanron on 16-3-24.
 */
public class NavigationHeader extends FrameLayout implements IPlayer.Callback {

    private IPlayer player;
    private ImageView ivMusicPic;
    private TextView tvMusicTitle;
    private TextView tvMusicArtist;


    public NavigationHeader(Context context, IPlayer player) {
        super(context,null);
        this.player = player;
        LayoutInflater.from(context).inflate(R.layout.navigation_header,this);
        ivMusicPic = (ImageView) findViewById(R.id.civ_music_pic);
        tvMusicTitle = (TextView) findViewById(R.id.tv_music_title);
        tvMusicArtist = (TextView) findViewById(R.id.tv_music_artist);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        player.addCallback(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        player.removeCallback(this);
    }

    @Override
    public void onLoadedPicture(Bitmap musicPic) {
        if(musicPic == null){
            ivMusicPic.setImageResource(R.mipmap.default_song_pic);
        }else{
            ivMusicPic.setImageBitmap(musicPic);
        }
    }

    @Override
    public void onStateChange(int state) {
        switch (state){
            case IPlayer.STATE_STOP:{
                tvMusicTitle.setText("叮咚音乐");
                tvMusicArtist.setText("");
            }break;

            case IPlayer.STATE_PREPARED:{
                Playable playable = player.getQueue().get(player.getCurrentIndex());
                tvMusicTitle.setText(playable.title());
                tvMusicArtist.setText(playable.artist());
            }break;
        }
    }

    @Override
    public void onModeChange(int newMode) {

    }

    @Override
    public void onBufferingUpdate(int bufferedPosition) {

    }
}
