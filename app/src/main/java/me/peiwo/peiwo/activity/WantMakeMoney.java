package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.*;
import net.simonvt.numberpicker.NumberPicker;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Dong Fuhai on 2014-07-23 15:02.
 *
 * @modify:
 */
public class WantMakeMoney extends BaseActivity {
    private static final int WHAT_DATA_RECEIVE_GETPRICE = 3000;
    private static final int WHAT_DATA_RECEIVE_AGREEMENT_INFO = 4000;
    private static final int WHAT_DATA_RECEIVE_DO_AGREE = 5000;
    private static final int REQUEST_CODE_AVATAR_VERIFY = 6000;
    private static final int REQUEST_CODE_CHARGE = 60001;
    private static final int WHAT_RECEIVE_REWARD_PRICE = 60002;
    private static final int WHAT_RECEIVE_GETPRICE_NULL = 60003;
    private static final int WHAT_DATA_UPDATE_BALANCE = 60004;
    private static final int REQUEST_RECORD_LAZY_VOICE_WHEN_SET_PRICE = 0x01;
    private static final int REQUEST_CODE_BIND = 0x10;
    private static final int REQUEST_CODE_WITHDRAW = 0x20;
    private static final int WHAT_DATA_RECEIVE_PRICE_INFO = 0x30;
    private float mPrice = -1;
    private float oldPrice;
    private Float[] price_range;
    private String[] price_lable;
    private MyHandler mHandler;
    private TextView tv_call_price;
    private NumberPicker np_price_set;
    private LinearLayout ll_set_price;
    private RelativeLayout rl_avatar_verify;
    private String agreement_url;
    private int system_version;  //系统女生特权的版本
    private PWUserModel mUser;
    private TextView tv_verify;
    private int flags;
    private String newAvater;
    private TextView tv_balance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_money);
        mUser = UserManager.getPWUser(this);
        mHandler = new MyHandler(this);
        init();
    }

    static class MyHandler extends Handler {
        WeakReference<WantMakeMoney> activity_ref;

        public MyHandler(WantMakeMoney activity) {
            activity_ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            WantMakeMoney theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) {
                return;
            }
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    theActivity.showToast(theActivity, "设置成功");
                    //theActivity.tv_call_price.setText(theActivity.mPrice == 0 ? "免费" : String.format(Locale.getDefault(), "%s元/分钟", theActivity.mPrice));
                    theActivity.finish();
                    break;
                case WHAT_DATA_RECEIVE_AGREEMENT_INFO:
                    if (msg.obj instanceof JSONObject)
                        theActivity.setAgreement((JSONObject) msg.obj);
                    break;
                case WHAT_DATA_RECEIVE_ERROR:
                    theActivity.showToast(theActivity, "设置失败");
                    break;
                case WHAT_DATA_RECEIVE_GETPRICE:
                    theActivity.setPicker();
                    break;
                case WHAT_DATA_RECEIVE_DO_AGREE:
                    theActivity.showPriceSelector();
                    break;
                case WHAT_RECEIVE_REWARD_PRICE:
                    TextView tv_reward_price = (TextView) theActivity.findViewById(R.id.tv_reward_price);
                    tv_reward_price.setText(String.valueOf(msg.obj) + "元/上限");
                    break;
                case WHAT_RECEIVE_GETPRICE_NULL:
                    theActivity.findViewById(R.id.rl_set_call_price).setEnabled(false);
                    theActivity.tv_call_price.setText("免费");
                    break;
                case WHAT_DATA_UPDATE_BALANCE:
                    theActivity.dismissAnimLoading();
                    if (theActivity.tv_balance != null)
                        theActivity.tv_balance.setText(String.valueOf(msg.obj));
                    break;
                case WHAT_DATA_RECEIVE_PRICE_INFO:
                    TextView tv = (TextView) theActivity.findViewById(R.id.tv_price_info);
                    tv.setText(String.valueOf("*" + msg.obj));
                    break;
                default:
                    break;
            }
            theActivity.dismissAnimLoading();
            super.handleMessage(msg);
        }
    }

    private void setAgreement(JSONObject o) {

        try {
            agreement_url = o.getString("agreement_url");
            system_version = o.optInt("system_version");
            int user_version = o.optInt("user_version");
            if (system_version > user_version) {
                showAgreementDialog();
            } else {
                checkLazyVoice();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkLazyVoice() {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_USERINFO_LAZY_VOICE, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> showPriceSelector());
            }

            @Override
            public void onError(int error, Object ret) {
                if (error == LazyGuyActivity.HAVE_NOT_UPLOAD_YET) {
                    Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> showAlertDialog());
                }
            }
        });
    }

    private void showAlertDialog() {
        new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.record_lazy_voice))
                .setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> {
                            showPriceSelector();
                        }
                )
                .setPositiveButton(getResources().getString(R.string.record), (dialog, which) -> {
                            Intent it = new Intent(WantMakeMoney.this, LazyGuyActivity.class);
                            startActivityForResult(it, REQUEST_RECORD_LAZY_VOICE_WHEN_SET_PRICE);
                        }
                ).create().show();
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("avatar", newAvater);
        setResult(RESULT_OK, intent);
        super.finish();
    }

    private void setPicker() {
        //如果可设置超过0.8元的价值，显示认证一栏
        if (price_range.length > 8) {
//            rl_avatar_verify.setVisibility(View.VISIBLE);
            flags = UserManager.getPWUserFlags(this);
            //setVerifyInfo(flags);
        }
        np_price_set.setMaxValue(price_range.length - 1);
        np_price_set.setMinValue(0);
        np_price_set.setFocusable(true);
        np_price_set.setFocusableInTouchMode(true);
        np_price_set.setDisplayedValues(price_lable);
        for (int i = 0; i < price_range.length; i++) {
            if (oldPrice == price_range[i]) {
                np_price_set.setValue(i);
                break;
            }
        }
    }


    private void init() {
        setTitle("陪我特权");
        setMoneyBG();
        TextView btn_getmoney = (TextView) findViewById(R.id.btn_getmoney);
//        if (mUser.gender == AsynHttpClient.GENDER_MASK_FEMALE) {
        btn_getmoney.setText("提现");
        btn_getmoney.setEnabled(true);
//        } else {
//            btn_getmoney.setText("暂未开放");
//            btn_getmoney.setEnabled(false);
//        }
        tv_balance = (TextView) findViewById(R.id.tv_balance);

        getBalanceFromServer();
        oldPrice = Float.valueOf(mUser.price);
        tv_call_price = (TextView) findViewById(R.id.tv_call_price);
        tv_call_price.setText(Float.valueOf(mUser.price) == 0 ? "免费" : String.format(Locale.getDefault(), "%s元/分钟", mUser.price));
        np_price_set = (NumberPicker) findViewById(R.id.np_price_set);
        ll_set_price = (LinearLayout) findViewById(R.id.ll_set_price);
        rl_avatar_verify = (RelativeLayout) findViewById(R.id.rl_avatar_verify);
        tv_verify = (TextView) findViewById(R.id.tv_verify);
        getPriceRange();
    }

    private void getBalanceFromServer() {
        showAnimLoading();
        ApiRequestWrapper.openAPIGET(this, new ArrayList<NameValuePair>(), AsynHttpClient.API_USERINFO_GETFINANCE, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("onReceive. data is : " + data);
                Message msg = mHandler.obtainMessage(WHAT_DATA_UPDATE_BALANCE);
                String money = (data.optString("money"));
                UserManager.updateMoney(WantMakeMoney.this, money);
                msg.obj = money;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("onError. ret is : " + ret);
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                });
            }
        });
    }

    private void setMoneyBG() {
        ImageView iv_money = (ImageView) findViewById(R.id.iv_money);
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.drawable.make_money, options);
            DisplayMetrics metrics = PWUtils.getMetrics(this);
            int height = 317 * metrics.widthPixels / 750;
            options.inSampleSize = ImageUtil.calculateInSampleSize(options, metrics.widthPixels, height);
            options.inJustDecodeBounds = false;
            iv_money.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.make_money, options));
        } catch (Exception e) {

        }
    }

    private void getAgreementInfo() {
        ApiRequestWrapper.openAPIGET(this, new ArrayList<NameValuePair>(), AsynHttpClient.API_AGREEMENT_INFORMATION, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Message msg = mHandler.obtainMessage();
                msg.obj = data;
                msg.what = WHAT_DATA_RECEIVE_AGREEMENT_INFO;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onError(int error, Object ret) {

            }
        });

    }

    private void getPriceRange() {
        ApiRequestWrapper.getPriceRange(this, mUser.uid, new MsgStructure() {
            @Override
            public void onReceive(final JSONObject data) {
                //{"price":[0.1,0.1]}
                //{"price":[0.1,2.3000000000000003]}
                String price_info = data.optString("price_description");
                mHandler.sendMessage(mHandler.obtainMessage(WHAT_DATA_RECEIVE_PRICE_INFO, price_info));
                String reward_price = data.optString("reward_price");
                mHandler.sendMessage(mHandler.obtainMessage(WHAT_RECEIVE_REWARD_PRICE, reward_price));
                //UserManager.updateFlags(WantMakeMoney.this, String.valueOf(data.optInt("flags")));
                try {
                    DecimalFormat format = new DecimalFormat("#.0");
                    JSONArray array = data.getJSONArray("price");
                    if (array.length() == 0) {
                        //不能调整价格
                        mHandler.sendEmptyMessage(WHAT_RECEIVE_GETPRICE_NULL);
                        return;
                    }
                    if (array.getDouble(0) == array.getDouble(1)) {
                        price_range = new Float[array.length()];
                        price_lable = new String[array.length()];
                        for (int i = 0; i < array.length(); i++) {
                            price_range[i] = Float.valueOf(format.format(array.getDouble(i)));
                            price_lable[i] = String.format(Locale.getDefault(), "%s元/分钟", price_range[i]);
                        }
                    } else {
                        //double ds = array.getDouble(0);
                        float fe = Float.valueOf(format.format(array.getDouble(1)));
                        int it = (int) (fe / 0.1f);
                        price_range = new Float[it + 1];
                        price_lable = new String[it + 1];
                        price_range[0] = 0f;
                        price_lable[0] = "免费";
                        float f = 0.1f;

                        for (int i = 1; i <= it; i++) {
                            price_range[i] = Float.valueOf(format.format(f));
                            price_lable[i] = String.format(Locale.getDefault(), "%s元/分钟", price_range[i]);
                            f += 0.1f;
                        }
                    }

                    if (price_range[0] == 0.1f && price_range[1] == 0.1f) {
                        price_range[0] = 0f;
                    }
                    if (price_range[0] == 0f) {
                        price_lable[0] = "免费";
                    }
//                    for (int i = 0; i < price_range.length; i++) {
//                        Log.i("tag", "price_range == " + price_range[i]);
//                    }
//                    for (int i = 0; i < price_lable.length; i++) {
//                        Log.i("tag", "price_lable == " + price_lable[i]);
//                    }

                    mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_GETPRICE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("get price range, error code is : " + error);
            }
        });
    }


    @Override
    public void left_click(View v) {
        doSettingCallPrice(true);
    }

    private void doSettingCallPrice(boolean isloading) {
        if (mPrice == -1) {
            finish();
            return;
        }
        if (isloading) {
            showAnimLoading();
        }
        ApiRequestWrapper.settingPrice(this, mUser.uid, String.format("%.1f", mPrice), new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                UserManager.updatePrice(WantMakeMoney.this, mPrice);
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE);
            }

            @Override
            public void onError(int error, Object ret) {
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
            }
        });
    }

    private void showPriceSelector() {
        ll_set_price.setVisibility(View.VISIBLE);
    }

    public void click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.rl_set_call_price:
                PeiwoApp app = (PeiwoApp) getApplicationContext();
                if (app.getIsCalling()) {
                    showToast(this, "通话中不能修改通话资费，请通话结束后再试！");
                    return;
                }
                getAgreementInfo();
                break;
            case R.id.rl_avatar_verify:
//                if (getAvatarVerify(flags) % 2 == 0) {   //0代表未认证，2代表认证失败，这两个状态可以点进去
//                    startActivityForResult(new Intent(this, PWAvatarVerifyActivity.class), REQUEST_CODE_AVATAR_VERIFY);
//                } else if (getAvatarVerify(flags) == 1) {  //提交认证中
//                    showToast(this, "您已经提交认证资料，请留意系统消息获取审核结果");
//                }
                break;
            case R.id.tv_ok:
                if (price_range == null) {
                    ll_set_price.setVisibility(View.INVISIBLE);
                    return;
                }
                float price = price_range[np_price_set.getValue()];
                //不再弹出认证弹框
//                if (price >= 0.8) {
//                    if (getAvatarVerify(flags) != 3) {  //还没有通过认证
//                        showAvatarVerifyDiloag();
//                        ll_set_price.setVisibility(View.INVISIBLE);
//                        return;
//                    }
//                }
//                if (price >= 3.0) {
//                    new AlertDialog.Builder(this).setTitle("提示")
//                            .setMessage("暂未开通3元/分钟及以上的时间价值,敬请期待！")
//                            .setNegativeButton("知道了", null)
//                            .create()
//                            .show();
//                    ll_set_price.setVisibility(View.INVISIBLE);
//                    return;
//                }
                tv_call_price.setText(price == 0f ? "免费" : String.format(Locale.getDefault(), "%s元/分钟", price));
                mPrice = price;
                ll_set_price.setVisibility(View.INVISIBLE);
                break;
            case R.id.tv_cancel:
                ll_set_price.setVisibility(View.INVISIBLE);
                break;
            case R.id.btn_getmoney:
//                startActivity(new Intent(this, GetMoneyActivity.class));
                String phone = UserManager.getPWUserPhone(this);
                int uid = UserManager.getUid(this);
                String alipay_account = SharedPreferencesUtil.getStringExtra(this, Constans.SP_KEY_ALIPAY_ACCOUNT + uid, "");
                if (TextUtils.isEmpty(phone)) {
                    bindPhone();
                } else if (TextUtils.isEmpty(alipay_account)) {
                    bindAlipayAccount();
                } else {
//                    if (Double.valueOf(UserManager.getPWUser(this).money) < 20) {
//                        showToast(this, "金额不足无法提现");
//                        return;
//                    }
                    Intent withdrawIntent = new Intent(this, WithdrawActivity.class);
                    withdrawIntent.putExtra("my_balance", tv_balance.getText().toString());
                    startActivityForResult(withdrawIntent, REQUEST_CODE_WITHDRAW);
                }
                UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEACCOUNTWITHDRAW);
                break;
            case R.id.btn_recharge:
                startActivityForResult(new Intent(this, ChargeActivity.class), REQUEST_CODE_CHARGE);
                UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEACCOUNTRECHARGE);
                break;
            default:
                break;
        }
    }

    private void showAvatarVerifyDiloag() {
//        new AlertDialog.Builder(this).setTitle("提示")
//                .setMessage("设置0.8元/分钟及以上的时间价值需进行个人头像认证！")
//                .setNegativeButton("以后再说", null)
//                .setPositiveButton("马上认证", new AlertDialog.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (getAvatarVerify(flags) % 2 == 0) {   //0代表未认证，2代表认证失败，这两个状态可以点进去
//                            startActivityForResult(new Intent(WantMakeMoney.this, PWAvatarVerifyActivity.class), REQUEST_CODE_AVATAR_VERIFY);
//                        } else if (getAvatarVerify(flags) == 1) {  //提交认证中
//                            showToast(WantMakeMoney.this, "您已经提交认证资料，请留意系统消息获取审核结果");
//                        }
//                    }
//                })
//                .create()
//                .show();
    }

    private void bindAlipayAccount() {
        Intent it = new Intent(WantMakeMoney.this, AddOrUpdateAlipayAccountActivity.class);
        startActivity(it);
    }

    private void bindPhone() {
        new android.app.AlertDialog.Builder(this)
                .setMessage("为了您的账户安全，提现需先绑定手机号！")
                .setNegativeButton("取消", null)
                .setPositiveButton("去绑定", (dialog, which) -> {
                    Intent intent = new Intent(WantMakeMoney.this, BindPhoneActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_BIND);
                })
                .create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_AVATAR_VERIFY) {
                flags = data.getIntExtra("flags", 0);
                newAvater = data.getStringExtra("avatar");
                setVerifyInfo(flags);
            } else if (requestCode == REQUEST_CODE_CHARGE) {
                /*TextView tv_balance = (TextView) findViewById(R.id.tv_balance);
                String lastmoney = data.getStringExtra("lastmoney");
                if (!TextUtils.isEmpty(lastmoney)) {
                    tv_balance.setText(lastmoney + "元");
                }*/
                getBalanceFromServer();
            } else if (requestCode == REQUEST_CODE_WITHDRAW) {
                getBalanceFromServer();
            }
        }
        if (requestCode == REQUEST_RECORD_LAZY_VOICE_WHEN_SET_PRICE) {
            showPriceSelector();
        }
    }

    private void doAgreement(final boolean agree) {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("agreement_version", String.valueOf(system_version)));
        ApiRequestWrapper.openAPIPOST(this, params, agree ? AsynHttpClient.API_AGREEMENT_AGREE : AsynHttpClient.API_AGREEMENT_DISAGREE, new MsgStructure() {
            @Override
            public boolean onInterceptRawData(String rawStr) {
                if (agree)
                    mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_DO_AGREE);
                return true;
            }

            @Override
            public void onReceive(JSONObject data) {

            }

            @Override
            public void onError(int error, Object ret) {

            }
        });
    }

    private void showAgreementDialog() {
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        View v = LayoutInflater.from(this).inflate(R.layout.layout_dialog_agreement, null);
        WebView wv = (WebView) v.findViewById(R.id.webview);
        TextView tv_error_tips = (TextView) v.findViewById(R.id.tv_error_tips);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                wv.setVisibility(View.GONE);
                tv_error_tips.setText("抱歉，当前服务器出现小故障，请稍后重试，我们会尽快解决问题的!");
            }
        });
        wv.loadUrl(agreement_url);
        v.findViewById(R.id.btn_agree).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                doAgreement(true);
            }
        });
        v.findViewById(R.id.btn_disagree).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                doAgreement(false);
            }
        });
        dialog.setView(v);
        dialog.show();
    }


    private void setVerifyInfo(int flags) {
        //假的
        switch ((flags & 3)) {
            case 0:
                tv_verify.setText("未认证");
                break;
            case 1:
                tv_verify.setText("认证中");
                break;
            case 2:
                tv_verify.setText("认证失败");
                break;
            case 3:
                tv_verify.setText("已认证");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        doSettingCallPrice(true);
    }

    private int getAvatarVerify(int flags) {
        //假的
        switch ((flags & 3)) {
            case 0:
                return 0;  //未认证
            case 1:
                return 1;  //正在审核中
            case 2:
                return 2;  //审核失败
            case 3:
                return 3;  //审核通过
        }
        return 0;
    }


}