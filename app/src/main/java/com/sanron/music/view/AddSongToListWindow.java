package com.sanron.music.view;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.sanron.music.R;
import com.sanron.music.db.model.Music;
import com.sanron.music.db.model.PlayList;
import com.sanron.music.task.AddMusicToListTask;
import com.sanron.music.utils.TUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加歌曲到列表窗口
 */
public class AddSongToListWindow extends PopupWindow {
    private View contentView;
    private ListView playLists;
    private Button btnCancel;
    private Activity activity;
    private float oldAlpha;

    public AddSongToListWindow(final Activity activity, final List<PlayList> playLists, final List<Music> musics) {
        super(activity);
        this.activity = activity;
        this.contentView = LayoutInflater.from(activity).inflate(R.layout.window_add_to_playlist, null);
        this.playLists = (ListView) contentView.findViewById(R.id.list_playlist);
        this.btnCancel = (Button) contentView.findViewById(R.id.btn_cancel);
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        setFocusable(true);
        setTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(screenHeight / 2);
        setAnimationStyle(R.style.MyWindowAnim);
        setContentView(contentView);

        List<String> playListNames = new ArrayList<>();
        for (PlayList playList : playLists) {
            playListNames.add(playList.getTitle());
        }
        this.playLists.setAdapter(new ArrayAdapter<>(this.activity, android.R.layout.simple_list_item_1, playListNames));
        this.playLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                        TUtils.show(AddSongToListWindow.this.activity, msg);
                        AddSongToListWindow.this.dismiss();
                        progressDialog.dismiss();
                    }
                }.execute();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void show(View parent) {
        showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        animateShow();
    }

    //activity背景恢复动画
    private void animateDismiss() {
        final WindowManager.LayoutParams attr = activity.getWindow().getAttributes();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.7f, oldAlpha);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                attr.alpha = (float) animation.getAnimatedValue();
                activity.getWindow().setAttributes(attr);
            }
        });
        valueAnimator.start();
    }

    //activity背景变暗动画
    private void animateShow() {
        final WindowManager.LayoutParams attr = activity.getWindow().getAttributes();
        oldAlpha = attr.alpha;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(attr.alpha, 0.7f);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                attr.alpha = (float) animation.getAnimatedValue();
                activity.getWindow().setAttributes(attr);
            }
        });
        valueAnimator.start();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        animateDismiss();
    }
}
