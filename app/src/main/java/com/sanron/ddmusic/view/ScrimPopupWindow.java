package com.sanron.ddmusic.view;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

/**
 * 弹出窗口,背景变暗
 * Created by sanron on 16-4-19.
 */
public class ScrimPopupWindow extends PopupWindow {

    private Activity mActivity;
    private float mOriginAlpha;

    public ScrimPopupWindow(Activity activity) {
        super(activity);
        this.mActivity = activity;
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        animShow();
        super.showAtLocation(parent, gravity, x, y);
    }

    @Override
    public void dismiss() {
        animDismiss();
        super.dismiss();
    }


    //activity背景恢复动画
    private void animDismiss() {
        final WindowManager.LayoutParams attr = mActivity.getWindow().getAttributes();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.7f, mOriginAlpha);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                attr.alpha = (float) animation.getAnimatedValue();
                mActivity.getWindow().setAttributes(attr);
            }
        });
        valueAnimator.start();
    }

    //activity背景变暗动画
    private void animShow() {
        final WindowManager.LayoutParams attr = mActivity.getWindow().getAttributes();
        mOriginAlpha = attr.alpha;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(attr.alpha, 0.7f);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                attr.alpha = (float) animation.getAnimatedValue();
                mActivity.getWindow().setAttributes(attr);
            }
        });
        valueAnimator.start();
    }

    public Activity getActivity() {
        return mActivity;
    }
}

