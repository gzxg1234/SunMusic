package com.sanron.music.fragments;

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

import com.sanron.music.R;
import com.sanron.music.fragments.base.BaseFragment;

/**
 * Created by Administrator on 2015/12/15.
 */
public class PagerFragment extends BaseFragment {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private LocalPagerAdapter mAdapter;
    private String[] mTitles;
    private String[] mFragments;
    public static final String ARG_TITLES = "titles";
    public static final String ARG_FRAGMENTS = "fragments";

    public static PagerFragment newInstance(String[] titles, String[] fragments) {
        Bundle args = new Bundle();
        args.putStringArray(ARG_TITLES, titles);
        args.putStringArray(ARG_FRAGMENTS, fragments);
        PagerFragment pagerFragment = new PagerFragment();
        pagerFragment.setArguments(args);
        return pagerFragment;
    }

    public void setCurrentItem(int position) {
        mViewPager.setCurrentItem(position);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mTitles = args.getStringArray(ARG_TITLES);
            mFragments = args.getStringArray(ARG_FRAGMENTS);
        }
    }

    @Override
    public void setEnterTransition(Object transition) {
        super.setEnterTransition(transition);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_pager, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager = $(R.id.viewpager);
        mTabLayout = $(R.id.tab_layout);
        mAdapter = new LocalPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public class LocalPagerAdapter extends FragmentPagerAdapter {

        public LocalPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return Fragment.instantiate(getContext(), mFragments[position]);
        }

    }
}
