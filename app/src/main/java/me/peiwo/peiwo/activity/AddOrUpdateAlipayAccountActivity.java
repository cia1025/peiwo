package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.util.SharedPreferencesUtil;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.InputBoxView;

/**
 * Created by fuhaidong on 14/11/12.
 */
public class AddOrUpdateAlipayAccountActivity extends BaseActivity {

    //    private EditText et_account;
//    private EditText et_account_name;
    private TextView tv_alipay_name;
    private TextView tv_alipay_account;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_update_alipay);

        init();
    }

    private void init() {
        //TextView tv_curr_account = (TextView) findViewById(R.id.tv_curr_account);
        TextView tv_account_label = (TextView) findViewById(R.id.tv_account_label);
//        et_account = (EditText) findViewById(R.id.et_account);
//        et_account_name = (EditText) findViewById(R.id.et_account_name);
        int uid = UserManager.getUid(this);
        tv_alipay_account = (TextView) findViewById(R.id.tv_alipay_account);
        tv_alipay_name = (TextView) findViewById(R.id.tv_alipay_name);
        String account = SharedPreferencesUtil.getStringExtra(this, Constans.SP_KEY_ALIPAY_ACCOUNT + uid, "");
        Resources res = getResources();
        String title = res.getString(R.string.bind_alipay_account);
        if (!TextUtils.isEmpty(account)) {
            View ll_last_account_display = findViewById(R.id.ll_last_account_display);
            ll_last_account_display.setVisibility(View.VISIBLE);
            TextView tv_last_account = (TextView) findViewById(R.id.tv_last_account);
            tv_last_account.setText(String.format("账户：%s", account));
            TextView tv_last_account_name = (TextView) findViewById(R.id.tv_last_account_name);
            String accountName = SharedPreferencesUtil.getStringExtra(this, Constans.SP_KEY_ALIPAY_ACCOUNT_NAME + uid, "");
            tv_last_account_name.setText(String.format("姓名：%s", accountName));

            title = res.getString(R.string.modify_alipay_account);
            tv_account_label.setVisibility(View.VISIBLE);
            //tv_curr_account.setVisibility(View.VISIBLE);
            //tv_curr_account.setText("当前支付宝：" + account);
//            tv_account_label.setText("新的支付宝账户");
//            et_account.setHint("请输入新的支付宝账户");
        }
        setTitleBar(title);
    }

    private void inputAccount() {
        String title = getResources().getString(R.string.input_alipay_account);
        InputBoxView inputBox = InputBoxView.newInstance(title, "");
        inputBox.show(getSupportFragmentManager(), "boxview");
        inputBox.setOnInputConfirmListener(tv_alipay_account::setText);
    }

    private void inputAccountName() {
        String title = getResources().getString(R.string.input_alipay_account_name);
        InputBoxView inputBox = InputBoxView.newInstance(title, "");
        inputBox.show(getSupportFragmentManager(), "boxview");
        inputBox.setOnInputConfirmListener(tv_alipay_name::setText);
    }

    private void setTitleBar(String title) {
        TitleUtil.setTitleBar(this, title, (v) -> {
            finish();
        }
                , null);
    }

    public void click(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                String account = tv_alipay_account.getText().toString();
                String account_name = tv_alipay_name.getText().toString();
                if (TextUtils.isEmpty(account.trim())) {
                    showToast(this, "请输入正确的支付宝账户");
                    return;
                }
                if (TextUtils.isEmpty(account_name)) {
                    showToast(this, "请输入真实姓名");
                    return;
                }
                int uid = UserManager.getUid(this);
                account = account.replaceAll(" ", "");
                Intent data = new Intent();
                String last_account = SharedPreferencesUtil.getStringExtra(this, WithdrawActivity.ALIPAY_ACCOUNT + uid, "");
                data.putExtra(WithdrawActivity.ALIPAY_ACCOUNT + uid, account);
                data.putExtra(WithdrawActivity.ALIPAY_ACCOUNT_NAME + uid, account_name);
                if (TextUtils.isEmpty(last_account)) {
                    data.setClass(this, WithdrawActivity.class);
                    startActivity(data);
                } else {
                    setResult(RESULT_OK, data);
                }

                SharedPreferencesUtil.putStringExtra(this, Constans.SP_KEY_ALIPAY_ACCOUNT + uid, account);
                SharedPreferencesUtil.putStringExtra(this, Constans.SP_KEY_ALIPAY_ACCOUNT_NAME + uid, account_name);
                finish();
                break;
            case R.id.rl_alipay_name:
                inputAccountName();
                break;
            case R.id.rl_alipay_account:
                inputAccount();
                break;
            default:
                break;
        }
    }
}