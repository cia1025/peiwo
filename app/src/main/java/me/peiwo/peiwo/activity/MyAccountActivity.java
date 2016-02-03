package me.peiwo.peiwo.activity;

import java.lang.ref.WeakReference;
import java.util.Locale;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.util.UmengStatisticsAgent;
import me.peiwo.peiwo.util.UserManager;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyAccountActivity extends BaseActivity implements
        OnClickListener {

    private LinearLayout mTvCharge;
    private LinearLayout mTvGetMoney;
    private PWUserModel model;
    private MyHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_account_activity);
        model = UserManager.getPWUser(this);
        mHandler = new MyHandler(this);
        initView();

        getUserinfo();
    }

    class MyHandler extends Handler {
        WeakReference<MyAccountActivity> activity_ref;

        public MyHandler(MyAccountActivity activity) {
            activity_ref = new WeakReference<MyAccountActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MyAccountActivity theActivity = activity_ref.get();
            if (theActivity == null) {
                return;
            }
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    updateBalance();
                    break;

                case WHAT_DATA_RECEIVE_ERROR:
                    break;

            }
            super.handleMessage(msg);
        }
    }


    private void getUserinfo() {
        ApiRequestWrapper.getUserInfo(this, model.uid, String.valueOf(model.uid), new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                UserManager.saveUser(MyAccountActivity.this, new PWUserModel(data));
                model = UserManager.getPWUser(MyAccountActivity.this);
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE);
            }

            @Override
            public void onError(int error, Object ret) {

            }
        });
    }


    private void initView() {
        TitleUtil.setTitleBar(this, "我的钱包", new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        }, null);

        mTvCharge = (LinearLayout) findViewById(R.id.lin_charge);
        mTvGetMoney = (LinearLayout) findViewById(R.id.lin_get_money);
        mTvCharge.setOnClickListener(this);
        if (model.gender == AsynHttpClient.GENDER_MASK_MALE) {
            //mTvCharge.setVisibility(View.VISIBLE);
            mTvGetMoney.setVisibility(View.GONE);
        } else {
            //mTvCharge.setVisibility(View.GONE);
            mTvGetMoney.setVisibility(View.VISIBLE);
            mTvGetMoney.setOnClickListener(this);
        }

    }

    private void updateBalance() {
        TextView tvBalance = (TextView) findViewById(R.id.balance);
        tvBalance.setText(String.format(Locale.getDefault(), "%s元", UserManager.getPWUser(this).money));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBalance();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_charge:
                startActivity(new Intent(MyAccountActivity.this, ChargeActivity.class));
                UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEACCOUNTRECHARGE);
                break;
            case R.id.lin_get_money:
                startActivity(new Intent(this, GetMoneyActivity.class));
                UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEACCOUNTWITHDRAW);
                break;
            default:
                break;
        }
    }
}
