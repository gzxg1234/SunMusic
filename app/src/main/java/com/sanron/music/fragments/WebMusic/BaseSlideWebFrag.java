package com.sanron.music.fragments.WebMusic;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.view.SlideBackLayout;

/**
 * Created by sanron on 16-4-12.
 */
public abstract class BaseSlideWebFrag extends BaseWebFrag implements SlideBackLayout.SlideBackCallback {

    private SlideBackLayout slideBackLayout;
    protected View topBar;
    protected TextView tvTitle;
    private View viewBack;
    private View viewLoading;
    private View viewLoadFailed;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        topBar = $(R.id.top_bar);
        tvTitle = $(R.id.tv_title);
        slideBackLayout = $(R.id.slide_finish_layout);
        viewBack = $(R.id.view_back);
        viewLoading = $(R.id.layout_loading);
        viewLoadFailed = $(R.id.layout_load_failed);
        appContext.setViewFitsStatusBar(topBar);

        slideBackLayout.setSlideBackCallback(this);
        viewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager()
                        .popBackStackImmediate(BaseSlideWebFrag.this.getClass().getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }


    protected void setTitle(String title) {
        tvTitle.setText(title);
    }

    protected void hideLoadingView() {
        viewLoading.setVisibility(View.GONE);
    }

    protected void onEnterAnimationEnd() {
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
                        onEnterAnimationEnd();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                return enterAnim;
            } catch (Resources.NotFoundException e) {
                onEnterAnimationEnd();
            }

        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    protected void showLoadFailedView() {
        viewLoadFailed.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSlideBack() {
        getFragmentManager()
                .popBackStackImmediate(BaseSlideWebFrag.this.getClass().getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
