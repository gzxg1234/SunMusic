package com.sanron.ddmusic.view;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * Created by sanron on 16-4-5.
 */
public class SlideBackLayout extends FrameLayout {

    public static final int INVALID_POINTER_ID = -1;
    public static final float FINISH_X_VELOCITY = 6000;

    private int scrollDuration = 300;

    private float mLastX = -1;

    private float mLastY = -1;

    private VelocityTracker mVelocityTracker;

    private boolean mIsBeingDrag;

    private int mTouchSlop;

    private int mActivePointerId = INVALID_POINTER_ID;

    private Scroller mScroller;

    private int mScrimColor = 0x99000000;

    private SlideBackCallback mSlideBackCallback;

    public SlideBackCallback getSlideBackCallback() {
        return mSlideBackCallback;
    }

    public void setSlideBackCallback(SlideBackCallback slideBackCallback) {
        this.mSlideBackCallback = slideBackCallback;
    }

    public SlideBackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller = new Scroller(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_MOVE && mIsBeingDrag) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastX = ev.getX();
                mLastY = ev.getY();
                mActivePointerId = ev.getPointerId(ev.getActionIndex());
                mIsBeingDrag = !mScroller.isFinished();
                aquireTracker();
                mVelocityTracker.addMovement(ev);
            }
            break;

            case MotionEvent.ACTION_MOVE: {

                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    break;
                }

                float x = ev.getX(activePointerIndex);
                float y = ev.getY(activePointerIndex);
                float deltaX = x - mLastX;
                float deltaY = y - mLastY;
                if (deltaX > mTouchSlop
                        && Math.abs(deltaX) > Math.abs(deltaY)) {
                    mIsBeingDrag = true;
                    mLastX = x;
                    mVelocityTracker.addMovement(ev);
                }
            }
            break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                mIsBeingDrag = false;
                mActivePointerId = INVALID_POINTER_ID;
            }
            break;
        }
        return mIsBeingDrag;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        aquireTracker();
        mVelocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mActivePointerId = event.getPointerId(event.getActionIndex());
                mLastX = event.getX();
                if (mIsBeingDrag = !mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
            }
            break;

            case MotionEvent.ACTION_MOVE: {

                final int activePointerIndex = event.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    break;
                }

                float x = event.getX(event.findPointerIndex(mActivePointerId));
                float deltaX = x - mLastX;
                if (!mIsBeingDrag
                        && deltaX > mTouchSlop) {
                    mIsBeingDrag = true;
                    deltaX -= mTouchSlop;
                }

                if (mIsBeingDrag) {
                    mLastX = x;
                    int toScrollX = (int) (getScrollX() - deltaX);
                    toScrollX = Math.min(0, Math.max(-getWidth(), toScrollX));
                    scrollTo(toScrollX, 0);
                }
            }
            break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (mIsBeingDrag) {
                    final int scrollX = getScrollX();
                    mVelocityTracker.computeCurrentVelocity(1000);
                    float xVelocity = mVelocityTracker.getXVelocity();
                    if (xVelocity > FINISH_X_VELOCITY) {
                        mScroller.startScroll(scrollX, 0, -getWidth() - scrollX, 0, scrollDuration);
                    } else {
                        if (scrollX < -getWidth() / 2) {
                            mScroller.startScroll(scrollX, 0, -getWidth() - scrollX, 0, scrollDuration);
                        } else {
                            mScroller.startScroll(scrollX, 0, -scrollX, 0, scrollDuration);
                        }
                    }
                    invalidate();
                }
                recycleTracker();
                mIsBeingDrag = false;
            }
            break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(event);
                mActivePointerId = MotionEventCompat.getPointerId(event, index);
                mLastX = MotionEventCompat.getX(event, index);
                mLastY = MotionEventCompat.getY(event, index);
            }
            break;

            case MotionEvent.ACTION_POINTER_UP: {
                int index = event.getActionIndex();
                int pointerId = event.getPointerId(index);
                if (pointerId == mActivePointerId) {
                    int newIndex = index == 0 ? 1 : 0;
                    mActivePointerId = event.getPointerId(newIndex);
                }
                mLastX = event.getX(event.findPointerIndex(mActivePointerId));
            }
            break;
        }
        return true;
    }

    private void aquireTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            scrollTo(x, 0);
            postInvalidate();
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        updateBackgroundColor();
        if (l == -getWidth()
                && mSlideBackCallback != null) {
            //已经滑出
            mSlideBackCallback.onSlideBack();
        }
    }

    private void updateBackgroundColor() {
        final int baseAlpha = (mScrimColor & 0xff000000) >>> 24;
        final float opacity = 1 - Math.abs(getScrollX() / (float) getWidth());
        final int img = (int) (baseAlpha * opacity);
        final int color = img << 24 | (mScrimColor & 0xffffff);
        setBackgroundColor(color);
    }

    public interface SlideBackCallback {
        void onSlideBack();
    }
}
