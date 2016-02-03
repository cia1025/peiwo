package me.peiwo.peiwo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.PWActionConfig;
import me.peiwo.peiwo.util.UmengStatisticsAgent;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class BaseActivity extends BaseFragmentActivity {

    private FinishReceiver finishreceiver;
    private DispatchHandler dispatchHandler;

    private TextView title;
    private TextView btn_right;
    private ImageView iv_right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UmengStatisticsAgent.openActivityDurationTrack(false);
        registNeedFinishReceiver();
        dispatchHandler = new DispatchHandler(this);
    }

    @Override
    public void setTitle(CharSequence title) {
        if (this.title != null) {
            this.title.setText(title);
        }
    }

    protected void setRightText(CharSequence sequence) {
        if (this.btn_right != null) {
            this.btn_right.setVisibility(View.VISIBLE);
            btn_right.setText(sequence);
        }
    }

    protected void hideRightText() {
        if (this.btn_right != null) {
            this.btn_right.setVisibility(View.GONE);
        }
    }


    protected void setRightImage(@DrawableRes int resId) {
        if (this.iv_right != null) {
            this.iv_right.setVisibility(View.VISIBLE);
            this.iv_right.setImageResource(resId);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        title = (TextView) findViewById(R.id.title);
        btn_right = (TextView) findViewById(R.id.btn_right);
        iv_right = (ImageView) findViewById(R.id.iv_right);
    }

    private String className = "";
    public static final String[] ACTIVITYS = {
            RealCallActivity.class.getSimpleName(),
            WildCatCallActivity.class.getSimpleName(), MainActivity.class.getSimpleName()};

    private void registNeedFinishReceiver() {
        //Trace.i("activity name == " + getClass().getSimpleName());
        className = getClass().getSimpleName();
        finishreceiver = new FinishReceiver();
        registerReceiver(finishreceiver, new IntentFilter(PWActionConfig.ACTION_FINISH_ALL));
    }

    class FinishReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (PWActionConfig.ACTION_FINISH_ALL.equals(intent.getAction())) {
                int type = intent.getIntExtra("finish_type", 0);
                if (type == 0) {
                    boolean need = true;
                    for (String name : ACTIVITYS) {
                        if (className.equals(name)) {
                            need = false;
                            break;
                        }
                    }
                    if (need) {
                        finish();
                    }
                } else if (type == 1) {
                    if (!className.equals(WelcomeActivity.class.getSimpleName())) {
                        finish();
                        //Log.i("test", "%%%%%%%%%%%%%%%%%%%%%%" + className + " finish()");
                    }
                }
            }
        }
    }

    @Override
    public void finish() {
        //Trace.i("activity 销毁了" + getClass().getSimpleName());
        super.finish();
    }

    @Override
    protected void onDestroy() {
        if (finishreceiver != null)
            unregisterReceiver(finishreceiver);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        UmengStatisticsAgent.onResume(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        UmengStatisticsAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    protected String getTextByView(TextView view) {
        if (view == null) return null;
        return view.getText().toString();
    }

    static class DispatchHandler extends Handler {
        WeakReference<BaseActivity> activity_ref;

        public DispatchHandler(BaseActivity activity) {
            activity_ref = new WeakReference<BaseActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) {
                return;
            }
            theActivity.handle_message(msg.what, msg.obj != null ? (JSONObject) msg.obj : null);
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

    public void left_click(View v) {
        finish();
    }

    public void right_click(View v) {

    }

}
