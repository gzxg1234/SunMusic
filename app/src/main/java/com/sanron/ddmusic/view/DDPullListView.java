package com.sanron.ddmusic.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChildHelper;
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
import com.sanron.ddmusic.R;

/**
 * Created by sanron on 16-3-30.
 */
public class DDPullListView extends ListView {

    /**
     * 头部展开最大高度
     */
    private int mMaxHeaderHeight = 0;

    /**
     * 头部正常高度
     */
    private int mNormalHeaderHeight;

    /**
     * 头部
     */
    private Space mPullHeader;

    /**
     * 底部
     */
    private FrameLayout mPullFooter;
    private ImageView mLoadIcon;
    private TextView mStateLabel;

    private String mNormalLabel = "加载更多";
    private String mReleaseLabel = "松开加载";
    private String mLoadingLabel = "正在加载";
    private String mNoMoreLabel = "没有更多";
    private String mEmptyLabel = "没有数据";

    private int mState = STATE_NORMAL;

    private boolean mHasMore = true;

    private ValueAnimator mUpBackAnimator;
    private ValueAnimator mDownBackAnimator;
    private RotateAnimation mRotateAnimation;

    private int mActivePointerId = -1;

    /**
     * 上次触摸y位置
     */
    private float mLastY;
    private int mTouchSlop;
    private int mAnimDuration = 300;
    private boolean mReadyPullDown;
    private boolean mReadyPullUp;

    private NestedScrollingChildHelper mNestedScrollingChildHelper;
    private OnPullDownListener mOnPullDownListener;
    private OnScrollListener mOnScrollListener;
    private OnLoadMoreListener mOnLoadMoreListener;


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

    private OnScrollListener mInternalScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            mReadyPullDown = false;
            mReadyPullUp = false;
            if (firstVisibleItem == 0
                    && visibleItemCount > 0) {
                mReadyPullDown = (mPullHeader.getTop() - getPaddingTop() == 0);
            }
            if (firstVisibleItem + visibleItemCount == totalItemCount) {
                mReadyPullUp = (mPullFooter.getBottom() + getPaddingBottom() == getMeasuredHeight());
            }
            if (mOnScrollListener != null) {
                mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        }
    };

    public DDPullListView(Context context) {
        this(context, null);
    }

    public DDPullListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DDPullListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        initHeader();
        initFooter();
        setOverScrollMode(OVER_SCROLL_NEVER);
        super.setOnScrollListener(mInternalScrollListener);
    }

    private void initHeader() {
        mPullHeader = new Space(getContext());
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mPullHeader.setLayoutParams(lp);
        addHeaderView(mPullHeader);
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

        mLoadIcon = new ImageView(getContext());
        mLoadIcon.setScaleType(ImageView.ScaleType.CENTER);
        mLoadIcon.setImageResource(R.mipmap.ic_refresh_black_alpha_90_24dp);
        LinearLayout.LayoutParams lpIcon = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mLoadIcon.setLayoutParams(lpIcon);

        mStateLabel = new TextView(getContext());
        mStateLabel.setTextColor(defaultTextColor);
        mStateLabel.setText(mEmptyLabel);
        mStateLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize);
        LinearLayout.LayoutParams lpText = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int txtMarginLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                getResources().getDisplayMetrics());
        lpText.setMargins(txtMarginLeft, 0, 0, 0);
        mStateLabel.setLayoutParams(lpText);

        footerContent.addView(mLoadIcon);
        footerContent.addView(mStateLabel);

        mPullFooter = new FrameLayout(getContext());
        mPullFooter.addView(footerContent, lp);
        addFooterView(mPullFooter);
        mPullFooter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnLoadMoreListener != null
                        && mState == STATE_NORMAL
                        && mHasMore) {
                    changePullUpState(STATE_LOADING);
                }
            }
        });

        mRotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setDuration(500);
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.setRepeatCount(Animation.INFINITE);
        mRotateAnimation.setRepeatMode(Animation.RESTART);
    }

    public String getEmptyLabel() {
        return mEmptyLabel;
    }

    public void setEmptyLabel(String emptyLabel) {
        this.mEmptyLabel = emptyLabel;
    }

    public String getNoMoreLabel() {
        return mNoMoreLabel;
    }

    public void setNoMoreLabel(String noMoreLabel) {
        this.mNoMoreLabel = noMoreLabel;
    }


    public String getReleaseLabel() {
        return mReleaseLabel;
    }

    public void setReleaseLabel(String releaseLabel) {
        this.mReleaseLabel = releaseLabel;
    }

    public String getmNormalLabel() {
        return mNormalLabel;
    }

    public void setmNormalLabel(String mNormalLabel) {
        this.mNormalLabel = mNormalLabel;
    }

    public String getLoadingLabel() {
        return mLoadingLabel;
    }

    public void setLoadingLabel(String loadingLabel) {
        this.mLoadingLabel = loadingLabel;
    }

    public int getmState() {
        return mState;
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        this.mOnScrollListener = l;
    }


    public void setOnPullDownListener(OnPullDownListener onPullDownListener) {
        this.mOnPullDownListener = onPullDownListener;
    }


    public int getmMaxHeaderHeight() {
        return mMaxHeaderHeight;
    }

    public void setmMaxHeaderHeight(int mMaxHeaderHeight) {
        this.mMaxHeaderHeight = mMaxHeaderHeight;
    }

    public int getNormalHeaderHeight() {
        return mNormalHeaderHeight;
    }

    public void setNormalHeaderHeight(int normalHeaderHeight) {
        this.mNormalHeaderHeight = normalHeaderHeight;
        updateHeaderHeight(normalHeaderHeight);
    }

    //更新header高度
    private void updateHeaderHeight(int height) {
        LayoutParams lp = (LayoutParams) mPullHeader.getLayoutParams();
        if (lp.height != height) {
            lp.height = height;
            mPullHeader.setLayoutParams(lp);
        }
    }

    public Space getPullHeader() {
        return mPullHeader;
    }

    public View getPullFooter() {
        return mPullFooter;
    }

    public boolean hasMore() {
        return mHasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.mHasMore = hasMore;
        changePullUpState(STATE_NORMAL);
    }

    private boolean isNoData() {
        return getAdapter() == null
                || getAdapter().isEmpty();
    }

    /**
     * 回缩动画
     */
    private void upBackAnim() {
        if (mUpBackAnimator == null) {
            mUpBackAnimator = ObjectAnimator.ofInt();
            mUpBackAnimator.setDuration(mAnimDuration);
            mUpBackAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mUpBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int newHeaderHeight = (int) animation.getAnimatedValue();
                    updateHeaderHeight(newHeaderHeight);
                    if (mOnPullDownListener != null) {
                        mOnPullDownListener.onPullDown(newHeaderHeight - mPullHeader.getHeight());
                    }
                }
            });
        } else if (mUpBackAnimator.isRunning()) {
            mUpBackAnimator.cancel();
        }
        mUpBackAnimator.setIntValues(mPullHeader.getHeight(), mNormalHeaderHeight);
        mUpBackAnimator.start();
    }

    private void downBackAnim() {
        if (mDownBackAnimator == null) {
            mDownBackAnimator = ObjectAnimator.ofInt();
            mDownBackAnimator.setDuration(mAnimDuration);
            mDownBackAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mDownBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    updateFooterPaddingBottom((Integer) animation.getAnimatedValue());
                }
            });
        } else if (mDownBackAnimator.isRunning()) {
            mDownBackAnimator.cancel();
        }
        mDownBackAnimator.setIntValues(mPullFooter.getPaddingBottom(), 0);
        mDownBackAnimator.start();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if ((mState == STATE_PULL_TO_LOAD
                || mState == STATE_PULLING_DOWN)
                && ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
            return true;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mActivePointerId = ev.getPointerId(ev.getActionIndex());
                mLastY = ev.getY();
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    break;
                }

                float y = ev.getY(activePointerIndex);
                float deltaY = y - mLastY;
                if (mState == STATE_NORMAL
                        && Math.abs(deltaY) > mTouchSlop) {
                    mLastY = y;
                    if (deltaY > 0
                            && mReadyPullDown) {
                        mState = STATE_PULLING_DOWN;
                    } else if (deltaY < 0
                            && mReadyPullUp
                            && mHasMore
                            && mPullFooter.getParent() == this) {
                        mState = STATE_PULL_TO_LOAD;
                    }
                    return true;
                }
            }
            break;

            case MotionEvent.ACTION_UP: {
                mState = STATE_NORMAL;
                mActivePointerId = INVALID_POINTER_ID;
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
                mActivePointerId = ev.getPointerId(ev.getActionIndex());
                mLastY = ev.getY();

                if (mDownBackAnimator != null
                        && mDownBackAnimator.isRunning()
                        && mState == STATE_NORMAL) {
                    mDownBackAnimator.cancel();
                    mState = STATE_PULL_TO_LOAD;
                }
                if (mUpBackAnimator != null
                        && mUpBackAnimator.isRunning()) {
                    //按下停止动画
                    mUpBackAnimator.cancel();
                    mState = STATE_PULLING_DOWN;
                }
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    break;
                }

                float y = ev.getY(activePointerIndex);
                float deltaY = y - mLastY;
                if (mState == STATE_NORMAL
                        && Math.abs(deltaY) > mTouchSlop) {
                    mLastY = y;
                    if (deltaY > 0
                            && mReadyPullDown) {
                        mState = STATE_PULLING_DOWN;
                        deltaY -= mTouchSlop;
                    } else if (deltaY < 0
                            && mReadyPullUp
                            && mHasMore) {
                        mState = STATE_PULL_TO_LOAD;
                        deltaY += mTouchSlop;
                    }
                }

                if (mState == STATE_PULLING_DOWN) {
                    mLastY = y;
                    //已在最顶端
                    //下滑，且header未展开到最大
                    final int headerHeight = mPullHeader.getHeight();
                    if (deltaY > 0
                            && headerHeight < mMaxHeaderHeight) {
                        //阻力效果
                        deltaY *= (1 - (float) (headerHeight - mNormalHeaderHeight)
                                / (mMaxHeaderHeight - mNormalHeaderHeight));
                        int newHeaderHeight = (int) (headerHeight + Math.ceil(deltaY));
                        newHeaderHeight = Math.min(newHeaderHeight, mMaxHeaderHeight);
                        updateHeaderHeight(newHeaderHeight);
                        if (mOnPullDownListener != null) {
                            mOnPullDownListener.onPullDown(newHeaderHeight - headerHeight);
                        }
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                    } else if (deltaY < 0
                            && headerHeight > mNormalHeaderHeight) {
                        //上滑，且header已展开未恢复
                        int setHeight = (int) (headerHeight + Math.ceil(deltaY));
                        setHeight = Math.max(mNormalHeaderHeight, setHeight);
                        updateHeaderHeight(setHeight);
                        if (mOnPullDownListener != null) {
                            mOnPullDownListener.onPullDown(setHeight - headerHeight);
                        }
                        if (setHeight <= mNormalHeaderHeight) {
                            //listview从展开状态回到收缩状态后能够继续滑动，设置为点击事件
                            mState = STATE_NORMAL;
                            ev.setAction(MotionEvent.ACTION_DOWN);
                            return super.onTouchEvent(ev);
                        }
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                    }
                }

                if (mState == STATE_PULL_TO_LOAD
                        || mState == STATE_RELEASE_TO_LOAD) {
                    mLastY = y;
                    //释放加载需要达到的拉动距离
                    final int footerHeight = mPullFooter.getChildAt(0).getHeight();
                    final int maxPaddingBottom = footerHeight * 2;
                    final int readyPaddingBottom = footerHeight;
                    final int paddingBottom = mPullFooter.getPaddingBottom();
                    if (deltaY < 0
                            && paddingBottom < maxPaddingBottom) {
                        //上滑
                        deltaY *= (1 - paddingBottom / (float) maxPaddingBottom);
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
                            mState = STATE_NORMAL;
                        }
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                    }
                }
            }
            break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (mState == STATE_PULLING_DOWN) {
                    upBackAnim();
                    mState = STATE_NORMAL;
                } else if (mState == STATE_PULL_TO_LOAD) {
                    downBackAnim();
                    mState = STATE_NORMAL;
                } else if (mState == STATE_RELEASE_TO_LOAD) {
                    downBackAnim();
                    changePullUpState(STATE_LOADING);
                }
                mActivePointerId = INVALID_POINTER_ID;
            }
            break;

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                int index = ev.getActionIndex();
                mLastY = ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
            }
            break;

            case MotionEventCompat.ACTION_POINTER_UP: {
                final int index = ev.getActionIndex();
                final int pointerId = ev.getPointerId(index);
                if (pointerId == mActivePointerId) {
                    int newIndex = index == 0 ? 1 : 0;
                    mActivePointerId = ev.getPointerId(newIndex);
                }
                mLastY = ev.getY(ev.findPointerIndex(mActivePointerId));
            }
            break;

        }
        return super.onTouchEvent(ev);
    }

    private void updateFooterPaddingBottom(int paddingBottom) {
        final int maxPaddingBottom = mPullFooter.getChildAt(0).getHeight() * 2;
        mPullFooter.setPadding(0, 0, 0, paddingBottom);
        mLoadIcon.setPivotX(mLoadIcon.getWidth() / 2);
        mLoadIcon.setPivotY(mLoadIcon.getHeight() / 2);
        mLoadIcon.setRotation(360f * paddingBottom / maxPaddingBottom);
    }

    public void onLoadCompleted() {
        mRotateAnimation.cancel();
        changePullUpState(STATE_NORMAL);
    }

    public void load() {
        if (mState != STATE_LOADING) {
            changePullUpState(STATE_LOADING);
        }
    }

    public interface OnPullDownListener {
        void onPullDown(int pullOffset);
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }


    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.mOnLoadMoreListener = onLoadMoreListener;
    }

    private void changePullUpState(int state) {
        this.mState = state;
        switch (state) {
            case STATE_LOADING: {
                mLoadIcon.setAnimation(mRotateAnimation);
                mRotateAnimation.start();
                mStateLabel.setText(mLoadingLabel);
                if (mOnLoadMoreListener != null) {
                    mOnLoadMoreListener.onLoadMore();
                }
            }
            break;

            case STATE_PULL_TO_LOAD:
            case STATE_NORMAL: {
                if (isNoData()) {
                    mStateLabel.setText(mEmptyLabel);
                } else if (!mHasMore) {
                    mStateLabel.setText(mNoMoreLabel);
                } else {
                    mStateLabel.setText(mNormalLabel);
                }
            }
            break;

            case STATE_RELEASE_TO_LOAD: {
                mStateLabel.setText(mReleaseLabel);
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