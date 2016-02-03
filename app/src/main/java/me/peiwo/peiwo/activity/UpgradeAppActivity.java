package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.PWActionConfig;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.service.SynchronizedService;
import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class UpgradeAppActivity extends BaseActivity implements OnClickListener {

    private TextView tv_title;
    private TextView tv_content;
    private String update_url;
    private static final int UPDATE_CONTENT = 1000;
    private MyHandler mHandler;
    private boolean isForced = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PeiwoApp.getApplication().existence_update_dialog = true;
        setContentView(R.layout.layout_upgrade_app_ui);
        mHandler = new MyHandler(this);
        initView();

    }

    @Override
    protected void onStart() {
        super.onStart();
        setConent();
    }

    @Override
    protected void onDestroy() {
        PeiwoApp.getApplication().existence_update_dialog = false;
        super.onDestroy();
    }

    private void initView() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_content = (TextView) findViewById(R.id.tv_content);
        TextView tv_wait = (TextView) findViewById(R.id.tv_wait);
        TextView tv_download = (TextView) findViewById(R.id.tv_download);
        tv_wait.setOnClickListener(this);
        tv_download.setOnClickListener(this);

        String dataStr = getIntent().getStringExtra("data_str");
        if (TextUtils.isEmpty(dataStr)) return;
        try {
            JSONObject data = new JSONObject(dataStr);
            String forced = data.optString("forced");
            if ("1".equals(forced)) {
                tv_wait.setVisibility(View.GONE);
                isForced = true;
            }
            this.update_url = data.optString("update_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setConent() {
        showAnimLoading();
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        ApiRequestWrapper.openAPIGET(getApplicationContext(), params, AsynHttpClient.API_UPDATE, new MsgStructure() {

            @Override
            public void onReceive(JSONObject data) {
                Message msg = mHandler.obtainMessage();
                msg.obj = data;
                msg.what = UPDATE_CONTENT;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onError(int error, Object ret) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_wait:
                finish();
                break;

            case R.id.tv_download:
                Intent intent = new Intent(this, SynchronizedService.class);
                intent.setAction(PWActionConfig.ACTION_DOWNLOAD_NEW_VER);
                intent.putExtra("download_url", this.update_url);
                this.startService(intent);
                finish();
                break;
        }
    }

    static class MyHandler extends Handler {
        WeakReference<UpgradeAppActivity> activity_ref;

        public MyHandler(UpgradeAppActivity activity) {
            activity_ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            UpgradeAppActivity theActivity = activity_ref.get();
            switch (msg.what) {
                case UPDATE_CONTENT:
                    theActivity.dismissAnimLoading();
                    JSONObject data = (JSONObject) msg.obj;
                    theActivity.tv_content.setText(data.optString("content"));
                    theActivity.tv_title.setText(data.optString("title"));
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //禁用返回键
            if (isForced) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}