package com.sanron.sunmusic.fragments;

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

/**
 * Created by Administrator on 2015/12/15.
 */
public class PagerFragment extends BaseFragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private LocalPagerAdapter adapter;
    private String[] titles;
    private String[] fragments;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if(arguments != null) {
            titles = arguments.getStringArray("titles");
            fragments = arguments.getStringArray("fragments");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.frag_layout_pager, null);
        viewPager = $(R.id.viewpager);
        adapter = new LocalPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout = $(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
        return contentView;
    }

    public class LocalPagerAdapter extends FragmentPagerAdapter {

        public LocalPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return Fragment.instantiate(getContext(),fragments[position]);
        }
    }

}
