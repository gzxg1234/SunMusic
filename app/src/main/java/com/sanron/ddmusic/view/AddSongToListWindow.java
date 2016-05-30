package com.sanron.ddmusic.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.sanron.ddmusic.R;
import com.sanron.ddmusic.common.ViewTool;
import com.sanron.ddmusic.db.AppDB;
import com.sanron.ddmusic.db.ResultCallback;
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.db.bean.PlayList;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

/**
 * 添加歌曲到列表窗口
 */
public class AddSongToListWindow extends ScrimPopupWindow {
    View mContentView;
    @BindView(R.id.list_playlist)
    ListView mLvPlayLists;
    @BindView(R.id.btn_cancel)
    Button mBtnCancel;

    private List<PlayList> mPlayLists;
    private List<Music> mMusics;

    public AddSongToListWindow(final Activity activity, final List<PlayList> playLists, final List<Music> musics) {
        super(activity);
        mMusics = musics;
        mPlayLists = playLists;
        mContentView = LayoutInflater.from(activity)
                .inflate(R.layout.window_add_to_playlist, null);
        setContentView(mContentView);
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        setFocusable(true);
        setTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(screenHeight / 2);
        setAnimationStyle(R.style.MyWindowAnim);
        ButterKnife.bind(this, mContentView);

        List<String> playListNames = new ArrayList<>();
        for (PlayList playList : mPlayLists) {
            playListNames.add(playList.getTitle());
        }
        mLvPlayLists.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, playListNames));
    }

    @OnItemClick(R.id.list_playlist)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.show();
        AppDB.get(getActivity()).addMusicToPlaylist(mPlayLists.get(position), mMusics,
                new ResultCallback<Integer>() {
                    @Override
                    public void onResult(Integer result) {
                        String msg = result + "首歌曲添加成功,";
                        ViewTool.show(msg);
                        AddSongToListWindow.this.dismiss();
                        progressDialog.dismiss();
                    }
                });
    }

    @OnClick(R.id.btn_cancel)
    public void onClickCancel() {
        dismiss();
    }

    public void show(View parent) {
        showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }
}
