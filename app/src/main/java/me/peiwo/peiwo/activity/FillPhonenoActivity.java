package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.Bind;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.HourGlassAgent;
import me.peiwo.peiwo.util.PWTimer;
import me.peiwo.peiwo.util.TitleUtil;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by fuhaidong on 14-9-24.
 * 找回密码第一步
 */
public class FillPhonenoActivity extends BaseActivity {

    public static final String KEY_PHONENO = "pno";
    public static final String KEY_PCODE = "pcode";
    public static final String KEY_VERFI_CODE = "verfi_code";
    public static final String KEY_REGISTER = "register";
    private static final int REQUEST_CODE_USERINIT = 1000;
    private static final int REQUEST_CODE_COUNTRY_CODE = 1001;
    private static final int REQUEST_SETUP_PWD = 1002;
    //private TextView tv_phone_no;
    private Button btn_getverifcode;
    private EditText et_verifcode;
    private EditText et_phoneno;
    @Bind(R.id.btn_submit)
    Button btn_submit;
    private AtomicBoolean isChecked = new AtomicBoolean(false);

    //private EditText et_pwd;

    private MyHandler mHandler;
    private String mPhoneCode;

    public static final String CAPTCHA_TYPE_REGISTER = "1";
    public static final String CAPTCHA_TYPE_FORGET_PWD = "2";
    public static final String CAPTCHA_TYPE_BIND_PHONE = "3";
    public static final String CAPTCHA_TYPE_RESET_PHONE = "4";
    private Subscription mSubscription;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_phoneno);

        init();
    }

    private void init() {
        mHandler = new MyHandler(this);
        setTitleBar();
        //tv_phone_no = (TextView) findViewById(R.id.tv_phone_no);
        //String source = String.format(Locale.getDefault(), "验证码已经发送至  +%s  %s", PWUtils.getPhoneCode(mPhoneNo), PWUtils.getRealPhone(mPhoneNo));
        //SpannableString spannableString = new SpannableString(source);
        //spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.c_blue2)), source.lastIndexOf(" "), source.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //tv_phone_no.setText(spannableString);
        btn_getverifcode = (Button) findViewById(R.id.btn_getverifcode);
        et_verifcode = (EditText) findViewById(R.id.et_verifcode);
        et_phoneno = (EditText) findViewById(R.id.et_phoneno);
        et_verifcode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        //et_pwd = (EditText) findViewById(R.id.et_pwd);
        //submitPhoneNo();
        //countTimeForVerifcode();
        Observable<TextViewAfterTextChangeEvent> et_phone_observable = RxTextView.afterTextChangeEvents(et_phoneno).observeOn(AndroidSchedulers.mainThread());
        Observable<TextViewAfterTextChangeEvent> et_code_observable = RxTextView.afterTextChangeEvents(et_verifcode).observeOn(AndroidSchedulers.mainThread());
        Observable.combineLatest(et_phone_observable, et_code_observable, (textViewAfterTextChangeEvent, textViewAfterTextChangeEvent2) -> check()).subscribe(aBoolean -> {
            if (aBoolean) {
                btn_submit.setClickable(true);
                btn_submit.setBackgroundColor(getResources().getColor(R.color.valid_clickable_color));
            } else {
                btn_submit.setClickable(false);
                btn_submit.setBackgroundColor(getResources().getColor(R.color.invalid_clickable_color));
            }
        });
    }

    private void submitPhoneNo() {
        if (TextUtils.isEmpty(et_phoneno.getText()) || et_phoneno.getText().toString().length() < 4 || et_phoneno.getText().toString().length() > 20) {
            showToast(this, "请输入正确的手机号");
            return;
        }
        showAnimLoading("", false, false, false);
        btn_getverifcode.setTextColor(Color.parseColor("#ffffff"));
        //boolean forgetPhoneNo = getIntent().getBooleanExtra(KEY_FLAG_FORGET_PHONE, false);
        if (mPhoneCode == null) {
            mPhoneCode = "86";
        }
        StringBuilder phone = new StringBuilder(mPhoneCode);
        phone.append(":").append(et_phoneno.getText().toString().trim());
        ApiRequestWrapper.captcha(this, phone.toString(), CAPTCHA_TYPE_FORGET_PWD, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                //Trace.i("phone data == " + data.toString());
                //{"state":-1} 未注册的
                mHandler.sendMessage(mHandler.obtainMessage(WHAT_DATA_RECEIVE, data));
                isChecked.set(true);
            }

            @Override
            public void onError(int error, Object ret) {
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                    showErrorToast(ret, getString(R.string.verification_code_unreach));

                });
            }
        });
    }

    private void setTitleBar() {
        TitleUtil.setTitleBar(this, "找回密码", v -> {
            finish();
        }, null);
    }

    private boolean check() {
        if (et_verifcode.getText().toString().length() < 4) {
            return false;
        }
        if (TextUtils.isEmpty(et_phoneno.getText()) || et_phoneno.getText().toString().length() < 7) {
            return false;
        }
//        if (TextUtils.isEmpty(et_pwd.getText()) || et_pwd.getText().toString().length() < 6) {
//            showToast(this, "请输入正确的密码");
//            return false;
//        }
        return true;
    }

    private void doNextStep() {
        if (!isChecked.get()) {
            Toast.makeText(FillPhonenoActivity.this, "请输入正确的验证码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (check()) {
            Intent intent = new Intent(this, ForgetPwdActivity.class);
            intent.putExtra(KEY_PCODE, mPhoneCode);
            intent.putExtra(KEY_PHONENO, et_phoneno.getText().toString());
            intent.putExtra(KEY_VERFI_CODE, et_verifcode.getText().toString());
            startActivityForResult(intent, REQUEST_SETUP_PWD);
        }
    }

//    private void doNextStep() {
////        mpost.set(false);
//        showAnimLoading("", false, false, false);
//        //resetPhoneNo ? CAPTCHA_TYPE_RESETPWD : CAPTCHA_TYPE_REGISTER
//        ApiRequestWrapper.forgetPhone(this, mPhoneNo, et_verifcode.getText().toString(), et_pwd.getText().toString(), new MsgStructure() {
//            @Override
//            public void onReceive(JSONObject data) {
//                PWUserModel modle = new PWUserModel(data);
//                if (UserManager.saveUser(FillPhonenoActivity.this, modle)) {
//                    mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_RESET_PHONENO);
//                } else {
//                    mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_SIGNUP_ERROR);
//                }
//            }
//
//            @Override
//            public void onError(int error, Object ret) {
//                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_SIGNUP_ERROR);
//            }
//        });
//
//    }

    static class MyHandler extends Handler {
        WeakReference<FillPhonenoActivity> activity_ref;

        public MyHandler(FillPhonenoActivity activity) {
            activity_ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final FillPhonenoActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) return;
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    theActivity.dismissAnimLoading();
                    if (msg.obj != null) {
                        if (theActivity.phoneVerfi(msg.obj)) {
                            PWTimer timer = new PWTimer(60 * 1000, 1000) {
                                @Override
                                public void onFinish() {
                                    DfineAction.verificationCodeMap.remove(theActivity.et_phoneno.getText().toString().trim());
                                }
                            };
                            timer.start();
                            DfineAction.verificationCodeMap.put(theActivity.et_phoneno.getText().toString().trim(), timer);
                            theActivity.countDown();
                        }
                    } else {
                        PWTimer timer = new PWTimer(60 * 1000, 1000) {
                            @Override
                            public void onFinish() {
                                DfineAction.verificationCodeMap.remove(theActivity.et_phoneno.getText().toString().trim());
                            }
                        };
                        timer.start();
                        DfineAction.verificationCodeMap.put(theActivity.et_phoneno.getText().toString().trim(), timer);
                        theActivity.countDown();
                    }
                    break;
                case WHAT_DATA_RECEIVE_ERROR:
                    theActivity.btn_getverifcode.setText("获取验证码");
                    theActivity.mpost.set(false);
                    theActivity.dismissAnimLoading();
                    theActivity.showToast(theActivity, "获取验证码失败");
                    break;
//                case WHAT_DATA_RECEIVE_SIGNUP:
//                    theActivity.dismissAnimLoading();
//
//                    theActivity.saveRegistInfo();
//
//                    Intent intent = new Intent(theActivity, UserDetailSettingActivity.class);
//                    intent.putExtra(UserDetailSettingActivity.ACTION, UserDetailSettingActivity.ACTION_PHONE_REGIST);
//                    intent.putExtra(KEY_PHONENO, theActivity.mPhoneNo);
//                    intent.putExtra(KEY_VERFICODE, theActivity.et_verifcode.getText().toString());
//                    intent.putExtra(KEY_PWD, theActivity.et_pwd.getText().toString());
//                    intent.putExtra(KEY_REGISTER, true);
//                    theActivity.startActivityForResult(intent, REQUEST_CODE_USERINIT);
//                    break;
//                case WHAT_DATA_RECEIVE_SIGNUP_ERROR:
//                    theActivity.dismissAnimLoading();
//                    showToast(theActivity, "请求失败，请检查验证码是否正确");
//                    break;

//                case WHAT_DATA_RECEIVE_RESET_PHONENO:
//                    theActivity.dismissAnimLoading();
//                    theActivity.saveRegistInfo();
//                    Intent result = new Intent();
//                    result.putExtra(Constans.SP_KEY_OPENID, theActivity.mPhoneNo);
//                    result.putExtra(Constans.SP_KEY_OPENTOKEN, theActivity.et_pwd.getText().toString());
//                    result.putExtra(Constans.SP_KEY_SOCIALTYPE, WelcomeActivity.SOCIAL_TYPE_PHONE);
//                    theActivity.setResult(RESULT_OK, result);
//                    theActivity.finish();
//                    break;
            }
            super.handleMessage(msg);
        }


    }

    private void saveRegistInfo() {
        //UserManager.saveOpenResultInPreference(this, mPhoneNo, et_pwd.getText().toString(), WelcomeActivity.SOCIAL_TYPE_PHONE);
        //saveOpenResultInPreference(mPhoneNo, et_pwd.getText().toString(), WelcomeActivity.SOCIAL_TYPE_PHONE);
    }

//    private void saveOpenResultInPreference(String openid, String opentoken, int socialType) {
//        SharedPreferencesUtil.putStringExtra(this, Constans.SP_KEY_OPENID, openid);
//        SharedPreferencesUtil.putStringExtra(this, Constans.SP_KEY_OPENTOKEN, opentoken);
//        SharedPreferencesUtil.putIntExtra(this, Constans.SP_KEY_SOCIALTYPE, socialType);
//    }

    private boolean phoneVerfi(Object obj) {
        boolean forgetPhoneNo = true;
        JSONObject o = (JSONObject) obj;
        int state = o.optInt("state");
        if (!forgetPhoneNo && state != -1) {
            showToast(this, "该手机号已被注册");
            return false;
        } else if (forgetPhoneNo && state == -1) {
            showToast(this, "该手机号尚未注册");
            return false;
        }
        return true;
    }

    private final AtomicBoolean mpost = new AtomicBoolean(true);

    private void countDown() {
        btn_getverifcode.setEnabled(false);
        btn_getverifcode.setTextColor(Color.parseColor("#ffffff"));
        final long countTime = 30;
        mSubscription = Observable.interval(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Long>() {

            @Override
            public void onCompleted() {
                if (!isUnsubscribed()) {
                    unsubscribe();
                }
                btn_getverifcode.setEnabled(true);
                btn_getverifcode.setText(getString(R.string.fetch_captcha));
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Long aLong) {
                long count = countTime - aLong;
                if (count > 0) {
                    btn_getverifcode.setText(String.format(Locale.getDefault(), "重新获取(%d%s)", count, "s")); //"重新获取(" + --timeCount + ")"
                } else {
                    onCompleted();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_USERINIT:
                    setResult(RESULT_OK);
                    finish();
                    break;
                case REQUEST_CODE_COUNTRY_CODE:
                    String pname = data.getStringExtra(CountriesPhoneCodeActivity.COUNTRY);
                    mPhoneCode = data.getStringExtra(CountriesPhoneCodeActivity.PHONE_CODE);
                    ((TextView) findViewById(R.id.tv_pcode)).setText("+" + mPhoneCode + "(" + pname + ")");
                    break;
                case REQUEST_SETUP_PWD:
                    Intent result = new Intent();
                    String phoneno = data.getStringExtra(Constans.SP_KEY_OPENID);
                    String pwd = data.getStringExtra(Constans.SP_KEY_OPENTOKEN);
                    String pcode = data.getStringExtra(Constans.SP_KEY_PCODE);
                    result.putExtra(Constans.SP_KEY_OPENID, phoneno);
                    result.putExtra(Constans.SP_KEY_OPENTOKEN, pwd);
                    result.putExtra(Constans.SP_KEY_PCODE, pcode);
                    result.putExtra(Constans.SP_KEY_SOCIALTYPE, WelcomeActivity.SOCIAL_TYPE_PHONE);
                    setResult(RESULT_OK, result);
                    finish();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_getverifcode:
                submitPhoneNo();
                HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
                if (hourGlassAgent.getStatistics() && hourGlassAgent.getK13() == 0) {
                    hourGlassAgent.setK13(1);
                    PeiwoApp app = (PeiwoApp) getApplicationContext();
                    app.postK("k13");
                }
                break;
            case R.id.btn_submit:
//                if (check()) {
                doNextStep();
//                }
                break;
            case R.id.ll_countries:
                startActivityForResult(new Intent(this, CountriesPhoneCodeActivity.class), REQUEST_CODE_COUNTRY_CODE);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        super.onDestroy();
    }
}