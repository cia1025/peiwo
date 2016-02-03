package me.peiwo.peiwo.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.NetUtil;
import me.peiwo.peiwo.util.Md5Util;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.util.UserManager;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ModifyPassWordActivty extends BaseActivity {

    private EditText et_old_pwd;
    private EditText et_new_pwd;
    private EditText et_repeat_pwd;
    private MyHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_password);
        init();
    }

    private void init() {
        setTitleBar();
        mHandler = new MyHandler(this);
        et_old_pwd = (EditText) findViewById(R.id.et_old_pwd);
        et_new_pwd = (EditText) findViewById(R.id.et_new_pwd);
        et_repeat_pwd = (EditText) findViewById(R.id.et_repeat_pwd);
    }

    private void setTitleBar() {
        TitleUtil.setTitleBar(this, "修改登录密码", new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        }, "保存", new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (check())
                    doModifyPassWord();
            }
        });
    }

    private boolean check() {
        if (TextUtils.isEmpty(getTextByView(et_old_pwd))) {
            showToast(this, "请输入旧密码");
            return false;
        }
        if (TextUtils.isEmpty(getTextByView(et_new_pwd))) {
            showToast(this, "请输入新密码");
            return false;
        }
        if (TextUtils.isEmpty(getTextByView(et_repeat_pwd))) {
            showToast(this, "请重复输入新密码");
            return false;
        }

        if (getTextByView(et_new_pwd).length() < 6) {
            showToast(this, "新密码不能少于6位");
            return false;
        }
        if (!getTextByView(et_new_pwd).equals(getTextByView(et_repeat_pwd))) {
            showToast(this, "新密码必须与重复密码一致");
            return false;
        }
        return true;
    }


    private void doModifyPassWord() {
        showAnimLoading("", false, false, false);
        //account/resetpassword 参数为old旧密码，new新密码
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("old", Md5Util.getMd5code(getTextByView(et_old_pwd))));
        params.add(new BasicNameValuePair("new", Md5Util.getMd5code(getTextByView(et_new_pwd))));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_ACCOUNT_RESETPASSWORD, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE);
            }

            @Override
            public void onError(int error, Object ret) {
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
            }
        });
    }


    static class MyHandler extends Handler {
        WeakReference<ModifyPassWordActivty> activity_ref;

        public MyHandler(ModifyPassWordActivty activity) {
            activity_ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ModifyPassWordActivty theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) return;
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    theActivity.dismissAnimLoading();
                    theActivity.showToast(theActivity, "修改密码成功");
                    UserManager.savePWD(theActivity, theActivity.getTextByView(theActivity.et_new_pwd));
                    theActivity.finish();
                    break;

                case WHAT_DATA_RECEIVE_ERROR:
                    theActivity.dismissAnimLoading();
                    PeiwoApp app = (PeiwoApp) theActivity.getApplicationContext();
                    if (app.getNetType() == NetUtil.NO_NETWORK) {
                        theActivity.showToast(theActivity, "网络连接失败");
                    } else {
                        theActivity.showToast(theActivity, "修改失败，请确认原始密码是否输入正确");
                    }
                    break;

            }
            super.handleMessage(msg);
        }


    }
}
