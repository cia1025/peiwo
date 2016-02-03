package me.peiwo.peiwo.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.util.SharedPreferencesUtil;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.util.UserManager;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class GetMoneyActivity extends BaseActivity implements
        OnClickListener {

    private static final int REQUEST_CODE_BIND = 1000;
    private static final int REQUEST_CODE_WITH_DRAW = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_money_activity);
        initView();
    }

    private TextView mTvGetMoneyNum;
    private TextView mTvIncomeMoney;

    private void initView() {
        //MyHandler mHandler = new MyHandler(this);
        TitleUtil.setTitleBar(this, "收入提现", new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        }, null);
        findViewById(R.id.rl_get_money).setOnClickListener(this);
        Button mBtnDone = (Button) findViewById(R.id.done);
        mBtnDone.setOnClickListener(this);
        mTvIncomeMoney = (TextView) findViewById(R.id.income_money);

        mTvGetMoneyNum = (TextView) findViewById(R.id.get_money_num);
        //EditText mEtAccountName = (EditText) findViewById(R.id.account_name);
    }

    @Override
    public void onResume() {
        super.onResume();
        mTvIncomeMoney.setText(String.format("￥ %s", UserManager.getPWUser(this).money));
    }

    private void enterIncomeMoney() {
        final EditText editInput = new EditText(this);
        editInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        editInput.setText(String.valueOf(Double.valueOf(UserManager.getPWUser(this).money).intValue()));
        new AlertDialog.Builder(this)
                .setTitle("提取金额")
                .setView(editInput)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String money = editInput.getText().toString()
                                        .trim();
                                mTvGetMoneyNum.setText(money + "元");
                            }
                        }).setNegativeButton(R.string.cancel, null).show();
    }

//    private void startWithdraw() {
//        showAnimLoading("", false, false, false);
//        String accountName = mEtAccountName.getText().toString();
//        ApiRequestWrapper.withdraw(this, UserManager.getUid(GetMoneyActivity.this), mMoney,
//                accountName, new MsgStructure() {
//                    @Override
//                    public void onReceive(JSONObject data) {
//                        //{"money":90}
//                        mHandler.sendMessage(mHandler.obtainMessage(WHAT_DATA_RECEIVE, data));
//                        //Trace.i("money == " + data.toString());
//
//                        //finish();
//                    }
//
//                    @Override
//                    public void onError(int error, Object ret) {
//                        Trace.i("error == " + error);
//                        mHandler.sendMessage(mHandler.obtainMessage(WHAT_DATA_RECEIVE_ERROR, error));
//                    }
//                });
//    }

//    private boolean checkForGetMoneyBtn() {
//        if (TextUtils.isEmpty(mEtAccountName.getText())) {
//            PPAlert.showToast(this, "请填写支付宝账号");
//            return false;
//        }
//        if (Double.valueOf(UserManager.getPWUser(this).money) < 10) {
//            PPAlert.showToast(this, "金额不足无法提现");
//            return false;
//        }
//
//        try {
//            if (TextUtils.isEmpty(mMoney))
//                mMoney = "0.0";
//            Float money = Float.parseFloat(mMoney);
//            if (money < 10.0f) {
//                PPAlert.showToast(this, "最小提现金额为10元");
//                return false;
//            }
//            if (Double.valueOf(UserManager.getPWUser(this).money) < money) {
//                PPAlert.showToast(this, "提现金额超过账户的钱了");
//                return false;
//            }
//        } catch (Exception e) {
//            PPAlert.showToast(this, "输入的金额不对");
//            return false;
//        }
//
//        return true;
//    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_get_money:
                String money = UserManager.getPWUser(this).money;
                if (TextUtils.isEmpty(money)) {
                    money = "0";
                }
                if (Double.valueOf(money) < 20) {
                    showToast(this, "当前收入少于最小提现金额20元");
                    return;
                }
                enterIncomeMoney();
                break;
            case R.id.done:
//                if (checkForGetMoneyBtn()) {
//                    startWithdraw();
//                }
                String phone = UserManager.getPWUserPhone(this);
                int uid = UserManager.getUid(this);
                String alipay_account = SharedPreferencesUtil.getStringExtra(this, Constans.SP_KEY_ALIPAY_ACCOUNT + uid, "");
                if (TextUtils.isEmpty(phone)) {
                    bindPhone();
                } else if(TextUtils.isEmpty(alipay_account)){
                    bindAlipayAccount();
                } else {
//                    if (Double.valueOf(UserManager.getPWUser(this).money) < 20) {
//                        showToast(this, "金额不足无法提现");
//                        return;
//                    }
                    Intent withdrawIntent = new Intent(this, WithdrawActivity.class);
                    startActivityForResult(withdrawIntent, REQUEST_CODE_WITH_DRAW);
                }
                break;
            default:
                break;
        }
    }

    private void bindAlipayAccount() {
        Intent it = new Intent(GetMoneyActivity.this, AddOrUpdateAlipayAccountActivity.class);
        startActivity(it);
    }

    private void bindPhone() {
        new AlertDialog.Builder(this)
                .setMessage("为了您的账户安全，提现需先绑定手机号！")
                .setNegativeButton("取消", null)
                .setPositiveButton("去绑定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(GetMoneyActivity.this, BindPhoneActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_BIND);
                    }
                })
                .create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_BIND:
                    showToast(this, "绑定手机号成功");
                    break;
                case REQUEST_CODE_WITH_DRAW:
                    //提现成功回调
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class MyHandler extends Handler {
        WeakReference<GetMoneyActivity> activity_ref;

        public MyHandler(GetMoneyActivity activity) {
            activity_ref = new WeakReference<GetMoneyActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            GetMoneyActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) return;
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    theActivity.dismissAnimLoading();
                    showToast(theActivity, "提交审核成功，核对成功后将在5个工作日之内打款到您的支付宝！");
                    JSONObject o = (JSONObject) msg.obj;
                    int money = o.optInt("money", -1);
                    if (money != -1) {
                        UserManager.updateMoney(theActivity, String.valueOf(money));
                    }
                    break;

                case WHAT_DATA_RECEIVE_ERROR:
                    theActivity.dismissAnimLoading();
                    Integer code = (Integer) msg.obj;
                    if (code == 20004) {
                        showToast(theActivity, "当前正在提现审核中，无法重复提现!");
                    } else if (code == 20003) {
                        showToast(theActivity, "余额不足");
                    } else {
                        showToast(theActivity, "操作错误！");
                    }
                    break;
            }
            super.handleMessage(msg);
        }


    }

}
