package com.sanron.music.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * Created by sanron on 16-3-30.
 */
public class DDPullListView extends ListView {

    /**
     * 图像扩展最大高度
     */
    private int expandHeight = 0;

    /**
     * 最小高度
     */
    private int collapseHeight;

    /**
     * 当前头部高度
     */
    private int headerHeight;

    /**
     * 最小高度占实际高度比
     */
    private float minHeightScale = 0.5f;

    /**
     * 最大高度占实际高度比
     */
    private float maxHeightScale = 1.0f;

    /**
     * 头部
     */
    private DDPullHeader header;

    /**
     * 回退动画
     */
    private ValueAnimator backAnimator;


    /**
     * 上次触摸y位置
     */
    private int oldY;

    /**
     * 是否在拖动
     */
    private boolean isDrag = false;

    /**
     * 最小滑动距离
     */
    private int touchSlop = 0;

    /**
     * 按下y位置
     */
    private int downY = 0;


    /**
     * 回退动画时间
     */
    private int animDuration = 300;

    /**
     * 是否到顶部了
     */
    private boolean isInTop;

    private OnScrollListener onScrollListener;

    private OnScrollListener internalListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (onScrollListener != null) {
                onScrollListener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (onScrollListener != null) {
                onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
            if (totalItemCount > 0
                    && firstVisibleItem == 0) {
                int bottom = header.getBottom();
                if (headerHeight != 0) {
                    header.getOperatorView().setAlpha((float) bottom / headerHeight);
                }
                if (getChildAt(0).getTop() == 0) {
                    //判断是否在顶部o
                    isInTop = true;
                    return;
                }
            }
            isInTop = false;
        }
    };

    @Override
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    public DDPullListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(context));
        header = new DDPullHeader(context);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        header.setLayoutParams(lp);
        addHeaderView(header);
        setOverScrollMode(OVER_SCROLL_NEVER);
        super.setOnScrollListener(internalListener);
    }


    public DDPullHeader getPullHeader() {
        return header;
    }

    private void updateHeightValues() {
        requestLayout();
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int measureHeight = header.getHeight();
                collapseHeight = (int) (measureHeight * minHeightScale);
                expandHeight = (int) (measureHeight * maxHeightScale);
                updateHeaderHeight(collapseHeight);
            }
        });
    }

    public void setMinHeightScale(float minHeightScale) {
        this.minHeightScale = minHeightScale;
    }

    public void setMaxHeightScale(float maxHeightScale) {
        this.maxHeightScale = maxHeightScale;
    }

    public int getHeaderHeight() {
        return headerHeight;
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
                int offsetY = y - oldY;
                if (isDrag) {
                    //已在最顶端
                    if (isInTop) {
                        //下滑，且header未展开到最大
                        if (offsetY > 0
                                && headerHeight < expandHeight) {
                            //阻力效果
                            offsetY *= (1 - (float) ((headerHeight - collapseHeight)) / (expandHeight - collapseHeight));
                            updateHeaderHeight(headerHeight + offsetY);
                            ev.setAction(MotionEvent.ACTION_CANCEL);
                        }
                        //上滑，且header已展开未收缩到最小
                        if (offsetY < 0
                                && headerHeight > collapseHeight) {
                            int setHeight = headerHeight + offsetY;
                            if (setHeight <= collapseHeight) {
                                updateHeaderHeight(headerHeight + offsetY);
                                //设置为点击事件，使listview从展开状态回到收缩状态后能够继续滑动
                                ev.setAction(MotionEvent.ACTION_DOWN);
                            } else {
                                ev.setAction(MotionEvent.ACTION_CANCEL);
                            }
                            updateHeaderHeight(setHeight);
                        }
                    }
                } else if (Math.abs(y - downY) > touchSlop) {
                    isDrag = true;
                }
                oldY = y;
            }
            break;

            case MotionEvent.ACTION_UP: {
                if (headerHeight > collapseHeight) {
                    animBack();
                }
                isDrag = false;
            }
            break;
        }

        return super.onTouchEvent(ev);
    }

    //更新header高度
    private void updateHeaderHeight(int height) {
        if (height > expandHeight) {
            height = expandHeight;
        } else if (height < collapseHeight) {
            height = collapseHeight;
        }
        LayoutParams lp = (LayoutParams) header.getLayoutParams();
        final int changeHeight = height - lp.height;
        if (changeHeight != 0) {
            lp.height = height;
            header.setLayoutParams(lp);
            headerHeight = height;
        }
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
        backAnimator.setIntValues(headerHeight, collapseHeight);
        backAnimator.start();
    }


    public class DDPullHeader extends RelativeLayout {

        private DDImageView imageView;
        private FrameLayout operatorView;

        public DDPullHeader(Context context) {
            super(context);
            imageView = new DDImageView(context);
            operatorView = new FrameLayout(context);
            LayoutParams lp1 = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            LayoutParams lp2 = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            addView(imageView, lp1);
            addView(operatorView, lp2);
        }

        public void setImageBitmap(Bitmap bmp) {
            imageView.setImageBitmap(bmp);
            updateHeightValues();
        }

        public void setImageResource(int resid) {
            imageView.setImageResource(resid);
            updateHeightValues();
        }

        public void setOperatorView(View view) {
            operatorView.removeAllViews();
            operatorView.addView(view);
            requestLayout();
        }

        public FrameLayout getOperatorView() {
            return operatorView;
        }

        public void setOperatorView(FrameLayout operatorView) {
            this.operatorView = operatorView;
        }

        public DDImageView getImageView() {
            return imageView;
        }

        public void setImageView(DDImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        }
    }
}
