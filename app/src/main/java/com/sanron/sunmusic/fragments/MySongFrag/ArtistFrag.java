package com.sanron.sunmusic.fragments.MySongFrag;

import android.content.Context;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.db.ArtistProvider;
import com.sanron.sunmusic.fragments.BaseFragment;
import com.sanron.sunmusic.model.Artist;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.task.GetArtistsTask;
import com.sanron.sunmusic.utils.DensityUtil;
import com.sanron.sunmusic.utils.T;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

/**
 * Created by Administrator on 2015/12/21.
 */
public class ArtistFrag extends BaseFragment implements Observer {

    private RecyclerView rvArtists;
    private ArtistItemAdapter mArtistAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArtistProvider.instance().addObserver(this);
        mArtistAdapter = new ArtistItemAdapter(getContext(), null);
        update(null, null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = LayoutInflater.from(getContext()).inflate(R.layout.frag_artist, null);
        rvArtists = $(R.id.rv_artists);
        rvArtists.setAdapter(mArtistAdapter);
        rvArtists.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        rvArtists.addItemDecoration(new MyItemDecoration());
        return contentView;
    }

    public static class MyItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            int right = 0;
            int bottom = 0;
            int padding = (int) parent.getContext().getResources().getDimension(R.dimen.recycle_item_padding);
            int position = lp.getViewLayoutPosition();
            int s = parent.getChildAdapterPosition(view);
            System.out.println(position);
            System.out.println(s);
            int itemCount = parent.getAdapter().getItemCount();
            //总列数
            int colCount = getSpanCount(parent.getLayoutManager());
            //总行数
            int rowCount = itemCount / colCount + (itemCount % colCount == 0 ? 0 : 1);
            //当前item对应行
            int curRow = (position + 1) / colCount + (position % colCount == 0 ? 0 : 1);
            if ((position + 1) % colCount != 0) {
                //非最后一列才需要右边距
                right = padding;
            }
            if (curRow != rowCount) {
                bottom = padding;
            }
            System.out.println(right);
            outRect.set(0, 0, right, bottom);
        }

        private int getSpanCount(RecyclerView.LayoutManager lm) {
            if (lm instanceof GridLayoutManager) {
                return ((GridLayoutManager) lm).getSpanCount();
            } else if (lm instanceof StaggeredGridLayoutManager) {
                return ((StaggeredGridLayoutManager) lm).getSpanCount();
            }
            return -1;
        }
    }

    public static class ArtistItemAdapter extends RecyclerView.Adapter<ArtistItemAdapter.ArtistHolder> {

        private List<Artist> mData;
        private Context mContext;

        public ArtistItemAdapter(Context context, List<Artist> data) {
            super();
            mContext = context;
            mData = data;
        }

        public void setData(List<Artist> data) {
            mData = data;
            notifyDataSetChanged();
        }


        @Override
        public ArtistHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.grid_item_artist, parent, false);
            return new ArtistHolder(view);
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        private int[] ids = new int[]{R.mipmap.d1, R.mipmap.d2, R.mipmap.d3, R.mipmap.d4};

        @Override
        public void onBindViewHolder(final ArtistHolder holder, final int position) {
            Artist artist = mData.get(position);
            holder.tvText1.setText(artist.getName());
            holder.tvText2.setText(artist.getAlbumNum() + "张专辑");
            int n = new Random().nextInt(4);
            holder.ivPicture.setImageResource(ids[n]);
        }

        public class ArtistHolder extends RecyclerView.ViewHolder {
            ImageView ivPicture;
            TextView tvText1;
            TextView tvText2;
            ImageButton btnAction;

            public ArtistHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        T.show(mContext,""+getLayoutPosition());
                    }
                });
                ivPicture = (ImageView) itemView.findViewById(R.id.iv_picture);
                tvText1 = (TextView) itemView.findViewById(R.id.tv_text1);
                tvText2 = (TextView) itemView.findViewById(R.id.tv_text2);
                btnAction = (ImageButton) itemView.findViewById(R.id.btn_action);
            }

        }

    }


    public static ArtistFrag newInstance() {
        return new ArtistFrag();
    }

    @Override
    public void update(Observable observable, Object data) {
        new GetArtistsTask() {
            @Override
            protected void onPostExecute(List<Artist> artists) {
                mArtistAdapter.setData(artists);
            }
        }.execute();
    }
}
