package com.sanron.music.fragments.pagerwebmusic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.R;
import com.sanron.music.api.JsonCallback;
import com.sanron.music.api.MusicApi;
import com.sanron.music.api.bean.BillCategoryData;
import com.sanron.music.fragments.base.LazyLoadFragment;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/10.
 */
public class BillboardFragment extends LazyLoadFragment {


    private RecyclerView mRecyclerView;
    private BillCategoryAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new BillCategoryAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_recycler_view, container, false);
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        super.initView(view, savedInstanceState);
        mRecyclerView = (RecyclerView) getView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void loadData() {
        MusicApi.billCategory(new JsonCallback<BillCategoryData>() {
            @Override
            public void onSuccess(BillCategoryData billCategoryData) {
                if (billCategoryData != null) {
                    for (int i = 0; i < billCategoryData.billCategories.size(); i++) {
                        //移除king榜
                        if (billCategoryData.billCategories.get(i).type == 100) {
                            billCategoryData.billCategories.remove(i);
                            break;
                        }
                    }
                    mAdapter.setData(billCategoryData.billCategories);
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    private class BillCategoryAdapter extends RecyclerView.Adapter<BillCategoryAdapter.BillCategoryHolder> {

        private List<BillCategoryData.BillCategory> mData;
        private final int[] TOP_TEXT_COLORS = new int[]{
                0xFFF50000, 0xFFF77722, 0xFFFFC505
        };

        public void setData(List<BillCategoryData.BillCategory> data) {
            mData = data;
            notifyDataSetChanged();
        }

        @Override
        public BillCategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_billboard_item, parent, false);
            BillCategoryHolder holder = new BillCategoryHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(final BillCategoryHolder holder, int position) {
            final BillCategoryData.BillCategory billCategory = mData.get(position);
            holder.ivPicture.setImageBitmap(null);
            ImageLoader.getInstance().cancelDisplayTask(holder.ivPicture);
            ImageLoader.getInstance()
                    .displayImage(billCategory.picS192, holder.ivPicture);
            holder.tvBillCategory.setText(billCategory.name);
            if (billCategory.topSongs != null) {
                for (int i = 0; i < billCategory.topSongs.size() && i < holder.tvTops.size(); i++) {
                    SpannableStringBuilder ss = new SpannableStringBuilder(String.valueOf(i + 1));
                    ss.setSpan(new ForegroundColorSpan(TOP_TEXT_COLORS[i]), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ss.append(" ")
                            .append(billCategory.topSongs.get(i).title)
                            .append("-")
                            .append(billCategory.topSongs.get(i).author);
                    holder.tvTops.get(i).setText(ss);
                }
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getMainActivity().showBillboardInfo(billCategory.type);
                }
            });
        }


        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        class BillCategoryHolder extends RecyclerView.ViewHolder {
            TextView tvBillCategory;
            ImageView ivPicture;
            List<TextView> tvTops;

            public BillCategoryHolder(View itemView) {
                super(itemView);
                tvBillCategory = (TextView) itemView.findViewById(R.id.tv_billcategory);
                ivPicture = (ImageView) itemView.findViewById(R.id.iv_picture);
                tvTops = new LinkedList<>();
                TextView tvTop1 = (TextView) itemView.findViewById(R.id.tv_top1);
                TextView tvTop2 = (TextView) itemView.findViewById(R.id.tv_top2);
                TextView tvTop3 = (TextView) itemView.findViewById(R.id.tv_top3);
                tvTops.add(tvTop1);
                tvTops.add(tvTop2);
                tvTops.add(tvTop3);
            }
        }
    }
}
