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
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.db.bean.PlayList;
import com.sanron.ddmusic.task.AddMusicToListTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加歌曲到列表窗口
 */
public class AddSongToListWindow extends ScrimPopupWindow {
    private View mContentView;
    private ListView mPlayLists;
    private Button mBtnCancel;

    public AddSongToListWindow(final Activity activity, final List<PlayList> playLists, final List<Music> musics) {
        super(activity);
        this.mContentView = LayoutInflater.from(activity)
                .inflate(R.layout.window_add_to_playlist, null);
        this.mPlayLists = (ListView) mContentView.findViewById(R.id.list_playlist);
        this.mBtnCancel = (Button) mContentView.findViewById(R.id.btn_cancel);
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        setFocusable(true);
        setTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(screenHeight / 2);
        setAnimationStyle(R.style.MyWindowAnim);
        setContentView(mContentView);

        List<String> playListNames = new ArrayList<>();
        for (PlayList playList : playLists) {
            playListNames.add(playList.getTitle());
        }
        this.mPlayLists.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, playListNames));
        this.mPlayLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new AddMusicToListTask(playLists.get(position), musics) {
                    private ProgressDialog progressDialog;

                    @Override
                    protected void onPreExecute() {
                        progressDialog = new ProgressDialog(activity);
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    }

                    @Override
                    protected void onPostExecute(Integer addNum) {
                        String msg = addNum + "首歌曲添加成功,";
                        ViewTool.show(msg);
                        AddSongToListWindow.this.dismiss();
                        progressDialog.dismiss();
                    }
                }.execute();
            }
        });

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void show(View parent) {
        showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }
}
