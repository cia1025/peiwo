package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.Bind;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.service.CountDownService;
import me.peiwo.peiwo.util.HourGlassAgent;
import me.peiwo.peiwo.util.PWTimer;
import me.peiwo.peiwo.util.PWUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by fuhaidong on 14-9-24.
 */
public class RegisterActivity extends BaseActivity {

    private static final int REQUEST_CODE_FILLPHONENO = 1000;
    private static final int REQUEST_CODE_COUNTRY_CODE = 2000;
    @Bind(R.id.et_phoneno)
    EditText et_phoneno;
    @Bind(R.id.et_password)
    EditText et_password;
    @Bind(R.id.btn_submit)
    Button btn_submit;
    private String mPhoneCode;
    private boolean mIsPhoneNumChange;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    private void init() {
        setTitle(getString(R.string.set_up_account_pwd));
        et_phoneno.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        TextView tv_clause = (TextView) findViewById(R.id.tv_clause);
        tv_clause.setText(PWUtils.getClauselinks(this, "点击下一步按钮，即表示同意《陪我用户协议》", 13));
        tv_clause.setMovementMethod(LinkMovementMethod.getInstance());

        Observable<TextViewAfterTextChangeEvent> et_phone_observable = RxTextView.afterTextChangeEvents(et_phoneno).observeOn(AndroidSchedulers.mainThread());
        et_phone_observable.subscribe(textViewAfterTextChangeEvent -> {
            mIsPhoneNumChange = true;
        });
        Observable<TextViewAfterTextChangeEvent> et_pwd_observable = RxTextView.afterTextChangeEvents(et_password).observeOn(AndroidSchedulers.mainThread());
        Observable.combineLatest(et_phone_observable, et_pwd_observable, (textViewAfterTextChangeEvent, textViewAfterTextChangeEvent2) -> check()).subscribe(aBoolean -> {
            if (aBoolean) {
                btn_submit.setClickable(true);
                btn_submit.setBackgroundColor(getResources().getColor(R.color.valid_clickable_color));
            } else {
                btn_submit.setClickable(false);
                btn_submit.setBackgroundColor(getResources().getColor(R.color.invalid_clickable_color));
            }
        });
    }

    Rect phoneRect = new Rect();
    Rect passwordRect = new Rect();

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                et_phoneno.getGlobalVisibleRect(phoneRect);
                et_password.getGlobalVisibleRect(passwordRect);
                if (!phoneRect.contains((int) ev.getRawX(), (int) ev.getRawY()) && !passwordRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    et_phoneno.clearFocus();
                    et_password.clearFocus();
                    PWUtils.hideSoftKeyBoard(this);
                }

                break;

        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void left_click(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
        if (hourGlassAgent.getStatistics() && hourGlassAgent.getK37() == 0) {
            hourGlassAgent.setK37(1);
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            app.postK("k37");
        }
    }

    public void click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_submit:
                doNextStep();
                break;
            case R.id.ll_countries:
                startActivityForResult(new Intent(this, CountriesPhoneCodeActivity.class), REQUEST_CODE_COUNTRY_CODE);
                break;
        }
    }

    static class MyHandler extends Handler {
        WeakReference<RegisterActivity> activity_ref;

        public MyHandler(RegisterActivity activity) {
            activity_ref = new WeakReference<RegisterActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            RegisterActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing())
                return;
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    theActivity.dismissAnimLoading();
                    final String phoneNumber = PWUtils.getFormatPhoneNo(theActivity.mPhoneCode, theActivity.et_phoneno.getText().toString());
                    if (theActivity.phoneVerfi(msg.obj)) {
                        PWTimer timer = new PWTimer(60 * 1000, 1000) {
                            @Override
                            public void onFinish() {
                                DfineAction.verificationCodeMap.remove(phoneNumber);
                            }
                        };
                        timer.start();
                        DfineAction.verificationCodeMap.put(phoneNumber, timer);
                        theActivity.fetchVerifyCode();
                    }
                    break;
                case WHAT_DATA_RECEIVE_ERROR:
                    theActivity.dismissAnimLoading();
                    theActivity.showToast(theActivity, "请求失败");
                    break;
            }
            super.handleMessage(msg);
        }

    }

    private boolean phoneVerfi(Object obj) {
        JSONObject o = (JSONObject) obj;
        int state = o.optInt("state");
        if (state != -1) {
            showToast(this, "该手机号已被注册");
            return false;
        }
        return true;
    }

    private void fetchVerifyCode() {
        showAnimLoading();
        ArrayList<NameValuePair> param = new ArrayList<>();
        String phone = et_phoneno.getText().toString();
        String pwd = et_password.getText().toString();
        param.add(new BasicNameValuePair("phone", getPhoneCode() + phone));
        ApiRequestWrapper.openAPIGET(this, param, AsynHttpClient.API_GET_CAPTCHA, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                mIsPhoneNumChange = false;
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                    startService(new Intent(RegisterActivity.this, CountDownService.class));

                    Intent intent = new Intent(RegisterActivity.this, FillVerificationCodeActivity.class);
                    intent.putExtra("phone", getPhoneCode() + et_phoneno.getText().toString());
                    intent.putExtra("password", pwd);
                    startActivityForResult(intent, REQUEST_CODE_COUNTRY_CODE);
                });
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

    private void doNextStep() {
        HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
        if (hourGlassAgent.getStatistics() && hourGlassAgent.getK36() == 0) {
            hourGlassAgent.setK36(1);
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            app.postK("k36");
        }
        if (et_password.getText().length() > 20) {
            showToast(this, getString(R.string.password_length_limit));
            return;
        }
        boolean netAvailable = PWUtils.isNetWorkAvailable(this);
        if (!netAvailable) {
            showToast(this, getResources().getString(R.string.umeng_common_network_break_alert));
            return;
        }
        if (mIsPhoneNumChange) {
            fetchVerifyCode();
            Intent it = new Intent(CountDownService.STOP_SELF);
            EventBus.getDefault().post(it);
        } else {
            Intent intent = new Intent(RegisterActivity.this, FillVerificationCodeActivity.class);
            intent.putExtra("phone", getPhoneCode() + et_phoneno.getText().toString());
            intent.putExtra("password", et_password.getText().toString());
            startActivityForResult(intent, REQUEST_CODE_COUNTRY_CODE);
        }


//		if (DfineAction.verificationCodeMap.containsKey(phoneNumber)) {
//			startFillPhoneActivity(phoneNumber);
//		} else {
//			showAnimLoading("", false, false, false);
//			ApiRequestWrapper.captcha(this, phoneNumber, FillPhonenoActivity.CAPTCHA_TYPE_REGISTER, new MsgStructure() {
//						@Override
//						public void onReceive(JSONObject data) {
//							 Trace.i("phone data == " + data.toString());
//							 {"state":-1} 未注册的
//							mHandler.sendMessage(mHandler.obtainMessage(WHAT_DATA_RECEIVE, data));
//						}
//
//						@Override
//						public void onError(int error, Object ret) {
//							mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
//						}
//			});
//		}
    }

    @Override
    public void finish() {
        super.finish();
        Intent it = new Intent(CountDownService.STOP_SELF);
        EventBus.getDefault().post(it);
    }

    private String getPhoneCode() {
        StringBuilder phoneCode = new StringBuilder("86:");
        if (!TextUtils.isEmpty(mPhoneCode)) {
            phoneCode = new StringBuilder(mPhoneCode).append(":");
        }
        return phoneCode.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FILLPHONENO:
                    setResult(RESULT_OK);
                    finish();
                    break;
                case REQUEST_CODE_COUNTRY_CODE:
                    // 国家电话代码回调
                    // data.putExtra(COUNTRY, model.country);
                    // data.putExtra(PHONE_CODE, model.p_code);
//                    ((TextView) findViewById(R.id.tv_country)).setText(data
//                            .getStringExtra(CountriesPhoneCodeActivity.COUNTRY));
//                    ((TextView) findViewById(R.id.tv_pcode)).setText("(+"
//                            + mPhoneCode + ")");
                    if (data == null)
                        return;
                    StringBuilder country_code = new StringBuilder();
                    country_code.append("+ ").append(data.getStringExtra(CountriesPhoneCodeActivity.PHONE_CODE)).append(" ");
//                    country_code.append("(").append(data.getStringExtra(CountriesPhoneCodeActivity.PHONE_CODE)).append(")");
                    mPhoneCode = data.getStringExtra(CountriesPhoneCodeActivity.PHONE_CODE);
                    country_code.append(data.getStringExtra(CountriesPhoneCodeActivity.COUNTRY));
                    ((TextView) findViewById(R.id.tv_pcode)).setText(country_code);
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean check() {
        String phone = et_phoneno.getText().toString();
        if (TextUtils.isEmpty(phone) || phone.length() < 7) {
            return false;
        }
        String pwd = et_password.getText().toString();
        if (TextUtils.isEmpty(pwd) || pwd.length() < 6) {
            return false;
        }
        return true;
    }
}