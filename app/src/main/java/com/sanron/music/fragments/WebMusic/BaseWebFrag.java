package com.sanron.music.fragments.WebMusic;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.sanron.music.R;
import com.sanron.music.fragments.BaseFragment;
import com.sanron.music.view.SlideFinishLayout;

/**
 * Created by sanron on 16-4-12.
 */
public class BaseWebFrag extends BaseFragment implements SlideFinishLayout.SlideFinishCallback {


    private SlideFinishLayout slideFinishLayout;
    private View viewBack;
    private View viewLoading;
    private View viewLoadFailed;
    private boolean isAnimEnd;
    private boolean hasLoadData;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        slideFinishLayout = $(R.id.slide_finish_layout);
        viewBack = $(R.id.view_back);
        viewLoading = $(R.id.layout_loading);
        viewLoadFailed = $(R.id.layout_load_failed);

        slideFinishLayout.setSlideFinishCallback(this);
        viewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager()
                        .popBackStackImmediate(BaseWebFrag.this.getClass().getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    protected void setHasLoadData(boolean hasLoadData) {
        this.hasLoadData = hasLoadData;
        if (hasLoadData
                && isAnimEnd) {
            viewLoading.setVisibility(View.GONE);
        }
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
                        isAnimEnd = true;
                        if(hasLoadData){
                            viewLoading.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                return enterAnim;
            } catch (Resources.NotFoundException e) {
                isAnimEnd = true;
                if(hasLoadData){
                    viewLoading.setVisibility(View.GONE);
                }
            }

        }
        return super.onCreateAnimation(transit, enter, nextAnim);
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
