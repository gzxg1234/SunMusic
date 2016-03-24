package com.sanron.music.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.sanron.music.db.model.PlayList;
import com.sanron.music.db.model.Music;
import com.sanron.music.task.DelListMusicTask;
import com.sanron.music.utils.TUtils;

import java.util.List;

/**
 * 移除歌曲对话框
 */
public class RemoveListSongDialogBuilder extends AlertDialog.Builder {
    private PlayList mPlayList;
    private List<Music> mRemoveSongs;
    private ProgressDialog mProgressDlg;

    public RemoveListSongDialogBuilder(Context context, PlayList playList, List<Music> removeSongs) {
        super(context);
        this.mPlayList = playList;
        this.mRemoveSongs = removeSongs;
        this.mProgressDlg = new ProgressDialog(context);
        mProgressDlg.setMessage("移除中");
        mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDlg.setCancelable(false);

        setTitle(playList.getName());
        if (mRemoveSongs.size() == 1) {
            setMessage("移除歌曲 \"" + mRemoveSongs.get(0).getTitle() + "\"");
        } else {
            setMessage("移除" + mRemoveSongs.size() + "首歌曲");
        }
        setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                new DelListMusicTask(mPlayList, mRemoveSongs) {
                    @Override
                    protected void onPostExecute(Integer num) {
                        if (num > 0) {
                            TUtils.show(getContext(), "移除" + num + "首歌曲");
                        } else {
                            TUtils.show(getContext(), "删除失败");
                        }
                    }
                }.execute();
            }
        });
        setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
    }
}