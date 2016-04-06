package com.sanron.music.view;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.Space;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListView;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * Created by sanron on 16-3-30.
 */
public class DDPullListView extends ListView {

    private static final int INVALID_POINTER_ID = -1;
    /**
     * 展开最大高度
     */
    private int maxHeaderHeight = 0;

    /**
     * 正常高度
     */
    private int normalHeaderHeight;

    /**
     * 头部
     */
    private Space dummyHeader;

    /**
     * 回退动画
     */
    private ValueAnimator backAnimator;

    private int activePointerId = -1;

    /**
     * 上次触摸y位置
     */
    private float lastY;

    /**
     * 是否在拖动
     */
    private boolean isBeingDraged;

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

    private OnPullListener onPullListener;
    private OnScrollListener onScrollListener;

    private OnScrollListener internalScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (onScrollListener != null) {
                onScrollListener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            isInTop = false;
            if (firstVisibleItem == 0
                    && visibleItemCount > 0) {
                isInTop = dummyHeader.getTop() == 0;
            }
            if (onScrollListener != null) {
                onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        }
    };

    public DDPullListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        dummyHeader = new Space(context);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dummyHeader.setLayoutParams(lp);
        addHeaderView(dummyHeader);
        setOverScrollMode(OVER_SCROLL_NEVER);
        super.setOnScrollListener(internalScrollListener);
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        this.onScrollListener = l;
    }

    public boolean isPulling() {
        return dummyHeader.getHeight() > normalHeaderHeight;
    }


    public void setOnPullListener(OnPullListener onPullListener) {
        this.onPullListener = onPullListener;
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
            final int offset = height - dummyHeader.getHeight();
            final int pullHeight = height - normalHeaderHeight;
            if (onPullListener != null) {
                onPullListener.onPull(offset, pullHeight);
            }
        }
    }

    public int getHeaderHeight() {
        return dummyHeader.getHeight();
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
        backAnimator.setIntValues(dummyHeader.getHeight(), normalHeaderHeight);
        backAnimator.start();
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                activePointerId = ev.getPointerId(ev.getActionIndex());
                lastY = ev.getY();
                if (backAnimator != null
                        && (isBeingDraged = backAnimator.isRunning())) {
                    //按下停止回缩动画
                    backAnimator.cancel();
                }
            }
            break;

            case MotionEvent.ACTION_MOVE: {

                int activePointerIndex = ev.findPointerIndex(activePointerId);
                if (activePointerIndex == -1) {
                    break;
                }

                float y = ev.getY(activePointerIndex);
                float deltaY = y - lastY;
                if (!isBeingDraged
                        && isInTop
                        && deltaY > touchSlop) {
                    isBeingDraged = true;
                    lastY = y;
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    super.onTouchEvent(ev);
                    return true;
                }

                if (isBeingDraged) {
                    lastY = y;
                    //已在最顶端
                    //下滑，且header未展开到最大
                    final int headerHeight = dummyHeader.getHeight();
                    if (deltaY > 0
                            && headerHeight < maxHeaderHeight) {
                        //阻力效果
                        deltaY *= (1 - (float) (headerHeight - normalHeaderHeight)
                                / (maxHeaderHeight - normalHeaderHeight));
                        int setHeight = (int) (headerHeight + Math.ceil(deltaY));
                        updateHeaderHeight(setHeight);
                    } else if (deltaY < 0
                            && headerHeight > normalHeaderHeight) {
                        //上滑，且header已展开未恢复
                        int setHeight = (int) (headerHeight + Math.ceil(deltaY));
                        updateHeaderHeight(setHeight);
                        if (setHeight == normalHeaderHeight) {
                            //listview从展开状态回到收缩状态后能够继续滑动，设置为点击事件
                            ev.setAction(MotionEvent.ACTION_DOWN);
                            return super.onTouchEvent(ev);
                        }
                    }
                }

            }
            break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (isBeingDraged) {
                    if (dummyHeader.getHeight() > normalHeaderHeight) {
                        animBack();
                    }
                    return true;
                }
                isBeingDraged = false;
                activePointerId = INVALID_POINTER_ID;
            }
            break;

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                int index = ev.getActionIndex();
                lastY = ev.getY(index);
                activePointerId = ev.getPointerId(index);
            }
            break;

            case MotionEventCompat.ACTION_POINTER_UP: {
                final int index = ev.getActionIndex();
                final int pointerId = ev.getPointerId(index);
                if (pointerId == activePointerId) {
                    int newIndex = index == 0 ? 1 : 0;
                    activePointerId = ev.getPointerId(newIndex);
                }
                lastY = ev.getY(ev.findPointerIndex(activePointerId));
            }
            break;

        }
        return super.onTouchEvent(ev);
    }

    public interface OnPullListener {
        void onPull(int pullOffset, int pullHeight);
    }
}