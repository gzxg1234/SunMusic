package com.sanron.music.fragments.pagerwebmusic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.R;
import com.sanron.music.common.ViewTool;
import com.sanron.music.fragments.base.LazyLoadFragment;
import com.sanron.music.net.JsonCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.SingerList;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/10.
 */
public class SingerFragment extends LazyLoadFragment {

    private RecyclerView mRecyclerView;
    private SingerList mHotSingerData;
    public static final int HOT_SINGER_NUM = 12;
    public static final String[] CLASSES = new String[]{
            "华语男歌手", "华语女歌手", "华语组合",
            "欧美男歌手", "欧美女歌手", "欧美组合",
            "日本男歌手", "日本女歌手", "日本组合",
            "韩国男歌手", "韩国女歌手", "韩国组合",
            "其他"
    };

    public static final int[] AREAS = new int[]{
            6, 6, 6,
            3, 3, 3,
            60, 60, 60,
            7, 7, 7,
            5
    };

    public static final int[] SEXS = new int[]{
            1, 2, 3,
            1, 2, 3,
            1, 2, 3,
            1, 2, 3,
            0
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_recycler_view, container, false);
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) getView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setAdapter(new SingerClassAdapter());
        final int padding = ViewTool.dpToPx(16);
        mRecyclerView.setPadding(padding, padding, padding, padding);
        if (mHotSingerData != null) {
            ((SingerClassAdapter) mRecyclerView.getAdapter())
                    .setHotSinger(mHotSingerData.singers);
        }
    }

    @Override
    protected void loadData() {
        MusicApi.hotSinger(0, HOT_SINGER_NUM, new JsonCallback<SingerList>() {
            @Override
            public void onSuccess(SingerList data) {
                mHotSingerData = data;
                if (data != null) {
                    ((SingerClassAdapter) mRecyclerView.getAdapter()).setHotSinger(data.singers);
                } else {
                    ((SingerClassAdapter) mRecyclerView.getAdapter()).setHotSinger(null);
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }


    private class HotSingerPagerAdapter extends PagerAdapter {

        private List<View> views;
        private List<com.sanron.music.net.bean.Singer> data;
        private boolean needUpdate;
        public final int COLUMN_NUM = 3;
        final int VERTICAL_SPACING;

        public HotSingerPagerAdapter() {
            VERTICAL_SPACING = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    8, getResources().getDisplayMetrics());
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

        public void setData(List<com.sanron.music.net.bean.Singer> data) {
            if (this.data == data) {
                return;
            }
            this.data = data;
            if (data != null) {
                views = new ArrayList<>();
                //计算页数
                final int pageCount = (int) Math.ceil(data.size() / (float) COLUMN_NUM);
                for (int i = 0; i < pageCount; i++) {
                    View view = LayoutInflater.from(getContext())
                            .inflate(R.layout.pager_hot_singer_item, null);
                    views.add(view);
                }
            } else {
                views.clear();
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return views == null ? 0 : views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ViewGroup view = (ViewGroup) views.get(position);
            if (view.getTag() == null) {
                int end = (position + 1) * COLUMN_NUM;
                end = Math.min(end, data.size());
                final List<com.sanron.music.net.bean.Singer> subData = data.subList(position * COLUMN_NUM, end);
                for (int i = 0; i < view.getChildCount() && i < subData.size(); i++) {
                    View child = view.getChildAt(i);
                    final com.sanron.music.net.bean.Singer singer = subData.get(i);
                    TextView tvName = (TextView) child.findViewById(R.id.tv_name);
                    ImageView ivPicture = (ImageView) child.findViewById(R.id.iv_artist_pic);
                    tvName.setText(singer.name);
                    ImageLoader.getInstance()
                            .displayImage(singer.avatarBig, ivPicture);
                    child.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getMainActivity().showSingerInfo(singer.artistId);
                        }
                    });
                }
                view.setTag(new Object());
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }
    }


    private class SingerClassAdapter extends RecyclerView.Adapter {

        public static final int TYPE_HEADER = 0;
        public static final int TYPE_CLASS = 1;
        private List<com.sanron.music.net.bean.Singer> mHotSingers;

        public void setHotSinger(List<com.sanron.music.net.bean.Singer> hotSingers) {
            this.mHotSingers = hotSingers;
            notifyItemChanged(0);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                View view = LayoutInflater.from(getContext())
                        .inflate(R.layout.layout_hot_singer, parent, false);
                return new HotSingerHolder(view);
            } else {
                View view = LayoutInflater.from(getContext())
                        .inflate(R.layout.list_singer_class_item, parent, false);
                return new SingerClassHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof HotSingerHolder) {
                HotSingerHolder hotSingerHolder = (HotSingerHolder) holder;
                if (mHotSingers != null) {
                    HotSingerPagerAdapter hotSingerPagerAdapter = new HotSingerPagerAdapter();
                    hotSingerPagerAdapter.setData(mHotSingers);
                    hotSingerHolder.mPagerHot.setAdapter(hotSingerPagerAdapter);
                    hotSingerHolder.mIndicator.setViewPager(hotSingerHolder.mPagerHot);
                }
            } else if (holder instanceof SingerClassHolder) {
                ((SingerClassHolder) holder).tvSingerClass.setText(CLASSES[position - 1]);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return CLASSES.length + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEADER;
            } else {
                return TYPE_CLASS;
            }
        }

        class HotSingerHolder extends RecyclerView.ViewHolder {
            ViewPager mPagerHot;
            CirclePageIndicator mIndicator;

            public HotSingerHolder(View itemView) {
                super(itemView);
                mPagerHot = (ViewPager) itemView.findViewById(R.id.pager_hot_singer);
                mIndicator = (CirclePageIndicator) itemView.findViewById(R.id.page_indicator);
            }
        }

        class SingerClassHolder extends RecyclerView.ViewHolder {
            private TextView tvSingerClass;

            public SingerClassHolder(View itemView) {
                super(itemView);
                tvSingerClass = (TextView) itemView.findViewById(R.id.tv_singer_class);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int i = getAdapterPosition() - 1;
                        int area = AREAS[i];
                        int sex = SEXS[i];
                        String title = CLASSES[i];
                        getMainActivity().showSingerList(title, area, sex);
                    }
                });
            }
        }
    }
}
