package com.sanron.music.fragments.WebMusic;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.sanron.music.R;
import com.sanron.music.activities.MainActivity;
import com.sanron.music.db.model.Music;
import com.sanron.music.net.ApiCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.FocusPic;
import com.sanron.music.net.bean.FocusPicData;
import com.sanron.music.net.bean.HotSongListData;
import com.sanron.music.net.bean.HotTagData;
import com.sanron.music.net.bean.RecmdSongData;
import com.sanron.music.net.bean.RecommendSong;
import com.sanron.music.net.bean.Song;
import com.sanron.music.net.bean.SongList;
import com.sanron.music.net.bean.Tag;
import com.sanron.music.view.RatioLayout;
import com.viewpagerindicator.CirclePageIndicator;

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
    private boolean isLoaded = false;
    private ImageLoader imageLoader = ImageLoader.getInstance();

    //轮播
    private List<FocusPic> focusPics;
    private ViewPager pagerFocusPic;
    private CirclePageIndicator pageIndicator;
    private FocusPicAdapter focusPicAdapter;

    //分类
    private List<Tag> hotTags;
    private GridView gvHotTag;
    private HotTagAdapter hotTagAdapter;
    private final int HOT_TAG_NUM = 3;

    //热门歌单
    private GridView gvHotSongList;
    private List<SongList> hotSongLists;
    private HotSongListAdapter hotSongListAdapter;
    private final int HOT_SONG_LIST_NUM = 6;

    //推荐歌曲
    private ListView lvRecmdSong;
    private List<RecommendSong> recmdSongs;
    private RecmdSongAdapter recmdSongAdapter;
    private final int RECMD_SONG_NUM = 5;

    private DisplayImageOptions imageOptions;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheOnDisk(true)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();
        focusPicAdapter = new FocusPicAdapter();
        hotTagAdapter = new HotTagAdapter();
        hotSongListAdapter = new HotSongListAdapter();
        recmdSongAdapter = new RecmdSongAdapter();
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
        gvHotTag = $(R.id.gv_hot_tag);
        gvHotSongList = $(R.id.gv_hot_song_list);
        lvRecmdSong = $(R.id.lv_recmd_song);

        pagerFocusPic.setAdapter(focusPicAdapter);
        pageIndicator.setViewPager(pagerFocusPic);
        gvHotTag.setAdapter(hotTagAdapter);
        gvHotSongList.setAdapter(hotSongListAdapter);
        lvRecmdSong.setAdapter(recmdSongAdapter);

        if (getUserVisibleHint()
                && !isLoaded) {
            getData();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser
                && !isLoaded
                && getView() != null) {
            getData();
            isLoaded = true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void getData() {
        System.out.println("getData");

        //获取轮播信息
        Call call1 = MusicApi.focusPic(10, new ApiCallback<FocusPicData>() {

            @Override
            public void onFailure(Exception e) {

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
                setFocusPics(result);
            }
        });

        //热门标签
        Call call2 = MusicApi.hotTag(HOT_TAG_NUM, new ApiCallback<HotTagData>() {

            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onSuccess(final HotTagData data) {
                setHogTags(data.tags);
            }
        });


        //获取热门歌单
        Call call3 = MusicApi.hotSongList(HOT_SONG_LIST_NUM, new ApiCallback<HotSongListData>() {

            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onSuccess(final HotSongListData data) {
                if (data != null
                        && data.content != null) {
                    setHotSongList(data.content.songLists);
                } else {
                    setHotSongList(null);
                }
            }
        });

        //获取推荐歌曲
        Call call4 = MusicApi.recmdSongs(RECMD_SONG_NUM, new ApiCallback<RecmdSongData>() {

            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onSuccess(final RecmdSongData data) {
                if (data != null
                        && data.content != null
                        && data.content.size() > 0) {
                    setRecmdSongs(data.content.get(0).songs);
                } else {
                    setRecmdSongs(null);
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
        ((RecmdSongAdapter) lvRecmdSong.getAdapter()).setData(recmdSongs);
    }

    private void setFocusPics(List<FocusPic> focusPics) {
        this.focusPics = focusPics;
        focusPicAdapter.setData(focusPics);
    }

    private void setHotSongList(List<SongList> hotSongLists) {
        this.hotSongLists = hotSongLists;
        ((HotSongListAdapter) gvHotSongList.getAdapter()).setData(hotSongLists);
    }

    private void setHogTags(List<Tag> hotTags) {
        this.hotTags = hotTags;
        ((HotTagAdapter) gvHotTag.getAdapter()).setData(hotTags);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        }
    }


    private class RecmdSongAdapter extends BaseAdapter {
        private List<RecommendSong> data;

        public void setData(List<RecommendSong> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return HOT_SONG_LIST_NUM;
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.list_recmd_song_item, parent, false);
            }
            ImageView ivPic = (ImageView) convertView.findViewById(R.id.iv_recmd_pic);
            TextView tvTitle = (TextView) convertView.findViewById(R.id.tv_recmd_name);
            TextView tvReason = (TextView) convertView.findViewById(R.id.tv_recmd_reason);
            if (data != null
                    && position < data.size()) {
                final RecommendSong recommendSong = data.get(position);
                tvTitle.setText(recommendSong.title);
                tvReason.setText(recommendSong.recommendReason);
                imageLoader.displayImage(recommendSong.bigPic, ivPic);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        player.clearQueue();
                        List<Music> musics = new ArrayList<>();
                        for (Song song : data) {
                            musics.add(song.toMusic());
                        }
                        player.enqueue(musics);
                        player.play(position);
                    }
                });
            }
            return convertView;
        }
    }

    private class HotSongListAdapter extends BaseAdapter {
        private List<SongList> data;

        public void setData(List<SongList> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return HOT_SONG_LIST_NUM;
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.list_hot_songlist_item, parent, false);
            }
            ImageView pic = (ImageView) convertView.findViewById(R.id.iv_songlist_pic);
            TextView title = (TextView) convertView.findViewById(R.id.tv_songlist_title);
            if (data != null
                    && position < data.size()) {
                final SongList songList = data.get(position);
                title.setText(songList.title);
                imageLoader.displayImage(songList.pic, pic);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).showSongList(songList.listId);
                        }
                    }
                });
            }
            return convertView;
        }
    }

    /**
     * 热门分类
     */
    private class HotTagAdapter extends BaseAdapter {
        private List<Tag> data;
        public final int[] ICONS = new int[]{
                R.mipmap.ic_classify_img01,
                R.mipmap.ic_classify_img02,
                R.mipmap.ic_classify_img03,
                R.mipmap.ic_classify_img04,
        };

        @Override
        public int getCount() {
            return HOT_TAG_NUM + 1;
        }

        public void setData(List<Tag> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            TextView tvTag = null;
            if (convertView == null) {
                RatioLayout ratioLayout = new RatioLayout(getContext(), null);
                ratioLayout.setType(RatioLayout.TYPE_HEIGHT);
                ratioLayout.setRatio(1f);
                ratioLayout.setBackgroundResource(ICONS[position]);

                tvTag = new TextView(getContext());
                tvTag.setTextColor(Color.WHITE);
                tvTag.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                tvTag.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                tvTag.setGravity(Gravity.CENTER);
                ratioLayout.addView(tvTag);
                convertView = ratioLayout;
            } else {
                tvTag = (TextView) ((ViewGroup) convertView).getChildAt(0);
            }
            if (position == getCount() - 1) {
                tvTag.setText("更多");
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).showAllTag();
                        }
                    }
                });
            }
            if (data != null
                    && position < data.size()) {
                String tag = data.get(position).title;
                tvTag.setText(tag);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).showTagSong(data.get(position).title);
                        }
                    }
                });
            }
            return convertView;
        }
    }


    /**
     * 轮播pager适配
     */
    private class FocusPicAdapter extends PagerAdapter {
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
                                if (getActivity() instanceof MainActivity) {
                                    ((MainActivity) getActivity()).showSongList(focusPic.code);
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
