package com.sanron.ddmusic.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by sanron on 16-4-18.
 */
public class WrapContentPager extends ViewPager {

    public WrapContentPager(Context context) {
        this(context, null);
    }

    public WrapContentPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childMaxHeight = 0;
        //计算子view最高高度
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int height = child.getMeasuredHeight();
            if (height > childMaxHeight) {
                childMaxHeight = height;
            }
        }
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(childMaxHeight,
                MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
