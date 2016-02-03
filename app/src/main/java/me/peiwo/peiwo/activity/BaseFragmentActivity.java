package me.peiwo.peiwo.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import me.peiwo.peiwo.widget.AnimLoadingDialog;

public class BaseFragmentActivity extends AppCompatActivity {
    public static final int ERROR_MSG_NETWORK_NOT_AVAILABLE = 100;
    public static final int WHAT_DATA_RECEIVE = 1000;
    public static final int WHAT_DATA_RECEIVE_ERROR = 2000;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private AnimLoadingDialog dialog;

    public void showAnimLoading() {
        showAnimLoading("", false, false, false);
    }

    public void showAnimLoading(String alert, boolean canWatchOutsideTouch,
                                boolean dimBehindEnabled, boolean cancelable) {
        if (dialog != null && dialog.isShowing()) {
            return;
        }
        dialog = new AnimLoadingDialog(this)
                .setWatchOutsideTouch(canWatchOutsideTouch);
        dialog.setCancelable(cancelable);
        dialog.setCanceledOnTouchOutside(cancelable);
        dialog.show();
        /*******这种方式是否更好？*****/
//        View v = getWindow().getDecorView();
//        if (v instanceof FrameLayout) {
//            ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleInverse);
//            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            params.gravity = Gravity.CENTER;
//            FrameLayout bg = new FrameLayout(this);
//            bg.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            bg.addView(bar, params);
//            bg.setId(R.id.pb_loading);
//            bg.setClickable(true);
//            ((FrameLayout) v).addView(bg);
//        }

    }

    public void dismissAnimLoading() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        dialog = null;

//        View v = getWindow().getDecorView();
//        if (v instanceof FrameLayout){
//            View bar = v.findViewById(R.id.pb_loading);
//            if (bar != null){
//                ((FrameLayout) v).removeView(bar);
//            }
//        }

    }

    protected void doRPCCommplete(Object obj) {

    }

    private Toast mToast = null;

    public void showToast(Context context, String msg) {
        if (TextUtils.isEmpty(msg) || context == null || (context instanceof Activity && ((Activity) context).isFinishing())) {
            return;
        }
        if (mToast == null) {
            mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        }
        mToast.setText(msg);
        mToast.show();
    }

    public void click(View v) {

    }
}
