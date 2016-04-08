package com.sanron.music.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.sanron.music.R;

/**
 * 长宽比例布局，设定长宽比
 * Created by sanron on 16-3-18.
 */
public class RatioLayout extends FrameLayout {

    /**
     * 长还是宽
     */
    private int type;
    /**
     * 比例
     */
    private float ratio;

    public static final int TYPE_WIDTH = 1;
    public static final int TYPE_HEIGHT = 2;
    public RatioLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RatioLayout);
        type = ta.getInt(R.styleable.RatioLayout_type,0);
        ratio = ta.getFloat(R.styleable.RatioLayout_ratio,1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        if(type == TYPE_WIDTH){
            int width = (int) (getMeasuredHeight()*ratio);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY);
        }else if(type == TYPE_HEIGHT){
            int height = (int) (getMeasuredWidth()*ratio);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    }
}
