package com.sanron.ddmusic.fragments.base;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.sanron.ddmusic.R;
import com.sanron.ddmusic.common.ViewTool;
import com.sanron.ddmusic.view.SlideBackLayout;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Call;

/**
 * Created by sanron on 16-4-12.
 */
public abstract class SlideWebFragment extends BaseFragment implements SlideBackLayout.SlideBackCallback {

    @BindView(R.id.slide_back_layout)
    protected SlideBackLayout mSlideBackLayout;
    @BindView(R.id.top_bar)
    protected View mTopBar;
    @BindView(R.id.tv_title)
    protected TextView mTvTitle;
    @BindView(R.id.view_back)
    protected View mViewBack;
    @BindView(R.id.layout_loading)
    protected View mViewLoading;
    @BindView(R.id.layout_load_failed)
    protected View mViewLoadFailed;

    private List<Call> mCalls;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
