package me.peiwo.peiwo.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.Md5Util;
import me.peiwo.peiwo.util.PWTimer;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.util.UserManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

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

/**
 * Created by fuhaidong on 14-9-30.
 */
public class FillForBindPhoneActivity extends BaseActivity {

    public static final String KEY_PHONENO = "pno";
    private static final int WHAT_DATA_RECEIVE_BIND = 4000;
    private static final int WHAT_DATA_RECEIVE_ERROR_BIND = 5000;
    private String mPhoneNo;
    private Button btn_getverifcode,btn_submit;
    private EditText et_verifcode;
    private EditText et_pwd;
    private TextView tv_phone_no;
    private MyHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_phoneno);

        init();
    }

    private void init() {
        setTitleBar();
        mHandler = new MyHandler(this);
        mPhoneNo = getIntent().getStringExtra(KEY_PHONENO);
        tv_phone_no = (TextView) findViewById(R.id.tv_phone_no);
        String source = String.format(Locale.getDefault(), "验证码已经发送至  +%s  %s", PWUtils.getPhoneCode(mPhoneNo), PWUtils.getRealPhone(mPhoneNo));
        SpannableString spannableString = new SpannableString(source);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.c_blue2)), source.lastIndexOf(" "), source.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_phone_no.setText(spannableString);
        btn_getverifcode = (Button) findViewById(R.id.btn_getverifcode);
        btn_submit=(Button)findViewById(R.id.btn_submit);
        btn_submit.setText("完成");
        et_verifcode = (EditText) findViewById(R.id.et_verifcode);
        et_verifcode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        countTimeForVerifcode();
    }

    private final AtomicBoolean mpost = new AtomicBoolean(true);

    private void countTimeForVerifcode() {
        mpost.set(true);
        int timeCount = 0;
        if (DfineAction.verificationCodeMap.containsKey(mPhoneNo)) {
        	timeCount = 60 - DfineAction.verificationCodeMap.get(mPhoneNo).count;
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

	private void setTitleBar() {
		TitleUtil.setTitleBar(this, "登录密码", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		}, null);
	}

    private void doNextStep() {
        //account/bindphone 参数为phone，captcha，password
        showAnimLoading("", false, false, false);
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("phone", mPhoneNo));
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
                        showToast(FillForBindPhoneActivity.this, msg);
                    }
                });
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR_BIND);
            }
        });
    }

    private boolean check() {
        if (TextUtils.isEmpty(et_verifcode.getText().toString()) || et_verifcode.getText().toString().length() != 6) {
            showToast(this, "请输入正确的验证码");
            return false;
        }
        if (TextUtils.isEmpty(et_pwd.getText()) || et_pwd.getText().toString().length() < 6) {
            showToast(this, "请输入正确的密码");
            return false;
        }
        return true;
    }

    class MyHandler extends Handler {
        WeakReference<FillForBindPhoneActivity> activity_ref;

        public MyHandler(FillForBindPhoneActivity activity) {
            activity_ref = new WeakReference<FillForBindPhoneActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            FillForBindPhoneActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) return;
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    dismissAnimLoading();
                    if (msg.obj != null) {
                        if (phoneVerfi(msg.obj)) {
        					PWTimer timer = new PWTimer(60 * 1000, 1000) {
        						@Override
        						public void onFinish() {
        							DfineAction.verificationCodeMap.remove(mPhoneNo);
        						}
        					};
        					timer.start();
        					DfineAction.verificationCodeMap.put(mPhoneNo, timer);
        					countTimeForVerifcode();
                        }
                    } else {
                    	PWTimer timer = new PWTimer(60 * 1000, 1000) {
    						@Override
    						public void onFinish() {
    							DfineAction.verificationCodeMap.remove(mPhoneNo);
    						}
    					};
    					timer.start();
    					DfineAction.verificationCodeMap.put(mPhoneNo, timer);
                        countTimeForVerifcode();
                    }
                    break;
                case WHAT_DATA_RECEIVE_ERROR:
                    btn_getverifcode.setEnabled(true);
                    btn_getverifcode.setText("获取验证码");
                    mpost.set(false);
                    dismissAnimLoading();
                    showToast(theActivity, "获取验证码失败");
                    break;
                case WHAT_DATA_RECEIVE_BIND:
                    dismissAnimLoading();
                    UserManager.updatePhone(theActivity, theActivity.mPhoneNo);
                    setResult(RESULT_OK);
                    finish();
                    break;
                case WHAT_DATA_RECEIVE_ERROR_BIND:
                    dismissAnimLoading();
                    break;

            }
            super.handleMessage(msg);
        }


    }

    private boolean phoneVerfi(Object obj) {
        JSONObject o = (JSONObject) obj;
        int state = o.optInt("state");
        if (state != -1) {
            showToast(this, "该手机号已被绑定");
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
            case R.id.btn_submit:
            	if(check()){
            		doNextStep();
            	}
            	break;
        }
    }

    private void submitPhoneNo() {
        showAnimLoading("", false, false, false);
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("phone", mPhoneNo));
        params.add(new BasicNameValuePair("captcha_type", FillPhonenoActivity.CAPTCHA_TYPE_BIND_PHONE));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_ACCOUNT_USERCAPTCHA, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                mHandler.sendMessage(mHandler.obtainMessage(WHAT_DATA_RECEIVE, data));
            }

            @Override
            public void onError(int error, Object ret) {
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
            }
        });
    }
}