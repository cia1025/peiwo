package me.peiwo.peiwo.widget;

import me.peiwo.peiwo.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

/**
 * Created by fuhaidong on 14/11/5.
 */
public class RemarksContactView extends Dialog implements View.OnClickListener {

    private EditText et_mark;

    public RemarksContactView(Context context) {
        super(context, R.style.AnimDialogLoading);
    }

    public RemarksContactView(Context context, int theme) {
        super(context, R.style.AnimDialogLoading);
    }

    protected RemarksContactView(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_remark_contact);

        findViewById(R.id.btn_remark).setOnClickListener(this);
        findViewById(R.id.btn_cancle).setOnClickListener(this);
        et_mark = (EditText) findViewById(R.id.et_mark);
    }

    public interface OnRemarkClickListener {
        public void onRemarkClick(String mark);
    }

    private OnRemarkClickListener listener;

    public void setOnRemarClickListener(OnRemarkClickListener listener) {
        this.listener = listener;
    }

    public void displayReMark(String remark) {
        if (et_mark != null) {
            et_mark.setText(remark);
            if (!TextUtils.isEmpty(remark))
                et_mark.setSelection(remark.length());
        }
    }

    @Override
    public void show() {
        try {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            params.dimAmount = 0.5f;
            super.show();
            DisplayMetrics metrics = new DisplayMetrics();
            getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            params.width = (int) (metrics.widthPixels * 0.9);
            getWindow().setAttributes(params);
        } catch (Exception ex) {
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancle:
                break;
            case R.id.btn_remark:
                if (this.listener != null) {
                    this.listener.onRemarkClick(et_mark != null ? et_mark.getText().toString() : "");
                }
                break;
        }
        dismiss();
    }
}