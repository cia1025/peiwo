package me.peiwo.peiwo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import butterknife.Bind;
import butterknife.OnClick;
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
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.util.*;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.lang.ref.WeakReference;

/**
 * Created by gaoxiang on 16/2/5.
 */
public class Bind3rdPartyAccountActivity extends BaseActivity {

    private HourGlassAgent mHourGlassAgent = HourGlassAgent.getInstance();
    private static final int REQUEST_CODE_USERINIT = 1000;
    private boolean islogin;
    private WXReceiver wxReceiver;
    private Tencent mTencent;
    private IWXAPI mWXApi;
    private AuthInfo mWeiboAuth;
    private SsoHandler mSsoHandler;
    private String openid2, opentoken2;
    @Bind(R.id.btn_wechat)
    ImageView btn_wechat;
    private static final int SOCIAL_TYPE_WEIBO = 1;
    private static final int SOCIAL_TYPE_QQ = 2;
    public static final int SOCIAL_TYPE_PHONE = 3;
    private static final int SOCIAL_TYPE_WECHAT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_3rd_party_account);
        init();
    }

    private void init() {
        mWXApi = WXAPIFactory.createWXAPI(this, Constans.WX_APP_ID, true);
        mWXApi.registerApp(Constans.WX_APP_ID);
        wxReceiver = new WXReceiver();
        registerReceiver(wxReceiver, new IntentFilter(PWActionConfig.ACTION_WXSHARE_SUCCESS));
        mWeiboAuth = new AuthInfo(this, Constans.WEIBO_APP_KEY, Constans.WEIBO_REDIRECT_URL, Constans.WEIBO_SCOPE);
        mTencent = Tencent.createInstance(Constans.QQ_APP_ID, this);

//        String openid = SharedPreferencesUtil.getStringExtra(this,
//                Constans.SP_KEY_OPENID, "");
//        String opentoken = SharedPreferencesUtil.getStringExtra(this,
//                Constans.SP_KEY_OPENTOKEN, "");
//        int socialType = SharedPreferencesUtil.getIntExtra(this,
//                Constans.SP_KEY_SOCIALTYPE, -1);
//        if (!TextUtils.isEmpty(openid) && !TextUtils.isEmpty(opentoken)
//                && socialType != -1) {
//            if (UserManager.getUserState(Bind3rdPartyAccountActivity.this) != UserManager.STATE_UNINITED) {
//                bindSocial(socialType, openid, opentoken);
//            }
//        }
    }

    @OnClick(R.id.btn_skip)
    void skip() {
        String phone = UserManager.getUserPhone(this);
        String pwd = SharedPreferencesUtil.getStringExtra(this, Constans.SP_KEY_OPENTOKEN, "");
        bindSocial(SOCIAL_TYPE_PHONE, phone, pwd);
    }

    @OnClick(R.id.btn_wechat)
    void clickWechat() {
        doWechatLogin();
        if (mHourGlassAgent.getStatistics() && mHourGlassAgent.getK43() == 0) {
            mHourGlassAgent.setK43(1);
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            app.postK("k43");
        }
    }

    @OnClick(R.id.btn_qq)
    void clickQQ() {
        doQQLogin();
        if (mHourGlassAgent.getStatistics() && mHourGlassAgent.getK45() == 0) {
            mHourGlassAgent.setK45(1);
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            app.postK("k45");
        }

    }

    @OnClick(R.id.btn_weibo)
    void clickWeibo() {
        doWeiboLogin();
        if (mHourGlassAgent.getStatistics() && mHourGlassAgent.getK47() == 0) {
            mHourGlassAgent.setK47(1);
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            app.postK("k47");
        }
    }

    private void doWechatLogin() {
        //微信注册
        if (!mWXApi.isWXAppInstalled()) {
            showToast(this, getString(R.string.wechat_not_installed));
            return;
        }
        SendAuth.Req wxReq = new SendAuth.Req();
        wxReq.scope = "snsapi_userinfo";
        wxReq.state = PWUtils.getDeviceId(this);
        mWXApi.sendReq(wxReq);
        //
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
                        mTencent.logout(Bind3rdPartyAccountActivity.this);
                        showToast(Bind3rdPartyAccountActivity.this, "qq登录失败");
                        return;
                    }
                    openid2 = openid;
                    opentoken2 = access_token;
                    UserManager.saveOpenResultInPreference(Bind3rdPartyAccountActivity.this, openid, access_token, SOCIAL_TYPE_QQ);
                    //saveOpenResultInPreference(openid, access_token, SOCIAL_TYPE_QQ);
                    bindSocial(SOCIAL_TYPE_QQ, openid, access_token);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(UiError uiError) {
                showToast(Bind3rdPartyAccountActivity.this, uiError.errorMessage);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void doWeiboLogin() {
        mSsoHandler = new SsoHandler(this, mWeiboAuth);
        mSsoHandler.authorize(new AuthListener(Bind3rdPartyAccountActivity.this));
    }

    static class AuthListener implements WeiboAuthListener {
        private final Bind3rdPartyAccountActivity theActivity;
        WeakReference<Bind3rdPartyAccountActivity> activity_ref;

        public AuthListener(Bind3rdPartyAccountActivity act) {
            activity_ref = new WeakReference<>(act);
            theActivity = activity_ref.get();
        }

        @Override
        public void onComplete(Bundle bundle) {
            Oauth2AccessToken mAccessToken = Oauth2AccessToken.parseAccessToken(bundle);
            if (mAccessToken.isSessionValid()) {
                UserManager.saveOpenResultInPreference(theActivity, mAccessToken.getUid(), mAccessToken.getToken(), SOCIAL_TYPE_WEIBO);
                //saveOpenResultInPreference(mAccessToken.getUid(), mAccessToken.getToken(), SOCIAL_TYPE_WEIBO);
                theActivity.bindSocial(SOCIAL_TYPE_WEIBO, mAccessToken.getUid(), mAccessToken.getToken());
            }
        }

        @Override
        public void onWeiboException(final WeiboException e) {
            Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                theActivity.showToast(theActivity, e.getMessage());
            });
        }

        @Override
        public void onCancel() {

        }
    }

    private void bindSocial(int social_type, String social_uid, String access_token) {
        showAnimLoading();
        int uid = SharedPreferencesUtil.getIntExtra(this, Constans.SP_KEY_UID, 0);
        PeiwoApp app = PeiwoApp.getApplication();
        HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
        if (social_type == SOCIAL_TYPE_WECHAT) {
            if (hourGlassAgent.getStatistics() && hourGlassAgent.getK44() == 0) {
                hourGlassAgent.setK44(1);
                app.postK("k44");
            }
        } else if (social_type == SOCIAL_TYPE_QQ) {
            if (hourGlassAgent.getStatistics() && hourGlassAgent.getK46() == 0) {
                hourGlassAgent.setK46(1);
                app.postK("k46");
            }
        } else if (social_type == SOCIAL_TYPE_WEIBO) {
            if (hourGlassAgent.getStatistics() && hourGlassAgent.getK48() == 0) {
                hourGlassAgent.setK48(1);
                app.postK("k48");
            }
        } else {
            if (hourGlassAgent.getStatistics() && hourGlassAgent.getK49() == 0) {
                hourGlassAgent.setK49(1);
                app.postK("k49");
            }
        }
        ApiRequestWrapper.bindSocial(this, uid, social_uid, access_token, social_type, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                    PWUserModel modle = new PWUserModel(data);
                    UserManager.saveUser(Bind3rdPartyAccountActivity.this, modle);
                    try {
                        //微信token会通过json返回
                        doHandleLogin(social_type, o.getString("social_uid"), access_token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                    showToast(Bind3rdPartyAccountActivity.this, tips);
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        //不允许用户返回
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_USERINIT:
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

    private void doRegistResult() {
        String phone = UserManager.getUserPhone(this);
        String pwd = SharedPreferencesUtil.getStringExtra(this, Constans.SP_KEY_OPENTOKEN, "");
        doHandleLogin(SOCIAL_TYPE_PHONE, phone, pwd);
    }

    private void doHandleLogin(int social_type, String social_uid, String token) {
        // 微博或者QQ登陆时未绑定手机
        String phone = UserManager.getUserPhone(getApplicationContext());

        if (UserManager.getUserState(this) == UserManager.STATE_UNINITED) {
            //未完善用户信息
            dismissAnimLoading();
            if (social_type == -1 || TextUtils.isEmpty(token)) {
                showToast(this, getString(R.string.user_info_access_deny));
                return;
            }
            Intent intent = new Intent(this, UserDetailSettingActivity.class);
            intent.putExtra("social_uid", social_uid);
            intent.putExtra("social_type", social_type);
            intent.putExtra("token", token);
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
            finish();
        }
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

    @Override
    protected void onDestroy() {
        mWXApi.unregisterApp();
        if (wxReceiver != null) {
            unregisterReceiver(wxReceiver);
            wxReceiver = null;
        }
        super.onDestroy();
    }

    class WXReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(btn_wechat == null || btn_wechat.getVisibility() != View.VISIBLE) {
                return;
            }
            HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            String wxcode = intent.getStringExtra("wxcode");
            if (!TextUtils.isEmpty(wxcode)) {
                bindWX(wxcode);
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

    private void bindWX(String wxcode) {
        bindSocial(SOCIAL_TYPE_WECHAT, "", wxcode);
    }

//    private void showSignErr(int error) {
//        String tipString = "连接失败,请稍后重试(" + error + ")";
//        if (error != AsynHttpClient.ERROR_MSG_NETWORK_NOT_AVAILABLE) {
//            mTencent.logout(Bind3rdPartyAccountActivity.this);
//        }
//        switch (error) {
//            case AsynHttpClient.PW_RESPONSE_DATA_NOT_AVAILABLE:
//                tipString = "此账号已被封禁";
//                break;
//            case AsynHttpClient.ERROR_MSG_NETWORK_NOT_AVAILABLE:
//                tipString = "网络连接失败";
//                break;
//            case AsynHttpClient.DATA_NOT_EXISTS:
//                tipString = "账号密码错误";
//                break;
//            case AsynHttpClient.PW_RESPONSE_OPERATE_ERROR:
//                tipString = "账号不存在";
//                break;
//        }
//        showToast(Bind3rdPartyAccountActivity.this, tipString);
//    }
}
