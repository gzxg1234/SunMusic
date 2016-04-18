package com.sanron.music.ui.web;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.R;
import com.sanron.music.ui.LazyLoadFragment;
import com.sanron.music.net.JsonCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.Singer;
import com.sanron.music.net.bean.SingerList;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/10.
 */
public class SingerFrag extends LazyLoadFragment {

    private ViewPager mPagerHot;
    private CirclePageIndicator mPageIndicator;
    private ListView mListView;
    private SingerList mHotSingerData;
    public static final int HOT_SINGER_NUM = 12;
    public static final String[] CLASSES = new String[]{
            "华语男歌手", "华语女歌手", "华语组合",
            "欧美男歌手", "欧美女歌手", "欧美组合",
            "日本男歌手", "日本女歌手", "日本组合",
            "韩国男歌手", "韩国女歌手", "韩国组合",
            "其他"
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mListView = new ListView(getContext());
        final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                getResources().getDisplayMetrics());
        final int dividerHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                getResources().getDisplayMetrics());
        mListView.setPadding(padding, padding, padding, padding);
        mListView.setDivider(null);
        mListView.setDividerHeight(dividerHeight);
        mListView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mListView.setVerticalScrollBarEnabled(false);
        View header = inflater.inflate(R.layout.layout_hot_singer, null);
        mPagerHot = (ViewPager) header.findViewById(R.id.pager_hot_singer);
        mPageIndicator = (CirclePageIndicator) header.findViewById(R.id.page_indicator);
        mListView.addHeaderView(header);
        return mListView;
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        mPagerHot.setAdapter(new HotSingerPagerAdapter());
        mListView.setAdapter(new SingerClassAdapter());
        mPageIndicator.setViewPager(mPagerHot);
        if (mHotSingerData != null) {
            ((HotSingerPagerAdapter) mPagerHot.getAdapter()).setData(mHotSingerData.singers);
        }
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    @Override
    protected void loadData() {
        MusicApi.hotSinger(0, HOT_SINGER_NUM, new JsonCallback<SingerList>() {
            @Override
            public void onSuccess(SingerList data) {
                mHotSingerData = data;
                if (data != null) {
                    ((HotSingerPagerAdapter) mPagerHot.getAdapter()).setData(data.singers);
                } else {
                    ((HotSingerPagerAdapter) mPagerHot.getAdapter()).setData(null);
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }


    private class HotSingerPagerAdapter extends PagerAdapter {

        private List<View> views;
        private List<Singer> data;
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

        public void setData(List<Singer> data) {
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
                final List<Singer> subData = data.subList(position * COLUMN_NUM, end);
                for (int i = 0; i < view.getChildCount() && i < subData.size(); i++) {
                    View child = view.getChildAt(i);
                    final Singer singer = subData.get(i);
                    TextView tvName = (TextView) child.findViewById(R.id.tv_name);
                    ImageView ivPicture = (ImageView) child.findViewById(R.id.iv_album_pic);
                    tvName.setText(singer.name);
                    ImageLoader.getInstance()
                            .displayImage(singer.avatarBig, ivPicture);
                    child.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getMainActivity().showSinger(singer.artistId);
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

    private class SingerClassAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return CLASSES.length;
        }

        @Override
        public Object getItem(int position) {
            return CLASSES[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.list_singer_class_item, parent, false);
            }
            TextView tvClass = (TextView) convertView.findViewById(R.id.tv_singer_class);
            tvClass.setText(CLASSES[position]);
            return convertView;
        }
    }
}
