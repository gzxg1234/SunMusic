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

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.db.ArtistProvider;
import com.sanron.sunmusic.fragments.BaseFragment;
import com.sanron.sunmusic.model.Artist;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.task.GetArtistsTask;
import com.sanron.sunmusic.utils.DensityUtil;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

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
        mArtistAdapter = new ArtistItemAdapter(getContext(),null);
        update(null,null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = LayoutInflater.from(getContext()).inflate(R.layout.frag_artist,null);
        rvArtists = $(R.id.rv_artists);
        rvArtists.setAdapter(mArtistAdapter);
        rvArtists.setLayoutManager(new GridLayoutManager(getContext(),2));
        rvArtists.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
                super.getItemOffsets(outRect, itemPosition, parent);
                if((itemPosition+1) % 2 == 0){
                    outRect.set(0,0,0,DensityUtil.dip2px(getContext(),8));
                }else{
                    outRect.set(0,0,DensityUtil.dip2px(getContext(),8),DensityUtil.dip2px(getContext(),8));
                }
            }
        });
        return contentView;
    }

    public static class ArtistItemAdapter extends RecyclerView.Adapter<ArtistItemAdapter.ArtistHolder> {

        private List<Artist> mData;
        private Context mContext;
        public ArtistItemAdapter(Context context, List<Artist> data) {
            super();
            mContext = context;
            mData = data;
        }

        public void setData(List<Artist> data){
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

        @Override
        public void onBindViewHolder(final ArtistHolder holder, final int position) {
            Artist artist = mData.get(position);
            holder.tvText1.setText(artist.getName());
            holder.tvText2.setText(artist.getAlbumNum()+"张专辑");
        }

        public class ArtistHolder extends RecyclerView.ViewHolder{
            ImageView ivPicture;
            TextView tvText1;
            TextView tvText2;
            ImageButton btnAction;

            public ArtistHolder(View itemView) {
                super(itemView);
                ivPicture = (ImageView) itemView.findViewById(R.id.iv_picture);
                tvText1 = (TextView) itemView.findViewById(R.id.tv_text1);
                tvText2 = (TextView) itemView.findViewById(R.id.tv_text2);
                btnAction = (ImageButton) itemView.findViewById(R.id.btn_action);
            }

        }

    }


    public static ArtistFrag newInstance(){
        return new ArtistFrag();
    }

    @Override
    public void update(Observable observable, Object data) {
        new GetArtistsTask(){
            @Override
            protected void onPostExecute(List<Artist> artists) {
                mArtistAdapter.setData(artists);
            }
        }.execute();
    }
}
