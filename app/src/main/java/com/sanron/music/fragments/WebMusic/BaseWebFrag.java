package com.sanron.music.fragments.WebMusic;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.fragments.BaseFragment;
import com.sanron.music.view.SlideFinishLayout;

import okhttp3.Call;

/**
 * Created by sanron on 16-4-12.
 */
public abstract class BaseWebFrag extends BaseFragment implements SlideFinishLayout.SlideFinishCallback {


    private SlideFinishLayout slideFinishLayout;
    protected View topBar;
    protected TextView tvTitle;
    protected Call dataCall;
    private View viewBack;
    private View viewLoading;
    private View viewLoadFailed;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        topBar = $(R.id.top_bar);
        tvTitle = $(R.id.tv_title);
        slideFinishLayout = $(R.id.slide_finish_layout);
        viewBack = $(R.id.view_back);
        viewLoading = $(R.id.layout_loading);
        viewLoadFailed = $(R.id.layout_load_failed);
        appContext.setViewFitsStatusBar(topBar);

        slideFinishLayout.setSlideFinishCallback(this);
        viewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager()
                        .popBackStackImmediate(BaseWebFrag.this.getClass().getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    protected void setTitle(String title) {
        tvTitle.setText(title);
    }

    protected void hideLoadingView() {
        viewLoading.setVisibility(View.GONE);
    }

    protected abstract Call loadData();

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
                        dataCall = loadData();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                return enterAnim;
            } catch (Resources.NotFoundException e) {
                dataCall = loadData();
            }

        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dataCall != null) {
            dataCall.cancel();
        }
    }

    protected void showLoadFailedView() {
        viewLoadFailed.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSlideFinish() {
        getFragmentManager()
                .popBackStackImmediate(BaseWebFrag.this.getClass().getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
