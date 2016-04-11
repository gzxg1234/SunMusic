package com.sanron.music.fragments.WebMusic;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.sanron.music.R;
import com.sanron.music.view.DDPullListView;
import com.sanron.music.view.SlideFinishLayout;

/**
 * Created by sanron on 16-3-30.
 */
public abstract class PullFrag extends BaseWebFrag implements SlideFinishLayout.SlideFinishCallback {

    protected DDPullListView pullListView;
    protected View viewInfo;
    protected View viewOperator;
    protected View topbar;
    protected View topBoard;
    protected TextView tvTitle;
    public static final String TAG = PullFrag.class.getSimpleName();

    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    private DDPullListView.OnPullDownListener pullListener = new DDPullListView.OnPullDownListener() {
        @Override
        public void onPullDown(int pullOffset) {
            int headerHeight = pullListView.getPullHeader().getHeight()+pullOffset;
            updateBoardHeight(topBoard.getHeight() + pullOffset);
            ViewHelper.setTranslationY(viewInfo,
                    -viewInfo.getHeight() + headerHeight - viewOperator.getHeight());
            ViewHelper.setTranslationY(viewOperator, headerHeight - viewOperator.getHeight());
        }
    };

    protected void updateBoardHeight(int height) {
        ViewGroup.LayoutParams lp = topBoard.getLayoutParams();
        lp.height = height;
        topBoard.setLayoutParams(lp);
    }

    private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (visibleItemCount > 0
                    &&
                    pullListView.getPullHeader().getHeight() == pullListView.getNormalHeaderHeight()) {
                int scrollY;
                int topbarHeight = topbar.getHeight();
                int normalHeaderHeight = pullListView.getNormalHeaderHeight();
                final int maxScrollY = normalHeaderHeight - topbarHeight - viewOperator.getHeight();
                if (firstVisibleItem == 0) {
                    View header = pullListView.getChildAt(0);
                    scrollY = header.getHeight() - header.getBottom();
                    scrollY = Math.min(maxScrollY, scrollY);
                } else {
                    scrollY = maxScrollY;
                }
                final float alpha = 1 - (float) scrollY / maxScrollY;
                viewInfo.setAlpha(alpha);
                if (alpha == 0f) {
                    tvTitle.setVisibility(View.VISIBLE);
                } else {
                    tvTitle.setVisibility(View.INVISIBLE);
                }

                ViewHelper.setTranslationY(topBoard, -scrollY);
                ViewHelper.setTranslationY(viewInfo, normalHeaderHeight
                        - viewInfo.getHeight() - scrollY - viewOperator.getHeight());
                ViewHelper.setTranslationY(viewOperator, normalHeaderHeight - scrollY - viewOperator.getHeight());
            }

        }
    };


    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewInfo = $(R.id.info);
        topBoard = $(R.id.top_board);
        topbar = $(R.id.top_bar);
        pullListView = $(R.id.pull_list_view);
        viewOperator = $(R.id.operator);
        tvTitle = $(R.id.tv_title);
        appContext.setViewFitsStatusBar(topbar);
        pullListView.setOnPullDownListener(pullListener);
        pullListView.setOnScrollListener(onScrollListener);
    }

}
