package com.sanron.ddmusic.common;

import android.app.Application;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.sanron.ddmusic.R;

/**
 * Created by sanron on 16-4-18.
 */
public class ViewTool {
    private static Application sApplication;
    private static int sStatusBarHeight = -1;

    public static void init(Application application) {
        sApplication = application;
        initStatusBarHeight();
    }

    private static void initStatusBarHeight() {
        if (Build.VERSION.SDK_INT < 19) {
            sStatusBarHeight = 0;
            return;
        }

        int resId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            sStatusBarHeight = Resources.getSystem().getDimensionPixelSize(resId);
        } else {
            sStatusBarHeight = sApplication.getResources().getDimensionPixelSize(R.dimen.status_height);
        }
    }

    public static int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                sApplication.getResources().getDisplayMetrics());
    }

    public static void show(String msg, int duration) {
        Toast.makeText(sApplication, msg, duration).show();
    }

    public static void show(String msg) {
        show(msg, Toast.LENGTH_SHORT);
    }

    public static void setViewFitsStatusBar(View view) {
        if (sStatusBarHeight == 0) {
            return;
        }
        view.setPadding(view.getPaddingLeft(), sStatusBarHeight,
                view.getPaddingRight(), view.getPaddingBottom());
        view.requestLayout();
        view.invalidate();
    }
}
