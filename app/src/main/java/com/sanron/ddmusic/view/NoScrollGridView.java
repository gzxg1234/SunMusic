package com.sanron.ddmusic.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by sanron on 16-4-14.
 */
public class NoScrollGridView extends GridView {
    public NoScrollGridView(Context context) {
        this(context, null);
    }


    public NoScrollGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoScrollGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setVerticalScrollBarEnabled(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST));
    }
}
