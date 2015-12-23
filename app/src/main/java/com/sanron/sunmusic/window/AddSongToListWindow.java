package com.sanron.sunmusic.window;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.task.AddSongToListTask;

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

    public AddSongToListWindow(final Activity activity, final List<PlayList> playLists, final SongInfo songInfo) {
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
                new AddSongToListTask(playLists.get(position), songInfo) {
                    @Override
                    protected void onPostData(Integer num) {
                        AddSongToListWindow.this.dismiss();
                        if (num == 0) {
                            Toast.makeText(mActivity, "添加失败", Toast.LENGTH_SHORT).show();
                        } else if (num == -1) {
                            Toast.makeText(mActivity, "此歌曲已存在列表中", Toast.LENGTH_SHORT).show();
                        } else if(num > 0){
                            Toast.makeText(mActivity, "添加成功", Toast.LENGTH_SHORT).show();
                        }
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

    public void setActivity(Activity activity){
        mActivity = activity;
    }

    public void show() {
        showAtLocation(mActivity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        animateShow();
    }

    //activity背景恢复动画
    private void animateDismiss(){
        final WindowManager.LayoutParams attr = mActivity.getWindow().getAttributes();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.7f,mOldAlpha);
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
    private void animateShow(){
        final WindowManager.LayoutParams attr = mActivity.getWindow().getAttributes();
        mOldAlpha = attr.alpha;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(attr.alpha,0.7f);
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
