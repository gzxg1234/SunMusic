package com.sanron.music.view;

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

    private float lastX = -1;

    private float lastY = -1;

    private VelocityTracker velocityTracker;

    private boolean isBeingDrag;

    private int touchSlop;

    private int activePointerId = INVALID_POINTER_ID;

    private Scroller scroller;

    private int scrimColor = 0x99000000;

    private SlideBackCallback slideBackCallback;

    public SlideBackCallback getSlideBackCallback() {
        return slideBackCallback;
    }

    public void setSlideBackCallback(SlideBackCallback slideBackCallback) {
        this.slideBackCallback = slideBackCallback;
    }

    public SlideBackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        scroller = new Scroller(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_MOVE && isBeingDrag) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                lastX = ev.getX();
                lastY = ev.getY();
                activePointerId = ev.getPointerId(ev.getActionIndex());
                isBeingDrag = !scroller.isFinished();
                aquireTracker();
                velocityTracker.addMovement(ev);
            }
            break;

            case MotionEvent.ACTION_MOVE: {

                final int activePointerIndex = ev.findPointerIndex(activePointerId);
                if (activePointerIndex == -1) {
                    break;
                }

                float x = ev.getX(activePointerIndex);
                float y = ev.getY(activePointerIndex);
                float deltaX = x - lastX;
                float deltaY = y - lastY;
                if (deltaX > touchSlop
                        && Math.abs(deltaX) > Math.abs(deltaY)) {
                    isBeingDrag = true;
                    lastX = x;
                    velocityTracker.addMovement(ev);
                }
            }
            break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                isBeingDrag = false;
                activePointerId = INVALID_POINTER_ID;
            }
            break;
        }
        return isBeingDrag;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        aquireTracker();
        velocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                activePointerId = event.getPointerId(event.getActionIndex());
                lastX = event.getX();
                if (isBeingDrag = !scroller.isFinished()) {
                    scroller.abortAnimation();
                }
            }
            break;

            case MotionEvent.ACTION_MOVE: {

                final int activePointerIndex = event.findPointerIndex(activePointerId);
                if (activePointerIndex == -1) {
                    break;
                }

                float x = event.getX(event.findPointerIndex(activePointerId));
                float deltaX = x - lastX;
                if (!isBeingDrag
                        && deltaX > touchSlop) {
                    isBeingDrag = true;
                    deltaX -= touchSlop;
                }

                if (isBeingDrag) {
                    lastX = x;
                    int toScrollX = (int) (getScrollX() - deltaX);
                    toScrollX = Math.min(0, Math.max(-getWidth(), toScrollX));
                    scrollTo(toScrollX, 0);
                }
            }
            break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (isBeingDrag) {
                    final int scrollX = getScrollX();
                    velocityTracker.computeCurrentVelocity(1000);
                    float xVelocity = velocityTracker.getXVelocity();
                    if (xVelocity > FINISH_X_VELOCITY) {
                        scroller.startScroll(scrollX, 0, -getWidth() - scrollX, 0, scrollDuration);
                    } else {
                        if (scrollX < -getWidth() / 2) {
                            scroller.startScroll(scrollX, 0, -getWidth() - scrollX, 0, scrollDuration);
                        } else {
                            scroller.startScroll(scrollX, 0, -scrollX, 0, scrollDuration);
                        }
                    }
                    invalidate();
                }
                recycleTracker();
                isBeingDrag = false;
            }
            break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(event);
                activePointerId = MotionEventCompat.getPointerId(event, index);
                lastX = MotionEventCompat.getX(event, index);
                lastY = MotionEventCompat.getY(event, index);
            }
            break;

            case MotionEvent.ACTION_POINTER_UP: {
                int index = event.getActionIndex();
                int pointerId = event.getPointerId(index);
                if (pointerId == activePointerId) {
                    int newIndex = index == 0 ? 1 : 0;
                    activePointerId = event.getPointerId(newIndex);
                }
                lastX = event.getX(event.findPointerIndex(activePointerId));
            }
            break;
        }
        return true;
    }

    private void aquireTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            int x = scroller.getCurrX();
            scrollTo(x, 0);
            postInvalidate();
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        updateBackgroundColor();
        if (l == -getWidth()
                && slideBackCallback != null) {
            //已经滑出
            slideBackCallback.onSlideBack();
        }
    }

    private void updateBackgroundColor() {
        final int baseAlpha = (scrimColor & 0xff000000) >>> 24;
        final float opacity = 1 - Math.abs(getScrollX() / (float) getWidth());
        final int img = (int) (baseAlpha * opacity);
        final int color = img << 24 | (scrimColor & 0xffffff);
        setBackgroundColor(color);
    }

    public interface SlideBackCallback {
        void onSlideBack();
    }
}
