package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.Md5Util;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.TitleUtil;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by fuhaidong on 14-9-30.
 */
public class ResetPhoneActivity extends BaseActivity {

    public static final String KEY_PHONENO = "pno";
    public static final String KEY_PWD = "pwd";
    private static final int REQUEST_CODE_VERFI_PHONE = 5000;
    private static final int REQUEST_CODE_COUNTRY_CODE = 6000;
    private TextView tv_phone_no;
    private String mPhoneNo;
    private EditText et_pwd;
    private EditText et_phoneno;
    private MyHandler mHandler;
    private String mPhoneCode;
    private TextView tv_country;
    private TextView tv_pcode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_phone);

        init();
    }

    private void init() {
        setTitleBar();
        mHandler = new MyHandler(this);
        mPhoneNo = getIntent().getStringExtra(KEY_PHONENO);
        mPhoneCode = PWUtils.getPhoneCode(mPhoneNo);
        tv_phone_no = (TextView) findViewById(R.id.tv_phone_no);
        //tv_phone_no.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        String realNo = PWUtils.getRealPhone(mPhoneNo);
        String source = String.format(Locale.getDefault(), "当前已绑定手机号  +%s  %s", mPhoneCode, realNo);
        SpannableString spannableString = new SpannableString(source);
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#00b8d0")), source.lastIndexOf(" "), source.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_phone_no.setText(spannableString);

        et_pwd = (EditText) findViewById(R.id.et_pwd);
        et_phoneno = (EditText) findViewById(R.id.et_phoneno);

        tv_country = (TextView) findViewById(R.id.tv_country);
        tv_pcode = (TextView) findViewById(R.id.tv_pcode);
        if (mPhoneNo.indexOf(":") > 0) {
            tv_country.setText(PWUtils.getCountryForPhoneCode(this, mPhoneCode));
            tv_pcode.setText("+" + mPhoneCode);
        } else {
            tv_country.setText("中国");
            tv_pcode.setText("+86");
        }

    }

    private void setTitleBar() {
        TitleUtil.setTitleBar(this, "更换手机号", v -> {
                    finish();
                }, "下一步", v -> {
                    if (check()) {
                        doNextStep();
                    }
                }
        );
    }

    private void doNextStep() {
        //account/resetphone 参数为phone, captcha
        showAnimLoading("", false, false, false);
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("phone", PWUtils.getFormatPhoneNo(mPhoneCode, getTextByView(et_phoneno))));
        params.add(new BasicNameValuePair("captcha_type", FillPhonenoActivity.CAPTCHA_TYPE_RESET_PHONE));
        params.add(new BasicNameValuePair("password", Md5Util.getMd5code(getTextByView(et_pwd))));
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
                        showToast(ResetPhoneActivity.this, msg);
                    }
                });
            }
        });
    }

    private boolean check() {
        if (TextUtils.isEmpty(getTextByView(et_pwd))) {
            showToast(this, "密码不能为空");
            return false;
        }
        if (TextUtils.isEmpty(getTextByView(et_phoneno))) {
            showToast(this, "手机号不能为空");
            return false;
        }
        if (getTextByView(et_phoneno).length() < 4 || getTextByView(et_phoneno).length() > 20) {
            showToast(this, "手机号不正确");
            return false;
        }
        return true;
    }

    static class MyHandler extends Handler {
        WeakReference<ResetPhoneActivity> activity_ref;

        public MyHandler(ResetPhoneActivity activity) {
            activity_ref = new WeakReference<ResetPhoneActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ResetPhoneActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) return;
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    theActivity.dismissAnimLoading();
                    Intent intent = new Intent(theActivity, ResetVerfiPhoneActivity.class);
                    intent.putExtra(KEY_PHONENO, PWUtils.getFormatPhoneNo(theActivity.mPhoneCode, theActivity.et_phoneno.getText().toString()));
                    intent.putExtra(KEY_PWD, theActivity.et_pwd.getText().toString());
                    theActivity.startActivityForResult(intent, REQUEST_CODE_VERFI_PHONE);
//                    if (theActivity.phoneVerfi(msg.obj)) {
//                        Intent intent = new Intent(theActivity, ResetVerfiPhoneActivity.class);
//                        intent.putExtra(KEY_PHONENO, theActivity.et_phoneno.getText().toString());
//                        intent.putExtra(KEY_PWD, theActivity.et_pwd.getText().toString());
//                        theActivity.startActivityForResult(intent, REQUEST_CODE_VERFI_PHONE);
//                    }
                    break;

                case WHAT_DATA_RECEIVE_ERROR:
                    theActivity.dismissAnimLoading();
                    //PPAlert.showToast(theActivity, "请求失败");
                    break;

            }
            super.handleMessage(msg);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_VERFI_PHONE:
                    setResult(RESULT_OK);
                    finish();
                    break;
                case REQUEST_CODE_COUNTRY_CODE:
                    tv_country.setText(data.getStringExtra(CountriesPhoneCodeActivity.COUNTRY));
                    mPhoneCode = data.getStringExtra(CountriesPhoneCodeActivity.PHONE_CODE);
                    tv_pcode.setText("(" + mPhoneCode + ")");
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void click(View v) {
        switch (v.getId()) {
            case R.id.ll_countries:
                startActivityForResult(new Intent(this, CountriesPhoneCodeActivity.class), REQUEST_CODE_COUNTRY_CODE);
                break;
        }
    }
}