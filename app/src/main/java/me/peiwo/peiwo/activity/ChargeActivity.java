
package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.alipay.sdk.app.PayTask;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.alipay.PayResult;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.model.PaymentItemModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.util.ArrayList;

public class ChargeActivity extends BaseActivity {

    private static final int ID_RECEIVE_PAYMENT_LIST = 1001;
    private static final int ID_RECEIVE_PAYMENT_LIST_ERROR = 1002;
    private static final int ID_UPDATE_BALANCE = 1003;
    private static final int RQF_PAY_ERROR = 1004;
    private static final int RQF_PAY = 1005;
    private static final int ID_RECEIVE_ALIPAY_STATE = 1006;
    public static final String ACTION_WECHAT_PAYMENT_DONE = "me.peiwo.peiwo.WECHAT_PAYMENT_DONE";
    private int mUid;
    private Rect rect_et_charge = new Rect();
    @Bind(R.id.et_input_charge)
    EditText et_input_charge;
    @Bind(R.id.tv_balance)
    TextView mTvBalance;
    //    private ChargeAdapter adapter;
    //    private View progressBar;

    private IWXAPI mWXAPI;
    private String mWXpaymentId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.charge_activity);
        mWXAPI = WXAPIFactory.createWXAPI(this, Constans.WX_APP_ID, true);
        mWXAPI.registerApp(Constans.WX_APP_ID);
        mUid = UserManager.getUid(this);
        EventBus.getDefault().register(this);
        initView();
    }


    @Override
    protected void handle_message(int message_id, JSONObject obj) {
        switch (message_id) {
            case ID_UPDATE_BALANCE:
                dismissAnimLoading();
                lastMoney = obj.optString("money");
                mTvBalance.setText(String.format("%s元", lastMoney));
                break;
            case ID_RECEIVE_PAYMENT_LIST_ERROR:
//                progressBar.setVisibility(View.GONE);
                dismissAnimLoading();
                showToast(this, "网络连接失败");
                break;
//            case ID_GET_USER_INFO_ERROR:
//                showToast(ChargeActivity.this, "刷新钱数失败，目前的钱数显示不对，错误码：" + error);
//                break;
            case ID_RECEIVE_PAYMENT_LIST:
//                progressBar.setVisibility(View.GONE);
                dismissAnimLoading();
                handleRes(obj);
                break;
//            case GET_USER_INFO:
//                updateUserBalance();
//                break;
            case RQF_PAY_ERROR:
                dismissAnimLoading();
                showToast(this, "网络连接失败");
                break;
            case RQF_PAY:
                dismissAnimLoading();
                aliPay(obj);
                break;
            case ID_RECEIVE_ALIPAY_STATE:
                handleAliPayState(obj.optString("rst"));
                break;

        }
    }

    private void handleAliPayState(String rst) {
        //resultStatus={6001};memo={操作已经取消。};result={}
        PayResult payResult = new PayResult(rst);
        String resultStatus = payResult.getResultStatus();
        if (TextUtils.equals(resultStatus, "9000")) {
            showToast(this, "支付成功");
            updateUserBalance();
        } else if (TextUtils.equals(resultStatus, "6001")) {
            showToast(ChargeActivity.this, payResult.getMemo());
        } else {
            if (TextUtils.equals(resultStatus, "8000")) {
                showToast(ChargeActivity.this, "支付结果确认中");

            } else {
                // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                showToast(this, getString(R.string.pay_failed));
            }
        }
    }

    private void aliPay(final JSONObject obj) {
        new Thread(() -> {
            try {
                String order = obj.optString("order");
                PayTask alipay = new PayTask(ChargeActivity.this);
                String resultString = alipay.pay(order);
                distributeMessage(ID_RECEIVE_ALIPAY_STATE, new JSONObject().put("rst", resultString));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleRes(JSONObject obj) {
        try {
            JSONArray array = obj.optJSONArray(AsynHttpClient.KEY_ITEMS);
            ArrayList<PaymentItemModel> models = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                models.add(new PaymentItemModel(array.getJSONObject(i)));
            }
//            RecyclerView v_recycler_charge = (RecyclerView) findViewById(R.id.v_recycler_charge);
//            v_recycler_charge.setHasFixedSize(true);
//            //v_recycler_charge.setItemAnimator(new DefaultItemAnimator());
//            MyGridLayoutManager layoutManager = new MyGridLayoutManager(this, 3, models.size() / 3);
//            v_recycler_charge.setLayoutManager(layoutManager);
//            adapter = new ChargeAdapter(this, models);
//            v_recycler_charge.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //防止产品变卦,还是先留着吧
    /*static class MyGridLayoutManager extends GridLayoutManager {

        private int columnCount;

        public MyGridLayoutManager(Context context, int spanCount, int columnCount) {
            super(context, spanCount);
            this.columnCount = columnCount;
        }


        @Override
        public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
            View view = recycler.getViewForPosition(0);
            if (view != null) {
                measureChild(view, widthSpec, heightSpec);
                //int measuredWidth = View.MeasureSpec.getSize(widthSpec);
                int measuredHeight = view.getMeasuredHeight();
                setMeasuredDimension(widthSpec, measuredHeight * columnCount);
            } else {
                super.onMeasure(recycler, state, widthSpec, heightSpec);
            }
        }
    }*/


    private void getPaymentList() {
//        progressBar = findViewById(R.id.progressBar);
        ApiRequestWrapper.getPaymentList(ChargeActivity.this, mUid, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                distributeMessage(ID_RECEIVE_PAYMENT_LIST, data);
            }

            @Override
            public void onError(int error, Object ret) {
                distributeMessage(ID_RECEIVE_PAYMENT_LIST_ERROR, null);
            }
        });
    }

    /*@Override
    public void click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.alipay_client:
                createAlipayOrder();
                UmengStatisticsAgent.onEvent(this, UMEventIDS.UMECLKPAY);
                break;
            case R.id.wechat_client:

                break;
            default:
                break;
        }
    }*/

    @OnClick(R.id.alipay_client)
    void doAlipayCharge() {
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMECLKPAY);
        createAlipayOrder();
    }

    @OnClick(R.id.wechat_client)
    void doWechatCharge() {
        if (!mWXAPI.isWXAppInstalled()) {
            showToast(this, getString(R.string.wechat_not_installed));
            return;
        }
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMECLKPAY);
        createWXOrder();
    }

    private void createAlipayOrder() {
        CustomLog.d("createAlipayOrder");
//        PaymentItemModel model = adapter.getCheckedPaymentEntity();
//        if (modelel == null) {
//            showToast(ChargeActivity.this, "请选择要充值的金额");
//            return;
//        }

//        ApiRequestWrapper.createAlipayOrder(ChargeActivity.this, mUid, model.item_id, 1, new MsgStructure() {
//            @Override
//            public void onReceive(JSONObject data) {
//                distributeMessage(RQF_PAY, data);
//            }
//
//            @Override
//            public void onError(int error, Object ret) {
//                distributeMessage(RQF_PAY_ERROR, null);
//            }
//        });
        String money = et_input_charge.getText().toString();
        if (TextUtils.isEmpty(money) || "0".equals(money)) {
            showToast(ChargeActivity.this, getString(R.string.input_charged_money));
            return;
        }
        showAnimLoading();
        ArrayList<NameValuePair> paramList = new ArrayList<>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_UID, String.valueOf(mUid)));
        paramList.add(new BasicNameValuePair("money", money));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_CHANNEL, "1"));
        ApiRequestWrapper.openAPIGET(this, paramList, AsynHttpClient.API_PAYMENT_ORDER, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                distributeMessage(RQF_PAY, data);
            }

            @Override
            public void onError(int error, Object ret) {
                distributeMessage(RQF_PAY_ERROR, null);
            }
        });
    }

    private void createWXOrder() {
        String money = et_input_charge.getText().toString();
        if (TextUtils.isEmpty(money) || "0".equals(money)) {
            showToast(ChargeActivity.this, getString(R.string.input_charged_money));
            return;
        }
        showAnimLoading();
        ArrayList<NameValuePair> paramList = new ArrayList<>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_MONEY, money));
        ApiRequestWrapper.openAPIPOST(this, paramList, AsynHttpClient.API_CREATE_WEIXIN_ORDER, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("createOrder. data is : " + data);
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(jsonObject -> {
                    dismissAnimLoading();
                    doWXPayment(jsonObject);
                });
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("createOrder. error is : " + error);
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                });
            }
        });
    }

    protected void doWXPayment(JSONObject item) {
        if (item == null) {
            return;
        }
        try {
            PayReq req = new PayReq();
            req.appId = Constans.WX_APP_ID;
            mWXpaymentId = item.getString("payment_id");
            req.partnerId = item.getString("partnerid");
            req.nonceStr = item.getString("noncestr");
            req.packageValue = item.getString("package");
            req.prepayId = item.getString("prepayid");
            req.timeStamp = item.getString("timestamp");
            req.sign = item.getString("sign");
            mWXAPI.sendReq(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setTitleBar() {
        TitleUtil.setTitleBar(this, "充值", v -> {
            onBackPressed();
        }, "充值明细", v -> {
            startActivity(new Intent(ChargeActivity.this, ChargeLogActvitiy.class));
        });
    }

    private void initView() {
        setTitleBar();
        et_input_charge.clearFocus();
        et_input_charge.post(() -> et_input_charge.getGlobalVisibleRect(rect_et_charge));
        updateUserBalance();
        getPaymentList();
        RxTextView.afterTextChangeEvents(et_input_charge).skip(1).subscribe(textViewAfterTextChangeEvent -> {
            String str = textViewAfterTextChangeEvent.editable().toString();
            if (!TextUtils.isEmpty(str)) {
                if (Integer.valueOf(str) > 1000) {
                    showToast(ChargeActivity.this, getString(R.string.charged_money_limited_by_1000));
                    et_input_charge.setText("1000");
                } else if ("0".equals(str)) {
                    showToast(ChargeActivity.this, getString(R.string.charged_money_limited_by_0));
                    et_input_charge.setText("");
                }
            }
        });
    }


    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("lastmoney", lastMoney);
        setResult(RESULT_OK, data);
        super.finish();
    }

    private String lastMoney;

    private void updateUserBalance() {
        showAnimLoading();
        ApiRequestWrapper.getUserInfo(this, mUid, String.valueOf(mUid), new MsgStructure() {
            public void onReceive(final JSONObject data) {
                UserManager.saveUser(ChargeActivity.this, new PWUserModel(data));
                distributeMessage(ID_UPDATE_BALANCE, data);
            }

            public void onError(final int error, Object ret) {
                dismissAnimLoading();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                int rx = (int) ev.getRawX();
                int ry = (int) ev.getRawY();
                if (!rect_et_charge.contains(rx, ry)) {
                    PWUtils.hideSoftKeyBoard(this);
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public void onEventMainThread(Intent intent) {
        if (ACTION_WECHAT_PAYMENT_DONE.equals(intent.getAction()))
            verifyWXPayment();
    }

    private void verifyWXPayment() {
        showAnimLoading();
        ArrayList<NameValuePair> paramList = new ArrayList<>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_PAYMENT_ID, mWXpaymentId));
        ApiRequestWrapper.openAPIGET(this, paramList, AsynHttpClient.API_GET_WEIXIN_PAYMENT_VERIFY, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("API_GET_WEIXIN_PAYMENT_VERIFY data is : " + data);
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                    showToast(ChargeActivity.this, getString(R.string.pay_successful));
                    setResult(RESULT_OK, getIntent());
                    finish();
                });
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("API_GET_WEIXIN_PAYMENT_VERIFY error is : " + error);
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                    if (o == 60001) {
                        showToast(ChargeActivity.this, getString(R.string.pay_failed));
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
