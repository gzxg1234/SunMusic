package com.sanron.music.view;

import android.content.Context;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.widget.Space;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * Created by sanron on 16-3-30.
 */
public class DDPullListView extends ObservableListView {

    /**
     * 展开最大高度
     */
    private int maxHeaderHeight = 0;

    /**
     * 正常高度
     */
    private int normalHeaderHeight;

    /**
     * 当前头部高度
     */
    private int headerHeight;

    /**
     * 头部
     */
    private Space dummyHeader;

    /**
     * 回退动画
     */
    private ValueAnimator backAnimator;

    /**
     * 上次触摸y位置
     */
    private float oldY;

    /**
     * 按下y位置
     */
    private float downY;

    /**
     * 是否在拖动
     */
    private boolean isDrag;

    /**
     * 最小滑动距离
     */
    private int touchSlop;


    /**
     * 回退动画时间
     */
    private int animDuration = 300;

    /**
     * 是否到顶部了
     */
    private boolean isInTop;

    private OnScrollListener onScrollListener;
    private OnPullListener onPullListener;
    private ObservableScrollViewCallbacks callbacks;

    private ObservableScrollViewCallbacks internalCallbacks = new ObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
            if (callbacks != null && !isPulling()) {
                callbacks.onScrollChanged(scrollY, firstScroll, dragging);
            }
            isInTop = scrollY == 0;
        }

        @Override
        public void onDownMotionEvent() {
            if (callbacks != null) {
                callbacks.onDownMotionEvent();
            }
        }

        @Override
        public void onUpOrCancelMotionEvent(ScrollState scrollState) {
            if (callbacks != null) {
                callbacks.onUpOrCancelMotionEvent(scrollState);
            }
        }
    };

    public DDPullListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(context));
        dummyHeader = new Space(context);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dummyHeader.setLayoutParams(lp);
        addHeaderView(dummyHeader);
        setOverScrollMode(OVER_SCROLL_NEVER);
        super.setScrollViewCallbacks(internalCallbacks);
    }


    public boolean isPulling() {
        return headerHeight != normalHeaderHeight;
    }


    public void setOnPullListener(OnPullListener onPullListener) {
        this.onPullListener = onPullListener;
    }

    @Override
    public void setScrollViewCallbacks(ObservableScrollViewCallbacks listener) {
        this.callbacks = listener;
    }

    public int getMaxHeaderHeight() {
        return maxHeaderHeight;
    }

    public void setMaxHeaderHeight(int maxHeaderHeight) {
        this.maxHeaderHeight = maxHeaderHeight;
    }

    public int getNormalHeaderHeight() {
        return normalHeaderHeight;
    }

    public void setNormalHeaderHeight(int normalHeaderHeight) {
        this.normalHeaderHeight = normalHeaderHeight;
        updateHeaderHeight(normalHeaderHeight);
    }

    //更新header高度
    private void updateHeaderHeight(int height) {
        height = Math.max(normalHeaderHeight, Math.min(height, maxHeaderHeight));
        LayoutParams lp = (LayoutParams) dummyHeader.getLayoutParams();
        if (lp.height != height) {
            lp.height = height;
            dummyHeader.setLayoutParams(lp);
            final int offset = height - headerHeight;
            final int pullHeight = height - normalHeaderHeight;
            headerHeight = height;
            if (onPullListener != null) {
                onPullListener.onPull(offset, pullHeight);
            }
        }
    }

    public int getHeaderHeight() {
        return headerHeight;
    }


    @Override
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    /**
     * 回缩动画
     */
    private void animBack() {
        if (backAnimator == null) {
            backAnimator = ObjectAnimator.ofInt();
            backAnimator.setDuration(animDuration);
            backAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            backAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    updateHeaderHeight((Integer) animation.getAnimatedValue());
                }
            });
        } else if (backAnimator.isRunning()) {
            backAnimator.cancel();
        }
        backAnimator.setIntValues(headerHeight, normalHeaderHeight);
        backAnimator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                oldY = y;
                downY = y;
                if (backAnimator != null
                        && backAnimator.isRunning()) {
                    //按下停止回缩动画
                    backAnimator.cancel();
                }
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                float offsetY = y - oldY;
                oldY = y;
                if (isDrag) {
                    //已在最顶端
                    if (isInTop) {
                        //下滑，且header未展开到最大
                        if (offsetY > 0
                                && headerHeight < maxHeaderHeight) {
                            //阻力效果
                            offsetY *= (1 - (float) (headerHeight - normalHeaderHeight)
                                    / (maxHeaderHeight - normalHeaderHeight));
                            int setHeight = (int) (headerHeight + Math.ceil(offsetY));
                            updateHeaderHeight(setHeight);
                        } else if (offsetY < 0
                                && headerHeight > normalHeaderHeight) {
                            //上滑，且header已展开未恢复
                            int setHeight = (int) (headerHeight + Math.ceil(offsetY));
                            updateHeaderHeight(setHeight);
                            if (setHeight == normalHeaderHeight) {
                                //listview从展开状态回到收缩状态后能够继续滑动，设置为点击事件
                                ev.setAction(MotionEvent.ACTION_DOWN);
                                return super.onTouchEvent(ev);
                            }
                        }
                    }
                } else if (Math.abs(y - downY) > touchSlop) {
                    isDrag = true;
                }
                if (isPulling()) {
                    return true;
                }
            }
            break;

            case MotionEvent.ACTION_UP: {
                if (headerHeight > normalHeaderHeight) {
                    animBack();
                }
                isDrag = false;
            }
            break;
        }
        return super.onTouchEvent(ev);
    }

    public interface OnPullListener {
        void onPull(int pullOffset, int pullHeight);
    }

}
