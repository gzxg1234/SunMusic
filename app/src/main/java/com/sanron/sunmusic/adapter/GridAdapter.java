package com.sanron.sunmusic.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanron.sunmusic.R;

import java.util.List;

public abstract class GridAdapter<Data> extends RecyclerView.Adapter<GridAdapter.GridItemHolder> {

    protected List<Data> mData;
    protected Context mContext;
    public GridAdapter(Context context, List<Data> data) {
        super();
        mContext = context;
        mData = data;
    }

    public void setData(List<Data> data) {
        mData = data;
        notifyDataSetChanged();
    }


    @Override
    public GridItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.grid_item_1, parent, false);
        return new GridItemHolder(view);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public abstract void onBindViewHolder(GridItemHolder holder, final int position);

    public static class GridItemHolder extends RecyclerView.ViewHolder{
        public ImageView ivPicture;
        public TextView tvText1;
        public TextView tvText2;
        public ImageButton btnAction;

        public GridItemHolder(View itemView) {
            super(itemView);
            ivPicture = (ImageView) itemView.findViewById(R.id.iv_picture);
            tvText1 = (TextView) itemView.findViewById(R.id.tv_text1);
            tvText2 = (TextView) itemView.findViewById(R.id.tv_text2);
            btnAction = (ImageButton) itemView.findViewById(R.id.btn_action);
        }
    }
}