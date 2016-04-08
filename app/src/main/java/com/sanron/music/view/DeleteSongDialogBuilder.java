package com.sanron.music.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.model.Music;
import com.sanron.music.task.DeleteTask;
import com.sanron.music.utils.TUtils;

import java.util.List;

/**
 * 删除歌曲对话框
 */
public class DeleteSongDialogBuilder extends AlertDialog.Builder implements DialogInterface.OnClickListener, DeleteTask.DeleteCallback {
    private List<Music> deleteMusics;
    private boolean isDeleteFile;
    private ProgressDialog progressDialog;

    public DeleteSongDialogBuilder(Context context, List<Music> deleteMusics) {
        super(context);
        this.deleteMusics = deleteMusics;
        this.progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("删除中");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        if (deleteMusics.size() == 1) {
            setTitle("删除歌曲 " + deleteMusics.get(0).getTitle());
        } else {
            setTitle("删除" + deleteMusics.size() + "首歌曲");
        }
        setMultiChoiceItems(new String[]{"同时删除音乐文件"},
                new boolean[]{isDeleteFile}, null);
        setPositiveButton("确定", this);
        setNegativeButton("取消", this);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_NEGATIVE: {
                dialog.cancel();
            }
            break;

            case DialogInterface.BUTTON_POSITIVE: {
                new DeleteTask()
                        .table(DBHelper.ListMember.TABLE)
                        .where(DBHelper.ListMember.LIST_ID + "=" + DBHelper.List.TYPE_LOCAL_ID
                                + " and " + DBHelper.ListMember.MUSIC_ID + " in(" + createIn() + ")")
                        .execute(this);
            }
            break;
        }
    }


    private String createIn() {
        StringBuilder in = new StringBuilder();
        for (Music music : deleteMusics) {
            in.append(music.getId()).append(",");
        }
        if (in.length() > 0) {
            in.deleteCharAt(in.length() - 1);
        }
        return in.toString();
    }

    @Override
    public void onPreDelete() {
        progressDialog.show();
    }

    @Override
    public void onDeleteFinish(int deleteCount) {
        if (deleteCount > 0) {
            TUtils.show(getContext(), "删除" + deleteCount + "首歌曲");
        } else {
            TUtils.show(getContext(), "删除失败");
        }
        progressDialog.dismiss();
    }
}