package com.sanron.ddmusic.fragments.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.nineoldandroids.view.ViewHelper;
import com.sanron.ddmusic.R;
import com.sanron.ddmusic.view.DDPullListView;
import com.sanron.ddmusic.view.SlideBackLayout;

import butterknife.BindView;

/**
 * Created by sanron on 16-3-30.
 */
public abstract class PullFragment extends SlideWebFragment implements SlideBackLayout.SlideBackCallback {

    @BindView(R.id.pull_list_view)
    protected DDPullListView mPullListView;
    @BindView(R.id.info)
    protected View mViewInfo;
    @BindView(R.id.operator)
    protected View mViewOperator;
    @BindView(R.id.top_board)
    protected View mTopBoard;

    private DDPullListView.OnPullDownListener pullListener = new DDPullListView.OnPullDownListener() {
        @Override
        public void onPullDown(int pullOffset) {
            int headerHeight = mPullListView.getPullHeader().getHeight() + pullOffset;
            updateBoardHeight(mTopBoard.getHeight() + pullOffset);
            ViewHelper.setTranslationY(mViewInfo,
                    -mViewInfo.getHeight() + headerHeight - mViewOperator.getHeight());
            ViewHelper.setTranslationY(mViewOperator, headerHeight - mViewOperator.getHeight());
        }
    };

    protected void updateBoardHeight(int height) {
        ViewGroup.LayoutParams lp = mTopBoard.getLayoutParams();
        lp.height = height;
        mTopBoard.setLayoutParams(lp);
    }

    private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (visibleItemCount > 0
                    &&
                    mPullListView.getPullHeader().getHeight() == mPullListView.getNormalHeaderHeight()) {
                int scrollY;
                int topBarHeight = mTopBar.getHeight();
                int normalHeaderHeight = mPullListView.getNormalHeaderHeight();
                final int maxScrollY = normalHeaderHeight - topBarHeight - mViewOperator.getHeight();
                if (firstVisibleItem == 0) {
                    View header = mPullListView.getChildAt(0);
                    scrollY = header.getHeight() - header.getBottom();
                    scrollY = Math.min(maxScrollY, scrollY);
                } else {
                    scrollY = maxScrollY;
                }
                final float alpha = 1 - (float) scrollY / maxScrollY;
                mViewInfo.setAlpha(alpha);
                if (alpha == 0f) {
                    mTvTitle.setVisibility(View.VISIBLE);
                } else {
                    mTvTitle.setVisibility(View.INVISIBLE);
                }

                ViewHelper.setTranslationY(mTopBoard, -scrollY);
                ViewHelper.setTranslationY(mViewInfo, normalHeaderHeight
                        - mViewInfo.getHeight() - scrollY - mViewOperator.getHeight());
                ViewHelper.setTranslationY(mViewOperator, normalHeaderHeight - scrollY - mViewOperator.getHeight());
            }

        }
    };

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPullListView.setOnPullDownListener(pullListener);
        mPullListView.setOnScrollListener(onScrollListener);
    }
}
