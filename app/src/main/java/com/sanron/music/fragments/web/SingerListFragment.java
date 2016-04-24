package com.sanron.music.fragments.web;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.R;
import com.sanron.music.common.ViewTool;
import com.sanron.music.fragments.base.SlideWebFragment;
import com.sanron.music.api.JsonCallback;
import com.sanron.music.api.MusicApi;
import com.sanron.music.api.bean.Singer;
import com.sanron.music.api.bean.SingerList;
import com.sanron.music.view.DDPullListView;
import com.sanron.music.view.ScrimPopupWindow;

import java.util.List;

import okhttp3.Call;

/**
 * Created by sanron on 16-4-19.
 */
public class SingerListFragment extends SlideWebFragment implements DDPullListView.OnLoadListener {

    private int area;
    private int sex;
    private int abcIndex;
    private String title;
    private View viewSort;
    private DDPullListView mListView;
    private SingerAdapter mAdapter;
    public static final int LIMIT = 30;

    public static final String ARG_TITLE = "title";
    public static final String ARG_AREA = "area";
    public static final String ARG_SEX = "sex";

    public static final String[] ABC = new String[28];

    static {
        for (int i = 1; i < 27; i++) {
            ABC[i] = String.valueOf((char) ('a' + i - 1));
        }
        ABC[27] = "other";
    }

    public static SingerListFragment newInstance(String title, int area, int sex) {
        SingerListFragment fragment = new SingerListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_AREA, area);
        args.putInt(ARG_SEX, sex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(ARG_TITLE);
            area = args.getInt(ARG_AREA);
            sex = args.getInt(ARG_SEX);
        }
        mAdapter = new SingerAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.web_frag_singer_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = $(R.id.pull_list_view);
        viewSort = $(R.id.view_sort);
        setTitle(title);
        mListView.setAdapter(mAdapter);
        viewSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SelectSortWindow(getActivity())
                        .showAtLocation(getView(), Gravity.BOTTOM, 0, 0);
            }
        });
        mListView.setOnLoadListener(this);
    }

    @Override
    protected void loadData() {
        loadMore(true);
    }

    @Override
    public void onLoad() {
        loadMore(false);
    }

    private void loadMore(final boolean first) {
        Call call = MusicApi.singerList(mAdapter.getCount(), LIMIT, area, sex, 1, ABC[abcIndex], new JsonCallback<SingerList>() {
            @Override
            public void onSuccess(SingerList singerList) {
                if (singerList != null) {
                    mAdapter.addData(singerList.singers);
                    mListView.setHasMore(singerList.havemore == 1);
                }
                if (first) {
                    hideLoadingView();
                }
                mListView.onLoadCompleted();
            }

            @Override
            public void onFailure(Exception e) {
                if (first) {
                    showLoadFailedView();
                }
                mListView.onLoadCompleted();
            }
        });
        addCall(call);
    }

    private class SingerAdapter extends BaseAdapter {

        private List<Singer> mData;

        public void setData(List<Singer> data) {
            this.mData = data;
            notifyDataSetChanged();
        }

        public void addData(List<Singer> data) {
            if (mData == null) {
                mData = data;
            } else {
                mData.addAll(data);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            SingerHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.list_common_item, parent, false);
                ImageView ivMenu = (ImageView) convertView.findViewById(R.id.iv_menu);
                ivMenu.setImageResource(R.mipmap.ic_chevron_right_black_90_24dp);
                TextView text2 = (TextView) convertView.findViewById(R.id.tv_text2);
                text2.setVisibility(View.GONE);

                holder = new SingerHolder();
                holder.ivPicture = (ImageView) convertView.findViewById(R.id.iv_picture);
                holder.tvName = (TextView) convertView.findViewById(R.id.tv_text1);
                convertView.setTag(holder);
            } else {
                holder = (SingerHolder) convertView.getTag();
            }
            //取消之前的加载任务
            ImageLoader.getInstance()
                    .cancelDisplayTask(holder.ivPicture);
            holder.ivPicture.setImageBitmap(null);
            ImageLoader.getInstance()
                    .displayImage(mData.get(position).avatarMiddle, holder.ivPicture);
            holder.tvName.setText(mData.get(position).name);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getMainActivity().showSingerInfo(mData.get(position).artistId);
                }
            });
            return convertView;
        }

        class SingerHolder {
            ImageView ivPicture;
            TextView tvName;
        }
    }

    public class SelectSortWindow extends ScrimPopupWindow {

        private Context mContext;

        public SelectSortWindow(Activity activity) {
            super(activity);
            mContext = activity;
            View root = LayoutInflater.from(mContext)
                    .inflate(R.layout.window_select_sort, null);
            setContentView(root);
            setOutsideTouchable(true);
            setFocusable(true);
            setAnimationStyle(R.style.MyWindowAnim);
            setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

            GridView gv = (GridView) root.findViewById(R.id.grid_view);
            Button btnCancel = (Button) root.findViewById(R.id.btn_cancel);
            gv.setAdapter(new SortAdapter());
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }

        private class SortAdapter extends BaseAdapter {

            @Override
            public int getCount() {
                return 28;
            }

            @Override
            public Object getItem(int position) {
                return position;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) convertView;
                if (textView == null) {
                    textView = new TextView(mContext);
                    final int height = ViewTool.dpToPx(40);
                    textView.setLayoutParams(
                            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
                    textView.setGravity(Gravity.CENTER);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                }

                if (position == abcIndex) {
                    textView.setTextColor(getResources().getColor(R.color.colorAccent));
                } else {
                    textView.setTextColor(Color.BLACK);
                }

                if (position == 0) {
                    textView.setText("热门");
                } else if (position == getCount() - 1) {
                    textView.setText("其他");
                } else {
                    char ch = (char) (position - 1 + 'A');
                    textView.setText(String.valueOf(ch));
                }
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (abcIndex == position) {
                            return;
                        }
                        abcIndex = position;
                        mAdapter.setData(null);
                        mListView.setHasMore(true);
                        mListView.load();
                        dismiss();
                    }
                });
                return textView;
            }
        }
    }

}
