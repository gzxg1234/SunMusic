package com.sanron.ddmusic.fragments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.sanron.ddmusic.R;
import com.sanron.ddmusic.api.AppHttpClient;
import com.sanron.ddmusic.api.LrcPicProvider;
import com.sanron.ddmusic.api.StringCallback;
import com.sanron.ddmusic.common.FastBlur;
import com.sanron.ddmusic.common.ViewTool;
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.fragments.base.BaseFragment;
import com.sanron.ddmusic.playback.Player;
import com.sanron.ddmusic.service.PlayUtil;
import com.sanron.ddmusic.task.CheckFavoriteTask;
import com.sanron.ddmusic.task.FavoriteMusicTask;
import com.sanron.ddmusic.view.ShowPlayQueueWindow;
import com.sanron.lyricview.model.Lyric;
import com.sanron.lyricview.view.LyricView;
import com.viewpagerindicator.PageIndicator;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Request;

/**
 * 播放界面
 * Created by Administrator on 2016/3/5.
 */
public class NowPlayingFragment extends BaseFragment implements View.OnClickListener,
        Player.OnPlayStateChangeListener, Player.OnBufferListener, SeekBar.OnSeekBarChangeListener,
        LrcPicProvider.OnLrcPicChangeCallback {

    @BindView(R.id.small_player)
    ViewGroup mSmallPlayer;
    @BindView(R.id.s_play_progress)
    ProgressBar mSplayProgress;
    @BindView(R.id.s_iv_song_pic)
    ImageView mSivSongPicture;
    @BindView(R.id.s_tv_title)
    TextView mStvTitle;
    @BindView(R.id.s_tv_artist)
    TextView mStvArtist;
    @BindView(R.id.s_ibtn_play_pause)
    ImageButton mSibtnTogglePlay;
    @BindView(R.id.s_ibtn_next)
    ImageButton mSibtnNext;

    @BindView(R.id.big_player)
    ViewGroup mBigPlayer;
    @BindView(R.id.top_bar)
    ViewGroup mTopBar;
    @BindView(R.id.tv_music_title)
    TextView mTvTitle;
    @BindView(R.id.tv_music_artist)
    TextView mTvArtist;
    @BindView(R.id.seek_play_progress)
    SeekBar mPlayProgress;
    @BindView(R.id.tv_music_progress)
    TextView mTvPlayPosition;
    @BindView(R.id.tv_music_duration)
    TextView mTvDuration;
    @BindView(R.id.view_back)
    View mViewBack;
    @BindView(R.id.iv_play_mode)
    ImageView mIvChangeMode;
    @BindView(R.id.iv_previous)
    ImageView mIvPrevious;
    @BindView(R.id.fab_toggle_play)
    FloatingActionButton mFabTogglePlay;
    @BindView(R.id.iv_next)
    ImageView mIvNext;
    @BindView(R.id.iv_play_queue)
    ImageView mIvPlayQueue;

    @BindView(R.id.tv_buffering_hint)
    TextView mBufferingHint;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    @BindView(R.id.page_indicator)
    PageIndicator mPageIndicator;
    @BindView(R.id.vs_blur_background)
    ViewSwitcher mViewSwitcher;

    ShowPlayQueueWindow mShowPlayQueueWindow;

    //1
    ListView mLvSimilarInfo;
    //2
    View mPagerView2;
    ImageView mIvDownload;
    ImageView mIvFavorite;
    ImageView mIvSongPicture;
    //3
    View mPagerView3;
    LyricView mLyricView;
    View mLyricSetting;

    private boolean mIsFavorite;
    private List<View> mPagerViews;
    private NonViewAware mNonViewAware;
    /**
     * 提示播放模式
     */
    private Toast mModeToast;

    private Timer mTimer = new Timer();
    private TimerTask mUpdateProgressTask = new UpdateProgressTask();
    private TimerTask mUpdateLyricTask = new UpdateLyricTask();

    private UIHandler mHandler = new UIHandler(this);

    public static final int WHAT_RESET_SONG_PICTURE = 1;
    public static final int WHAT_UPDATE_PROGRESS = 2;
    public static final int WHAT_UPDATE_LYRIC = 3;

    private static class UIHandler extends Handler {
        private WeakReference<NowPlayingFragment> mReference;

        public UIHandler(NowPlayingFragment nowPlayingFragment) {
            mReference = new WeakReference<>(nowPlayingFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            NowPlayingFragment nowPlayingFragment = mReference.get();
            if (nowPlayingFragment == null) {
                return;
            }
            switch (msg.what) {
                case WHAT_RESET_SONG_PICTURE: {
                    nowPlayingFragment.setSongPicture(null);
                }
                break;

                case WHAT_UPDATE_PROGRESS: {
                    nowPlayingFragment.setPlayProgress(msg.arg1);
                }
                break;

                case WHAT_UPDATE_LYRIC: {
                    nowPlayingFragment.mLyricView.setCurrentTime(msg.arg1);
                }
                break;
            }
        }
    }

    @Override
    public int getViewResId() {
        return R.layout.now_playing;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPlayProgress.setOnSeekBarChangeListener(this);
        mSibtnTogglePlay.setOnClickListener(this);
        mSibtnNext.setOnClickListener(this);
        mIvChangeMode.setOnClickListener(this);
        mIvPrevious.setOnClickListener(this);
        mFabTogglePlay.setOnClickListener(this);
        mIvNext.setOnClickListener(this);
        mIvPlayQueue.setOnClickListener(this);
        mViewBack.setOnClickListener(this);

        ViewTool.setViewFitsStatusBar(mTopBar);
        setupViewPager();
        for (int i = 0; i < mViewSwitcher.getChildCount(); i++) {
            //设置颜色滤镜，调暗色调
            ((ImageView) mViewSwitcher.getChildAt(i)).setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        }

        if (savedInstanceState != null) {
            mSmallPlayer.setVisibility(savedInstanceState.getInt("smallPlayerVisibility", View.VISIBLE));
            mBigPlayer.setVisibility(savedInstanceState.getInt("bigPlayerVisibility", View.VISIBLE));
        }
    }

    private void setupViewPager() {
        mPagerViews = new ArrayList<>();

        mLvSimilarInfo = new ListView(getContext());
        mPagerViews.add(mLvSimilarInfo);

        mPagerView2 = LayoutInflater.from(getContext())
                .inflate(R.layout.now_playing_picture, null);
        mIvFavorite = ButterKnife.findById(mPagerView2, R.id.iv_favorite);
        mIvDownload = ButterKnife.findById(mPagerView2, R.id.iv_download);
        mIvSongPicture = ButterKnife.findById(mPagerView2, R.id.iv_song_picture);
        mIvFavorite.setOnClickListener(this);
        mPagerViews.add(mPagerView2);

        mPagerView3 = LayoutInflater.from(getContext())
                .inflate(R.layout.now_playing_lyric, null);
        mLyricView = ButterKnife.findById(mPagerView3, R.id.lyric_view);
        mLyricView.setEmptyTip(getString(R.string.app_name));
        mLyricSetting = ButterKnife.findById(mPagerView3, R.id.lyric_setting);
        mPagerViews.add(mPagerView3);

        mViewPager.setAdapter(new LocalPagerAdapter());
        mPageIndicator.setViewPager(mViewPager, 1);
    }

    @Override
    public void onPlayerReady() {
        setupPlayState();
    }

    /**
     * 设置监听和设置界面状态
     */
    private void setupPlayState() {
        PlayUtil.addPlayStateChangeListener(this);
        PlayUtil.addOnBufferListener(this);
        LrcPicProvider.get().addOnLrcPicChangeCallback(this);

        Music music = PlayUtil.getCurrentMusic();
        if (music != null) {
            setTitleText(music.getTitle());
            setArtistText(music.getArtist());
        }

        setPlayProgress(PlayUtil.getProgress());
        setSongDuration(PlayUtil.getDuration());
        if (PlayUtil.isPlaying()) {
            setIsPlaying(true);
            //是否收藏过
            new CheckFavoriteTask(getContext(), music) {
                @Override
                protected void onPostExecute(Boolean isFavorite) {
                    setIsFavorite(isFavorite);
                }
            }.execute();
        } else {
            setIsPlaying(false);
        }

        setModeIcon(PlayUtil.getPlayMode());
        onLrcPicChange();
    }

    @Override
    public void onPlayStateChange(int state) {
        switch (state) {
            case Player.STATE_IDLE: {
                setTitleText(getContext().getString(R.string.app_name));
                setArtistText("");
                setSongPicture(null);
                mLyricView.setLyric(null);
                setSongDuration(0);
                setPlayProgress(0);
                setIsPlaying(false);
            }
            break;

            case Player.STATE_PAUSE: {
                setIsPlaying(false);
            }
            break;

            case Player.STATE_PLAYING: {
                setIsPlaying(true);
            }
            break;

            case Player.STATE_PREPARING: {
                Music music = PlayUtil.getCurrentMusic();
                if (music == null) {
                    return;
                }
                //3秒后设置默认图片
                mHandler.removeMessages(WHAT_RESET_SONG_PICTURE);
                mHandler.sendEmptyMessageDelayed(WHAT_RESET_SONG_PICTURE, 3000);
                setTitleText(music.getTitle());
                setArtistText(music.getArtist());
                setSongDuration(0);
                setPlayProgress(0);
                mLyricView.setLyric(null);
                setIsPlaying(false);

                //是否收藏过
                new CheckFavoriteTask(getContext(), music) {
                    @Override
                    protected void onPostExecute(Boolean isFavorite) {
                        setIsFavorite(isFavorite);
                    }
                }.execute();
            }
            break;

            case Player.STATE_PREPARED: {
                Music music = PlayUtil.getCurrentMusic();
                if (music == null) {
                    return;
                }
                setSongDuration(PlayUtil.getDuration());
                setPlayProgress(PlayUtil.getProgress());
                if (music.getType() == Music.TYPE_LOCAL) {
                    mPlayProgress.setSecondaryProgress(PlayUtil.getDuration());
                } else if (music.getType() == Music.TYPE_WEB) {
                    mPlayProgress.setSecondaryProgress(0);
                }
            }
            break;
        }
    }

    private void setIsPlaying(boolean playing) {
        if (playing) {
            startUpdateTask();
            mSibtnTogglePlay.setImageResource(R.mipmap.ic_pause_black_36dp);
            mFabTogglePlay.setImageResource(R.mipmap.ic_pause_white_24dp);
        } else {
            stopUpdateTask();
            mSibtnTogglePlay.setImageResource(R.mipmap.ic_play_arrow_black_36dp);
            mFabTogglePlay.setImageResource(R.mipmap.ic_play_arrow_white_24dp);
        }
    }

    @Override
    public void onLrcPicChange() {
        if (mNonViewAware != null) {
            ImageLoader.getInstance().cancelDisplayTask(mNonViewAware);
        }
        String songPicture = LrcPicProvider.get().getSongPictureLink();
        if (!TextUtils.isEmpty(songPicture)) {
            mNonViewAware = new NonViewAware(
                    new ImageSize(mIvSongPicture.getWidth(), mIvSongPicture.getHeight()),
                    ViewScaleType.CROP);
            ImageLoader
                    .getInstance()
                    .displayImage(songPicture, mNonViewAware, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            mHandler.removeMessages(WHAT_RESET_SONG_PICTURE);
                            setSongPicture(loadedImage);
                        }
                    });

        }

        String lyricLink = LrcPicProvider.get().getLyricLink();
        if (!TextUtils.isEmpty(lyricLink)) {
            Request request = new Request.Builder().url(lyricLink).get().build();
            AppHttpClient.get(request, new StringCallback() {
                @Override
                public void onSuccess(String s) {
                    mLyricView.setLyric(Lyric.read(s));
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        }
    }

    @Override
    public void onBufferingUpdate(int bufferedPosition) {
//        if (bufferedPosition > mPlayProgress.getSecondaryProgress()) {
        mPlayProgress.setSecondaryProgress(bufferedPosition);
//        }
    }

    @Override
    public void onBufferStart() {
        mBufferingHint.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBufferEnd() {
        mBufferingHint.setVisibility(View.INVISIBLE);
    }

    public void setSongPicture(Bitmap img) {
        if (img == null) {
            mSivSongPicture.setImageResource(R.mipmap.default_song_pic);
            mIvSongPicture.setImageResource(R.mipmap.default_song_pic);
            setBlurBackground(null);
        } else {
            mSivSongPicture.setImageBitmap(img);
            mIvSongPicture.setImageBitmap(img);
            new AsyncTask<Bitmap, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(Bitmap... params) {
                    //预先降低图片质量，减少内存占用
                    Bitmap src = params[0];
                    Bitmap resizeBmp = Bitmap.createBitmap(src.getWidth() / 2, src.getHeight() / 2,
                            Bitmap.Config.RGB_565);
                    Canvas canvas = new Canvas(resizeBmp);
                    canvas.drawBitmap(src,
                            new Rect(0, 0, src.getWidth(), src.getHeight()),
                            new Rect(0, 0, resizeBmp.getWidth(), resizeBmp.getHeight()),
                            new Paint());
                    return FastBlur.doBlur(resizeBmp, 40, true);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    setBlurBackground(bitmap);
                }
            }.execute(img);
        }
    }

    private void setBlurBackground(Bitmap bitmap) {
        Drawable drawable;
        if (bitmap == null) {
            drawable = new ColorDrawable(0x88000000);
        } else {
            drawable = new BitmapDrawable(bitmap);
        }
        if (mViewSwitcher.getDisplayedChild() == 0) {
            ((ImageView) mViewSwitcher.getChildAt(1)).setImageDrawable(drawable);
            mViewSwitcher.showNext();
        } else {
            ((ImageView) mViewSwitcher.getChildAt(0)).setImageDrawable(drawable);
            mViewSwitcher.showPrevious();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopUpdateTask();
        PlayUtil.removePlayStateChangeListener(this);
        PlayUtil.removeBufferListener(this);
        LrcPicProvider.get().removeOnLrcPicChangeCallback(this);
        if (mShowPlayQueueWindow != null && mShowPlayQueueWindow.isShowing()) {
            mShowPlayQueueWindow.dismiss();
        }
    }


    private void setTitleText(String title) {
        mStvTitle.setText(title);
        mTvTitle.setText(title);
    }

    private void setArtistText(String artist) {
        if (artist == null
                || artist.equals("<unknown>")) {
            artist = "未知歌手";
        }
        mStvArtist.setText(artist);
        mTvArtist.setText(artist);
    }

    private void setPlayProgress(int position) {
        position = Math.max(0, position);
        mPlayProgress.setProgress(position);
        mSplayProgress.setProgress(position);
        mTvPlayPosition.setText(formatTime(position));
    }

    private void setSongDuration(int duration) {
        mPlayProgress.setMax(duration);
        mSplayProgress.setMax(duration);
        mTvDuration.setText(formatTime(duration));
    }

    private String formatTime(int millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        return sdf.format(new Date(millis));
    }

    private void setModeIcon(int mode) {
        int iconId = 0;
        switch (mode) {
            case Player.MODE_IN_TURN:
                iconId = R.mipmap.ic_repeat_white_24dp;
                break;
            case Player.MODE_LOOP:
                iconId = R.mipmap.ic_repeat_one_white_24dp;
                break;
            case Player.MODE_RANDOM:
                iconId = R.mipmap.ic_shuffle_white_24dp;
                break;
        }
        mIvChangeMode.setImageResource(iconId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.iv_play_mode: {
                int newMode = (PlayUtil.getPlayMode() + 1) % 3;
                PlayUtil.setPlayMode(newMode);
                String msg = "";
                setModeIcon(newMode);
                switch (newMode) {
                    case Player.MODE_IN_TURN:
                        msg = "顺序播放";
                        break;
                    case Player.MODE_LOOP:
                        msg = "单曲循环";
                        break;
                    case Player.MODE_RANDOM:
                        msg = "随机播放";
                        break;
                }
                if (mModeToast != null) {
                    mModeToast.cancel();
                }
                mModeToast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
                mModeToast.setText(msg);
                mModeToast.show();
            }
            break;

            case R.id.iv_previous: {
                PlayUtil.previous();
            }
            break;

            case R.id.s_ibtn_play_pause:
            case R.id.fab_toggle_play: {
                if (PlayUtil.getState() == Player.STATE_IDLE) {
                    List<Music> queue = PlayUtil.getQueue();
                    if (queue != null
                            && queue.size() > 0) {
                        PlayUtil.play(0);
                    } else {
                        ViewTool.show("播放列表为空");
                    }
                    return;
                }
                PlayUtil.togglePlayPause();
            }
            break;

            case R.id.s_ibtn_next:
            case R.id.iv_next: {
                PlayUtil.next();
            }
            break;

            case R.id.iv_play_queue: {
                mShowPlayQueueWindow = new ShowPlayQueueWindow(getActivity());
                mShowPlayQueueWindow.showAtLocation(getView(), Gravity.BOTTOM, 0, 0);
            }
            break;

            case R.id.view_back: {
                getMainActivity().collapseSlidingPanel();
            }
            break;

            case R.id.iv_favorite: {
                if (mIsFavorite) {
                } else {
                    new FavoriteMusicTask(getContext(), PlayUtil.getCurrentMusic()) {
                        @Override
                        protected void onPostExecute(Boolean aBoolean) {
                            setIsFavorite(aBoolean);
                        }
                    }.execute();
                }
            }
            break;
        }
    }

    public void setIsFavorite(boolean favorite) {
        mIsFavorite = favorite;
        if (favorite) {
            mIvFavorite.setImageResource(R.mipmap.ic_favorite_pink_600_24dp);
        } else {
            mIvFavorite.setImageResource(R.mipmap.ic_favorite_border_white_24dp);
        }
    }

    //主界面底部的小控制面板可见性
    public void setSmallControllerVisibility(int visibility) {
        mSmallPlayer.setVisibility(visibility);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("smallPlayerVisibility", mSmallPlayer.getVisibility());
        outState.putInt("bigPlayerVisibility", mBigPlayer.getVisibility());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        stopUpdateTask();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (PlayUtil.isPlaying()) {
            startUpdateTask();
        }
        PlayUtil.seekTo(seekBar.getProgress());
//        if (seekBar.getProgress() <= seekBar.getSecondaryProgress()) {
//            PlayUtil.seekTo(seekBar.getProgress());
//            mLyricView.setCurrentTime(seekBar.getProgress());
//        } else {
//            seekBar.setProgress(PlayUtil.getProgress());
//        }
    }

    private void startUpdateLyric() {
        if (mUpdateLyricTask != null) {
            mUpdateLyricTask.cancel();
            mUpdateLyricTask = new UpdateLyricTask();
        }
        mTimer.schedule(mUpdateLyricTask, 0, 500);
    }

    private void stopUpdateLyric() {
        if (mUpdateLyricTask != null) {
            mUpdateLyricTask.cancel();
        }
    }

    private void startUpdateProgress() {
        if (mUpdateProgressTask != null) {
            mUpdateProgressTask.cancel();
            mUpdateProgressTask = new UpdateProgressTask();
        }
        mTimer.schedule(mUpdateProgressTask, 0, 100);
    }

    private void stopUpdatkProgress() {
        if (mUpdateProgressTask != null) {
            mUpdateProgressTask.cancel();
        }
    }

    private void startUpdateTask() {
        startUpdateProgress();
        startUpdateLyric();
    }

    private void stopUpdateTask() {
        stopUpdateLyric();
        stopUpdatkProgress();
    }


    private class UpdateLyricTask extends TimerTask {
        @Override
        public void run() {
            Message message = Message.obtain();
            message.what = WHAT_UPDATE_LYRIC;
            message.arg1 = PlayUtil.getProgress();
            mHandler.sendMessage(message);
        }
    }

    private class UpdateProgressTask extends TimerTask {
        @Override
        public void run() {
            Message message = Message.obtain();
            message.what = WHAT_UPDATE_PROGRESS;
            message.arg1 = PlayUtil.getProgress();
            mHandler.sendMessage(message);
        }
    }


    private class LocalPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mPagerViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mPagerViews.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mPagerViews.get(position));
        }
    }
}
