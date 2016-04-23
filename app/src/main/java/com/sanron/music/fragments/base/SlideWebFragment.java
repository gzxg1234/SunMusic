package com.sanron.music.fragments.base;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.common.ViewTool;
import com.sanron.music.view.SlideBackLayout;

import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;

/**
 * Created by sanron on 16-4-12.
 */
public abstract class SlideWebFragment extends BaseFragment implements SlideBackLayout.SlideBackCallback {

    private List<Call> mCalls;
    private SlideBackLayout mSlideBackLayout;
    protected View mTopBar;
    protected TextView mTvTitle;
    private View mViewBack;
    private View mViewLoading;
    private View mViewLoadFailed;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTopBar = $(R.id.top_bar);
        mTvTitle = $(R.id.tv_queue_item_title);
        mSlideBackLayout = $(R.id.slide_back_layout);
        mViewBack = $(R.id.view_back);
        mViewLoading = $(R.id.layout_loading);
        mViewLoadFailed = $(R.id.layout_load_failed);
        ViewTool.setViewFitsStatusBar(mTopBar);

        mSlideBackLayout.setSlideBackCallback(this);
        mViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager()
                        .popBackStackImmediate(SlideWebFragment.this.getClass().getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }


    protected void setTitle(String title) {
        mTvTitle.setText(title);
    }

    protected void hideLoadingView() {
        mViewLoading.setVisibility(View.GONE);
    }

    protected abstract void loadData();

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (enter) {
            Animation enterAnim = null;
            try {
                enterAnim = AnimationUtils.loadAnimation(getContext(), nextAnim);
            } catch (Resources.NotFoundException e) {
            }
            if (enterAnim == null) {
                loadData();
            } else {
                enterAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        //动画完成再加载数据,使动画流畅
                        loadData();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
            return enterAnim;
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    protected void addCall(Call call) {
        if (mCalls == null) {
            mCalls = new LinkedList<>();
        }
        mCalls.add(call);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mCalls != null) {
            for (Call call : mCalls) {
                call.cancel();
            }
            mCalls.clear();
        }
    }

    protected void showLoadFailedView() {
        mViewLoadFailed.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSlideBack() {
        getFragmentManager()
                .popBackStackImmediate(SlideWebFragment.this.getClass().getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
