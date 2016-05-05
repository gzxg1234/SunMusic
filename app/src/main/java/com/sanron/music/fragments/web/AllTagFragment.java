package com.sanron.music.fragments.web;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.api.JsonCallback;
import com.sanron.music.api.MusicApi;
import com.sanron.music.api.bean.AllTag;
import com.sanron.music.api.bean.HotTagData;
import com.sanron.music.api.bean.Tag;
import com.sanron.music.fragments.base.SlideWebFragment;
import com.sanron.music.view.NoScrollGridView;
import com.sanron.music.view.RatioLayout;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by sanron on 16-4-14.
 */
public class AllTagFragment extends SlideWebFragment {

    private ListView mLvTags;
    private CategoryAdapter mAdapter;
    private AllTag mData;
    private HotTagData mHotTagData;

    public static Fragment newInstance() {
        return new AllTagFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.web_frag_all_tag, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLvTags = $(R.id.lv_tags);
        setTitle("歌曲分类");
        mTopBar.setBackgroundColor(Color.BLACK);
        mAdapter = new CategoryAdapter();
        mLvTags.setAdapter(mAdapter);
    }

    @Override
    protected void loadData() {
        Call call1 = MusicApi.allTag(new JsonCallback<AllTag>() {
            @Override
            public void onSuccess(final AllTag data) {
                setData(data);
            }

            @Override
            public void onFailure(Exception e) {
                showLoadFailedView();
            }
        });

        Call call2 = MusicApi.hotTag(8, new JsonCallback<HotTagData>() {
            @Override
            public void onSuccess(final HotTagData data) {
                mHotTagData = data;
                mAdapter.setHotTagData(data);
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
        addCall(call1);
        addCall(call2);
    }

    private void setData(AllTag data) {
        this.mData = data;
        if (data != null) {
            if (data.tagList != null) {
                List<Map.Entry<String, List<Tag>>> items = new LinkedList<>();
                for (Map.Entry<String, List<Tag>> entry : data.tagList.entrySet()) {
                    items.add(entry);
                }
                mAdapter.setCategories(items);
            }
        }
        hideLoadingView();
    }

    private class CategoryAdapter extends BaseAdapter {

        private HotTagData mHotTagData;
        private List<Map.Entry<String, List<Tag>>> mCategories;

        public static final int HOT_TAG_HEADER = 0;
        public static final int CATEGORY_ITEM = 1;

        public void setCategories(List<Map.Entry<String, List<Tag>>> data) {
            this.mCategories = data;
            notifyDataSetChanged();
        }

        public void setHotTagData(HotTagData mHotTagData) {
            this.mHotTagData = mHotTagData;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return 1 + (mCategories == null ? 0 : +mCategories.size());
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (getItemViewType(position) == CATEGORY_ITEM) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_tag_category_item, parent, false);
                }
                TextView tvCategory = (TextView) convertView.findViewById(R.id.tv_category);
                GridView gvTags = (GridView) convertView.findViewById(R.id.grid_view);
                String category = mCategories.get(position - 1).getKey();
                List<Tag> tags = mCategories.get(position - 1).getValue();
                tvCategory.setText(category);
                gvTags.setAdapter(new TagAdapter(tags));
            } else {
                if (convertView == null) {
                    convertView = createHotTagHeaderView();
                }
                GridView gv = (GridView) convertView;
                HotTagAdapter hotTagAdapter =
                        (HotTagAdapter) gv.getAdapter();
                if (hotTagAdapter != null
                        && mHotTagData != null) {
                    hotTagAdapter.setData(mHotTagData.tags);
                }
            }
            return convertView;
        }

        private GridView createHotTagHeaderView() {
            final int spacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            NoScrollGridView gv = new NoScrollGridView(getContext());
            gv.setNumColumns(4);
            gv.setHorizontalSpacing(spacing);
            gv.setVerticalSpacing(spacing);
            gv.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            gv.setAdapter(new HotTagAdapter());
            return gv;
        }


        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return HOT_TAG_HEADER;
            } else {
                return CATEGORY_ITEM;
            }
        }
    }

    private class HotTagAdapter extends BaseAdapter {
        private List<Tag> tags;
        public final int[] ICONS = new int[]{
                R.mipmap.ic_classify_img01,
                R.mipmap.ic_classify_img02,
                R.mipmap.ic_classify_img03,
                R.mipmap.ic_classify_img04,
                R.mipmap.ic_classify_img05,
                R.mipmap.ic_classify_img06,
                R.mipmap.ic_classify_img07,
                R.mipmap.ic_classify_img08,
        };

        @Override
        public int getCount() {
            return 8;
        }

        public void setData(List<Tag> data) {
            this.tags = data;
            notifyDataSetChanged();
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
            TextView tvTag = null;
            if (convertView == null) {
                RatioLayout ratioLayout = new RatioLayout(getContext(), null);
                ratioLayout.setType(RatioLayout.TYPE_HEIGHT);
                ratioLayout.setRatio(1f);
                ratioLayout.setBackgroundResource(ICONS[position]);

                tvTag = new TextView(getContext());
                tvTag.setTextColor(Color.WHITE);
                tvTag.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                tvTag.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                tvTag.setGravity(Gravity.CENTER);
                ratioLayout.addView(tvTag);
                convertView = ratioLayout;
            } else {
                tvTag = (TextView) ((ViewGroup) convertView).getChildAt(0);
            }
            if (tags != null
                    && position < tags.size()) {
                String tag = tags.get(position).title;
                tvTag.setText(tag);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getMainActivity().showTagSong(tags.get(position).title);
                }
            });
            return convertView;
        }
    }

    private class TagAdapter extends BaseAdapter {

        private List<Tag> tags;

        public TagAdapter(List<Tag> tags) {
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
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.tag_text_view, parent, false);
            }
            TextView tvTag = (TextView) convertView;
            tvTag.setText(tags.get(position).title);
            tvTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getMainActivity().showTagSong(tags.get(position).title);
                }
            });
            return tvTag;
        }
    }
}
