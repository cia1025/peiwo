package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.service.CountDownService;
import me.peiwo.peiwo.util.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by gaoxiang on 16/2/4.
 */
public class FillVerificationCodeActivity extends BaseActivity {

    @Bind(R.id.tv_phone_number)
    TextView tv_phone_number;
    @Bind(R.id.btn_getverifcode)
    Button btn_getverifcode;
    @Bind(R.id.et_verifcode)
    EditText et_verifcode;
    @Bind(R.id.btn_submit)
    Button btn_submit;
    private String mPhoneNum;
    private String mPassword;
    private static final long COUNT_DOWN_SEC = 30;
    private Intent mCountIntent;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_verification_code);
        init();
        mCountIntent = new Intent(this, CountDownService.class);
    }

    private void init() {
        setTitle(getString(R.string.get_verification_code));
        EventBus.getDefault().register(this);
        btn_getverifcode.setTextColor(Color.parseColor("#ffffff"));
        mPhoneNum = getIntent().getStringExtra("phone");
        mPassword = getIntent().getStringExtra("password");
        CustomLog.d("mPhoneNum is : " + mPhoneNum);
        tv_phone_number.setText(new StringBuilder("+").append(mPhoneNum));
        clickToGetcodeListener();
        RxTextView.afterTextChangeEvents(et_verifcode).observeOn(AndroidSchedulers.mainThread()).subscribe(textViewAfterTextChangeEvent -> {
            if (textViewAfterTextChangeEvent.editable().length() >= 4) {
                btn_submit.setClickable(true);
                btn_submit.setBackgroundColor(getResources().getColor(R.color.valid_clickable_color));
            } else {
                btn_submit.setClickable(false);
                btn_submit.setBackgroundColor(getResources().getColor(R.color.invalid_clickable_color));
            }
        });
    }

    private void clickToGetcodeListener() {
        RxView.clicks(findViewById(R.id.btn_getverifcode)).throttleFirst(200, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
            btn_getverifcode.setEnabled(false);
            HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
            if (hourGlassAgent.getStatistics() && hourGlassAgent.getK40() == 0) {
                hourGlassAgent.setK40(1);
                PeiwoApp app = (PeiwoApp) getApplicationContext();
                app.postK("k40");
            }
            fetchVerifyCode();
            startCountService();
        });
    }

    @OnClick(R.id.btn_submit)
    void submit() {
        checkVerificationCode(et_verifcode.getText().toString());
        HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
        if (hourGlassAgent.getStatistics() && hourGlassAgent.getK38() == 0) {
            hourGlassAgent.setK38(1);
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            app.postK("k38");
        }
    }

    @OnClick(R.id.btn_veri_code_unreach)
    void goUnreachPage() {
        Intent it = new Intent(this, VerifiCodeNotReceiveActivity.class);
        it.putExtra("phone", mPhoneNum);
        startActivity(it);
        HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
        if (hourGlassAgent.getStatistics() && hourGlassAgent.getK41() == 0) {
            hourGlassAgent.setK41(1);
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            app.postK("k41");
        }
    }

    Rect verifRect = new Rect();

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                et_verifcode.getGlobalVisibleRect(verifRect);
                if (!verifRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    et_verifcode.clearFocus();
                    PWUtils.hideSoftKeyBoard(this);
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void startCountService() {
        mCountIntent = new Intent(this, CountDownService.class);
        startService(mCountIntent);
    }

    @Override
    public void finish() {
        super.finish();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(Intent intent) {
        if (intent == null || intent.getAction().equals("")) {
            return;
        }

        if (intent.getAction().equals(CountDownService.COUNT_DOWN_ACTION)) {
            long count = COUNT_DOWN_SEC - intent.getLongExtra("count", 0);
            if (count == 0) {
                btn_getverifcode.setEnabled(true);
                btn_getverifcode.setText(getString(R.string.fetch_captcha));
            } else {
                btn_getverifcode.setEnabled(false);
                btn_getverifcode.setText(String.format(Locale.getDefault(), "重新获取(%d%s)", count, "s")); //"重新获取(" + --timeCount + ")"
            }
        }
    }


    private void fetchVerifyCode() {
        ArrayList<NameValuePair> param = new ArrayList<>();
        param.add(new BasicNameValuePair("phone", mPhoneNum));
        ApiRequestWrapper.openAPIGET(this, param, AsynHttpClient.API_GET_CAPTCHA, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("countDown. data is : " + data);
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("countDown. error is : " + ret);
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    showErrorToast(ret, getString(R.string.verification_code_unreach));
                });
            }
        });
    }

    private void checkVerificationCode(String code) {
        if (mPhoneNum != null && !TextUtils.isEmpty(mPhoneNum)) {
            showAnimLoading();
            ArrayList<NameValuePair> param = new ArrayList<>();
            param.add(new BasicNameValuePair("phone", mPhoneNum));
            param.add(new BasicNameValuePair("password", mPassword));
            param.add(new BasicNameValuePair("captcha", code));
            ApiRequestWrapper.openAPIPOST(this, param, AsynHttpClient.API_ACCOUNT_SIGNUP, new MsgStructure() {
                @Override
                public void onReceive(JSONObject data) {
                    CustomLog.d("checkVerificationCode() data is : " + data);
                    Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(jobj -> {
                        dismissAnimLoading();
                        SharedPreferencesUtil.putIntExtra(FillVerificationCodeActivity.this, Constans.SP_KEY_UID, data.optInt("uid"));
                        saveRegistInfo(data.optString("session_data"));
                        PWUserModel model = new PWUserModel(data);
                        UserManager.saveUser(FillVerificationCodeActivity.this, model);
                        Intent it = new Intent(FillVerificationCodeActivity.this, Bind3rdPartyAccountActivity.class);
                        startActivity(it);
                    });
                }

                @Override
                public void onError(int error, Object ret) {
                    CustomLog.d("checkVerificationCode() error is : " + ret);
                    Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                        dismissAnimLoading();
                        showErrorToast(ret, getString(R.string.verification_incorrect));
                    });
                }
            });
        }
    }

    private void saveRegistInfo(String session_data) {
        UserManager.saveOpenResultInPreference(this, mPhoneNum, mPassword, WelcomeActivity.SOCIAL_TYPE_PHONE, session_data);
    }

    @Override
    public void left_click(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
        if (hourGlassAgent.getStatistics() && hourGlassAgent.getK39() == 0) {
            hourGlassAgent.setK39(1);
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            app.postK("k39");
        }
    }
}
