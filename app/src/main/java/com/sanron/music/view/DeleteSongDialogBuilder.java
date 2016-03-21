package com.sanron.music.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.sanron.music.db.Music;
import com.sanron.music.task.DelLocalMusicTask;
import com.sanron.music.utils.TUtils;

import java.util.List;

/**
 * 删除歌曲对话框
 */
public class DeleteSongDialogBuilder extends AlertDialog.Builder {
    private List<Music> mDeleteSongs;
    private boolean mIsDeleteFile;
    private ProgressDialog mProgressDlg;

    public DeleteSongDialogBuilder(Context context, List<Music> deleteSongs) {
        super(context);
        this.mDeleteSongs = deleteSongs;
        this.mProgressDlg = new ProgressDialog(context);
        mProgressDlg.setMessage("删除中");
        mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDlg.setCancelable(false);

        if(deleteSongs.size() == 1){
            setTitle("删除歌曲 "+deleteSongs.get(0).getTitle());
        }else{
            setTitle("删除"+deleteSongs.size()+"首歌曲");
        }
        setMultiChoiceItems(new String[]{"同时删除音乐文件"},
                new boolean[]{mIsDeleteFile},
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        mIsDeleteFile = isChecked;
                    }
                });
        setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                new DelLocalMusicTask(getContext(), mDeleteSongs, mIsDeleteFile) {
                    @Override
                    protected void onPreExecute() {
                        mProgressDlg.show();
                }

                    @Override
                    protected void onPostExecute(Integer num) {
                        mProgressDlg.cancel();
                        if (num > 0) {
                            TUtils.show(getContext(), "删除" + num + "首歌曲");
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