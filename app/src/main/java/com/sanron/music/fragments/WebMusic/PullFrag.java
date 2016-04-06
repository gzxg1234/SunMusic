package com.sanron.music.fragments.WebMusic;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.sanron.music.R;
import com.sanron.music.fragments.BaseFragment;
import com.sanron.music.view.DDImageView;
import com.sanron.music.view.DDPullListView;
import com.sanron.music.view.SlideFinishLayout;

/**
 * Created by sanron on 16-3-30.
 */
public abstract class PullFrag extends BaseFragment implements SlideFinishLayout.SlideFinishCallback {


    protected boolean isLoaded;
    private boolean animEnd;
    private View viewLoading;
    private View viewLoadFailed;
    protected DDPullListView pullListView;
    private View topbar;
    private SlideFinishLayout slideFinishLayout;
    private DDImageView topImage;
    private FrameLayout viewInfo;
    private LinearLayout floatGroup;
    private TextView tvTitle;
    private ImageButton ibtnBack;
    protected Handler handler = new Handler(Looper.getMainLooper());
    private float imageHeightRatio = 0.6f;
    public static final String TAG = PullFrag.class.getSimpleName();

    protected abstract View createViewInfo();

    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    private DDPullListView.OnPullListener pullListener = new DDPullListView.OnPullListener() {
        @Override
        public void onPull(int pullOffset, int pullHeight) {
            int normalHeaderHeight = pullListView.getNormalHeaderHeight();
            updateImageHeight(topImage.getHeight() + pullOffset);
            ViewHelper.setTranslationY(viewInfo, normalHeaderHeight
                    - viewInfo.getHeight() + pullHeight - floatGroup.getHeight());
            ViewHelper.setTranslationY(floatGroup, normalHeaderHeight + pullHeight - floatGroup.getHeight());
        }
    };

    private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem == 0
                    && visibleItemCount > 0
                    && !pullListView.isPulling()) {
                View header = pullListView.getChildAt(0);
                int scrollY = header.getHeight() - header.getBottom();
                int topbarHeight = topbar.getHeight();
                int normalHeaderHeight = pullListView.getNormalHeaderHeight();
                final int maxScrollY = normalHeaderHeight - topbarHeight - floatGroup.getHeight();
                scrollY = Math.min(maxScrollY, scrollY);

                final float alpha = 1 - (float) scrollY / maxScrollY;
                viewInfo.setAlpha(alpha);
                if (alpha == 0f) {
                    tvTitle.setVisibility(View.VISIBLE);
                } else {
                    tvTitle.setVisibility(View.INVISIBLE);
                }

                ViewHelper.setTranslationY(topImage, -scrollY);
                ViewHelper.setTranslationY(viewInfo, normalHeaderHeight
                        - viewInfo.getHeight() - scrollY - floatGroup.getHeight());
                ViewHelper.setTranslationY(floatGroup, normalHeaderHeight - scrollY - floatGroup.getHeight());
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.layout_pull_frag, null);
        slideFinishLayout = (SlideFinishLayout) contentView;
        viewLoading = $(R.id.layout_loading);
        viewLoadFailed = $(R.id.layout_load_failed);
        viewInfo = $(R.id.view_info);
        topImage = $(R.id.top_image);
        topbar = $(R.id.top_bar);
        pullListView = $(R.id.pull_list_view);
        floatGroup = $(R.id.float_group);
        tvTitle = $(R.id.tv_title);
        ibtnBack = $(R.id.ibtn_back);
        return contentView;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appContext.setViewFitsStatusBar(topbar);
        viewInfo.addView(createViewInfo());
        slideFinishLayout.setSlideFinishCallback(this);
        ibtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager()
                        .popBackStackImmediate(PullFrag.this.getClass().getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

            }
        });
        pullListView.setOnPullListener(pullListener);
        pullListView.setOnScrollListener(onScrollListener);
    }

    protected void hideLoadingView() {
        if (animEnd && isLoaded) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    viewLoading.setVisibility(View.GONE);
                }
            }, 300);
        }
    }

    protected void showLoadFailedView() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                viewLoadFailed.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (enter) {
            try {
                Animation enterAnim = AnimationUtils.loadAnimation(getContext(), nextAnim);
                enterAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        animEnd = true;
                        hideLoadingView();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                return enterAnim;
            } catch (Resources.NotFoundException e) {
                animEnd = true;
            }

        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    protected void setHeaderImage(Bitmap bmp) {
        topImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                topImage.getViewTreeObserver().removeOnPreDrawListener(this);
                int height = topImage.getHeight();
                int newImageHeight = (int) (height * imageHeightRatio);
                int normalHeaderHieght = newImageHeight + floatGroup.getHeight();
                pullListView.setMaxHeaderHeight(height + floatGroup.getHeight());
                pullListView.setNormalHeaderHeight(normalHeaderHieght);
                updateImageHeight(newImageHeight);
                return true;
            }
        });
        topImage.setImageBitmap(bmp);
    }

    private void updateImageHeight(int height) {
        ViewGroup.LayoutParams lp = topImage.getLayoutParams();
        lp.height = height;
        topImage.setLayoutParams(lp);
    }

    protected void addFloatView(View view) {
        floatGroup.addView(view);
    }

    @Override
    public void onSlideFinish() {
        getFragmentManager()
                .popBackStackImmediate(PullFrag.this.getClass().getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
