package com.sanron.music.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanron.music.R;

import java.util.List;

/**
 * Created by Administrator on 2015/12/29.
 */
public abstract class DataListAdapter<Data> extends RecyclerView.Adapter<DataListAdapter<Data>.ItemHolder> {

    private Context mContext;
    private List<Data> mData;
    private SparseBooleanArray mSelectedState;
    private int mItemLayoutId;
    private Drawable mDefaultBackground;
    private Drawable mSelectableBackground;
    private OnItemActionClickListener mOnItemActionClickListener;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongLickListener mOnItemLongLickListener;
    private OnItemCheckedListener mOnItemCheckedListener;

    public DataListAdapter(Context context, int itemid) {
        super();
        mContext = context;
        mItemLayoutId = itemid;

        TypedArray ta = mContext.obtainStyledAttributes(new int[]{android.support.design.R.attr.selectableItemBackground});
        mDefaultBackground = ta.getDrawable(0);
        ta.recycle();

        mSelectableBackground = mContext.getResources().getDrawable(R.drawable.multi_mode_selector);
    }

    public void setData(List<Data> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public Data getItem(int position) {
        return mData.get(position);
    }

    public void startMutliMode() {
        mSelectedState = new SparseBooleanArray(getItemCount());
        notifyDataSetChanged();
    }

    public void endMutliMode() {
        mSelectedState = null;
        notifyDataSetChanged();
    }

    public void setItemSelected(int position,boolean value){
        if(value) {
            mSelectedState.put(position, value);
        }else {
            mSelectedState.delete(position);
        }
        notifyDataSetChanged();
        if (mOnItemCheckedListener != null) {
            mOnItemCheckedListener.onItemSelectedChange(position, value);
        }
    }

    public boolean isItemSelected(int position){
        return mSelectedState.get(position,false);
    }

    public int getSelectedItemCount() {
        return mSelectedState.size();
    }

    public boolean isMultiMode(){
        return mSelectedState != null;
    }

    public SparseBooleanArray getSelectedItemPositions(){
        return mSelectedState;
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        if (isMultiMode()) {
            holder.itemView.setBackgroundDrawable(mSelectableBackground.getConstantState().newDrawable());
            holder.itemView.setSelected(isItemSelected(position));
        } else {
            holder.itemView.setBackgroundDrawable(mDefaultBackground.getConstantState().newDrawable());
            holder.itemView.setSelected(false);
        }
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(mItemLayoutId, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public ImageView ivPicture;
        public TextView tvText1;
        public TextView tvText2;
        public ImageButton btnAction;
        public View itemView;
        public ItemHolder(View itemView) {
            super(itemView);
            this.itemView = itemView.findViewById(R.id.item_view);
            ivPicture = (ImageView) itemView.findViewById(R.id.top_image);
            tvText1 = (TextView) itemView.findViewById(R.id.tv_title);
            tvText2 = (TextView) itemView.findViewById(R.id.tv_artist);
            btnAction = (ImageButton) itemView.findViewById(R.id.ibtn_item_menu);
            this.itemView.setOnClickListener(this);
            this.itemView.setOnLongClickListener(this);
            btnAction.setOnClickListener(this);
        }



        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ibtn_item_menu: {
                    if (mOnItemActionClickListener != null
                            && !isMultiMode()) {
                        mOnItemActionClickListener.onItemActionClick(v, getAdapterPosition());
                    }
                }
                break;
                case R.id.item_view: {
                    //多选模式下
                    if (isMultiMode()) {
                        boolean isSelected = isItemSelected(getAdapterPosition());
                        setItemSelected(getAdapterPosition(),!isSelected);
                        v.setSelected(!isSelected);
                        if (mOnItemCheckedListener != null) {
                            mOnItemCheckedListener.onItemSelectedChange(getAdapterPosition(), isSelected);
                        }
                        return;
                    }

                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(v, getAdapterPosition());
                    }
                }
                break;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mOnItemLongLickListener != null) {
                return mOnItemLongLickListener.onItemLongClick(v, getAdapterPosition());
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongLickListener {
        boolean onItemLongClick(View view, int position);
    }

    public interface OnItemActionClickListener {
        void onItemActionClick(View view, int potisiton);
    }

    public OnItemLongLickListener getOnItemLongLickListener() {
        return mOnItemLongLickListener;
    }

    public void setOnItemLongLickListener(OnItemLongLickListener onItemLongLickListener) {
        this.mOnItemLongLickListener = onItemLongLickListener;
    }

    public OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public OnItemActionClickListener getOnItemActionClickListener() {
        return mOnItemActionClickListener;
    }

    public void setOnItemActionClickListener(OnItemActionClickListener onItemActionClickListener) {
        this.mOnItemActionClickListener = onItemActionClickListener;
    }

    public interface OnItemCheckedListener {
        void onItemSelectedChange(int position, boolean isSelected);
    }

    public OnItemCheckedListener getOnItemSelectedListener() {
        return mOnItemCheckedListener;
    }

    public void setOnItemSelectedListener(OnItemCheckedListener onItemCheckedListener) {
        this.mOnItemCheckedListener = onItemCheckedListener;
    }

    public List<Data> getData() {
        return mData;
    }
}

