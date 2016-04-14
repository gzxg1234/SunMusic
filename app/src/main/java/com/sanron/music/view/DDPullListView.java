package com.sanron.music.view;

import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.Space;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
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
    private String loaddingLable = "正在加载";
    private String nomoreLable = "没有更多";

    private int state = STATE_NORMAL;


    private boolean hasMore = true;

    /**
     * 回退动画
     */
    private ValueAnimator upBackAnimator;
    private ValueAnimator downBackAnimator;

    private int activePointerId = -1;

    /**
     * 上次触摸y位置
     */
    private float lastY;


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
    private boolean readyPullDown;
    private boolean readyPullUp;

    private OnPullDownListener onPullDownListener;
    private OnScrollListener onScrollListener;

    public static final int STATE_NO_DATA = -1;
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
    public static final int STATE_PULLING_UP = 2;

    /**
     * 松开加载
     */
    public static final int STATE_RELEASE_TO_LOAD = 3;

    /**
     * 加载状态
     */
    public static final int STATE_LOADING = 4;


    private static final int INVALID_POINTER_ID = -1;

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
                if (DDPullListView.this == pullFooter.getParent()) {
                    readyPullUp = (pullFooter.getBottom() + getPaddingBottom() == getMeasuredHeight());
                }
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

    public String getNomoreLable() {
        return nomoreLable;
    }

    public void setNomoreLable(String nomoreLable) {
        this.nomoreLable = nomoreLable;
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

    public String getLoaddingLable() {
        return loaddingLable;
    }

    public void setLoaddingLable(String loaddingLable) {
        this.loaddingLable = loaddingLable;
    }

    private void initHeader() {
        pullHeader = new Space(getContext());
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        pullHeader.setLayoutParams(lp);
        addHeaderView(pullHeader);
    }

    private void initFooter() {
        Resources resources = getContext().getResources();
        int defaultTextColor = resources.getColor(R.color.pull_list_footer_default_label_color);
        int defaultTextSize = (int) resources.getDimension(R.dimen.pull_list_footer_default_label_size);
        int defaultFooterHeight = (int) resources.getDimension(R.dimen.pull_list_footer_default_height);
        LinearLayout footerContent = new LinearLayout(getContext());
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                defaultFooterHeight);
        footerContent.setLayoutParams(lp);
        footerContent.setOrientation(LinearLayout.HORIZONTAL);
        footerContent.setGravity(Gravity.CENTER);

        icon = new ImageView(getContext());
        icon.setImageResource(R.mipmap.ic_refresh_black_alpha_90_24dp);
        LinearLayout.LayoutParams lpIcon = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        icon.setLayoutParams(lpIcon);

        label = new TextView(getContext());
        label.setTextColor(defaultTextColor);
        label.setText(normalLabel);
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize);
        LinearLayout.LayoutParams lpText = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpText.setMargins(20, 0, 0, 0);
        label.setLayoutParams(lpText);

        footerContent.addView(icon);
        footerContent.addView(label);

        pullFooter = new FrameLayout(getContext());
        pullFooter.addView(footerContent, lp);
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
        this.hasMore = hasMore;
        if (!hasMore) {
            label.setText(nomoreLable);
        } else {
            label.setText(normalLabel);
        }
    }

    public void setFooterContent(int resid) {
        pullFooter.removeAllViews();
        inflate(getContext(), resid, pullFooter);
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
                    pullFooter.setPadding(0, 0, 0, (Integer) animation.getAnimatedValue());
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
        if ((state == STATE_PULLING_UP
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
                        state = STATE_PULLING_UP;
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
                    state = STATE_PULLING_UP;
                    return super.onTouchEvent(ev);
                }
                if (upBackAnimator != null
                        && upBackAnimator.isRunning()) {
                    //按下停止动画
                    upBackAnimator.cancel();
                    state = STATE_PULLING_DOWN;
                    return super.onTouchEvent(ev);
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
                        state = STATE_PULLING_UP;
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

                if (state == STATE_PULLING_UP
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
                        int setPaddingBottom = (int) (paddingBottom - Math.ceil(deltaY));
                        setPaddingBottom = Math.min(setPaddingBottom, maxPaddingBottom);
                        if (setPaddingBottom >= readyPaddingBottom) {
                            changePullUpState(STATE_RELEASE_TO_LOAD);
                        }
                        pullFooter.setPadding(0, 0, 0, setPaddingBottom);
                        return super.onTouchEvent(ev);
                    } else if (deltaY > 0
                            && paddingBottom > 0) {
                        int setPaddingBottom = (int) (paddingBottom - Math.ceil(deltaY));
                        setPaddingBottom = Math.max(setPaddingBottom, 0);
                        if (paddingBottom < readyPaddingBottom) {
                            changePullUpState(STATE_PULLING_UP);
                        }
                        pullFooter.setPadding(0, 0, 0, setPaddingBottom);
                        if (setPaddingBottom == 0) {
                            state = STATE_NORMAL;
                        }
                    }
                }
            }
            break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (state == STATE_PULLING_DOWN) {
                    upBackAnim();
                    state = STATE_NORMAL;
                } else if (state == STATE_PULLING_UP) {
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

    public void onLoadCompleted() {
        changePullUpState(STATE_NORMAL);
    }

    public interface OnPullDownListener {
        void onPullDown(int pullOffset);
    }

    public interface OnPullUpListener {
        void onStateChange(int state);
    }

    private OnPullUpListener onPullUpListener;

    public void setOnPullUpListener(OnPullUpListener onPullUpListener) {
        this.onPullUpListener = onPullUpListener;
    }

    private void changePullUpState(int state) {
        if (this.state != state) {
            this.state = state;
            switch (state) {
                case STATE_LOADING: {
                    label.setText(loaddingLable);
                }
                break;

                case STATE_PULLING_UP:
                case STATE_NORMAL: {
                    label.setText(normalLabel);
                }
                break;

                case STATE_RELEASE_TO_LOAD: {
                    label.setText(releaseLabel);
                }
                break;
            }
            if (onPullUpListener != null) {
                onPullUpListener.onStateChange(state);
            }
        }
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(new WrapAdapter(adapter));
    }

    public class WrapAdapter extends BaseAdapter {

        private ListAdapter adapter;
        public static final int TYPE_EMPTY = 1;

        public WrapAdapter(ListAdapter listAdapter) {
            this.adapter = listAdapter;
            if (adapter != null) {
                adapter.registerDataSetObserver(emptyObserver);
            }
        }

        private DataSetObserver emptyObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                if (isNoData()) {
                    removeFooterView(pullFooter);
                } else {
                    addFooterView(pullFooter);
                }
            }
        };

        @Override
        public int getCount() {
            return isNoData() ?
                    1 : adapter.getCount();
        }

        @Override
        public Object getItem(int position) {
            return adapter.getItem(position);
        }

        @Override
        public int getItemViewType(int position) {
            if (isNoData()) {
                return TYPE_EMPTY;
            }
            return adapter.getItemViewType(position);
        }


        @Override
        public long getItemId(int position) {
            return position;
        }

        private boolean isNoData() {
            return adapter == null
                    || adapter.getCount() == 0;
        }

        @Override
        public int getViewTypeCount() {
            return adapter.getViewTypeCount() + 1;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (adapter != null) {
                adapter.unregisterDataSetObserver(observer);
            }
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            if (adapter != null) {
                adapter.registerDataSetObserver(observer);
            }
        }

        @Override
        public boolean isEmpty() {
            return isNoData();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (isNoData()) {
                TextView textView = new TextView(getContext());
                textView.setGravity(Gravity.CENTER);
                textView.setText("没有数据");
                textView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        getResources().getDimensionPixelSize(R.dimen.pull_list_footer_default_height)));
                convertView = textView;
            } else {
                return adapter.getView(position, convertView, parent);
            }
            return convertView;
        }
    }
}