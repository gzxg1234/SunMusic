package com.sanron.sunmusic.window;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sanron.sunmusic.R;

public class ListNameInputDialog extends Dialog {
    private EditText etInput;
    private Button btnOk;
    private Button btnCancel;
    private TextView tvTitle;

    public ListNameInputDialog(Context context) {
        super(context);
        setCancelable(false);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_input_listname);
        tvTitle = (TextView) findViewById(R.id.tv_dlg_title);
        etInput = (EditText) findViewById(R.id.et_input);
        btnOk = (Button) findViewById(R.id.btn_ok);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }

    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    public String getInput() {
        return etInput.getText().toString();
    }

    public void setInputError(String error) {
        etInput.setError(error);
    }

    public void setInput(String input){
        etInput.setText(input);
    }

    public void setOnOkClickListener(View.OnClickListener onClickListener) {
        btnOk.setOnClickListener(onClickListener);
    }

}