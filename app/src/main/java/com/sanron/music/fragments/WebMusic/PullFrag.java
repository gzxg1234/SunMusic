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
    protected DDPullListView pullListView;
    protected FrameLayout infoContainer;
    protected LinearLayout operatorContainer;
    private boolean animEnd;
    private View viewLoading;
    private View viewLoadFailed;
    private View topbar;
    private SlideFinishLayout slideFinishLayout;
    private DDImageView topImage;
    private TextView tvTitle;
    private ImageButton ibtnBack;
    protected Handler handler = new Handler(Looper.getMainLooper());
    private float imageHeightRatio = 0.6f;
    public static final String TAG = PullFrag.class.getSimpleName();

    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    private DDPullListView.OnPullListener pullListener = new DDPullListView.OnPullListener() {
        @Override
        public void onPull(int pullOffset, int pullHeight) {
            int normalHeaderHeight = pullListView.getNormalHeaderHeight();
            updateImageHeight(topImage.getHeight() + pullOffset);
            ViewHelper.setTranslationY(infoContainer, normalHeaderHeight
                    - infoContainer.getHeight() + pullHeight - operatorContainer.getHeight());
            ViewHelper.setTranslationY(operatorContainer, normalHeaderHeight + pullHeight - operatorContainer.getHeight());
        }
    };

    private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (visibleItemCount > 0
                    && !pullListView.isPulling()) {
                int scrollY;
                int topbarHeight = topbar.getHeight();
                int normalHeaderHeight = pullListView.getNormalHeaderHeight();
                final int maxScrollY = normalHeaderHeight - topbarHeight - operatorContainer.getHeight();
                if (firstVisibleItem == 0) {
                    View header = pullListView.getChildAt(0);
                    scrollY = header.getHeight() - header.getBottom();
                    scrollY = Math.min(maxScrollY, scrollY);
                } else {
                    scrollY = maxScrollY;
                }
                final float alpha = 1 - (float) scrollY / maxScrollY;
                infoContainer.setAlpha(alpha);
                if (alpha == 0f) {
                    tvTitle.setVisibility(View.VISIBLE);
                } else {
                    tvTitle.setVisibility(View.INVISIBLE);
                }

                ViewHelper.setTranslationY(topImage, -scrollY);
                ViewHelper.setTranslationY(infoContainer, normalHeaderHeight
                        - infoContainer.getHeight() - scrollY - operatorContainer.getHeight());
                ViewHelper.setTranslationY(operatorContainer, normalHeaderHeight - scrollY - operatorContainer.getHeight());
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
        infoContainer = $(R.id.info_container);
        topImage = $(R.id.top_image);
        topbar = $(R.id.top_bar);
        pullListView = $(R.id.pull_list_view);
        operatorContainer = $(R.id.operator_container);
        tvTitle = $(R.id.tv_title);
        ibtnBack = $(R.id.ibtn_back);
        return contentView;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appContext.setViewFitsStatusBar(topbar);
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
                int normalHeaderHieght = newImageHeight + operatorContainer.getHeight();
                pullListView.setMaxHeaderHeight(height + operatorContainer.getHeight());
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

    @Override
    public void onSlideFinish() {
        getFragmentManager()
                .popBackStackImmediate(PullFrag.this.getClass().getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
