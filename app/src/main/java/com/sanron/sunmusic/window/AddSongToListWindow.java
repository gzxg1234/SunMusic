package com.sanron.sunmusic.window;

import android.animation.ValueAnimator;
import android.app.Activity;
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

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.task.AddSongsToListTask;
import com.sanron.sunmusic.utils.T;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加歌曲到列表窗口
 */
public class AddSongToListWindow extends PopupWindow {
    private View mContentView;
    private ListView mListPlayList;
    private Button mBtnCancel;
    private List<PlayList> mPlayLists;
    private Activity mActivity;
    private float mOldAlpha;

    public AddSongToListWindow(final Activity activity, final List<PlayList> playLists, final List<SongInfo> songInfos) {
        super(activity);
        this.mActivity = activity;
        this.mPlayLists = playLists;
        this.mContentView = LayoutInflater.from(activity).inflate(R.layout.window_sel_playlist, null);
        this.mListPlayList = (ListView) mContentView.findViewById(R.id.list_playlist);
        this.mBtnCancel = (Button) mContentView.findViewById(R.id.btn_cancel);

        List<String> playListNames = new ArrayList<>();
        for (PlayList playList : playLists) {
            playListNames.add(playList.getName());
        }
        mListPlayList.setAdapter(new ArrayAdapter<>(mActivity, android.R.layout.simple_list_item_1, playListNames));
        mListPlayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new AddSongsToListTask(playLists.get(position), songInfos) {
                    @Override
                    protected void onPostExecute(Integer[] num) {
                        AddSongToListWindow.this.dismiss();
                        String msg = num[0] + "首歌曲添加成功,";
                        msg += num[1] == 0 ? "" : num[1] + "首歌曲已存在";
                        T.show(mActivity, msg);
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
        setFocusable(true);
        setTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setAnimationStyle(R.style.MyWindow);
        setContentView(mContentView);
    }

    public void show() {
        showAtLocation(mActivity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        animateShow();
    }

    //activity背景恢复动画
    private void animateDismiss() {
        final WindowManager.LayoutParams attr = mActivity.getWindow().getAttributes();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.7f, mOldAlpha);
        valueAnimator.setDuration(400);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                attr.alpha = (float) animation.getAnimatedValue();
                mActivity.getWindow().setAttributes(attr);
            }
        });
        valueAnimator.start();
    }

    //activity背景变暗动画
    private void animateShow() {
        final WindowManager.LayoutParams attr = mActivity.getWindow().getAttributes();
        mOldAlpha = attr.alpha;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(attr.alpha, 0.7f);
        valueAnimator.setDuration(400);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                attr.alpha = (float) animation.getAnimatedValue();
                mActivity.getWindow().setAttributes(attr);
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
