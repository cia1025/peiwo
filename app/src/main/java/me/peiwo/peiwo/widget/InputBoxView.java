package me.peiwo.peiwo.widget;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import me.peiwo.peiwo.R;

/**
 * Created by fuhaidong on 15/11/25.
 */
public class InputBoxView extends DialogFragment implements View.OnClickListener {

    private TextView tv_input_title;
    private EditText et_input_field;
//    private View v_input_cancel;
//    private View v_input_confirm;

    public static InputBoxView newInstance() {
        return new InputBoxView();
    }

    public static InputBoxView newInstance(String input_title, String input_hint) {
        InputBoxView boxView = new InputBoxView();
        Bundle data = new Bundle();
        data.putString("input_title", input_title);
        data.putString("input_hint", input_hint);
        boxView.setArguments(data);
        return boxView;
    }

    public static InputBoxView newInstance(String input_title, String input_hint, int input_type) {
        InputBoxView boxView = new InputBoxView();
        Bundle data = new Bundle();
        data.putString("input_title", input_title);
        data.putString("input_hint", input_hint);
        data.putInt("input_type", input_type);
        boxView.setArguments(data);
        return boxView;
    }

    public static InputBoxView newInstance(String input_text) {
        InputBoxView boxView = new InputBoxView();
        Bundle data = new Bundle();
        data.putString("input_text", input_text);
        boxView.setArguments(data);
        return boxView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.layout_input_box, null);
        tv_input_title = (TextView) v.findViewById(R.id.tv_input_title);
        et_input_field = (EditText) v.findViewById(R.id.et_input_field);
        initVar();
        View v_input_cancel = v.findViewById(R.id.v_input_cancel);
        View v_input_confirm = v.findViewById(R.id.v_input_confirm);
        v_input_cancel.setOnClickListener(this);
        v_input_confirm.setOnClickListener(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        return builder.create();
    }

    private void initVar() {
        Bundle data = getArguments();
        if (data != null) {
            if (data.containsKey("input_title")) {
                tv_input_title.setText(data.getString("input_title"));
            }
            if (data.containsKey("input_hint")) {
                et_input_field.setHint(data.getString("input_hint"));
            }
            if (data.containsKey("input_text")) {
                et_input_field.setText(data.getString("input_text"));
                et_input_field.setSelection(et_input_field.getText().length());
            }
            if (data.containsKey("input_type")) {
                et_input_field.setInputType(data.getInt("input_type"));
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.v_input_cancel:
                if (inputCancelListener != null) {
                    inputCancelListener.onInputCancel();
                }
                break;

            case R.id.v_input_confirm:
                if (inputConfirmListener != null) {
                    inputConfirmListener.onInputConfirm(et_input_field.getText().toString());
                }
                break;
        }
        dismiss();
    }

    private InputCancelListener inputCancelListener;

    public void setOnInputCancelListener(InputCancelListener inputCancelListener) {
        this.inputCancelListener = inputCancelListener;
    }

    private InputConfirmListener inputConfirmListener;

    public void setOnInputConfirmListener(InputConfirmListener inputConfirmListener) {
        this.inputConfirmListener = inputConfirmListener;
    }

    public interface InputCancelListener {
        void onInputCancel();
    }

    public interface InputConfirmListener {
        void onInputConfirm(String rst);
    }
}
