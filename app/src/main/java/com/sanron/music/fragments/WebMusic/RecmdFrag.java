package com.sanron.music.fragments.WebMusic;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.sanron.music.R;
import com.sanron.music.activities.MainActivity;
import com.sanron.music.net.ApiCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.FocusPic;
import com.sanron.music.net.bean.FocusPicData;
import com.sanron.music.net.bean.HotSongListData;
import com.sanron.music.net.bean.HotTagData;
import com.sanron.music.net.bean.RecmdSongData;
import com.sanron.music.net.bean.RecommendSong;
import com.sanron.music.net.bean.SongList;
import com.sanron.music.net.bean.Tag;
import com.sanron.music.view.HotSongListView;
import com.sanron.music.view.RecmdSongView;
import com.viewpagerindicator.CirclePageIndicator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;

/**
 * Created by Administrator on 2016/3/10.
 */
public class RecmdFrag extends BaseWebFrag implements View.OnClickListener {

    /**
     * 是否已经获取过数据
     */
    private boolean hasLoadData = false;
    private ImageLoader imageLoader = ImageLoader.getInstance();

    //轮播
    private List<FocusPic> focusPics;
    private ViewPager pagerFocusPic;
    private CirclePageIndicator pageIndicator;
    private FocusPicAdapter focusPicAdapter;

    //分类
    private List<Tag> hotTags;
    private List<TextView> tvHotTags;
    private TextView tvMoreTag;

    //热门歌单
    private List<SongList> hotSongLists;
    private List<HotSongListView> hotSongListViews;

    //推荐歌曲
    private List<RecommendSong> recmdSongs;
    private List<RecmdSongView> recmdSongViews;

    private DisplayImageOptions imageOptions;


    public static final int EVENT_CLICK_TAG = 1;
    public static final int EVENT_CLICK_SONGLIST = 2;
    public static final int EVENT_CLICK_MORE_TAG = 3;

    public static final String EXTRA_SONGLIST_ID = "list_id";
    public static final String EXTRA_TAG_NAME = "tag";


    private Handler handler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheOnDisk(true)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();
        focusPicAdapter = new FocusPicAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.web_frag_recmd, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //轮播
        pagerFocusPic = $(R.id.pager_focus_pic);
        pageIndicator = $(R.id.page_indicator);

        pagerFocusPic.setAdapter(focusPicAdapter);
        pageIndicator.setViewPager(pagerFocusPic);

        //热门标签
        tvHotTags = new LinkedList<>();
        LinearLayout hotTagGroup = $(R.id.hot_tag_group);
        for (int i = 0; i < hotTagGroup.getChildCount(); i++) {
            TextView tv = (TextView) (hotTagGroup.getChildAt(i));
            if (i == hotTagGroup.getChildCount() - 1) {
                tvMoreTag = tv;
                break;
            }
            tvHotTags.add(tv);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String tag = ((TextView) view).getText().toString();
                    if (!TextUtils.isEmpty(tag)) {

                    }
                }
            });
        }

        //热门歌单
        hotSongListViews = new LinkedList<>();
        LinearLayout songListGroup1 = $(R.id.hot_songlist_group1);
        LinearLayout songListGroup2 = $(R.id.hot_songlist_group2);
        for (int i = 0; i < songListGroup1.getChildCount(); i++) {
            hotSongListViews.add((HotSongListView) songListGroup1.getChildAt(i));
        }
        for (int i = 0; i < songListGroup2.getChildCount(); i++) {
            hotSongListViews.add((HotSongListView) songListGroup2.getChildAt(i));
        }

        //推荐歌曲
        recmdSongViews = new ArrayList<>();
        ViewGroup recmdSongGroup = $(R.id.recmd_song_group);
        for (int i = 0; i < recmdSongGroup.getChildCount(); i++) {
            recmdSongViews.add((RecmdSongView) recmdSongGroup.getChildAt(i));
        }


        if (!hasLoadData) {
            getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    getData();
                }
            });
            hasLoadData = true;
        } else {
            setFocusPics(focusPics);
            setHogTags(hotTags);
            setHotSongList(hotSongLists);
            setRecmdSongs(recmdSongs);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void getData() {
        //获取轮播信息
        Call call1 = MusicApi.focusPic(10, new ApiCallback<FocusPicData>() {

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onSuccess(FocusPicData data) {
                List<FocusPic> focusPics = data.pics;
                final List<FocusPic> result = new LinkedList<>();
                if (focusPics != null) {
                    for (int i = 0; i < focusPics.size() && result.size() < 6; i++) {
                        FocusPic focusPic = focusPics.get(i);
                        if (focusPic.type == FocusPic.TYPE_ALBUM
                                || focusPic.type == FocusPic.TYPE_SONG_LIST) {
                            result.add(focusPic);
                        }
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setFocusPics(result);
                    }
                });
            }
        });

        //热门标签
        Call call2 = MusicApi.hotTag(tvHotTags.size(), new ApiCallback<HotTagData>() {

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onSuccess(final HotTagData data) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setHogTags(data.tags);
                    }
                });
            }
        });


        //获取热门歌单
        Call call3 = MusicApi.hotSongList(6, new ApiCallback<HotSongListData>() {
            @Override
            public void onFailure(Call call, IOException e) {
            }


            @Override
            public void onSuccess(final HotSongListData data) {
                if (data != null
                        && data.content != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            setHotSongList(data.content.songLists);
                        }
                    });
                }
            }
        });

        //获取推荐歌曲
        Call call4 = MusicApi.recmdSongs(6, new ApiCallback<RecmdSongData>() {

            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onSuccess(final RecmdSongData data) {
                if (data.content != null
                        && data.content.size() > 0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            setRecmdSongs(data.content.get(0).songs);
                        }
                    });
                }
            }
        });

        addCall(call1);
        addCall(call2);
        addCall(call3);
        addCall(call4);
    }

    private void setRecmdSongs(List<RecommendSong> recmdSongs) {
        this.recmdSongs = recmdSongs;
        if (recmdSongs != null) {
            for (int i = 0; i < recmdSongs.size() && i < recmdSongViews.size(); i++) {
                RecommendSong recommendSong = recmdSongs.get(i);
                RecmdSongView recmdSongView = recmdSongViews.get(i);
                String artists = recommendSong.author;
                imageLoader.displayImage(recommendSong.bigPic, recmdSongView.getPicView(), imageOptions);
                recmdSongView.getTitleView().setText(recommendSong.title + "-" + artists);
                recmdSongView.getReasonView().setText(recommendSong.recommendReason);
            }
        }
    }

    private void setFocusPics(List<FocusPic> focusPics) {
        this.focusPics = focusPics;
        focusPicAdapter.setData(focusPics);
    }

    private void setHotSongList(List<SongList> hotSongLists) {
        this.hotSongLists = hotSongLists;
        if (hotSongLists != null) {
            for (int i = 0; i < hotSongLists.size() && i < hotSongListViews.size(); i++) {
                final SongList songList = hotSongLists.get(i);
                HotSongListView hotSongListView = hotSongListViews.get(i);
                hotSongListView.getTitleView().setText(songList.title);
                hotSongListView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getActivity() instanceof MainActivity){
                            ((MainActivity)getActivity()).showSongList(songList.listId);
                        }
                    }
                });
                imageLoader.displayImage(songList.pic, hotSongListView.getImageView(), imageOptions);
            }
        }
    }

    private void setHogTags(List<Tag> hotTags) {
        this.hotTags = hotTags;
        int vs = tvHotTags.size();
        if (hotTags != null) {
            int ts = hotTags.size();
            for (int i = 0; i < vs && i < ts; i++) {
                final TextView tvTag = tvHotTags.get(i);
                final String title = hotTags.get(i).title;
                tvTag.setText(title);
                tvTag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getActivity() instanceof MainActivity){
                            ((MainActivity)getActivity()).showTagSong(title);
                        }
                    }
                });
            }
            tvMoreTag.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_more_tag: {
                if(getActivity() instanceof MainActivity){
                    ((MainActivity)getActivity()).showAllTag();
                }
            }
            break;
        }
    }


    /**
     * 轮播pager适配
     */
    public class FocusPicAdapter extends PagerAdapter {
        private List<FocusPic> data;
        private List<ImageView> views;

        private void createViews() {
            views = new ArrayList<>();
            if (data != null) {
                for (int i = 0; i < data.size(); i++) {
                    final FocusPic focusPic = data.get(i);
                    ImageView view = new ImageView(getContext());
                    ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT,
                            ViewPager.LayoutParams.WRAP_CONTENT);
                    view.setLayoutParams(lp);
                    view.setAdjustViewBounds(true);
                    views.add(view);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (focusPic.type == FocusPic.TYPE_SONG_LIST) {
                                if(getActivity() instanceof MainActivity){
                                    ((MainActivity)getActivity()).showSongList(focusPic.code);
                                }
                            }
                        }
                    });
                    imageLoader.displayImage(focusPic.picUrl, view, imageOptions);
                }
            }
        }

        public List<FocusPic> getData() {
            return data;
        }

        public void setData(List<FocusPic> data) {
            if (this.data == data) {
                return;
            }

            this.data = data;
            createViews();
            notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = views.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }
    }

}
