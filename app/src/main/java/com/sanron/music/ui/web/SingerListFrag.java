package com.sanron.music.ui.web;

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
import com.sanron.music.net.JsonCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.Singer;
import com.sanron.music.net.bean.SingerList;
import com.sanron.music.ui.BaseSlideWebFrag;
import com.sanron.music.view.DDPullListView;
import com.sanron.music.view.ScrimPopupWindow;

import java.util.List;

import okhttp3.Call;

/**
 * Created by sanron on 16-4-19.
 */
public class SingerListFrag extends BaseSlideWebFrag implements DDPullListView.OnLoadListener {

    private int area;
    private int sex;
    private int abcIndex;
    private String title;
    private View viewSort;
    private DDPullListView mListView;
    private SingerAdapter mAdapter;
    public static final String[] ABC = new String[28];
    public static final int LIMIT = 30;

    public static SingerListFrag newInstance(String title, int area, int sex) {
        SingerListFrag singerListFrag = new SingerListFrag();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("area", area);
        args.putInt("sex", sex);
        singerListFrag.setArguments(args);
        return singerListFrag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            area = args.getInt("area");
            sex = args.getInt("sex");
            title = args.getString("title");
        }
        for (int i = 1; i < 27; i++) {
            ABC[i] = String.valueOf((char) ('a' + i - 1));
        }
        ABC[27] = "other";
        abcIndex = 0;
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
        mAdapter = new SingerAdapter();
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
        Call call = MusicApi.singerList(0, LIMIT, area, sex, 1, null, new JsonCallback<SingerList>() {
            @Override
            public void onSuccess(SingerList singerList) {
                if (singerList != null) {
                    mAdapter.setData(singerList.singers);
                }
                hideLoadingView();
            }

            @Override
            public void onFailure(Exception e) {
                showLoadFailedView();
            }
        });
        addCall(call);
    }

    @Override
    public void onLoad() {
        Call call = MusicApi.singerList(mAdapter.getCount(), LIMIT, area, sex, 1, ABC[abcIndex], new JsonCallback<SingerList>() {
            @Override
            public void onSuccess(SingerList singerList) {
                if (singerList != null) {
                    mAdapter.addData(singerList.singers);
                    mListView.setHasMore(singerList.havemore == 1);
                }
                mListView.onLoadCompleted();
            }

            @Override
            public void onFailure(Exception e) {
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
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.list_common_item, parent, false);
                ImageView ivMenu = (ImageView) convertView.findViewById(R.id.iv_menu);
                ivMenu.setImageResource(R.mipmap.ic_chevron_right_black_90_24dp);
                TextView text2 = (TextView) convertView.findViewById(R.id.tv_text2);
                text2.setVisibility(View.GONE);
            }
            ImageView ivPicture = (ImageView) convertView.findViewById(R.id.iv_picture);
            TextView tvName = (TextView) convertView.findViewById(R.id.tv_text1);
            //取消之前的加载任务
            ImageLoader.getInstance()
                    .cancelDisplayTask(ivPicture);
            ivPicture.setImageBitmap(null);
            ImageLoader.getInstance()
                    .displayImage(mData.get(position).avatarMiddle, ivPicture);
            tvName.setText(mData.get(position).name);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getMainActivity().showSingerInfo(mData.get(position).artistId);
                }
            });
            return convertView;
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
