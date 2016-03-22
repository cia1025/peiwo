package me.peiwo.peiwo.widget;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import me.peiwo.peiwo.R;

/**
 * Created by fuhaidong on 15/11/25.
 */
public class PWAlertDialog extends DialogFragment implements View.OnClickListener {

    TextView tv_title;
    CheckBox cb_no_remind;

    public static PWAlertDialog newInstance() {
        return new PWAlertDialog();
    }

    public static PWAlertDialog newInstance(String input_text) {
        PWAlertDialog boxView = new PWAlertDialog();
        Bundle data = new Bundle();
        data.putString("input_text", input_text);
        boxView.setArguments(data);
        return boxView;
    }

    public static PWAlertDialog newInstance(String input_text, String checkbox_text) {
        PWAlertDialog boxView = new PWAlertDialog();
        Bundle data = new Bundle();
        data.putString("input_text", input_text);
        data.putString("checkbox_text", checkbox_text);
        boxView.setArguments(data);
        return boxView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.self_alert_dialog, null);
        tv_title = (TextView) v.findViewById(R.id.tv_title);
        cb_no_remind = (CheckBox) v.findViewById(R.id.cb_no_remind);
        v.findViewById(R.id.tv_cancel).setOnClickListener(this);
        v.findViewById(R.id.tv_ok).setOnClickListener(this);
        initVar();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        return builder.create();
    }

    private void initVar() {
        Bundle data = getArguments();
        if (data != null) {
            if (data.containsKey("input_title")) {
                tv_title.setText(data.getString("input_title"));
            }
            if (data.containsKey("checkbox_text")) {
                cb_no_remind.setVisibility(View.VISIBLE);
                cb_no_remind.setText(data.getString("checkbox_text"));
            } else {
                cb_no_remind.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_ok:
                if (mConfirmListener != null) {
                    mConfirmListener.onConfirm(!cb_no_remind.isChecked());
                }
                break;
            case R.id.tv_cancel:
                if (mCancelListener != null) {
                    mCancelListener.onCancel();
                }
                break;
            default:
                break;
        }
        dismiss();
    }

    private CancelListener mCancelListener;

    public void setOnInputCancelListener(CancelListener CancelListener) {
        this.mCancelListener = CancelListener;
    }

    private ConfirmListener mConfirmListener;

    public void setOnInputConfirmListener(ConfirmListener inputConfirmListener) {
        this.mConfirmListener = inputConfirmListener;
    }

    public interface CancelListener {
        void onCancel();
    }

    public interface ConfirmListener {
        void onConfirm(boolean alertAgain);
    }
}
