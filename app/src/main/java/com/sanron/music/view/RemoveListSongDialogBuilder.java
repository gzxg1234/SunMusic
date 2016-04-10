package com.sanron.music.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.model.Music;
import com.sanron.music.db.model.PlayList;
import com.sanron.music.task.DeleteTask;
import com.sanron.music.utils.T;

import java.util.List;

/**
 * 移除歌曲对话框
 */
public class RemoveListSongDialogBuilder extends AlertDialog.Builder implements DialogInterface.OnClickListener, DeleteTask.DeleteCallback {
    private PlayList playList;
    private List<Music> removeMusics;
    private ProgressDialog progressDialog;

    public RemoveListSongDialogBuilder(Context context, final PlayList playList, List<Music> removeSongs) {
        super(context);
        this.playList = playList;
        this.removeMusics = removeSongs;
        this.progressDialog = new ProgressDialog(context);

        setTitle(playList.getTitle());
        if (removeMusics.size() == 1) {
            setMessage("移除歌曲 \"" + removeMusics.get(0).getTitle() + "\"?");
        } else {
            setMessage("移除" + removeMusics.size() + "首歌曲?");
        }
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
                String in = createIn();
                new DeleteTask()
                        .table(DBHelper.ListMember.TABLE)
                        .where(DBHelper.ListMember.LIST_ID + "=" + playList.getId()
                                + " and " + DBHelper.ListMember.MUSIC_ID + " in(" + in + ")")
                        .execute(this);
            }
            break;
        }
    }

    private String createIn() {
        StringBuilder in = new StringBuilder();
        for (Music music : removeMusics) {
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
            T.show(getContext(), "移除" + deleteCount + "首歌曲");
        } else {
            T.show(getContext(), "移除失败");
        }
        progressDialog.dismiss();
    }
}