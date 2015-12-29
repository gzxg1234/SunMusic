package com.sanron.sunmusic.fragments.MySongFrag;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sanron.sunmusic.R;

import java.util.Observable;

/**
 * Created by Administrator on 2015/12/15.
 */
public class MySongFrag extends Fragment {

    protected View contentView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SongPagerAdapter pagerAdapter;

    public static final String TAG = "MySongFrag";

    public static MySongFrag newInstance() {
        return new MySongFrag();
    }

    public <T extends View> T $(int id) {
        return (T) contentView.findViewById(id);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.frag_mysong, null);
        viewPager = $(R.id.viewpager);
        pagerAdapter = new SongPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        tabLayout = $(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
        return contentView;
    }

    public static class SongPagerAdapter extends FragmentPagerAdapter {

        public static final String[] TITLES = new String[]{"播放列表", "最近播放", "本地音乐", "艺术家", "专辑"};

        public SongPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = PlayListFrag.newInstance();
                    break;
                case 1:
                    fragment = RecentPlayFrag.newInstance();
                    break;
                case 2:
                    fragment = LocalSongFrag.newInstance();
                    break;
                case 3:
                    fragment = ArtistFrag.newInstance();
                    break;
                case 4:
                    fragment = AlbumFrag.newInstance();
                    break;
            }
            return fragment;
        }
    }
}
