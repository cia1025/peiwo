package me.peiwo.peiwo.widget;

import me.peiwo.peiwo.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by Dong Fuhai on 2014-07-25 15:26.
 *
 * @modify:
 */
public class RequestCallView extends Dialog implements View.OnClickListener {

    private EditText et_request_des;
    private ImageView iv_my_face;
    private ImageView iv_target_face;

    public RequestCallView(Context context) {
        super(context, R.style.AnimDialogLoading);
    }

    public RequestCallView(Context context, int theme) {
        super(context, R.style.AnimDialogLoading);
    }

    protected RequestCallView(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dlg_request_call);
        findViewById(R.id.btn_send_request).setOnClickListener(this);
        findViewById(R.id.btn_cancle).setOnClickListener(this);
        et_request_des = (EditText) findViewById(R.id.et_request_des);
        et_request_des.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        iv_my_face = (ImageView) findViewById(R.id.iv_my_face);
        iv_target_face = (ImageView) findViewById(R.id.iv_target_face);
    }

    public interface OnRequestClickListener {
        public void onRequestClick(String request_des);
    }

    private OnRequestClickListener listener;

    public void setOnRequestClickListener(OnRequestClickListener listener) {
        this.listener = listener;
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
        	ex.printStackTrace();
        }
    }

    public void displayFaces(String my_face_url, String target_face_url) {
        ImageLoader loader = ImageLoader.getInstance();
        loader.displayImage(my_face_url, iv_my_face);
        loader.displayImage(target_face_url, iv_target_face);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send_request:
                if (listener != null) {
                    String result = et_request_des.getText().toString();
                    listener.onRequestClick(result);
                }
                dismiss();
                break;
            case R.id.btn_cancle:
                dismiss();
                break;
        }
    }
}
