package com.sanron.ddmusic.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.sanron.ddmusic.R;

import java.lang.ref.WeakReference;

/**
 * Created by sanron on 16-7-20.
 */
public class IndexBar extends View {

    private int mHeight;
    private int mWidth;
    private float mTextSize;
    private int mTextColor;
    private int mBackgroundColor;
    private Path mPath;
    private Paint mPaint;
    private RectF mRectF;
    private boolean mIsShow = true;
    private Animator mAnimator;
    private TextView mIndicator;
    private RecyclerView mRecyclerView;

    //保存每个字母的第一个位置
    private SparseArray<Integer> mIndexPositions;
    private HideHandler mHandler = new HideHandler(this);

    private static int ANIMAT_DURATION = 300;

    private char[] mIndexs = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#".toCharArray();

    private void setIndexs(char[] indexs) {
        mIndexs = indexs;
        invalidate();
    }

    public interface OnIndexTouchListener {
        void onIndexTouch(char index);
    }

    public IndexBar(Context context) {
        this(context, null);
    }

    public IndexBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndexBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.IndexBar);
        mBackgroundColor = ta.getColor(R.styleable.IndexBar_backgroundColor, Color.LTGRAY);
        mTextColor = ta.getColor(R.styleable.IndexBar_indexColor, Color.WHITE);
        ta.recycle();

        mPath = new Path();
        mPaint = new Paint();
        mRectF = new RectF();

        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                context.getResources().getDisplayMetrics());
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mTextSize);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        boolean handled = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mHandler.removeMessages(0);
                if (mIsShow && isShown()) {
                    int index = findIndexForPoint(event.getY());
                    if (index != -1) {
                        onIndexTouch(mIndexs[index]);
                        handled = true;
                    }
                }
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                if (mIsShow && isShown()) {
                    int index = findIndexForPoint(event.getY());
                    if (index != -1) {
                        onIndexTouch(mIndexs[index]);
                    }
                    handled = true;
                }
            }
            break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (mIndicator != null) {
                    mIndicator.setVisibility(View.INVISIBLE);
                }
                if (mRecyclerView != null
                        && mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    mHandler.sendEmptyMessageDelayed(0, 1000);
                }
                handled = true;
            }
            break;
        }
        return handled;
    }

    private void onIndexTouch(char index) {
        if (mOnIndexTouchListener != null) {
            mOnIndexTouchListener.onIndexTouch(index);
        }
        if (mIndicator != null) {
            mIndicator.setVisibility(View.VISIBLE);
            mIndicator.setText(String.valueOf(index));
        }
        if (mRecyclerView != null) {
            Integer pos = mIndexPositions.get(index);
            if (pos != null) {
                LinearLayoutManager lm = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                lm.scrollToPositionWithOffset(pos, 0);
            }
        }
    }


    public void setIndicator(TextView indicator) {
        mIndicator = indicator;
        if (indicator != null) {
            indicator.setVisibility(View.INVISIBLE);
        }
    }

    public boolean isAttach() {
        return mRecyclerView != null;
    }

    public void attach(RecyclerView recyclerView) {
        valiteRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
        updateSectionPositions();
        setVisibility(View.INVISIBLE);
        recyclerView.getAdapter().registerAdapterDataObserver(mDataObserver);
        mRecyclerView.addOnScrollListener(mScrollListener);
    }

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                mHandler.sendEmptyMessageDelayed(0, 1000);
            } else {
                mHandler.removeMessages(0);
                show();
            }
        }
    };

    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            updateSectionPositions();
        }
    };

    public void detach() {
        if (mRecyclerView != null) {
            mRecyclerView.getAdapter().unregisterAdapterDataObserver(mDataObserver);
            mRecyclerView.removeOnScrollListener(mScrollListener);
            mRecyclerView = null;
            mIndexPositions.clear();
        }
        setVisibility(View.INVISIBLE);
    }

    private void valiteRecyclerView(RecyclerView recyclerView) {
        if (recyclerView.getAdapter() == null) {
            throw new IllegalStateException("must set adapter before attach");
        }
        if (!(recyclerView.getAdapter() instanceof Indexable)) {
            throw new IllegalStateException("adapter must implement " + Indexable.class.getSimpleName());
        }
        if (!(recyclerView.getLayoutManager() instanceof LinearLayoutManager)) {
            throw new IllegalStateException("only support " + LinearLayoutManager.class.getSimpleName());
        }
    }

    private void updateSectionPositions() {
        Indexable indexable = (Indexable) mRecyclerView.getAdapter();
        mIndexPositions = new SparseArray<>();
        for (int pos = 0; pos < indexable.getCount(); pos++) {
            int sec = indexable.getIndexForPosition(pos);
            if (mIndexPositions.indexOfKey(sec) < 0) {
                mIndexPositions.put(sec, pos);
            }
        }
    }

    private int findIndexForPoint(float y) {
        int i = (int) ((y - getPaddingTop()) / getTextHeight(mTextSize));
        if (i < 0 || i > mIndexs.length - 1) {
            return -1;
        }
        return i;
    }


    public void hide() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }

        mAnimator = ObjectAnimator.ofFloat(this, "alpha", getAlpha(), 0);
        mAnimator.setDuration(ANIMAT_DURATION)
                .addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        setVisibility(View.INVISIBLE);
                    }
                });
        mAnimator.start();
        mIsShow = false;
    }

    public void show() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }

        setVisibility(View.VISIBLE);
        mAnimator = ObjectAnimator.ofFloat(this, "alpha", getAlpha(), 1);
        mAnimator.setDuration(ANIMAT_DURATION)
                .start();
        mIsShow = true;
    }

    private OnIndexTouchListener mOnIndexTouchListener;

    public void setOnIndexTouchListener(OnIndexTouchListener onIndexTouchListener) {
        mOnIndexTouchListener = onIndexTouchListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = computeHeight(heightMeasureSpec);
        int width = computeWidth(widthMeasureSpec);
        setMeasuredDimension(width,
                height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBG(canvas);
        drawIndex(canvas);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    private int computeHeight(int heightMeasureSpec) {
        int height = 0;
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST: {
                //计算index所占的总高度
                int totalTextHeight = computeTotalTextHeight();
                totalTextHeight += getPaddingTop() + getPaddingBottom();
                height = Math.min(totalTextHeight, size);
            }
            break;

            case MeasureSpec.EXACTLY: {
                height = size;
                //给了确定的高度，则计算字体大小以容下全部字母
                setSutiableTextSize(height);
            }
            break;
        }
        return height;
    }

    private int computeWidth(int widthMeasureSpec) {
        int width = 0;
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST: {
                //得到最宽index的宽度
                int textMaxWidth = computeMaxTextWidth(mPaint);
                textMaxWidth += getPaddingLeft() + getPaddingRight();
                width = Math.min(textMaxWidth, size);
            }
            break;

            case MeasureSpec.EXACTLY: {
                width = size;
            }
            break;
        }
        return width;
    }

    private void setSutiableTextSize(int height) {
        float requestTextHeight = (height - getPaddingTop() - getPaddingBottom())
                / (float) mIndexs.length;

        //找合适的TextSize
        float start = 0;
        float end = requestTextHeight;
        float mid = requestTextHeight / 2;
        int textHeight = getTextHeight(mid);
        while (textHeight != requestTextHeight && end > start) {
            if (textHeight > requestTextHeight) {
                end = mid - 0.5f;
            } else if (textHeight < requestTextHeight) {
                start = mid + 0.5f;
            }
            mid = (start + end) / 2;
            textHeight = getTextHeight(mid);
        }
        mTextSize = mid;
    }

    private int getTextHeight(float textSize) {
        mPaint.setTextSize(textSize);
        Paint.FontMetricsInt metricsInt = mPaint.getFontMetricsInt();
        return metricsInt.descent - metricsInt.ascent;
    }

    private int computeTotalTextHeight() {
        return getTextHeight(mTextSize) * mIndexs.length;
    }


    private int computeMaxTextWidth(Paint paint) {
        int maxWidth = 0;
        for (int i = 0; i < mIndexs.length; i++) {
            int indexWidth = (int) paint.measureText(String.valueOf(mIndexs[i]));
            if (indexWidth > maxWidth) {
                maxWidth = indexWidth;
            }
        }
        return maxWidth;
    }

    private void drawIndex(Canvas canvas) {
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(mTextSize);
        mPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetricsInt metricsInt = mPaint.getFontMetricsInt();
        float indexHeight = (mHeight - getPaddingBottom() - getPaddingTop()) / (float) mIndexs.length;
        float y = indexHeight / 2 - (metricsInt.ascent + metricsInt.descent) / 2f + getPaddingTop();
        float x = (mWidth - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
        for (int i = 0; i < mIndexs.length; i++) {
            canvas.drawText(String.valueOf(mIndexs[i]), x, y, mPaint);
            y += indexHeight;
        }
    }


    private void drawBG(Canvas canvas) {
        mPaint.setColor(mBackgroundColor);

        int r = mWidth / 2;
        mPath.reset();
        mRectF.set(0, 0, mWidth, mWidth);
        mPath.addArc(mRectF, 0, -180);
        mPath.lineTo(0, mHeight - r);
        mRectF.set(0, mHeight - mWidth, mWidth, mHeight);
        mPath.arcTo(mRectF, -180, -180);
        canvas.drawPath(mPath, mPaint);
    }

    private static class HideHandler extends Handler {
        private WeakReference<IndexBar> mReference;

        public HideHandler(IndexBar indexBar) {
            mReference = new WeakReference<>(indexBar);
        }

        @Override
        public void handleMessage(Message msg) {
            IndexBar indexBar = mReference.get();
            if (indexBar != null) {
                indexBar.hide();
            }
        }
    }

}
