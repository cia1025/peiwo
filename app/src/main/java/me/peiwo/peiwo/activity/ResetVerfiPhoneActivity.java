package me.peiwo.peiwo.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.Md5Util;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.util.UserManager;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fuhaidong on 14-9-30.
 */
public class ResetVerfiPhoneActivity extends BaseActivity {

    private static final int WHAT_DATA_RECEIVE_VERFI = 5000;
    private static final int WHAT_DATA_RECEIVE_ERROR_VERFI = 6000;
    private TextView tv_phone_no;
    private String mPhoneNo;
    private Button btn_getverifcode;
    private EditText et_verifcode;

    private MyHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verfi_phone);

        init();
    }

    private void init() {
        setTitleBar();
        mHandler = new MyHandler(this);
        mPhoneNo = getIntent().getStringExtra(ResetPhoneActivity.KEY_PHONENO);
        btn_getverifcode = (Button) findViewById(R.id.btn_getverifcode);
        et_verifcode = (EditText) findViewById(R.id.et_verifcode);
        et_verifcode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        tv_phone_no = (TextView) findViewById(R.id.tv_phone_no);
        String source = String.format(Locale.getDefault(), "验证码已发送至  %s  %s", PWUtils.getPhoneCode(mPhoneNo), PWUtils.getRealPhone(mPhoneNo));
        SpannableString spannableString = new SpannableString(source);
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), source.lastIndexOf(" "), source.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_phone_no.setText(spannableString);
        countTimeForVerifcode();
    }

    private final AtomicBoolean mpost = new AtomicBoolean(true);

    private void countTimeForVerifcode() {
        mpost.set(true);
        mHandler.post(new Runnable() {
            int timeCount = 60;

            @Override
            public void run() {
                btn_getverifcode.setEnabled(false);
                btn_getverifcode.setText(String.format(Locale.getDefault(), "重新获取(%d%s)", --timeCount, "s")); //"重新获取(" + --timeCount + ")"
                if (timeCount > 0 && mpost.get()) {
                    mHandler.postDelayed(this, 1000);
                } else {
                    btn_getverifcode.setEnabled(true);
                    btn_getverifcode.setText("获取验证码");
                }
            }
        });
    }

    private void setTitleBar() {
        TitleUtil.setTitleBar(this, "验证手机号", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }, "完成", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (check()) {
                            doNextStep();
                        }
                    }
                }
        );
    }

    private void doNextStep() {
        //account/resetphone 参数为phone, captcha
        showAnimLoading("", false, false, false);
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("phone", mPhoneNo));
        params.add(new BasicNameValuePair("captcha", getTextByView(et_verifcode)));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_ACCOUNT_RESETPHONE, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                mHandler.sendMessage(mHandler.obtainMessage(WHAT_DATA_RECEIVE_VERFI, data));
            }

            @Override
            public void onError(int error, Object ret) {
//                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR_VERFI);
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                    showErrorToast(ret, getString(R.string.verification_incorrect));
                });
            }
        });
    }

    private boolean check() {
        if (TextUtils.isEmpty(getTextByView(et_verifcode))) {
            showToast(this, "请输入验证码");
            return false;
        }
        return true;
    }

    public void click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_getverifcode:
                submitPhoneNo();
                break;
        }
    }

    private void submitPhoneNo() {
        showAnimLoading("", false, false, false);
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("phone", mPhoneNo));
        params.add(new BasicNameValuePair("captcha_type", FillPhonenoActivity.CAPTCHA_TYPE_RESET_PHONE));
        params.add(new BasicNameValuePair("password", Md5Util.getMd5code(getIntent().getStringExtra(ResetPhoneActivity.KEY_PWD))));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_ACCOUNT_USERCAPTCHA, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                mHandler.sendMessage(mHandler.obtainMessage(WHAT_DATA_RECEIVE, data));
            }

            @Override
            public void onError(int error, Object ret) {
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
                final String msg;
                if (error == AsynHttpClient.PW_RESPONSE_DATA_ALREADY_EXIST) {
                    msg = "该手机号已被注册";
                } else if (error == AsynHttpClient.PW_RESPONSE_DATA_NOT_AVAILABLE) {
                    msg = "密码错误，请重试！";
                } else {
                    msg = "获取验证码出错，请重试！";
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast(ResetVerfiPhoneActivity.this, msg);
                    }
                });
            }
        });
    }


    class MyHandler extends Handler {
        WeakReference<ResetVerfiPhoneActivity> activity_ref;

        public MyHandler(ResetVerfiPhoneActivity activity) {
            activity_ref = new WeakReference<ResetVerfiPhoneActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ResetVerfiPhoneActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) return;
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    theActivity.dismissAnimLoading();
                    //if (theActivity.phoneVerfi(msg.obj))
                    theActivity.countTimeForVerifcode();
                    break;

                case WHAT_DATA_RECEIVE_ERROR:
                    theActivity.btn_getverifcode.setEnabled(true);
                    theActivity.btn_getverifcode.setText("获取验证码");
                    theActivity.mpost.set(false);
                    theActivity.dismissAnimLoading();
                    showToast(theActivity, "获取验证码失败");
                    break;

                case WHAT_DATA_RECEIVE_VERFI:
                    theActivity.dismissAnimLoading();
                    UserManager.updatePhone(theActivity, theActivity.mPhoneNo);
                    theActivity.setResult(RESULT_OK);
                    theActivity.finish();
                    break;
                case WHAT_DATA_RECEIVE_ERROR_VERFI:
                    theActivity.dismissAnimLoading();
                    showToast(theActivity, "请求失败");
                    break;


            }
            super.handleMessage(msg);
        }
    }
}