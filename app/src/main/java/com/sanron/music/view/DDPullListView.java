package com.sanron.music.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ListViewCompat;
import android.support.v4.widget.Space;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.sanron.music.R;

/**
 * Created by sanron on 16-3-30.
 */
public class DDPullListView extends ListView {

    /**
     * 头部展开最大高度
     */
    private int maxHeaderHeight = 0;

    /**
     * 头部正常高度
     */
    private int normalHeaderHeight;

    /**
     * 头部
     */
    private Space pullHeader;

    /**
     * 底部
     */
    private FrameLayout pullFooter;
    private ImageView icon;
    private TextView label;

    private String normalLabel = "加载更多";
    private String releaseLabel = "松开加载";
    private String loaddingLabel = "正在加载";
    private String nomoreLabel = "没有更多";
    private String emptyLabel = "没有数据";

    private int state = STATE_NORMAL;

    private boolean hasMore = true;

    private ValueAnimator upBackAnimator;
    private ValueAnimator downBackAnimator;
    private RotateAnimation rotateAnimation;

    private int activePointerId = -1;

    /**
     * 上次触摸y位置
     */
    private float lastY;
    private int touchSlop;
    private int animDuration = 300;
    private boolean readyPullDown;
    private boolean readyPullUp;

    private OnPullDownListener onPullDownListener;
    private OnScrollListener onScrollListener;
    private OnLoadListener onLoadListener;


    /**
     * 正常状态
     */
    public static final int STATE_NORMAL = 0;

    /**
     * 下拉
     */
    public static final int STATE_PULLING_DOWN = 1;

    /**
     * 上拉状态
     */
    public static final int STATE_PULL_TO_LOAD = 2;

    /**
     * 松开加载
     */
    public static final int STATE_RELEASE_TO_LOAD = 3;

    /**
     * 加载状态
     */
    public static final int STATE_LOADING = 4;

    private static final int INVALID_POINTER_ID = -1;

    public static final int DEFAULT_LABEL_SIZE = 14;//
    public static final int DEFAULT_FOOTER_HEIGHT = 56;
    public static final int DEFAULT_LABEL_COLOR = 0x99000000;

    private OnScrollListener internalScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (onScrollListener != null) {
                onScrollListener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            readyPullDown = false;
            readyPullUp = false;
            if (firstVisibleItem == 0
                    && visibleItemCount > 0) {
                readyPullDown = (pullHeader.getTop() - getPaddingTop() == 0);
            }
            if (firstVisibleItem + visibleItemCount == totalItemCount) {
                readyPullUp = (pullFooter.getBottom() + getPaddingBottom() == getMeasuredHeight());
            }
            if (onScrollListener != null) {
                onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        }
    };

    public DDPullListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        initHeader();
        initFooter();
        setOverScrollMode(OVER_SCROLL_NEVER);
        super.setOnScrollListener(internalScrollListener);
    }

    private void initHeader() {
        pullHeader = new Space(getContext());
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        pullHeader.setLayoutParams(lp);
        addHeaderView(pullHeader);
    }

    private void initFooter() {
        int defaultTextColor = DEFAULT_LABEL_COLOR;
        int defaultTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_LABEL_SIZE,
                getResources().getDisplayMetrics());
        int defaultFooterHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_FOOTER_HEIGHT,
                getResources().getDisplayMetrics());
        LinearLayout footerContent = new LinearLayout(getContext());
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                defaultFooterHeight);
        footerContent.setLayoutParams(lp);
        footerContent.setOrientation(LinearLayout.HORIZONTAL);
        footerContent.setGravity(Gravity.CENTER);

        icon = new ImageView(getContext());
        icon.setScaleType(ImageView.ScaleType.CENTER);
        icon.setImageResource(R.mipmap.ic_refresh_black_alpha_90_24dp);
        LinearLayout.LayoutParams lpIcon = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        icon.setLayoutParams(lpIcon);

        label = new TextView(getContext());
        label.setTextColor(defaultTextColor);
        label.setText(emptyLabel);
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize);
        LinearLayout.LayoutParams lpText = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpText.setMargins(20, 0, 0, 0);
        label.setLayoutParams(lpText);

        footerContent.addView(icon);
        footerContent.addView(label);

        pullFooter = new FrameLayout(getContext());
        pullFooter.addView(footerContent, lp);
        addFooterView(pullFooter);
        pullFooter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onLoadListener != null
                        && state == STATE_NORMAL
                        && hasMore) {
                    changePullUpState(STATE_LOADING);
                }
            }
        });

        rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(500);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setRepeatMode(Animation.RESTART);
    }

    public String getEmptyLabel() {
        return emptyLabel;
    }

    public void setEmptyLabel(String emptyLabel) {
        this.emptyLabel = emptyLabel;
    }

    public String getNomoreLabel() {
        return nomoreLabel;
    }

    public void setNomoreLabel(String nomoreLabel) {
        this.nomoreLabel = nomoreLabel;
    }


    public String getReleaseLabel() {
        return releaseLabel;
    }

    public void setReleaseLabel(String releaseLabel) {
        this.releaseLabel = releaseLabel;
    }

    public String getNormalLabel() {
        return normalLabel;
    }

    public void setNormalLabel(String normalLabel) {
        this.normalLabel = normalLabel;
    }

    public String getLoaddingLabel() {
        return loaddingLabel;
    }

    public void setLoaddingLabel(String loaddingLabel) {
        this.loaddingLabel = loaddingLabel;
    }

    public int getState() {
        return state;
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        this.onScrollListener = l;
    }


    public void setOnPullDownListener(OnPullDownListener onPullDownListener) {
        this.onPullDownListener = onPullDownListener;
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
        LayoutParams lp = (LayoutParams) pullHeader.getLayoutParams();
        if (lp.height != height) {
            lp.height = height;
            pullHeader.setLayoutParams(lp);
        }
    }

    public Space getPullHeader() {
        return pullHeader;
    }

    public View getPullFooter() {
        return pullFooter;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        if (this.hasMore == hasMore) {
            return;
        }

        this.hasMore = hasMore;
        if (!hasMore) {
            label.setText(nomoreLabel);
        } else {
            label.setText(normalLabel);
        }
    }

    private boolean isNoData() {
        return getAdapter() == null
                || getAdapter().isEmpty();
    }

    /**
     * 回缩动画
     */
    private void upBackAnim() {
        if (upBackAnimator == null) {
            upBackAnimator = ObjectAnimator.ofInt();
            upBackAnimator.setDuration(animDuration);
            upBackAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            upBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int newHeaderHeight = (int) animation.getAnimatedValue();
                    updateHeaderHeight(newHeaderHeight);
                    if (onPullDownListener != null) {
                        onPullDownListener.onPullDown(newHeaderHeight - pullHeader.getHeight());
                    }
                }
            });
        } else if (upBackAnimator.isRunning()) {
            upBackAnimator.cancel();
        }
        upBackAnimator.setIntValues(pullHeader.getHeight(), normalHeaderHeight);
        upBackAnimator.start();
    }

    private void downBackAnim() {
        if (downBackAnimator == null) {
            downBackAnimator = ObjectAnimator.ofInt();
            downBackAnimator.setDuration(animDuration);
            downBackAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            downBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    updateFooterPaddingBottom((Integer) animation.getAnimatedValue());
                }
            });
        } else if (downBackAnimator.isRunning()) {
            downBackAnimator.cancel();
        }
        downBackAnimator.setIntValues(pullFooter.getPaddingBottom(), 0);
        downBackAnimator.start();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if ((state == STATE_PULL_TO_LOAD
                || state == STATE_PULLING_DOWN)
                && ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
            return true;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                activePointerId = ev.getPointerId(ev.getActionIndex());
                lastY = ev.getY();
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                int activePointerIndex = ev.findPointerIndex(activePointerId);
                if (activePointerIndex == -1) {
                    break;
                }

                float y = ev.getY(activePointerIndex);
                float deltaY = y - lastY;
                if (state == STATE_NORMAL
                        && Math.abs(deltaY) > touchSlop) {
                    lastY = y;
                    if (deltaY > 0
                            && readyPullDown) {
                        state = STATE_PULLING_DOWN;
                    } else if (deltaY < 0
                            && readyPullUp
                            && hasMore
                            && pullFooter.getParent() == this) {
                        state = STATE_PULL_TO_LOAD;
                    }
                    return true;
                }
            }
            break;

            case MotionEvent.ACTION_UP: {
                state = STATE_NORMAL;
                activePointerId = INVALID_POINTER_ID;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                activePointerId = ev.getPointerId(ev.getActionIndex());
                lastY = ev.getY();

                if (downBackAnimator != null
                        && downBackAnimator.isRunning()
                        && state == STATE_NORMAL) {
                    downBackAnimator.cancel();
                    state = STATE_PULL_TO_LOAD;
                }
                if (upBackAnimator != null
                        && upBackAnimator.isRunning()) {
                    //按下停止动画
                    upBackAnimator.cancel();
                    state = STATE_PULLING_DOWN;
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
                if (state == STATE_NORMAL
                        && Math.abs(deltaY) > touchSlop) {
                    lastY = y;
                    if (deltaY > 0
                            && readyPullDown) {
                        state = STATE_PULLING_DOWN;
                        deltaY -= touchSlop;
                    } else if (deltaY < 0
                            && readyPullUp
                            && hasMore) {
                        state = STATE_PULL_TO_LOAD;
                        deltaY += touchSlop;
                    }
                }

                if (state == STATE_PULLING_DOWN) {
                    lastY = y;
                    //已在最顶端
                    //下滑，且header未展开到最大
                    final int headerHeight = pullHeader.getHeight();
                    if (deltaY > 0
                            && headerHeight < maxHeaderHeight) {
                        //阻力效果
                        deltaY *= (1 - (float) (headerHeight - normalHeaderHeight)
                                / (maxHeaderHeight - normalHeaderHeight));
                        int newHeaderHeight = (int) (headerHeight + Math.ceil(deltaY));
                        newHeaderHeight = Math.min(newHeaderHeight, maxHeaderHeight);
                        updateHeaderHeight(newHeaderHeight);
                        if (onPullDownListener != null) {
                            onPullDownListener.onPullDown(newHeaderHeight - headerHeight);
                        }
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                    } else if (deltaY < 0
                            && headerHeight > normalHeaderHeight) {
                        //上滑，且header已展开未恢复
                        int setHeight = (int) (headerHeight + Math.ceil(deltaY));
                        setHeight = Math.max(normalHeaderHeight, setHeight);
                        updateHeaderHeight(setHeight);
                        if (onPullDownListener != null) {
                            onPullDownListener.onPullDown(setHeight - headerHeight);
                        }
                        if (setHeight <= normalHeaderHeight) {
                            //listview从展开状态回到收缩状态后能够继续滑动，设置为点击事件
                            state = STATE_NORMAL;
                            ev.setAction(MotionEvent.ACTION_DOWN);
                            return super.onTouchEvent(ev);
                        }
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                    }
                }

                if (state == STATE_PULL_TO_LOAD
                        || state == STATE_RELEASE_TO_LOAD) {
                    lastY = y;
                    //释放加载需要达到的拉动距离
                    final int footerHeight = pullFooter.getChildAt(0).getHeight();
                    final int maxPaddingBottom = footerHeight * 2;
                    final int readyPaddingBottom = footerHeight;
                    final int paddingBottom = pullFooter.getPaddingBottom();
                    if (deltaY < 0
                            && paddingBottom < maxPaddingBottom) {
                        //上滑
                        deltaY *= (1 - paddingBottom
                                / (float) maxPaddingBottom);
                        int newPaddingBottom = (int) (paddingBottom - Math.ceil(deltaY));
                        newPaddingBottom = Math.min(newPaddingBottom, maxPaddingBottom);
                        int paddingBottomDiff = paddingBottom - newPaddingBottom;
                        if (newPaddingBottom >= readyPaddingBottom) {
                            changePullUpState(STATE_RELEASE_TO_LOAD);
                        }
                        updateFooterPaddingBottom(newPaddingBottom);
                        ListViewCompat.scrollListBy(this, -paddingBottomDiff);
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                    } else if (deltaY > 0
                            && paddingBottom > 0) {
                        int newPaddingBottom = (int) (paddingBottom - Math.ceil(deltaY));
                        newPaddingBottom = Math.max(newPaddingBottom, 0);
                        if (paddingBottom < readyPaddingBottom) {
                            changePullUpState(STATE_PULL_TO_LOAD);
                        }
                        updateFooterPaddingBottom(newPaddingBottom);
                        if (newPaddingBottom == 0) {
                            state = STATE_NORMAL;
                        }
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                    }
                }
            }
            break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (state == STATE_PULLING_DOWN) {
                    upBackAnim();
                    state = STATE_NORMAL;
                } else if (state == STATE_PULL_TO_LOAD) {
                    downBackAnim();
                    state = STATE_NORMAL;
                } else if (state == STATE_RELEASE_TO_LOAD) {
                    downBackAnim();
                    changePullUpState(STATE_LOADING);
                }
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

    private void updateFooterPaddingBottom(int paddingBottom) {
        final int maxPaddingBottom = pullFooter.getChildAt(0).getHeight() * 2;
        pullFooter.setPadding(0, 0, 0, paddingBottom);
        icon.setPivotX(icon.getWidth() / 2);
        icon.setPivotY(icon.getHeight() / 2);
        icon.setRotation(360f * paddingBottom / maxPaddingBottom);
    }

    public void onLoadCompleted() {
        rotateAnimation.cancel();
        changePullUpState(STATE_NORMAL);
    }

    public interface OnPullDownListener {
        void onPullDown(int pullOffset);
    }

    public interface OnLoadListener {
        void onLoad();
    }


    public void setOnLoadListener(OnLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
    }

    private void changePullUpState(int state) {
        this.state = state;
        switch (state) {
            case STATE_LOADING: {
                icon.setAnimation(rotateAnimation);
                rotateAnimation.start();
                label.setText(loaddingLabel);
                if (onLoadListener != null) {
                    onLoadListener.onLoad();
                }
            }
            break;

            case STATE_PULL_TO_LOAD:
            case STATE_NORMAL: {
                if (isNoData()) {
                    label.setText(emptyLabel);
                } else if (!hasMore) {
                    label.setText(nomoreLabel);
                } else {
                    label.setText(normalLabel);
                }
            }
            break;

            case STATE_RELEASE_TO_LOAD: {
                label.setText(releaseLabel);
            }
            break;

        }
    }


    @Override
    public void setAdapter(final ListAdapter adapter) {
        super.setAdapter(adapter);
        if (adapter != null) {
            final DataSetObserver emptyDataObserver = new DataSetObserver() {
                @Override
                public void onChanged() {
                    changePullUpState(STATE_NORMAL);
                }
            };
            adapter.registerDataSetObserver(emptyDataObserver);
        }
        changePullUpState(STATE_NORMAL);
    }
}