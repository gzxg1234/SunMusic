package com.sanron.music.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by sanron on 16-3-30.
 */
public class DDImageView extends ImageView {


    public DDImageView(Context context) {
        this(context, null);
    }

    public DDImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAdjustViewBounds(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            int intrinsicHeight = drawable.getIntrinsicHeight();
            int intrinsicWidth = drawable.getIntrinsicWidth();
            float ratio = intrinsicHeight / (float) intrinsicWidth;
            drawable.setBounds(0, 0, getWidth(), (int) (getWidth() * ratio));
            drawable.draw(canvas);
        }
    }
}
