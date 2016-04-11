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

/**
 * Created by Administrator on 2015/12/15.
 */
public class PagerFragment extends BaseFragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private LocalPagerAdapter adapter;
    private String[] titles;
    private String[] fragments;

    public static PagerFragment newInstance(String[] titles, String[] fragments) {
        Bundle bundle = new Bundle();
        bundle.putStringArray("titles", titles);
        bundle.putStringArray("fragments", fragments);
        PagerFragment pagerFragment = new PagerFragment();
        pagerFragment.setArguments(bundle);
        return pagerFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            titles = arguments.getStringArray("titles");
            fragments = arguments.getStringArray("fragments");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_layout_pager, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = $(R.id.viewpager);
        tabLayout = $(R.id.tab_layout);

        adapter = new LocalPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
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
            return Fragment.instantiate(getContext(), fragments[position]);
        }
    }
}
