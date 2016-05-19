package com.sanron.music.api;

import android.text.TextUtils;

import com.sanron.music.api.bean.LrcPicData;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by sanron on 16-5-19.
 */
public class LrcPicProvider {

    private static volatile LrcPicProvider sInstance;
    private LrcPicData mLrcPicData;
    private String mSongPictureLink;
    private String mArtistPictureLink;
    private String mLyricLink;

    private List<OnLrcPicChangeCallback> mCallbacks;

    public static LrcPicProvider get() {
        if (sInstance == null) {
            synchronized (LrcPicProvider.class) {
                if (sInstance == null) {
                    sInstance = new LrcPicProvider();
                }
            }
        }
        return sInstance;
    }

    private LrcPicProvider() {
        mCallbacks = new CopyOnWriteArrayList<>();
    }

    public void search(String word, String artist) {
        mLrcPicData = null;
        mSongPictureLink = null;
        mArtistPictureLink = null;
        mLyricLink = null;
        MusicApi.searchLrcPic(word, artist, 2, new JsonCallback<LrcPicData>() {
            @Override
            public void onSuccess(LrcPicData data) {
                mLrcPicData = data;
                List<LrcPicData.LrcPic> lrcPics = data.lrcPics;
                if (lrcPics != null) {
                    for (LrcPicData.LrcPic lrcPic : lrcPics) {

                        if (TextUtils.isEmpty(mSongPictureLink)) {
                            mSongPictureLink = lrcPic.pic1000x1000;
                            if (TextUtils.isEmpty(mSongPictureLink)) {
                                mSongPictureLink = lrcPic.pic500x500;
                            }
                        }

                        if (TextUtils.isEmpty(mArtistPictureLink)) {
                            mArtistPictureLink = lrcPic.avatar180x180;
                            if (TextUtils.isEmpty(mArtistPictureLink)) {
                                mArtistPictureLink = lrcPic.avatar500x500;
                            }
                        }

                        if (TextUtils.isEmpty(mLyricLink)) {
                            mLyricLink = lrcPic.lrc;
                        }

                    }
                }
                for (OnLrcPicChangeCallback callback : mCallbacks) {
                    callback.onLrcPicChange();
                }
            }

            @Override
            public void onFailure(Exception e) {
                for (OnLrcPicChangeCallback callback : mCallbacks) {
                    callback.onLrcPicChange();
                }
            }
        });
    }


    public LrcPicData getLrcPicData() {
        return mLrcPicData;
    }

    public String getSongPictureLink() {
        return mSongPictureLink;
    }

    public String getArtistPictureLink() {
        return mArtistPictureLink;
    }

    public String getLyricLink() {
        return mLyricLink;
    }

    public void addOnLrcPicChangeCallback(OnLrcPicChangeCallback callback) {
        mCallbacks.add(callback);
    }

    public void removeOnLrcPicChangeCallback(OnLrcPicChangeCallback callback) {
        mCallbacks.remove(callback);
    }

    public interface OnLrcPicChangeCallback {
        void onLrcPicChange();
    }
}

