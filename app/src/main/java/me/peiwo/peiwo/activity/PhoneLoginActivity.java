package me.peiwo.peiwo.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.Bind;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.PWActionConfig;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.util.*;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * Created by fuhaidong on 14-9-25.
 */
public class PhoneLoginActivity extends BaseActivity {
    public static final int SOCIAL_TYPE_PHONE = 3;
    private static final int REQUEST_CODE_HASINIT = 5000;
    private static final int REQUEST_CODE_FORGETPWD = 5001;
    private static final int REQUEST_CODE_COUNTRY_CODE = 6000;
    private static final int REQUEST_CODE_USERINIT = 1000;
    private static final int REQUEST_CODE_BIND_PHONE = 4000;
    private static final int SOCIAL_TYPE_WEIBO = 1;
    private static final int SOCIAL_TYPE_QQ = 2;
    private static final int SOCIAL_TYPE_WECHAT = 4;
    @Bind(R.id.btn_submit)
    Button btn_submit;
    HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
    private EditText et_num;
    private EditText et_pwd;
    private MyHandler mHandler;
    private String mPhoneCode = "86";
    private TextView tv_pcode;
    //private TextView tv_pname;
    //private LinearLayout ll_phone_no;
    private AuthInfo mWeiboAuth;
    private SsoHandler mSsoHandler;
    private Tencent mTencent;
    private boolean islogin = false;
    private String openid2, opentoken2;
    private IWXAPI mWXApi;
    private WXReceiver wxReceiver;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonelogin);
        mWXApi = WXAPIFactory.createWXAPI(this, Constans.WX_APP_ID, true);
        mWXApi.registerApp(Constans.WX_APP_ID);

        wxReceiver = new WXReceiver();
        registerReceiver(wxReceiver, new IntentFilter(PWActionConfig.ACTION_WXSHARE_SUCCESS));
        mWeiboAuth = new AuthInfo(this, Constans.WEIBO_APP_KEY, Constans.WEIBO_REDIRECT_URL, Constans.WEIBO_SCOPE);
        mTencent = Tencent.createInstance(Constans.QQ_APP_ID, this);
        init();
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(Constans.NOTIFY_ID_WILDCAT_BACKGROUND);
        mNotifyMgr.cancel(Constans.NOTIFY_ID_CALL_BACKGROUND);
        mNotifyMgr.cancel(Constans.NOTIFY_ID_IM_MESSAGE);

        HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
//        if (mHourGlassAgent.getStatistics() && mHourGlassAgent.getK6() == 0) {
//            mHourGlassAgent.setK6(1);
//            PeiwoApp app = (PeiwoApp) getApplicationContext();
//            app.postK("k6");
//        }

        submitClick(hourGlassAgent);
        Observable<TextViewAfterTextChangeEvent> et_phone_observable = RxTextView.afterTextChangeEvents(et_num);
        Observable<TextViewAfterTextChangeEvent> et_pwd_observable = RxTextView.afterTextChangeEvents(et_pwd);
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

    private void submitClick(HourGlassAgent hourGlassAgent) {
        RxView.clicks(findViewById(R.id.btn_submit)).throttleFirst(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aVoid -> {
            boolean netAvailable = PWUtils.isNetWorkAvailable(this);
            if (!netAvailable) {
                showToast(this, getResources().getString(R.string.umeng_common_network_break_alert));
                return;
            }
            String p_no = et_num.getText().toString();
            if (!"86".equals(mPhoneCode)) {
                //海外手机号登录
                p_no = PWUtils.getFormatPhoneNo(mPhoneCode, et_num.getText().toString());
            }
            doPhoneLogin(p_no, et_pwd.getText()
                    .toString(), false);
            if (hourGlassAgent.getStatistics() && hourGlassAgent.getK18() == 0) {
                hourGlassAgent.setK18(1);
                PeiwoApp app = (PeiwoApp) getApplicationContext();
                app.postK("k18");
            }
        });
    }

    @Override
    protected void onDestroy() {
        mWXApi.unregisterApp();
        if (wxReceiver != null) {
            unregisterReceiver(wxReceiver);
            wxReceiver = null;
        }
        super.onDestroy();
    }

    private void signInWX(String wxcode) {
        showAnimLoading();
        ApiRequestWrapper.signin(this, String.valueOf(SOCIAL_TYPE_WECHAT), "", wxcode, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                    PWUserModel modle = new PWUserModel(data);
                    UserManager.saveUser(PhoneLoginActivity.this, modle);
                    PeiwoApp app = (PeiwoApp) getApplicationContext();
                    if (hourGlassAgent.getStatistics() && hourGlassAgent.getK30() == 0) {
                        hourGlassAgent.setK30(1);
                        app.postK("k30");
                    }
                    doHandleLogin();
                });
            }

            @Override
            public void onError(int error, Object ret) {
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                    String tips = "failed " + error;
                    if (ret instanceof JSONObject) {
                        tips = ((JSONObject) ret).optString("msg");
                    }
                    showToast(PhoneLoginActivity.this, tips);
                });
            }
        });
    }

    private void init() {
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //PWUtils.hideSoftKeyBoard(this)
        setTitleBar();
//        String openid = SharedPreferencesUtil.getStringExtra(this,
//                Constans.SP_KEY_OPENID, "");
//        String opentoken = SharedPreferencesUtil.getStringExtra(this,
//                Constans.SP_KEY_OPENTOKEN, "");
//        int socialType = SharedPreferencesUtil.getIntExtra(this,
//                Constans.SP_KEY_SOCIALTYPE, -1);
//        if (!TextUtils.isEmpty(openid) && !TextUtils.isEmpty(opentoken)
//                && socialType != -1) {
//
//            if (UserManager.getUserState(PhoneLoginActivity.this) != UserManager.STATE_UNINITED) {
//                onAuthLoginSuccess(socialType, openid, opentoken);
//            }
//        }

        mHandler = new MyHandler(this);
        //ll_phone_no = (LinearLayout) findViewById(R.id.ll_phone_no);
        et_num = (EditText) findViewById(R.id.et_num);
//        et_num.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    ll_phone_no.setBackgroundResource(R.drawable.pwtextfield_activated_holo_light);
//                } else {
//                    ll_phone_no.setBackgroundResource(R.drawable.pwtextfield_disabled_holo_light);
//                }
//            }
//        });
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        tv_pcode = (TextView) findViewById(R.id.tv_pcode);
        //tv_pname = (TextView) findViewById(R.id.tv_pname);
        ((TextView) findViewById(R.id.tv_overseas_login)).setText(Html.fromHtml("<u>海外手机号登录</u>"));
        //.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

        tv_pcode.setText("+" + mPhoneCode + "(中国)");
        //tv_pname.setText("中国");
    }

    private void setTitleBar() {
        TitleUtil.setTitleBar(this, getString(R.string.log_on), v -> {
            if (hourGlassAgent.getStatistics() && hourGlassAgent.getK19() == 0) {
                hourGlassAgent.setK19(1);
                PeiwoApp app = (PeiwoApp) getApplicationContext();
                app.postK("k19");
            }
            finish();
        }, getString(R.string.log_on_meet_problem_question), v -> {
//            startActivityForResult(new Intent(this, FillPhonenoActivity.class),
//                    REQUEST_CODE_FORGETPWD);
            startActivityForResult(new Intent(this, LogOnProblemActivity.class), REQUEST_CODE_FORGETPWD);
            if (hourGlassAgent.getStatistics() && hourGlassAgent.getK33() == 0) {
                hourGlassAgent.setK33(1);
                PeiwoApp app = (PeiwoApp) getApplicationContext();
                app.postK("k33");
            }
        });
    }

    public void click(View v) {
        int id = v.getId();
        boolean netAvailable = PWUtils.isNetWorkAvailable(this);
        switch (id) {
//            case R.id.btn_submit:
//
//                break;
//            case R.id.tv_forget_pwd:
//                // 忘记密码
//                startActivityForResult(new Intent(this, FillPhonenoActivity.class),
//                        REQUEST_CODE_FORGETPWD);
//                if (mHourGlassAgent.getStatistics() && mHourGlassAgent.getK23() == 0) {
//                    mHourGlassAgent.setK23(1);
//                    PeiwoApp app = (PeiwoApp) getApplicationContext();
//                    app.postK("k23");
//                }
//                break;
            case R.id.tv_pcode:
                startActivityForResult(new Intent(this, CountriesPhoneCodeActivity.class), REQUEST_CODE_COUNTRY_CODE);
                break;
            case R.id.tv_qq_login:
                if (!netAvailable) {
                    showToast(this, getResources().getString(R.string.umeng_common_network_break_alert));
                    return;
                }
//			Intent qqIntent = new Intent(Constans.SP_KEY_LOGINTYPE);
//			qqIntent.putExtra("logintype", "qq");
//			sendBroadcast(qqIntent);
                if (hourGlassAgent.getStatistics() && hourGlassAgent.getK20() == 0) {
                    hourGlassAgent.setK20(1);
                    PeiwoApp app = (PeiwoApp) getApplicationContext();
                    app.postK("k20");
                }
                doQQLogin();
                break;
            case R.id.tv_weibo_login:
                if (!netAvailable) {
                    showToast(this, getResources().getString(R.string.umeng_common_network_break_alert));
                    return;
                }
//			Intent weiboIntent = new Intent(Constans.SP_KEY_LOGINTYPE);
//			weiboIntent.putExtra("logintype", "weibo");
//			sendBroadcast(weiboIntent);
                doWeiboLogin();
                if (hourGlassAgent.getStatistics() && hourGlassAgent.getK22() == 0) {
                    hourGlassAgent.setK22(1);
                    PeiwoApp app = (PeiwoApp) getApplicationContext();
                    app.postK("k22");
                }
                break;
            case R.id.tv_weichat_login:
                //微信注册
                if (!netAvailable) {
                    showToast(this, getResources().getString(R.string.umeng_common_network_break_alert));
                    return;
                }
                if (!mWXApi.isWXAppInstalled()) {
                    showToast(this, getString(R.string.wechat_not_installed));
                    return;
                }
                SendAuth.Req wxReq = new SendAuth.Req();
                wxReq.scope = "snsapi_userinfo";
                wxReq.state = PWUtils.getDeviceId(this);
                mWXApi.sendReq(wxReq);
                //
                if (hourGlassAgent.getStatistics() && hourGlassAgent.getK21() == 0) {
                    hourGlassAgent.setK21(1);
                    PeiwoApp app = (PeiwoApp) getApplicationContext();
                    app.postK("k21");
                }
                break;
            default:
                break;
        }
    }

    private void doWeiboLogin() {
        mSsoHandler = new SsoHandler(this, mWeiboAuth);
        mSsoHandler.authorize(new AuthListener());
    }

    private void onAuthLoginSuccess(int social_type, String social_uid, String access_token) {
//        showAnimLoading();
//        ApiRequestWrapper.signin(PhoneLoginActivity.this, String.valueOf(social_type), String.valueOf(social_uid), access_token, new MsgStructure() {
//            @Override
//            public void onReceive(JSONObject data) {
//                PWUserModel modle = new PWUserModel(data);
//                if (UserManager.saveUser(PhoneLoginActivity.this, modle)) {
//                    mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE);
//                } else {
//                    mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
//                    mTencent.logout(PhoneLoginActivity.this);
//                }
//            }
//;
//            @Override
//            public void onError(int error, Object ret) {
//                Message msg = mHandler.obtainMessage();
//                msg.what = WHAT_DATA_RECEIVE_ERROR;
//                msg.arg1 = error;
//                mHandler.sendMessage(msg);
//            }
//        });

        showAnimLoading();
        ApiRequestWrapper.signin(this, String.valueOf(social_type), social_uid, access_token, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                    PWUserModel modle = new PWUserModel(data);
                    UserManager.saveUser(PhoneLoginActivity.this, modle);
                    PeiwoApp app = PeiwoApp.getApplication();
                    if (social_type == SOCIAL_TYPE_WEIBO) {
                        if (hourGlassAgent.getStatistics() && hourGlassAgent.getK32() == 0) {
                            hourGlassAgent.setK32(1);
                            app.postK("k32");
                        }
                    } else if (social_type == SOCIAL_TYPE_QQ) {
                        if (hourGlassAgent.getStatistics() && hourGlassAgent.getK31() == 0) {
                            hourGlassAgent.setK31(1);
                            app.postK("k31");
                        }
                    }
                    doHandleLogin();
                });
            }

            @Override
            public void onError(int error, Object ret) {
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                    showErrorToast(ret, getString(R.string.logon_failed));

                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_USERINIT:
                    doRegistResult();
                    break;
                case REQUEST_CODE_FORGETPWD:
                    String phoneno = data.getStringExtra(Constans.SP_KEY_OPENID);
                    String pcode = data.getStringExtra(Constans.SP_KEY_PCODE);
                    String pwd = data.getStringExtra(Constans.SP_KEY_OPENTOKEN);
                    if (!"86".equals(pcode)) {
                        phoneno = PWUtils.getFormatPhoneNo(pcode, phoneno);
                    }
                    if (!TextUtils.isEmpty(phoneno) && !TextUtils.isEmpty(pwd)) {
                        doPhoneLogin(phoneno, pwd, true);
                    }
                    break;
                case REQUEST_CODE_COUNTRY_CODE:
                    mPhoneCode = data.getStringExtra(CountriesPhoneCodeActivity.PHONE_CODE);
                    String pname = data.getStringExtra(CountriesPhoneCodeActivity.COUNTRY);
                    tv_pcode.setVisibility(View.VISIBLE);
                    tv_pcode.setText("+" + mPhoneCode + "(" + pname + ")");
                    et_num.setHint("请输入手机号");
                    break;
                case REQUEST_CODE_BIND_PHONE:
                    doRegistResult();
                    break;
                case REQUEST_CODE_HASINIT:
                    doRegistResult();
                    break;
                default:
                    if (mSsoHandler != null) {
                        mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
                    }
                    break;

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean check() {
        String phone = et_num.getText().toString();
        if (TextUtils.isEmpty(phone) || phone.length() < 3 || phone.length() > 20) {
            return false;
        }
        String pwd = et_pwd.getText().toString();
        if (TextUtils.isEmpty(pwd) || pwd.length() < 6) {
            return false;
        }
        return true;
    }

    private void saveRegistInfo() {
        String num = et_num.getText().toString();
        String pwd = et_pwd.getText().toString();
        if (TextUtils.isEmpty(num) || TextUtils.isEmpty(pwd))
            return;
        UserManager.saveOpenResultInPreference(this, et_num.getText()
                        .toString(), et_pwd.getText().toString(),
                WelcomeActivity.SOCIAL_TYPE_PHONE);
    }

    private void doRegistResult() {
        doHandleLogin();
    }

    private void doHandleLogin() {
        // 微博或者QQ登陆时未绑定手机
        String phone = UserManager.getUserPhone(getApplicationContext());

        if (UserManager.getUserState(this) == UserManager.STATE_UNINITED) {
            //未完善用户信息
            dismissAnimLoading();
            Intent intent = new Intent(this, Bind3rdPartyAccountActivity.class);
            if (TextUtils.isEmpty(phone)) {
                //未绑定手机
                intent.putExtra("nophone", true);
            }
            startActivityForResult(intent, REQUEST_CODE_USERINIT);
        } else {
            dismissAnimLoading();
            reportTokenOrRegID();
            islogin = true;
            EventBus.getDefault().post(new Intent(PWActionConfig.ACTION_LOGIN_IN));
            startActivity(new Intent(this, MainActivity.class));
            findViewById(R.id.btn_submit).setClickable(false);
            finish();
        }
    }

    private void doQQLogin() {
        mTencent.login(this, Constans.QQ_SCOPE, new IUiListener() {
            @Override
            public void onComplete(Object o) {
                try {
                    JSONObject jo = (JSONObject) o;
                    //PPAlert.showToast(LoginActivity.this, "json=="+jo.toString());
                    String openid = jo.has("openid") ? jo.getString("openid") : openid2;
                    String access_token = jo.has("access_token") ? jo.getString("access_token") : opentoken2;
                    if (TextUtils.isEmpty(openid) || TextUtils.isEmpty(access_token)) {
                        mTencent.logout(PhoneLoginActivity.this);
                        showToast(PhoneLoginActivity.this, "qq登录失败");
                        return;
                    }
                    openid2 = openid;
                    opentoken2 = access_token;
                    UserManager.saveOpenResultInPreference(PhoneLoginActivity.this, openid, access_token, SOCIAL_TYPE_QQ);
                    //saveOpenResultInPreference(openid, access_token, SOCIAL_TYPE_QQ);
                    onAuthLoginSuccess(SOCIAL_TYPE_QQ, openid, access_token);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(UiError uiError) {
                showToast(PhoneLoginActivity.this, uiError.errorMessage);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    @Override
    public void finish() {
        if (islogin) {
            TcpProxy.getInstance().connectionTcp();
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            app.getRongCloudTokenAndConnect();
            app.setUpAtuserAndNodisturb();
        }
        super.finish();
    }

    private void reportTokenOrRegID() {
        PeiwoApp app = (PeiwoApp) getApplicationContext();
        String token = SharedPreferencesUtil.getStringExtra(this, Constans.SP_KEY_XGTOKEN, "");
        String regId = SharedPreferencesUtil.getStringExtra(this, Constans.SP_KEY_XIAOMIREGID, "");
        if (!TextUtils.isEmpty(token)) {
            app.reportPushToken(token, Constans.PLATFORM_XG);
            return;
        }
        if (!TextUtils.isEmpty(regId)) {
            app.reportPushToken(regId, Constans.PLATFORM_XIAOMI);
        }
    }

    private void doPhoneLogin(String phonenum, String pwd,
                              final boolean isresetpwd) {
        if (phonenum.contains("+")) {
            phonenum = phonenum.substring(1, phonenum.length());
        }
        showAnimLoading();
        ApiRequestWrapper.signin(this,
                String.valueOf(SOCIAL_TYPE_PHONE), phonenum,
                pwd, new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                        // Trace.i("login data == " + data.toString());
                        Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                            PWUserModel modle = new PWUserModel(data);
                            if (UserManager.saveUser(PhoneLoginActivity.this, modle)) {
                                if (!isresetpwd) {
                                    saveRegistInfo();
                                }
                                doHandleLogin();
                            } else {
                                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
                            }
                            dismissAnimLoading();
                        });

                    }

                    @Override
                    public void onError(int error, Object ret) {
                        Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                            dismissAnimLoading();
                            showErrorToast(ret, getString(R.string.logon_failed));
                        });
                    }
                });
    }

    static class MyHandler extends Handler {
        WeakReference<PhoneLoginActivity> activity_ref;

        public MyHandler(PhoneLoginActivity activity) {
            activity_ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            PhoneLoginActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing())
                return;
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    //theActivity.dismissAnimLoading();
                    theActivity.doHandleLogin();
                    break;
                case WHAT_DATA_RECEIVE_ERROR:
                    theActivity.dismissAnimLoading();
                    int errorCode = msg.arg1;
                    String tipString = "连接失败,请稍后重试(" + errorCode + ")";
                    if (errorCode != AsynHttpClient.ERROR_MSG_NETWORK_NOT_AVAILABLE) {
                        theActivity.mTencent.logout(theActivity);
                    }
                    switch (errorCode) {
                        case AsynHttpClient.PW_RESPONSE_DATA_NOT_AVAILABLE:
                            tipString = "此账号已被封禁";
                            break;
                        case AsynHttpClient.ERROR_MSG_NETWORK_NOT_AVAILABLE:
                            tipString = "网络连接失败";
                            break;
                        case AsynHttpClient.DATA_NOT_EXISTS:
                            tipString = "账号密码错误";
                            break;
                        case AsynHttpClient.PW_RESPONSE_OPERATE_ERROR:
                            tipString = "账号不存在";
                            break;
                    }
                    theActivity.showToast(theActivity, tipString);
                    break;
            }
            super.handleMessage(msg);
        }

    }

    class WXReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (btn_submit == null || btn_submit.getVisibility() != View.VISIBLE) {
                return;
            }
            HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            String wxcode = intent.getStringExtra("wxcode");
            if (!TextUtils.isEmpty(wxcode)) {
                signInWX(wxcode);
                //
                if (hourGlassAgent.getStatistics() && hourGlassAgent.getK8() == 0) {
                    hourGlassAgent.setK8(1);
                    app.postK("k8");
                }
            } else {
                if (hourGlassAgent.getStatistics() && hourGlassAgent.getK9() == 0) {
                    hourGlassAgent.setK9(1);
                    app.postK("k9");
                }
            }
        }
    }

    class AuthListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle bundle) {
            Oauth2AccessToken mAccessToken = Oauth2AccessToken.parseAccessToken(bundle);
            if (mAccessToken.isSessionValid()) {
                UserManager.saveOpenResultInPreference(PhoneLoginActivity.this, mAccessToken.getUid(), mAccessToken.getToken(), SOCIAL_TYPE_WEIBO);
                //saveOpenResultInPreference(mAccessToken.getUid(), mAccessToken.getToken(), SOCIAL_TYPE_WEIBO);
                onAuthLoginSuccess(SOCIAL_TYPE_WEIBO, mAccessToken.getUid(), mAccessToken.getToken());
            } else {
            }
        }

        @Override
        public void onWeiboException(final WeiboException e) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showToast(PhoneLoginActivity.this, e.getMessage());
                }
            });
        }

        @Override
        public void onCancel() {

        }
    }

}
