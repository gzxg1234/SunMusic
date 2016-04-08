package com.sanron.music.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by sanron on 16-3-21.
 */
public class ParentViewPager extends ViewPager{

    public ParentViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if(v!=this && v instanceof ViewPager){
            return true;
        }
        return super.canScroll(v, checkV, dx, x, y);
    }
}
