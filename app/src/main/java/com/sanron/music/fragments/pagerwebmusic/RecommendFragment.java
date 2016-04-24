package com.sanron.music.fragments.pagerwebmusic;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
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

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.sanron.music.R;
import com.sanron.music.activities.MainActivity;
import com.sanron.music.api.JsonCallback;
import com.sanron.music.api.MusicApi;
import com.sanron.music.api.bean.FocusPic;
import com.sanron.music.api.bean.FocusPicData;
import com.sanron.music.api.bean.HotSongListData;
import com.sanron.music.api.bean.HotTagData;
import com.sanron.music.api.bean.RecmdSongData;
import com.sanron.music.api.bean.Song;
import com.sanron.music.api.bean.SongList;
import com.sanron.music.api.bean.Tag;
import com.sanron.music.db.bean.Music;
import com.sanron.music.fragments.base.LazyLoadFragment;
import com.sanron.music.service.PlayerUtil;
import com.sanron.music.view.RatioLayout;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/10.
 */
public class RecommendFragment extends LazyLoadFragment implements View.OnClickListener {

    /**
     * 是否已经获取过数据
     */

    //轮播
    private ViewPager mPagerFocusPic;
    private CirclePageIndicator mPageIndicator;
    private FocusPicAdapter mFocusPicAdapter;

    //分类
    private GridView mGvHotTag;
    private HotTagAdapter mHotTagAdapter;

    //热门歌单
    private GridView mGvHotSongList;
    private HotSongListAdapter mHotSongListAdapter;

    //推荐歌曲
    private ListView mLvRecmdSong;
    private RecmdSongAdapter mRecmdSongAdapter;

    private DisplayImageOptions mDisplayImageOptions;

    public final int HOT_TAG_NUM = 3;
    public final int HOT_SONG_LIST_NUM = 6;
    public final int RECOMMEND_SONG_NUM = 5;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDisplayImageOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheOnDisk(true)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();
        mFocusPicAdapter = new FocusPicAdapter();
        mHotTagAdapter = new HotTagAdapter();
        mHotSongListAdapter = new HotSongListAdapter();
        mRecmdSongAdapter = new RecmdSongAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.web_frag_recmd, container, false);
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        super.initView(view, savedInstanceState);
        //轮播
        mPagerFocusPic = $(R.id.pager_focus_pic);
        mPageIndicator = $(R.id.page_indicator);
        mGvHotTag = $(R.id.gv_hot_tag);
        mGvHotSongList = $(R.id.gv_hot_song_list);
        mLvRecmdSong = $(R.id.lv_recmd_song);

        mPagerFocusPic.setAdapter(mFocusPicAdapter);
        mPageIndicator.setViewPager(mPagerFocusPic);
        mGvHotTag.setAdapter(mHotTagAdapter);
        mGvHotSongList.setAdapter(mHotSongListAdapter);
        mLvRecmdSong.setAdapter(mRecmdSongAdapter);

    }

    @Override
    protected void loadData() {

        //获取轮播信息
        MusicApi.focusPic(10, new JsonCallback<FocusPicData>() {

            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onSuccess(FocusPicData data) {
                if (data == null
                        || data.pics == null) {
                    mFocusPicAdapter.setData(null);
                    return;
                }

                List<FocusPic> focusPics = data.pics;
                List<FocusPic> result = new LinkedList<>();
                for (int i = 0; i < focusPics.size() && result.size() < 6; i++) {
                    FocusPic focusPic = focusPics.get(i);
                    if (focusPic.type == FocusPic.TYPE_ALBUM
                            || focusPic.type == FocusPic.TYPE_SONG_LIST) {
                        result.add(focusPic);
                    }
                }
                mFocusPicAdapter.setData(result);
            }
        });

        //热门标签
        MusicApi.hotTag(HOT_TAG_NUM, new JsonCallback<HotTagData>() {

            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onSuccess(final HotTagData data) {
                if (data != null) {
                    mHotTagAdapter.setData(data.tags);
                } else {
                    mHotTagAdapter.setData(null);
                }
            }
        });


        //获取热门歌单
        MusicApi.hotSongList(HOT_SONG_LIST_NUM, new JsonCallback<HotSongListData>() {

            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onSuccess(final HotSongListData data) {
                if (data != null
                        && data.content != null) {
                    mHotSongListAdapter.setData(data.content.songLists);
                } else {
                    mHotSongListAdapter.setData(null);
                }
            }
        });

        //获取推荐歌曲
        MusicApi.recmdSongs(RECOMMEND_SONG_NUM, new JsonCallback<RecmdSongData>() {

            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onSuccess(final RecmdSongData data) {
                if (data != null
                        && data.content != null
                        && data.content.size() > 0) {
                    mRecmdSongAdapter.setData(data.content.get(0).songs);
                } else {
                    mRecmdSongAdapter.setData(null);
                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        }
    }


    private class RecmdSongAdapter extends BaseAdapter {
        private List<RecmdSongData.Content.RecommendSong> data;

        public void setData(List<RecmdSongData.Content.RecommendSong> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return RECOMMEND_SONG_NUM;
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
                        .inflate(R.layout.list_common_item, parent, false);
                convertView.setPadding(0, 0, 0, 0);
            }
            convertView.findViewById(R.id.iv_menu).setVisibility(View.GONE);
            RoundedImageView ivPic = (RoundedImageView) convertView.findViewById(R.id.iv_picture);
            ivPic.setOval(true);
            TextView tvTitle = (TextView) convertView.findViewById(R.id.tv_text1);
            TextView tvReason = (TextView) convertView.findViewById(R.id.tv_text2);
            if (data != null
                    && position < data.size()) {
                final RecmdSongData.Content.RecommendSong recommendSong = data.get(position);
                tvTitle.setText(recommendSong.title);

                //设置小图标和文字一样大小
                Rect bounds = new Rect();
                tvReason.getPaint().getTextBounds(recommendSong.recommendReason, 0,
                        recommendSong.recommendReason.length(), bounds);
                SpannableStringBuilder ssb = new SpannableStringBuilder(" ");
                Drawable drawable = getResources().getDrawable(R.mipmap.ic_love_heart);
                drawable.setBounds(0, 0, bounds.height(), bounds.height());

                ssb.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.append(recommendSong.recommendReason);
                tvReason.setText(ssb);
                ImageLoader.getInstance().displayImage(recommendSong.picBig, ivPic);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PlayerUtil.clearQueue();
                        List<Music> musics = new ArrayList<>();
                        for (Song song : data) {
                            musics.add(song.toMusic());
                        }
                        PlayerUtil.enqueue(musics);
                        PlayerUtil.play(position);
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
                ImageLoader.getInstance().displayImage(songList.pic, pic);
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
        private boolean needUpdate;

        private void createViews() {
            if (data == null) {
                return;
            }

            views = new ArrayList<>();
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
                            getMainActivity().showSongList(focusPic.code);
                        } else if (focusPic.type == FocusPic.TYPE_ALBUM) {
                            getMainActivity().showAlbumSongs(focusPic.code);
                        }
                    }
                });
                ImageLoader.getInstance()
                        .displayImage(focusPic.picUrl, view, mDisplayImageOptions);
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
        public void notifyDataSetChanged() {
            needUpdate = true;
            super.notifyDataSetChanged();
            needUpdate = false;
        }

        @Override
        public int getItemPosition(Object object) {
            return needUpdate ? POSITION_NONE : POSITION_UNCHANGED;
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
