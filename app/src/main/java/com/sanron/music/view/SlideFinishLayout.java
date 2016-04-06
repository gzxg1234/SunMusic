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
public class SlideFinishLayout extends FrameLayout {

    public static final int INVALID_POINTERID = -1;
    public static final float FINISH_X_VELOCITY = 6000;

    private int scrollDuration = 300;

    private float lastX = -1;

    private float lastY = -1;

    private VelocityTracker velocityTracker;

    private boolean isDraging;
    /**
     * 是否在滑动
     */
    private boolean isSlding;

    private int touchSlop;

    private int activePointerId = INVALID_POINTERID;

    private Scroller scroller;

    private int scrimColor = 0x99000000;

    private SlideFinishCallback slideFinishCallback;

    public SlideFinishCallback getSlideFinishCallback() {
        return slideFinishCallback;
    }

    public void setSlideFinishCallback(SlideFinishCallback slideFinishCallback) {
        this.slideFinishCallback = slideFinishCallback;
    }

    public SlideFinishLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        scroller = new Scroller(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        aquireTracker(event);
        System.out.println("dispatch");
        final int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                System.out.println("ACTION_DOWN");
                final int index = MotionEventCompat.getActionIndex(event);
                activePointerId = MotionEventCompat.getPointerId(event, index);
                lastX = MotionEventCompat.getX(event, index);
                lastY = MotionEventCompat.getY(event, index);
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                    isDraging = true;
                    isSlding = true;
                    return true;
                }
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                System.out.println("ACTION_MOVE");
                final int activeIndex = MotionEventCompat.findPointerIndex(event, activePointerId);
                float x = MotionEventCompat.getX(event, activeIndex);
                float y = MotionEventCompat.getY(event, activeIndex);
                float deltaX = x - lastX;
                float deltaY = y - lastY;
                if (!isDraging) {
                    int absDeltaX = (int) Math.abs(deltaX);
                    int absDeltaY = (int) Math.abs(deltaY);
                    if (absDeltaX > touchSlop
                            || absDeltaY > touchSlop) {
                        isDraging = true;
                        lastX = x;
                        lastY = y;
                        if (deltaX > 0
                                && absDeltaX > absDeltaY) {
                            //X轴移动距离比Y轴大，则为滑动状态
                            isSlding = true;
                            event.setAction(MotionEvent.ACTION_CANCEL);
                            return super.dispatchTouchEvent(event);
                        }
                    }
                } else {
                    lastX = x;
                    lastY = y;
                    if (isSlding) {
                        int toScrollX = (int) (getScrollX() - deltaX);
                        toScrollX = Math.min(0, Math.max(-getWidth(), toScrollX));
                        scrollTo(toScrollX, 0);
                        return true;
                    }
                }
            }
            break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (isSlding) {
                    final float xVelocity = computeXVelocity();
                    final int scrollX = getScrollX();
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
                isDraging = false;
                isSlding = false;
            }
            break;

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(event);
                activePointerId = MotionEventCompat.getPointerId(event, index);
                lastX = MotionEventCompat.getX(event, index);
                lastY = MotionEventCompat.getY(event, index);
            }
            break;

            case MotionEventCompat.ACTION_POINTER_UP: {
                int index = MotionEventCompat.getActionIndex(event);
                int pointerId = MotionEventCompat.getPointerId(event, index);
                if (pointerId == activePointerId) {
                    int newIndex = index == 0 ? 1 : 0;
                    activePointerId = MotionEventCompat.getPointerId(event, newIndex);
                }
                lastX = MotionEventCompat.getX(event, MotionEventCompat.findPointerIndex(event, activePointerId));
                lastY = MotionEventCompat.getY(event, MotionEventCompat.findPointerIndex(event, activePointerId));
            }
            break;
        }
        return super.dispatchTouchEvent(event);
    }

    private void aquireTracker(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
    }

    private float computeXVelocity() {
        velocityTracker.computeCurrentVelocity(1000);
        float xVelocity = velocityTracker.getXVelocity();
        velocityTracker.recycle();
        velocityTracker = null;
        return xVelocity;
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
                && slideFinishCallback != null) {
            //已经滑出
            slideFinishCallback.onSlideFinish();
        }
    }

    private void updateBackgroundColor() {
        final int baseAlpha = (scrimColor & 0xff000000) >>> 24;
        final float opacity = 1 - Math.abs(getScrollX() / (float) getWidth());
        final int imag = (int) (baseAlpha * opacity);
        final int color = imag << 24 | (scrimColor & 0xffffff);
        setBackgroundColor(color);
    }

    public interface SlideFinishCallback {
        void onSlideFinish();
    }
}
