package com.sanron.sunmusic.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 正方形布局
 * Created by sanron on 16-3-18.
 */
public class SquareLayout extends RelativeLayout {

    public SquareLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        int measureHeight = getMeasuredWidth();
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(measureHeight, MeasureSpec.EXACTLY));
    }
}
