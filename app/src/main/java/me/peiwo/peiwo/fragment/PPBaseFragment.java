package me.peiwo.peiwo.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.Toast;
import me.peiwo.peiwo.util.UmengStatisticsAgent;
import me.peiwo.peiwo.widget.AnimLoadingDialog;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class PPBaseFragment extends Fragment {

    public static final int WHAT_DATA_RECEIVE = 1000;
    public static final int WHAT_DATA_RECEIVE_ERROR = 2000;
    private DispatchHandler dispatchHandler;
    AnimLoadingDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dispatchHandler = new DispatchHandler(this);
    }

    protected void showAnimLoading(String alert, boolean canWatchOutsideTouch,
                                   boolean dimBehindEnabled) {
        dialog = new AnimLoadingDialog(getActivity())
                .setWatchOutsideTouch(canWatchOutsideTouch);
        // dialog.setDimBehindEnabled(dimBehindEnabled); //设计说默认都要全透明显示
        dialog.show();
    }

    protected void showAnimLoading(String alert, boolean canWatchOutsideTouch,
                                   boolean dimBehindEnabled, boolean cancelable) {
        dialog = new AnimLoadingDialog(getActivity())
                .setWatchOutsideTouch(canWatchOutsideTouch);
        dialog.setCancelable(cancelable);
        dialog.setCanceledOnTouchOutside(cancelable);
        dialog.show();
    }

    protected void dismissAnimLoading() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

    public void scrollToTop() {

    }


//    @Override
//    public void onResume() {
//        super.onResume();
//        UmengStatisticsAgent.onPageStart(PAGE_NAME);
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        UmengStatisticsAgent.onPageEnd(PAGE_NAME);
//    }

    static class DispatchHandler extends Handler {
        WeakReference<PPBaseFragment> fragment_ref;

        public DispatchHandler(PPBaseFragment fragment) {
            fragment_ref = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            PPBaseFragment theFragment = fragment_ref.get();
            if (theFragment == null || theFragment.isDetached()) {
                return;
            }
            theFragment.handle_message(msg.what, msg.obj != null ? (JSONObject) msg.obj : null);
        }
    }

    protected void distributeMessage(int message_id, JSONObject obj) {
        if (dispatchHandler != null) {
            if (obj == null) {
                dispatchHandler.sendEmptyMessage(message_id);
            } else {
                dispatchHandler.sendMessage(dispatchHandler.obtainMessage(message_id, obj));
            }
        }
    }


    protected void handle_message(int message_id, JSONObject obj) {

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

    @Override
    public void onResume() {
        super.onResume();
        UmengStatisticsAgent.onPageStart(getPageName());
    }

    @Override
    public void onPause() {
        super.onPause();
        UmengStatisticsAgent.onPageEnd(getPageName());
    }

    protected String getPageName() {
        return getClass().getSimpleName();
    }

}
