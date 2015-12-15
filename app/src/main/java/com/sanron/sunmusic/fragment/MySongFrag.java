package com.sanron.sunmusic.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sanron.sunmusic.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/15.
 */
public class MySongFrag extends BaseFragment{

    TabLayout tabLayout;
    ViewPager viewPager;
    SongPagerAdapter pagerAdapter;
    List<View> views;

    public static MySongFrag newInstance(){
        return new MySongFrag();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = LayoutInflater.from(getContext()).inflate(R.layout.frag_mysong,null);

        viewPager = $(R.id.viewpager);
        views = new ArrayList<>();
        for(int i=0; i<5; i++){
            TextView tv = new TextView(getContext());
            tv.setText("hello"+i);
            views.add(tv);
        }
        pagerAdapter = new SongPagerAdapter(views,getContext());
        viewPager.setAdapter(pagerAdapter);

        tabLayout = $(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
        return contentView;
    }


    public static class SongPagerAdapter extends PagerAdapter{

        private List<View> views;
        private Context context;
        public static final String[] TITLES = new String[]{"播放列表","最近播放","本地音乐","歌手","专辑"};

        public SongPagerAdapter(List<View> views,Context context){
            this.views = views;
            this.context = context;
        }

        @Override
        public int getCount() {
            return views==null ? 0 : views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(views.get(position));
            return views.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }
    }
}
