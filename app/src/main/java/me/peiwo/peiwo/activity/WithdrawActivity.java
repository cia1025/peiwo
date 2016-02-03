package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.*;
import me.peiwo.peiwo.widget.InputBoxView;
import org.apache.http.NameValuePair;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by fuhaidong on 14/11/12.
 */
public class WithdrawActivity extends BaseActivity {

    private static final int REQUEST_CODE_ALIPAY_UP = 1000;
    public static final String ALIPAY_ACCOUNT = "alipayaccount";
    public static final String ALIPAY_ACCOUNT_NAME = "alipay_name";
    private String currentAlipayAccount;
    private String currentAlipayAccountName;
    private String mMoney;
    private MyHandler mHandler;
    private TextView tv_alipay_account;
    private TextView tv_money;
    private TextView tv_withdraw_this_month;
    private Button btn_submit;
    private String mBalance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);

        init();
    }

    private void init() {
        mHandler = new MyHandler(this);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        tv_alipay_account = (TextView) findViewById(R.id.tv_alipay_account);
        tv_withdraw_this_month = (TextView) findViewById(R.id.tv_withdraw_money_this_month);
        tv_money = (TextView) findViewById(R.id.tv_withdraw_money);
        TextView tv_withdraw_tips = (TextView) findViewById(R.id.tv_withdraw_tips);
        int uid = UserManager.getUid(this);
        String alipay_account = SharedPreferencesUtil.getStringExtra(this, Constans.SP_KEY_ALIPAY_ACCOUNT + uid, "");
        currentAlipayAccount = alipay_account;
        if (TextUtils.isEmpty(alipay_account)) {
            tv_alipay_account.setText("添加");
        } else {
            tv_alipay_account.setText(alipay_account);
        }

        currentAlipayAccountName = SharedPreferencesUtil.getStringExtra(this, Constans.SP_KEY_ALIPAY_ACCOUNT_NAME + uid, "");
        mBalance = getIntent().getStringExtra("my_balance");
        if (TextUtils.isEmpty(mBalance)) {
            mBalance = UserManager.getPWUser(this).money;
        }
        tv_money.setText(String.format("本次可提现%s元", mBalance));
        //提现金额须不少于20元，预计在xxx时间前到账
        SimpleDateFormat formater = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm", Locale.getDefault());
        tv_withdraw_tips.setText(String.format("预计在%s时间前到账", TimeUtil.getFormatTime((System.currentTimeMillis() + 432000000), formater)));
        setTitleBar();
        getIncomeThisMonth();
    }

    private void setTitleBar() {
        TitleUtil.setTitleBar(this, "提现", (v) -> {
                    finish();

                }, "提现明细", (v) -> {
                    startActivity(new Intent(WithdrawActivity.this, WithdrawHistoryActivity.class));
                }
        );
    }

    public void click(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:
                if (TextUtils.isEmpty(currentAlipayAccount)) {
                    showToast(this, "请填写支付宝账户");
                    return;
                }
                if (checkForGetMoneyBtn())
                    submitWithDraw();
                break;
            case R.id.rl_withdraw_input:
                String money = mBalance;
                if (TextUtils.isEmpty(money)) {
                    money = "0";
                }
                if (Double.valueOf(money) < 120) {
                    showToast(this, "当前收入少于最小提现金额120元");
                    return;
                }
                enterIncomeMoney();
                break;
            case R.id.rl_alipay_account:
                Intent intent = new Intent(WithdrawActivity.this, AddOrUpdateAlipayAccountActivity.class);
                intent.putExtra(ALIPAY_ACCOUNT, currentAlipayAccount);
                intent.putExtra(ALIPAY_ACCOUNT_NAME, currentAlipayAccountName);
                startActivityForResult(intent, REQUEST_CODE_ALIPAY_UP);
                break;
        }
    }

    private boolean checkForGetMoneyBtn() {
        if (TextUtils.isEmpty(currentAlipayAccount)) {
            showToast(this, "请填写支付宝账户");
            return false;
        }
        if (Double.valueOf(mBalance) < 120) {
            showToast(this, "金额不足无法提现");
            return false;
        }

        try {
            if (TextUtils.isEmpty(mMoney))
                mMoney = "0.0";
            Float money = Float.parseFloat(mMoney);
            if (money < 120.0f) {
                showToast(this, "最小提现金额为120元");
                return false;
            }
            if (Double.valueOf(mBalance) < money) {
                showToast(this, "提现金额超过账户的钱了");
                return false;
            }
        } catch (Exception e) {
            showToast(this, "输入的金额不对");
            return false;
        }

        return true;
    }

    private void enterIncomeMoney() {
        String title = getResources().getString(R.string.input_withdraw_money);
        InputBoxView inputBox = InputBoxView.newInstance(title, "", InputType.TYPE_CLASS_NUMBER);
        inputBox.show(getSupportFragmentManager(), "boxview");
        inputBox.setOnInputConfirmListener((rst) -> {
                    tv_money.setText(getString(R.string.withdraw_for_how_much, rst));
                    mMoney = rst;
                    if (mMoney != null && Integer.valueOf(mMoney) < 120) {
                        btn_submit.setBackgroundColor(getResources().getColor(R.color.invalid_clickable_color));
                        btn_submit.setClickable(false);
                    } else {
                        btn_submit.setBackgroundColor(getResources().getColor(R.color.valid_clickable_color));
                        btn_submit.setClickable(true);
                    }
                }
        );
    }

    private void submitWithDraw() {
        String title = getResources().getString(R.string.input_peiwo_password);
        InputBoxView inputBox = InputBoxView.newInstance(title, "", InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputBox.show(getSupportFragmentManager(), "boxview");
        inputBox.setOnInputConfirmListener(this::startWithdraw);
    }

    private void startWithdraw(String password) {
        //添加参数 密码
        showAnimLoading("", false, false, false);
        ApiRequestWrapper.withdraw(this, UserManager.getUid(WithdrawActivity.this), mMoney,
                currentAlipayAccount, currentAlipayAccountName, Md5Util.getMd5code(password), new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                        //{"money":90}
                        mHandler.sendMessage(mHandler.obtainMessage(WHAT_DATA_RECEIVE, data));
                        //Trace.i("money == " + data.toString());

                        //finish();
                    }

                    @Override
                    public void onError(int error, Object ret) {
                        mHandler.sendMessage(mHandler.obtainMessage(WHAT_DATA_RECEIVE_ERROR, error));
                    }
                });
    }

    static class MyHandler extends Handler {
        WeakReference<WithdrawActivity> activity_ref;

        public MyHandler(WithdrawActivity activity) {
            activity_ref = new WeakReference<WithdrawActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            WithdrawActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) return;
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    theActivity.dismissAnimLoading();
                    theActivity.showToast(theActivity, "提交审核成功，核对成功后将在5个工作日之内打款到您的支付宝！");
                    JSONObject o = (JSONObject) msg.obj;
                    int money = o.optInt("money", -1);
                    if (money != -1) {
                        UserManager.updateMoney(theActivity, String.valueOf(money));
                    }
                    theActivity.setResult(RESULT_OK);
                    theActivity.finish();
                    break;

                case WHAT_DATA_RECEIVE_ERROR:
                    theActivity.dismissAnimLoading();
                    Integer code = (Integer) msg.obj;
                    if (code == 20004) {
                        theActivity.showToast(theActivity, "当前正在提现审核中，无法重复提现!");
                    } else if (code == 20003) {
                        theActivity.showToast(theActivity, "余额不足");
                    } else if (code == 20006) {
                        theActivity.showToast(theActivity, "填写的密码错误");
                    } else {
                        if (!PWUtils.isNetWorkAvailable(theActivity)) {
                            theActivity.showToast(theActivity, "网络连接失败");
                        } else {
                            theActivity.showToast(theActivity, "操作错误！");
                        }
                    }
                    break;
            }
            super.handleMessage(msg);
        }

    }

    public float getIncomeThisMonth() {
        ApiRequestWrapper.openAPIGET(this, new ArrayList<NameValuePair>(), AsynHttpClient.API_PAYMENT_WITHDRAW_MONTH, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("onReceive. data is : " + data);
                String money = data.optString("money");
                Observable.just(money).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    tv_withdraw_this_month.setText(getString(R.string.yuan_unit, money));
                });
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("onError. error code is :" + error + ", ret is : " + ret);
            }
        });
        return 1.0f;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_ALIPAY_UP:
                    int uid = UserManager.getUid(this);
                    currentAlipayAccount = data.getStringExtra(ALIPAY_ACCOUNT + uid);
                    currentAlipayAccountName = data.getStringExtra(ALIPAY_ACCOUNT_NAME + uid);
                    tv_alipay_account.setText(currentAlipayAccount);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}