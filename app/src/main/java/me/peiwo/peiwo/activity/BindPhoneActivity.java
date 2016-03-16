package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import io.rong.imlib.RongIMClient;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fuhaidong on 14-9-30. 未绑定过，开始绑定
 */
public class BindPhoneActivity extends BaseActivity {
    private static final int REQUEST_CODE_COUNTRY_CODE = 1000;
    private static final int WHAT_DATA_RECEIVE_CAPTCHA = 1001;
    private static final int WHAT_DATA_RECEIVE_ERROR_CAPTCHA_ERROR = 1002;
    private static final int WHAT_DATA_RECEIVE_BIND = 1003;
    private static final int WHAT_DATA_RECEIVE_ERROR_BIND = 1004;

    // public static final String BIND_ACTION = "action";
    // public static final int BIND_ACTION_BIND = 0; //绑定
    // public static final int BIND_ACTION_UPDATE_BIND = 1; //修改绑定

    private EditText et_phoneno;
    private EditText et_pwd;
    private EditText et_verifcode;
    private TextView tv_alert;
    private MyHandler mHandler;
    private static final int REQUEST_CODE_FILL_BIND = 4000;
    private String mPhoneCode;
    private Button btn_getverifcode;
    private final AtomicBoolean mpost = new AtomicBoolean(true);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bindphone);
        mHandler = new MyHandler(this);
        init();
    }

    private void init() {
        setTitleBar();
        et_phoneno = (EditText) findViewById(R.id.et_phoneno);
        btn_getverifcode = (Button) findViewById(R.id.btn_getverifcode);
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        et_verifcode = (EditText) findViewById(R.id.et_verifcode);
        tv_alert = (TextView) findViewById(R.id.tv_alert);
        tv_alert.setVisibility(View.VISIBLE);
        // TextView tv_clause = (TextView) findViewById(R.id.tv_clause);
        // tv_clause.setText(PWUtils.getClauselinks(this));
        // tv_clause.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setTitleBar() {
        TitleUtil.setTitleBar(this, "绑定手机号", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        }, null);
    }

    private void doNextStep() {
//account/bindphone 参数为phone，captcha，password
        showAnimLoading("", false, false, false);
        String phoneNumber = PWUtils.getFormatPhoneNo(mPhoneCode, et_phoneno.getText().toString());
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("phone", phoneNumber));
        params.add(new BasicNameValuePair("captcha", getTextByView(et_verifcode)));
        params.add(new BasicNameValuePair("password", Md5Util.getMd5code(getTextByView(et_pwd))));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_ACCOUNT_BINDPHONE, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                mHandler.sendMessage(mHandler.obtainMessage(WHAT_DATA_RECEIVE_BIND, data));
            }

            @Override
            public void onError(int error, Object ret) {
                final String msg;
                if (error == AsynHttpClient.PW_RESPONSE_DATA_ALREADY_EXIST) {
                    msg = "该手机号已被注册";
                } else {
                    msg = "请输入正确的验证码";
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast(BindPhoneActivity.this, msg);
                    }
                });
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR_BIND);
            }
        });
    }

    private void startFillPhoneActivity(String phoneNumber) {
        Intent intent = new Intent(this, FillForBindPhoneActivity.class);
        intent.putExtra(FillForBindPhoneActivity.KEY_PHONENO, phoneNumber);
        startActivityForResult(intent, REQUEST_CODE_FILL_BIND);
    }

    private boolean check() {
        if (getTextByView(et_phoneno).length() == 0) {
            showToast(this, "手机号不能为空");
            return false;
        }
//        if (getTextByView(et_phoneno).length() != 11) {
//            showToast(this, "手机号码填写错误");
//            return false;
//        }
//        if (getTextByView(et_phoneno).length() < 4 || getTextByView(et_phoneno).length() > 20) {
//            showToast(this, "请输入正确的手机号");
//            return false;
//        }
        if (getTextByView(et_pwd).length() < 6) {
            showToast(this, "密码至少为6位");
            return false;
        }
        if (getTextByView(et_verifcode).length() != 6) {
            showToast(this, "验证码不正确");
            return false;
        }
        return true;
    }

    class MyHandler extends Handler {
        WeakReference<BindPhoneActivity> activity_ref;

        public MyHandler(BindPhoneActivity activity) {
            activity_ref = new WeakReference<BindPhoneActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BindPhoneActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing())
                return;
            int what = msg.what;
            final String phoneNumber = PWUtils.getFormatPhoneNo(mPhoneCode, et_phoneno.getText().toString());
            switch (what) {
                case WHAT_DATA_RECEIVE_BIND:
                    dismissAnimLoading();
                    UserManager.updatePhone(theActivity, phoneNumber);
                    setResult(RESULT_OK);
                    finish();
                    break;
                case WHAT_DATA_RECEIVE_ERROR_BIND:
                    dismissAnimLoading();
                    break;
                case WHAT_DATA_RECEIVE_CAPTCHA:
                    dismissAnimLoading();

                    PWTimer timer = new PWTimer(60 * 1000, 1000) {
                        @Override
                        public void onFinish() {
                            DfineAction.verificationCodeMap.remove(phoneNumber);
                        }
                    };
                    timer.start();
                    DfineAction.verificationCodeMap.put(phoneNumber, timer);
                    countTimeForVerifcode();
                    break;
                case WHAT_DATA_RECEIVE_ERROR_CAPTCHA_ERROR:
                    btn_getverifcode.setEnabled(true);
                    btn_getverifcode.setText("获取验证码");
                    mpost.set(false);
                    dismissAnimLoading();
                    //showToast(theActivity, "获取验证码失败");
                    break;
                case WHAT_DATA_RECEIVE:

                    break;
                case WHAT_DATA_RECEIVE_ERROR:
                    dismissAnimLoading();
                    break;

            }
            super.handleMessage(msg);
        }

    }


    private void countTimeForVerifcode() {
        mpost.set(true);
        int timeCount = 0;
        String phoneNumber = PWUtils.getFormatPhoneNo(mPhoneCode, et_phoneno.getText().toString());
        if (DfineAction.verificationCodeMap.containsKey(phoneNumber)) {
            timeCount = 60 - DfineAction.verificationCodeMap.get(phoneNumber).count;
        } else {
            timeCount = 60;
        }
        final int count = timeCount;
        mHandler.post(new Runnable() {
            int tCount = count;

            @Override
            public void run() {
                btn_getverifcode.setEnabled(false);
                btn_getverifcode.setText(String.format(Locale.getDefault(), "重新获取(%d%s)", --tCount, "s")); //"重新获取(" + --timeCount + ")"
                if (tCount > 0 && mpost.get()) {
                    mHandler.postDelayed(this, 1000);
                } else {
                    btn_getverifcode.setEnabled(true);
                    btn_getverifcode.setText("获取验证码");
                }
            }
        });
    }

    // private boolean phoneVerfi(Object obj) {
    // JSONObject o = (JSONObject) obj;
    // int state = o.optInt("state");
    // if (state != -1) {
    // PPAlert.showToast(this, "该手机号已被绑定");
    // return false;
    // }
    // return true;
    // }
    private HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
    public void click(View v) {
        switch (v.getId()) {
            case R.id.ll_countries:
                startActivityForResult(new Intent(this,
                                CountriesPhoneCodeActivity.class),
                        REQUEST_CODE_COUNTRY_CODE);
                break;
            case R.id.btn_submit:
                if (check()) {
                    doNextStep();
                    if (hourGlassAgent.getStatistics() && hourGlassAgent.getK14() == 0) {
                        hourGlassAgent.setK14(1);
                        PeiwoApp app = (PeiwoApp) getApplicationContext();
                        app.postK("k14");
                    }
                }


                break;
            case R.id.btn_getverifcode:
                if (TextUtils.isEmpty(getTextByView(et_phoneno))) {
                    showToast(this, "请先填写手机号");
                    return;
                }
                getCaptcha();
                //HourGlassAgent mHourGlassAgent = HourGlassAgent.getInstance();
                if (hourGlassAgent.getStatistics() && hourGlassAgent.getK12() == 0) {
                    hourGlassAgent.setK12(1);
                    PeiwoApp app = (PeiwoApp) getApplicationContext();
                    app.postK("k12");
                }
                //重新获取验证码统计,判断第一次统计的k12
                if (hourGlassAgent.getStatistics() && hourGlassAgent.getK12() == 1) {
                    hourGlassAgent.setK12(2);
                    hourGlassAgent.setK15(1);
                    PeiwoApp app = (PeiwoApp) getApplicationContext();
                    app.postK("k15");
                }
                break;
            case R.id.tv_verificode_not_receive:
                startActivity(new Intent(this, VerifiCodeNotReceiveActivity.class));
                break;
        }
    }

    private void getCaptcha() {
        // account/usercaptcha 应用内使用的验证码 参数为phone和captcha_type,
        // 如果需要传密码，参数为password
        String phoneNumber = PWUtils.getFormatPhoneNo(mPhoneCode, et_phoneno.getText().toString());
//        if (DfineAction.verificationCodeMap.containsKey(phoneNumber)) {
//            startFillPhoneActivity(phoneNumber);
//        } else {
        showAnimLoading("", false, false, false);
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("phone", phoneNumber));
        params.add(new BasicNameValuePair("captcha_type",
                FillPhonenoActivity.CAPTCHA_TYPE_BIND_PHONE));
        ApiRequestWrapper.openAPIGET(this, params,
                AsynHttpClient.API_ACCOUNT_USERCAPTCHA, new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                        mHandler.sendMessage(mHandler.obtainMessage(WHAT_DATA_RECEIVE_CAPTCHA, data));
                    }

                    @Override
                    public void onError(int error, Object ret) {
                        final String msg;
                        if (error == AsynHttpClient.PW_RESPONSE_DATA_ALREADY_EXIST) {
                            msg = "该手机号已被注册";
                        } else {
                            if (ret instanceof JSONObject) {
                                msg = ((JSONObject) ret).optString("msg", "unknown");
                            } else {
                                msg = "";
                            }
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showToast(BindPhoneActivity.this, msg);
                            }
                        });
                        mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR_CAPTCHA_ERROR);
                    }
                });
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FILL_BIND:
                    // 绑定回调, 保存手机号已经在FillForBindPhoneActivity保存
                    setResult(RESULT_OK);
                    finish();
                    break;
                case REQUEST_CODE_COUNTRY_CODE:
                    //((TextView) findViewById(R.id.tv_country)).setText();
                    mPhoneCode = data
                            .getStringExtra(CountriesPhoneCodeActivity.PHONE_CODE);
                    String pname = data
                            .getStringExtra(CountriesPhoneCodeActivity.COUNTRY);
                    ((TextView) findViewById(R.id.tv_pcode)).setText("+" + mPhoneCode + "(" + pname + ")");
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void finish() {
        if (hourGlassAgent.getStatistics() && hourGlassAgent.getK16() == 0) {
            hourGlassAgent.setK16(1);
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            app.postK("k16");
        }
        super.finish();
    }
}