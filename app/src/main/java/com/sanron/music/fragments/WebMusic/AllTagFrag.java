package com.sanron.music.fragments.WebMusic;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.activities.MainActivity;
import com.sanron.music.net.ApiCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.AllTag;
import com.sanron.music.net.bean.HotTagData;
import com.sanron.music.net.bean.Tag;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by sanron on 16-4-14.
 */
public class AllTagFrag extends BaseSlideWebFrag {

    private List<TextView> tvHotTags;
    private ListView lvTag;
    private CategoryTagAdapter adapter;
    private Handler handler = new Handler();
    private AllTag data;

    public static final int EVENT_CLICK_TAG = 1;

    public static final String EXTRA_TAG_NAME = "tag";

    public static Fragment newInstance() {
        return new AllTagFrag();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.web_frag_all_tag, null);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout hotTagGroup1 = $(R.id.hot_tag_group1);
        LinearLayout hotTagGroup2 = $(R.id.hot_tag_group2);
        lvTag = $(R.id.lv_tag);

        setTitle("歌曲分类");
        topBar.setBackgroundColor(Color.BLACK);
        adapter = new CategoryTagAdapter();
        lvTag.setAdapter(adapter);

        tvHotTags = new LinkedList<>();
        for (int i = 0; i < hotTagGroup1.getChildCount(); i++) {
            tvHotTags.add((TextView) hotTagGroup1.getChildAt(i));
        }
        for (int i = 0; i < hotTagGroup2.getChildCount(); i++) {
            tvHotTags.add((TextView) hotTagGroup2.getChildAt(i));
        }

    }

    @Override
    protected void onEnterAnimationEnd() {
        Call call1 = MusicApi.allTag(new ApiCallback<AllTag>() {
            @Override
            public void onSuccess(final AllTag data) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setData(data);
                    }
                }, 500);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            showLoadFailedView();
                        }
                    });
                }
            }
        });

        Call call2 = MusicApi.hotTag(8, new ApiCallback<HotTagData>() {
            @Override
            public void onSuccess(final HotTagData data) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setUpHotTag(data);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
        addCall(call1);
        addCall(call2);
    }

    private void setUpHotTag(final HotTagData data) {
        if (data != null
                && data.tags != null) {
            for (int i = 0; i < tvHotTags.size() && i < data.tags.size(); i++) {
                final String tag = data.tags.get(i).title;
                TextView tvHotTag = tvHotTags.get(i);
                tvHotTag.setText(tag);
                tvHotTag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getActivity() instanceof MainActivity){
                            ((MainActivity)getActivity()).showTagSong(tag);
                        }
                    }
                });
            }
        }
    }

    private void setData(AllTag data) {
        this.data = data;
        if (data != null) {
            if (data.tagList != null) {
                List<Map.Entry<String, List<Tag>>> items = new LinkedList<>();
                for (Map.Entry<String, List<Tag>> entry : data.tagList.entrySet()) {
                    items.add(entry);
                }
                adapter.setData(items);
            }
        }
        hideLoadingView();
    }


    public class CategoryTagAdapter extends BaseAdapter {

        private List<Map.Entry<String, List<Tag>>> data;

        public void setData(List<Map.Entry<String, List<Tag>>> data) {
            if (this.data == data) {
                return;
            }
            this.data = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Map.Entry<String, List<Tag>> entry = (Map.Entry<String, List<Tag>>) getItem(position);
            String category = entry.getKey();
            List<Tag> tags = entry.getValue();
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.list_tag_category_item, parent, false);
            }
            TextView tvCategory = (TextView) convertView.findViewById(R.id.tv_category);
            GridView gvTags = (GridView) convertView.findViewById(R.id.grid_view);
            tvCategory.setText(category);
            gvTags.setAdapter(new TagItemAdapter(tags));
            return convertView;
        }

    }

    private class TagItemAdapter extends BaseAdapter {

        private List<Tag> tags;

        public TagItemAdapter(List<Tag> tags) {
            this.tags = tags;
        }

        @Override
        public int getCount() {
            return tags == null ? 0 : tags.size();
        }

        @Override
        public Object getItem(int position) {
            return tags.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final TextView tvTag;
            if (convertView == null) {
                tvTag = new TextView(getContext());
                AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tvTag.setGravity(Gravity.CENTER);
                tvTag.setPadding(10, 10, 10, 10);
                tvTag.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                tvTag.setTextColor(getResources().getColor(R.color.textColorSecondary));
                tvTag.setLayoutParams(lp);
                tvTag.setBackgroundResource(R.drawable.tv_tag_bg);
                ColorStateList csl = getResources().getColorStateList(R.drawable.tv_tag_bg);
                tvTag.setTextColor(csl);
            } else {
                tvTag = (TextView) convertView;
            }
            tvTag.setText(tags.get(position).title);
            tvTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getActivity() instanceof MainActivity){
                        ((MainActivity)getActivity()).showTagSong(tags.get(position).title);
                    }
                }
            });
            return tvTag;
        }
    }
}
