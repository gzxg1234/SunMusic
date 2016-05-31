package com.sanron.ddmusic.fragments.webmusic;

import android.os.Bundle;
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
import com.sanron.ddmusic.R;
import com.sanron.ddmusic.adapter.CommonItemViewHolder;
import com.sanron.ddmusic.api.callback.JsonCallback;
import com.sanron.ddmusic.api.MusicApi;
import com.sanron.ddmusic.api.bean.Singer;
import com.sanron.ddmusic.api.bean.SingerList;
import com.sanron.ddmusic.common.ViewTool;
import com.sanron.ddmusic.fragments.base.LazyLoadFragment;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Administrator on 2016/3/10.
 */
public class SingerFragment extends LazyLoadFragment {

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
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
    public int getViewResId() {
        return R.layout.layout_recycler_view;
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

        private List<View> mViews = new ArrayList<>();
        private List<com.sanron.ddmusic.api.bean.Singer> mData = new ArrayList<>();
        private boolean mNeedUpdate;
        public final int COLUMN_NUM = 3;
        final int VERTICAL_SPACING;

        public HotSingerPagerAdapter() {
            VERTICAL_SPACING = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    8, getResources().getDisplayMetrics());
        }

        @Override
        public void notifyDataSetChanged() {
            mNeedUpdate = true;
            super.notifyDataSetChanged();
            mNeedUpdate = false;
        }

        @Override
        public int getItemPosition(Object object) {
            return mNeedUpdate ? POSITION_NONE : POSITION_UNCHANGED;
        }

        public void setData(List<com.sanron.ddmusic.api.bean.Singer> data) {
            mData.clear();
            mData.addAll(data);
            mViews.clear();
            //计算页数
            final int pageCount = (int) Math.ceil(data.size() / (float) COLUMN_NUM);
            for (int i = 0; i < pageCount; i++) {
                View view = LayoutInflater.from(getContext())
                        .inflate(R.layout.pager_hot_singer_item, null);
                mViews.add(view);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ViewGroup view = (ViewGroup) mViews.get(position);
            if (view.getTag() == null) {
                int end = (position + 1) * COLUMN_NUM;
                end = Math.min(end, mData.size());
                final List<com.sanron.ddmusic.api.bean.Singer> subData = mData.subList(position * COLUMN_NUM, end);
                for (int i = 0; i < view.getChildCount() && i < subData.size(); i++) {
                    View child = view.getChildAt(i);
                    final com.sanron.ddmusic.api.bean.Singer singer = subData.get(i);
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
            container.removeView(mViews.get(position));
        }
    }


    private class SingerClassAdapter extends RecyclerView.Adapter {

        public static final int TYPE_HEADER = 0;
        public static final int TYPE_CLASS = 1;
        private List<Singer> mData = new ArrayList<>();

        public void setHotSinger(List<Singer> hotSingers) {
            mData.clear();
            mData.addAll(hotSingers);
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
                        .inflate(R.layout.list_common_item, parent, false);
                CommonItemViewHolder holder = new CommonItemViewHolder(view);
                holder.tvText1.setTextColor(getResources().getColor(R.color.textColorSecondary));
                holder.tvText2.setVisibility(View.GONE);
                holder.ivPicture.setVisibility(View.GONE);
                holder.ivMenu.setImageResource(R.mipmap.ic_chevron_right_black_90_24dp);
                return holder;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof HotSingerHolder) {
                HotSingerHolder hotSingerHolder = (HotSingerHolder) holder;
                HotSingerPagerAdapter hotSingerPagerAdapter = new HotSingerPagerAdapter();
                hotSingerPagerAdapter.setData(mData);
                hotSingerHolder.pagerHot.setAdapter(hotSingerPagerAdapter);
                hotSingerHolder.indicator.setViewPager(hotSingerHolder.pagerHot);
            } else if (holder instanceof CommonItemViewHolder) {
                ((CommonItemViewHolder) holder).tvText1.setText(CLASSES[position - 1]);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int i = position - 1;
                        int area = AREAS[i];
                        int sex = SEXS[i];
                        String title = CLASSES[i];
                        getMainActivity().showSingerList(title, area, sex);
                    }
                });
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
            ViewPager pagerHot;
            CirclePageIndicator indicator;

            public HotSingerHolder(View itemView) {
                super(itemView);
                pagerHot = (ViewPager) itemView.findViewById(R.id.pager_hot_singer);
                indicator = (CirclePageIndicator) itemView.findViewById(R.id.page_indicator);
            }
        }
    }
}
