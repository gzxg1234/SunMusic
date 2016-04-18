package com.sanron.music.common;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;

import com.sanron.music.R;

/**
 * Created by sanron on 16-4-18.
 */
public class ViewTool {
    private static Context appContext;
    private static int statusBarHeight = -1;

    public static void init(Application appContext) {
        ViewTool.appContext = appContext;
        initStatusBarHeight();
    }

    private static void initStatusBarHeight() {
        if (Build.VERSION.SDK_INT < 19) {
            statusBarHeight = 0;
            return;
        }

        int resId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            statusBarHeight = Resources.getSystem().getDimensionPixelSize(resId);
        } else {
            statusBarHeight = appContext.getResources().getDimensionPixelSize(R.dimen.status_height);
        }
    }

    public static int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                appContext.getResources().getDisplayMetrics());
    }


    public static void setViewFitsStatusBar(View view) {
        if (statusBarHeight == 0) {
            return;
        }
        view.setPadding(view.getPaddingLeft(), statusBarHeight,
                view.getPaddingRight(), view.getPaddingBottom());
        view.requestLayout();
        view.invalidate();
    }
}
