package com.sanron.music.fragments.WebMusic;

import com.sanron.music.fragments.BaseFragment;

import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;

/**
 * Created by sanron on 16-4-16.
 */
public abstract class BaseWebFrag extends BaseFragment {
    private List<Call> httpCalls;

    protected void addCall(Call call) {
        if (httpCalls == null) {
            httpCalls = new LinkedList<>();
        }
        httpCalls.add(call);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (httpCalls != null) {
            for (Call call : httpCalls) {
                call.cancel();
            }
            httpCalls.clear();
        }
    }
}
