package com.sanron.music.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.sanron.music.AppContext;
import com.sanron.music.R;
import com.sanron.music.db.model.Music;
import com.sanron.music.net.ApiCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.LrcPicResult;
import com.sanron.music.service.IPlayer;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;

/**
 * Created by sanron on 16-3-24.
 */
public class NavigationHeader extends FrameLayout implements IPlayer.OnPlayStateChangeListener {

    private IPlayer player;
    private LinearLayout linearLayout;
    private ImageView ivMusicPic;
    private TextView tvMusicTitle;
    private TextView tvMusicArtist;
    private Call getAvatarCall;

    public NavigationHeader(Context context, IPlayer player) {
        super(context, null);
        this.player = player;
        LayoutInflater.from(context).inflate(R.layout.navigation_header, this);
        linearLayout = (LinearLayout) getChildAt(0);
        ivMusicPic = (ImageView) findViewById(R.id.civ_music_pic);
        tvMusicTitle = (TextView) findViewById(R.id.tv_music_title);
        tvMusicArtist = (TextView) findViewById(R.id.tv_music_artist);
        ((AppContext) context.getApplicationContext()).setViewFitsStatusBar(linearLayout);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        player.addPlayStateChangeListener(this);
        if (player.getState() != player.STATE_STOP) {
            onPlayStateChange(IPlayer.STATE_PREPARING);
        }
        if(player.getState() >= IPlayer.STATE_PREPARED){
            onPlayStateChange(IPlayer.STATE_PREPARED);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        player.removePlayStateChangeListener(this);
    }

    @Override
    public void onPlayStateChange(int state) {
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
                if ("<unknown>".equals(artist)) {
                    artist = "未知歌手";
                }
                tvMusicArtist.setText(artist);
            }
            break;

            case IPlayer.STATE_PREPARED: {
                ivMusicPic.setImageResource(R.mipmap.default_small_song_pic);
                Music music = player.getCurrentMusic();
                final int currentIndex = player.getCurrentIndex();
                String artist = music.getArtist();
                if (getAvatarCall != null) {
                    getAvatarCall.cancel();
                }
                getAvatarCall = MusicApi.searchLrcPic(music.getTitle(),
                        "<unknown>".equals(artist) ? "" : artist,
                        2,
                        new ApiCallback<LrcPicResult>() {
                            final int requestIndex = currentIndex;

                            @Override
                            public void onSuccess(Call call, LrcPicResult data) {
                                List<LrcPicResult.LrcPic> lrcPics = data.getLrcPics();
                                Bitmap loadedImage = null;
                                String avatar = null;
                                if (lrcPics != null) {
                                    for (LrcPicResult.LrcPic lrcPic : lrcPics) {
                                        avatar = lrcPic.getAvatar500x500();
                                        if (TextUtils.isEmpty(avatar)) {
                                            avatar = lrcPic.getAvatar180x180();
                                        }
                                        if (!TextUtils.isEmpty(avatar)) {
                                            break;
                                        }
                                    }
                                }
                                if (!TextUtils.isEmpty(avatar)) {
                                    ImageSize imageSize = new ImageSize(ivMusicPic.getWidth(), ivMusicPic.getHeight());
                                    loadedImage = ImageLoader.getInstance().loadImageSync(avatar, imageSize);
                                }
                                if (requestIndex == player.getCurrentIndex()) {
                                    final Bitmap finalLoadedImage = loadedImage;
                                    post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ivMusicPic.setImageBitmap(finalLoadedImage);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(Call call, IOException e) {
                            }
                        });
            }
            break;
        }
    }
}
