package com.sanron.music.fragments.WebMusic;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.nineoldandroids.view.ViewHelper;
import com.sanron.music.R;
import com.sanron.music.fragments.BaseFragment;
import com.sanron.music.view.DDImageView;
import com.sanron.music.view.DDPullListView;

/**
 * Created by sanron on 16-3-30.
 */
public abstract class PullFrag extends BaseFragment {

    protected DDPullListView pullListView;
    protected View topbar;
    protected DDImageView topImage;
    protected FrameLayout viewInfo;
    protected LinearLayout floatGroup;
    private float imageHeightRatio = 0.6f;
    public static final String TAG = PullFrag.class.getSimpleName();


    protected abstract View createViewInfo();

    private DDPullListView.OnPullListener pullListener = new DDPullListView.OnPullListener() {
        @Override
        public void onPull(int pullOffset, int pullHeight) {
            updateImageHeight(topImage.getHeight() + pullOffset);
            int headerHeight = pullListView.getNormalHeaderHeight();

            ViewHelper.setTranslationY(viewInfo, headerHeight
                    - viewInfo.getHeight() + pullHeight);
            ViewHelper.setTranslationY(floatGroup, headerHeight + pullHeight);
        }
    };

    private ObservableScrollViewCallbacks scrollViewCallbacks = new ObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
            int topbarHeight = topbar.getHeight();
            int headerHeight = pullListView.getNormalHeaderHeight();
            scrollY = Math.min(headerHeight - topbarHeight, scrollY);
            ViewHelper.setTranslationY(topImage, -scrollY);
            ViewHelper.setTranslationY(viewInfo, headerHeight
                    - viewInfo.getHeight() - scrollY);
            ViewHelper.setTranslationY(floatGroup, headerHeight - scrollY);
        }

        @Override
        public void onDownMotionEvent() {
        }

        @Override
        public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.layout_pull_frag, null);
        viewInfo = $(R.id.view_info);
        topImage = $(R.id.top_image);
        topbar = $(R.id.top_bar);
        pullListView = $(R.id.lv_songlist_songs);
        floatGroup = $(R.id.float_group);
        viewInfo.addView(createViewInfo());
        return contentView;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appContext.setViewFitsStatusBar(topbar);
    }

    protected void setTopImage(Bitmap bmp) {
        topImage.setImageBitmap(bmp);
        topImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                topImage.getViewTreeObserver().removeOnPreDrawListener(this);
                int height = topImage.getHeight();
                int normalHeaderHieght = (int) (height * imageHeightRatio);
                pullListView.setMaxHeaderHeight(height);
                pullListView.setNormalHeaderHeight(normalHeaderHieght);
                updateImageHeight(normalHeaderHieght);
                return false;
            }
        });

        pullListView.setOnPullListener(pullListener);
        pullListView.setScrollViewCallbacks(scrollViewCallbacks);
    }

    private void updateImageHeight(int height) {
        ViewGroup.LayoutParams lp = topImage.getLayoutParams();
        lp.height = height;
        topImage.setLayoutParams(lp);
    }

    protected void addFloatView(View view, LinearLayout.LayoutParams lp) {
        floatGroup.addView(view, lp);
    }

    protected void addFloatView(View view) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        addFloatView(view, lp);
    }
}
