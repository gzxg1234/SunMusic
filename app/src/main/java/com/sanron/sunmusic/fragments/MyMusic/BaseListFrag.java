package com.sanron.sunmusic.fragments.MyMusic;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.sanron.sunmusic.R;
import com.sanron.sunmusic.adapter.DataListAdapter;
import com.sanron.sunmusic.db.DataProvider;
import com.sanron.sunmusic.fragments.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Administrator on 2016/3/3.
 */
public abstract class BaseListFrag<T> extends BaseFragment implements DataListAdapter.OnItemClickListener, DataListAdapter.OnItemActionClickListener, DataListAdapter.OnItemLongLickListener, Observer {

    public static final int LAYOUT_LINEAR = 1;
    public static final int LAYOUT_GRID = 2;

    protected String[] subscribes;

    private int layout = LAYOUT_LINEAR;
    protected RecyclerView recyclerView;
    protected DataListAdapter<T> mAdapter;

    private boolean isShowItemPicture = true;//是否加载图片
    private ImageLoader imageLoader;
    private MemoryCache memoryCache;
    private DisplayImageOptions imageOptions;
    private boolean isRecyclerViewInit = false;

    public BaseListFrag(int layout, String[] subscribes) {
        this.layout = layout;
        this.subscribes = subscribes;
        imageLoader = ImageLoader.getInstance();
        memoryCache = imageLoader.getMemoryCache();
        imageOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(true)
                .build();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        DataProvider.instance().addObserver(this);

        int layoutid = LAYOUT_LINEAR;
        if (layout == LAYOUT_LINEAR) {
            layoutid = R.layout.list_item;
        } else if (layout == LAYOUT_GRID) {
            layoutid = R.layout.grid_item;
        }

        mAdapter = new DataListAdapter<T>(getContext(), layoutid) {
            @Override
            public void onBindViewHolder(ItemHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                BaseListFrag.this.bindViewHolder(holder, position);
                if (isShowItemPicture) {
                    String picPath = onGetPicturePath(mAdapter.getItem(position));
                    if (!TextUtils.isEmpty(picPath)){

                        if(!isRecyclerViewInit){
                            //recyclerview初次绑定item时加载图片
                            imageLoader.displayImage("file://"+picPath.toString(),
                                    holder.ivPicture,
                                    imageOptions);
                            return;
                        }

                        if(displayFromMemoryCache(picPath,holder.ivPicture)){
                            //尝试从内存获取
                            return;
                        }
                    }
                    holder.ivPicture.setImageResource(R.mipmap.default_song_pic);
                }
            }

            @Override
            public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                ItemHolder holder = super.onCreateViewHolder(parent, viewType);
                if(!isShowItemPicture){
                    holder.ivPicture.setVisibility(View.GONE);
                }
                return holder;
            }
        };

        mAdapter.setOnItemLongLickListener(this);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemActionClickListener(this);

        refreshData();

    }

    private boolean displayFromMemoryCache(String path,ImageView imageView){
        List<Bitmap> bitmaps = MemoryCacheUtils.findCachedBitmapsForImageUri("file://"+path,memoryCache);
        if(bitmaps.size() > 0){
            imageView.setImageBitmap(bitmaps.get(0));
            return true;
        }
        return false;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        isRecyclerViewInit = false;
        if (isShowItemPicture) {
            //停止滑动时加载图片
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    isRecyclerViewInit = true;
                    switch (newState){
                        case RecyclerView.SCROLL_STATE_IDLE:{
                            loadItemPicture();
                        }break;

                        case RecyclerView.SCROLL_STATE_SETTLING:
                        case RecyclerView.SCROLL_STATE_DRAGGING:{
                            imageLoader.stop();
                        }break;
                    }
                }
            });
        }
    }

    public void setShowItemPicture(boolean showItemPicture) {
        isShowItemPicture = showItemPicture;
    }

    /**
     * 加载item图片
     */
    public void loadItemPicture() {
        int childCount = recyclerView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = recyclerView.getChildAt(i);
            int position = recyclerView.getChildAdapterPosition(child);
            DataListAdapter.ItemHolder holder =
                    (DataListAdapter.ItemHolder) recyclerView.getChildViewHolder(child);
            String picPath = onGetPicturePath(mAdapter.getItem(position));
            if (!TextUtils.isEmpty(picPath)) {
                imageLoader.displayImage("file://"+picPath.toString(), holder.ivPicture, imageOptions);
            }
        }
    }


    /**
     * 获取图片路径
     */
    public String onGetPicturePath(T data) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DataProvider.instance().deleteObserver(this);
    }


    @Override
    public void update(Observable observable, Object data) {
        if (subscribes == null) {
            return;
        }

        for (String subscribe : subscribes) {
            if (subscribe.equals(data)) {
                refreshData();
                break;
            }
        }
    }

    public abstract void refreshData();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.frag_recycler_layout, null);
        recyclerView = $(R.id.recycler_view);
        recyclerView.setAdapter(mAdapter);
        if (layout == LAYOUT_LINEAR) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        } else if (layout == LAYOUT_GRID) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2,GridLayoutManager.VERTICAL,false));
        }
        return contentView;
    }

    protected abstract void bindViewHolder(DataListAdapter.ItemHolder holder, int position);

    public boolean onCreateActionMenu(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onItemLongClick(View view, final int position) {

        getActivity().startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(final ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_selected_all, menu);
                if (!onCreateActionMenu(mode, menu)) {
                    return false;
                }

                mAdapter.startMutliMode();
                mAdapter.setOnItemSelectedListener(new DataListAdapter.OnItemCheckedListener() {
                    @Override
                    public void onItemSelectedChange(int position, boolean isSelected) {
                        mode.setTitle(mAdapter.getSelectedItemCount() + "项选中");
                    }
                });
                mAdapter.setItemSelected(position, true);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(final ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                final List<T> selectedData = new ArrayList<>();
                SparseBooleanArray selectedPositions = mAdapter.getSelectedItemPositions();
                for (int i = 0; i < selectedPositions.size(); i++) {
                    T data = mAdapter.getItem(selectedPositions.keyAt(i));
                    selectedData.add(data);
                }
                switch (item.getItemId()) {
                    case R.id.menu_select_all: {
                        if (mAdapter.getSelectedItemCount() == mAdapter.getItemCount()) {
                            setAll(false);
                        } else {
                            setAll(true);
                        }
                    }
                    break;
                    default: {
                        onActionItemSelected(item, selectedData);
                        mode.finish();
                    }
                }
                return true;
            }

            public void setAll(boolean selected) {
                for (int i = 0; i < mAdapter.getItemCount(); i++) {
                    mAdapter.setItemSelected(i, selected);
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mAdapter.endMutliMode();
            }
        });
        return true;
    }

    public void onActionItemSelected(MenuItem item, final List<T> songInfos) {

    }

    @Override
    public void onItemActionClick(View view, final int potisiton) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        onCreatePopupMenu(popupMenu.getMenu(), popupMenu.getMenuInflater(), potisiton);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onPopupItemSelected(item, potisiton);
                return true;
            }
        });
        popupMenu.show();
    }

    public void onCreatePopupMenu(Menu menu, MenuInflater inflater, int position) {

    }

    public void onPopupItemSelected(MenuItem item, int position) {

    }


    @Override
    public void onItemClick(View view, int position) {

    }
}
