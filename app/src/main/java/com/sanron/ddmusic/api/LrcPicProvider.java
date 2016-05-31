package com.sanron.ddmusic.api;

import android.text.TextUtils;

import com.sanron.ddmusic.api.bean.LrcPicData;
import com.sanron.ddmusic.api.callback.JsonCallback;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.Call;

/**
 * Created by sanron on 16-5-19.
 */
public class LrcPicProvider {

    private String search;
    private LrcPicData mLrcPicData;
    private String mSongPictureLink;
    private String mArtistPictureLink;
    private String mLyricLink;
    private Call mCall;

    private List<OnLrcPicChangeCallback> mCallbacks;

    private static volatile LrcPicProvider sInstance;

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

    public void search(final String word, final String artist) {
        if (mCall != null) {
            mCall.cancel();
        }
        mLrcPicData = null;
        mSongPictureLink = null;
        mArtistPictureLink = null;
        mLyricLink = null;
        search = word + "$" + artist;
        mCall = MusicApi.searchLrcPic(word, artist, 2, new JsonCallback<LrcPicData>() {
            final String requestSearch = word + "$" + artist;

            @Override
            public void onSuccess(LrcPicData data) {
                if (!search.equals(requestSearch)) {
                    return;
                }

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

