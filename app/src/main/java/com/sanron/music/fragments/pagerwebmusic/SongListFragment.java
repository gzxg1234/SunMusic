package com.sanron.music.fragments.pagerwebmusic;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.R;
import com.sanron.music.api.JsonCallback;
import com.sanron.music.api.MusicApi;
import com.sanron.music.api.bean.OfficialSongListData;
import com.sanron.music.api.bean.OfficialSongListSongs;
import com.sanron.music.api.bean.Song;
import com.sanron.music.api.bean.SongList;
import com.sanron.music.api.bean.SongListCategory;
import com.sanron.music.api.bean.SongListData;
import com.sanron.music.api.bean.TagSongListData;
import com.sanron.music.common.ViewTool;
import com.sanron.music.db.bean.Music;
import com.sanron.music.fragments.base.LazyLoadFragment;
import com.sanron.music.service.PlayerUtil;
import com.sanron.music.view.DDPullListView;
import com.sanron.music.view.ScrimPopupWindow;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/10.
 */
public class SongListFragment extends LazyLoadFragment implements DDPullListView.OnLoadMoreListener {

    private DDPullListView mListView;
    private SongListAdapter mAdapter;
    private TextView tvCurrentTag;
    private String currentTag = "全部";
    private int page = 0;
    public static final int PAGE_SIZE = 30;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new SongListAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mListView = new DDPullListView(getContext());
        mListView.setOnLoadMoreListener(this);
        mListView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        final int padding = ViewTool.dpToPx(16);
        mListView.setPadding(padding, padding, padding, padding);
        mListView.setAdapter(mAdapter);
        mListView.setDivider(null);
        mListView.setClipChildren(false);
        mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mListView.setClipToPadding(false);
        View header = LayoutInflater.from(getContext())
                .inflate(R.layout.songlist_title, null);
        tvCurrentTag = (TextView) header.findViewById(R.id.tv_text);
        tvCurrentTag.setText(currentTag);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicApi.songListCategory(new JsonCallback<SongListCategory>() {
                    @Override
                    public void onSuccess(SongListCategory songListCategory) {
                        if (songListCategory != null) {
                            new SelectCategoryWindow(getActivity(), songListCategory.content)
                                    .showAtLocation(SongListFragment.this.getView(), Gravity.BOTTOM, 0, 0);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        ViewTool.show("请求失败");
                    }
                });
            }
        });
        mListView.addHeaderView(header);
        return mListView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void loadData() {
        mListView.load();
    }

    private void switchTag(String tag) {
        if (currentTag.equals(tag)) {
            return;
        }

        currentTag = tag;
        tvCurrentTag.setText(currentTag);
        mAdapter.setData(null);
        page = 0;
        mListView.load();
    }

    @Override
    public void onLoadMore() {
        if (currentTag.equals("全部")) {
            MusicApi.songList(page + 1, PAGE_SIZE, new JsonCallback<SongListData>() {
                @Override
                public void onSuccess(SongListData songListData) {
                    mListView.onLoadCompleted();
                    if (songListData != null) {
                        mListView.setHasMore(songListData.havemore == 1);
                        List<SongListEntity[]> data = new ArrayList<>();
                        for (int i = 0; i < songListData.songLists.size(); i += 2) {
                            SongListEntity[] entities = new SongListEntity[2];
                            entities[0] = songListToEntity(songListData.songLists.get(i));
                            if (i + 1 < songListData.songLists.size()) {
                                entities[1] = songListToEntity(songListData.songLists.get(i + 1));
                            }
                            data.add(entities);
                        }
                        mAdapter.addData(data);
                        page++;
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    mListView.onLoadCompleted();
                }
            });
        } else if (currentTag.equals("音乐专题")) {
            MusicApi.officialSongList(page * PAGE_SIZE, PAGE_SIZE, new JsonCallback<OfficialSongListData>() {
                @Override
                public void onSuccess(OfficialSongListData officialSongListData) {
                    mListView.onLoadCompleted();
                    if (officialSongListData != null) {
                        List<SongListEntity[]> data = new ArrayList<>();
                        for (int i = 0; i < officialSongListData.songLists.size(); i += 2) {
                            SongListEntity[] entities = new SongListEntity[2];
                            entities[0] = officialSongListToEntity(officialSongListData.songLists.get(i));
                            if (i + 1 < officialSongListData.songLists.size()) {
                                entities[1] = officialSongListToEntity(officialSongListData.songLists.get(i + 1));
                            }
                            data.add(entities);
                        }
                        mAdapter.addData(data);
//                        返回hasmore数据有误，用total总数判断
//                        mListView.setHasMore(officialSongListData.havemore == 1);
                        try {
                            System.out.println(mAdapter.getCount()*2);
                            System.out.println(officialSongListData.total);
                            mListView.setHasMore(mAdapter.getCount() * 2
                                    < Integer.valueOf(officialSongListData.total));
                        } catch (NumberFormatException e) {
                            mListView.setHasMore(false);
                        }
                        page++;
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    mListView.onLoadCompleted();
                }
            });
        } else {
            MusicApi.songListByTag(currentTag, page + 1, PAGE_SIZE, new JsonCallback<TagSongListData>() {
                @Override
                public void onSuccess(TagSongListData tagSongListData) {
                    mListView.onLoadCompleted();
                    if (tagSongListData != null
                            && tagSongListData.songLists != null) {
                        mListView.setHasMore(tagSongListData.havemore == 1);
                        List<SongListEntity[]> data = new ArrayList<>();
                        for (int i = 0; i < tagSongListData.songLists.size(); i += 2) {
                            SongListEntity[] entities = new SongListEntity[2];
                            entities[0] = songListToEntity(tagSongListData.songLists.get(i));
                            if (i + 1 < tagSongListData.songLists.size()) {
                                entities[1] = songListToEntity(tagSongListData.songLists.get(i + 1));
                            }
                            data.add(entities);
                        }
                        mAdapter.addData(data);
                        page++;
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    mListView.onLoadCompleted();
                }
            });
        }
    }

    private SongListEntity songListToEntity(SongList songList) {
        SongListEntity entity = new SongListEntity();
        entity.id = songList.listId;
        entity.text1 = songList.title;
        entity.text2 = songList.tag;
        entity.pic = songList.pic300;
        entity.type = SongListEntity.NORMAL;
        return entity;
    }

    private SongListEntity officialSongListToEntity(OfficialSongListData.SongList songList) {
        SongListEntity entity = new SongListEntity();
        entity.id = songList.code;
        entity.text1 = songList.name;
        entity.text2 = songList.desc;
        entity.pic = songList.pic;
        entity.type = SongListEntity.OFFICIAL;
        return entity;
    }

    static class SongListEntity {
        String text1;
        String text2;
        String id;
        String pic;
        int type;
        static final int NORMAL = 1;
        static final int OFFICIAL = 2;
    }

    private class SongListAdapter extends BaseAdapter {
        private List<SongListEntity[]> mData = new ArrayList<>();

        public void setData(List<SongListEntity[]> data) {
            mData.clear();
            if (data != null) {
                mData.addAll(data);
            }
            notifyDataSetChanged();
        }

        public void addData(List<SongListEntity[]> data) {
            if (data != null) {
                mData.addAll(data);
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return position == 0 ? 0 : mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final SongListEntity[] item = mData.get(position);
            ItemHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.list_songlist_item, parent, false);
                holder = new ItemHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ItemHolder) convertView.getTag();
            }

            for (int i = 0; i < item.length; i++) {
                final SongListEntity listEntity = item[i];
                if (item[i] == null) {
                    holder.view[i].setVisibility(View.INVISIBLE);
                    continue;
                } else {
                    holder.view[i].setVisibility(View.VISIBLE);
                }

                holder.tvText1[i].setText(listEntity.text1);
                holder.tvText2[i].setText(listEntity.text2);
                holder.ivPicture[i].setImageBitmap(null);
                ImageLoader.getInstance()
                        .cancelDisplayTask(holder.ivPicture[i]);
                ImageLoader.getInstance()
                        .displayImage(listEntity.pic, holder.ivPicture[i]);

                if (listEntity.type == SongListEntity.NORMAL) {
                    //正常的歌单
                    holder.ivPlay[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MusicApi.songListInfo(listEntity.id, new JsonCallback<SongList>() {
                                @Override
                                public void onFailure(Exception e) {
                                    ViewTool.show("网络错误");
                                }

                                @Override
                                public void onSuccess(SongList data) {
                                    if (data != null
                                            && data.songs != null) {
                                        List<Music> musics = new LinkedList<>();
                                        for (Song song : data.songs) {
                                            musics.add(song.toMusic());
                                        }
                                        PlayerUtil.clearQueue();
                                        PlayerUtil.enqueue(musics);
                                        PlayerUtil.play(0);
                                    }
                                }
                            });
                        }
                    });
                    holder.view[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getMainActivity().showSongList(listEntity.id);
                        }
                    });
                } else if (listEntity.type == SongListEntity.OFFICIAL) {
                    //官方歌单
                    holder.ivPlay[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MusicApi.officialSongListSongs(listEntity.id, new JsonCallback<OfficialSongListSongs>() {
                                @Override
                                public void onFailure(Exception e) {
                                    ViewTool.show("网络错误");
                                }

                                @Override
                                public void onSuccess(OfficialSongListSongs data) {
                                    if (data != null
                                            && data.songs != null) {
                                        List<Music> musics = new LinkedList<>();
                                        for (Song song : data.songs) {
                                            musics.add(song.toMusic());
                                        }
                                        PlayerUtil.clearQueue();
                                        PlayerUtil.enqueue(musics);
                                        PlayerUtil.play(0);
                                    }
                                }
                            });
                        }
                    });
                    holder.view[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getMainActivity().showOfficialSongList(listEntity.id);
                        }
                    });
                }
            }
            return convertView;
        }

        class ItemHolder {
            View[] view = new View[2];
            TextView[] tvText1 = new TextView[2];
            TextView[] tvText2 = new TextView[2];
            ImageView[] ivPicture = new ImageView[2];
            ImageView[] ivPlay = new ImageView[2];

            public ItemHolder(View itemView) {
                view[0] = ((ViewGroup) itemView).getChildAt(0);
                ivPicture[0] = (ImageView) view[0].findViewById(R.id.iv_picture);
                tvText1[0] = (TextView) view[0].findViewById(R.id.tv_text1);
                tvText2[0] = (TextView) view[0].findViewById(R.id.tv_text2);
                ivPlay[0] = (ImageView) view[0].findViewById(R.id.iv_play);

                view[1] = ((ViewGroup) itemView).getChildAt(1);
                ivPicture[1] = (ImageView) view[1].findViewById(R.id.iv_picture);
                tvText1[1] = (TextView) view[1].findViewById(R.id.tv_text1);
                tvText2[1] = (TextView) view[1].findViewById(R.id.tv_text2);
                ivPlay[1] = (ImageView) view[1].findViewById(R.id.iv_play);
            }
        }
    }


    /**
     * 选择分类窗口
     */
    private class SelectCategoryWindow extends ScrimPopupWindow {

        public SelectCategoryWindow(Activity activity, List<SongListCategory.Content> contents) {
            super(activity);
            View root = LayoutInflater.from(getContext())
                    .inflate(R.layout.window_select_songlist_category, null);
            setContentView(root);
            setOutsideTouchable(true);
            setFocusable(true);
            setAnimationStyle(R.style.MyWindowAnim);
            int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
            setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            setHeight(screenHeight * 2 / 3);

            ListView listView = (ListView) root.findViewById(R.id.list_view);
            TextView textView = (TextView) LayoutInflater.from(getContext())
                    .inflate(R.layout.tag_text_view, null);
            textView.setText("全部");
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchTag("全部");
                    dismiss();
                }
            });
            listView.addHeaderView(textView);
            listView.setAdapter(new CategoryAdapter(contents));
            Button btnCancel = (Button) root.findViewById(R.id.btn_cancel);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }

        private class CategoryAdapter extends BaseAdapter {
            private List<SongListCategory.Content> mContents;

            public CategoryAdapter(List<SongListCategory.Content> contents) {
                mContents = contents;
            }

            @Override
            public int getCount() {
                return mContents.size();
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
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext())
                            .inflate(R.layout.list_tag_category_item, parent, false);
                }

                TextView tvCategory = (TextView) convertView.findViewById(R.id.tv_category);
                GridView gridView = (GridView) convertView.findViewById(R.id.grid_view);
                gridView.setNumColumns(3);
                gridView.setAdapter(new TagAdapter(mContents.get(position).tags));
                tvCategory.setText(mContents.get(position).title);
                return convertView;
            }


            private class TagAdapter extends BaseAdapter {

                private List<SongListCategory.Content.Tag> tags;

                public TagAdapter(List<SongListCategory.Content.Tag> tags) {
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
                    final String tag = tags.get(position).tag;
                    TextView tvTag = (TextView) convertView;
                    tvTag.setText(tag);
                    if (currentTag != null
                            && currentTag.equals(tag)) {
                        tvTag.setSelected(true);
                    } else {
                        tvTag.setSelected(false);
                    }
                    tvTag.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switchTag(tag);
                            dismiss();
                        }
                    });
                    return tvTag;
                }
            }
        }
    }
}
