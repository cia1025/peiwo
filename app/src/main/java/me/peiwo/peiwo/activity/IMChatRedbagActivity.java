package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.Bind;
import com.alibaba.fastjson.JSON;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.ChatRedBagAdapter;
import me.peiwo.peiwo.model.groupchat.PacketIconModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.UserManager;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import java.util.ArrayList;
import java.util.List;

public class IMChatRedbagActivity extends BaseActivity {
    private static final int REQUEST_CODE_MORE_PACKETS = 1000;
    public static final String K_MORE_PACKETS = "packets";
    public static final String K_SINGLE_PACKET = "packet";
    public static final String K_REDBAG_TYPE = "r_type";
    public static final int REDBAG_TYPE_PERSONAL = 1;
    public static final int REDBAG_TYPE_GROUP = 2;

    @Bind(R.id.v_recycler_redbags)
    RecyclerView v_recycler_redbags;
    @Bind(R.id.et_redbag_leaveword)
    EditText et_redbag_leaveword;
    @Bind(R.id.et_money_total)
    EditText et_money_total;
    @Bind(R.id.tv_alert)
    TextView tv_alert;
    @Bind(R.id.v_lable_money_total)
    TextView v_lable_money_total;
    @Bind(R.id.tv_redbag_img_provider)
    TextView tv_redbag_img_provider;
    @Bind(R.id.v_lable_money_unit)
    TextView v_lable_money_unit;

    private ChatRedBagAdapter adapter;
    private List<PacketIconModel> packIconsList = new ArrayList<>();
    private int redbag_type;
    private Rect r_et_money_total = new Rect();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imchat_redbag);
//        v_send_redbag.post(() -> v_send_redbag.setEnabled(false));
        init();
        et_money_total.post(() -> et_money_total.getGlobalVisibleRect(r_et_money_total));
    }

    private void init() {
        Intent data = getIntent();
        redbag_type = data.getIntExtra(IMChatRedbagActivity.K_REDBAG_TYPE, IMChatRedbagActivity.REDBAG_TYPE_PERSONAL);
        if (redbag_type == REDBAG_TYPE_GROUP) {
            setTitle("收益红包");
            //点击图片速抢钱
            et_redbag_leaveword.setHint("点击图片抢群收益");
        } else {
            setTitle("壕の技能");
            et_redbag_leaveword.setHint("我是红包快戳我");
        }
        //myBalance = (int) (Float.valueOf(UserManager.getPWUser(this).money) * 100);
        et_redbag_leaveword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
        checkMoneyTotal();
        getRedbagsIcons();
        combineLatestTextChangeForPersonal();
    }

    private void combineLatestTextChangeForPersonal() {
        RxTextView.afterTextChangeEvents(et_money_total).observeOn(AndroidSchedulers.mainThread()).subscribe(moneySumEvent -> {
            String moneyTemp = moneySumEvent.editable().toString();
            if(TextUtils.isEmpty(moneyTemp))
                moneyTemp = "0";
            float money_sum = Float.valueOf(moneyTemp);
            if(money_sum > 999) {
                moneySum999Error();
            } else {
                moneySumNice();
            }
        });
    }

    private void moneySumNice() {
        tv_alert.setText(null);
        et_money_total.setTextColor(getResources().getColor(R.color.text_normal_color));
        v_lable_money_total.setTextColor(getResources().getColor(R.color.text_normal_color));
        v_lable_money_unit.setTextColor(getResources().getColor(R.color.text_normal_color));
    }

    private void moneySum999Error() {
        tv_alert.setText("红包金额不可超过999元");
        et_money_total.setTextColor(getResources().getColor(R.color.c_red));
        v_lable_money_total.setTextColor(getResources().getColor(R.color.c_red));
        v_lable_money_unit.setTextColor(getResources().getColor(R.color.c_red));
    }

    private void checkMoneyTotal() {
        et_money_total.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            String src = et_money_total.getText().toString();
            if (src.contains(".")) {
                if (src.substring(src.indexOf(".")).length() > 2) {
                    return "";
                }
            }
            return null;
        }, new InputFilter.LengthFilter(6)});
    }

    private void getRedbagsIcons() {
        showAnimLoading();
        ArrayList<NameValuePair> params = new ArrayList<>();
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_GROUPCHAT_PACKET_ICONS, new MsgStructure() {
            @Override
            public boolean onInterceptRawData(String rawStr) {
                Observable.just(rawStr).observeOn(AndroidSchedulers.mainThread()).subscribe(IMChatRedbagActivity.this::fetchPackIcons);
                return true;
            }

            @Override
            public void onReceive(JSONObject data) {

            }

            @Override
            public void onError(int error, Object ret) {
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> dismissAnimLoading());
            }
        });
    }

    private void fetchPackIcons(String s) {
        try {
            dismissAnimLoading();
            JSONObject object = new JSONObject(s);
            JSONArray array = object.optJSONArray("data");
            for (int i = 0, z = array.length(); i < z; i++) {
                packIconsList.add(JSON.parseObject(array.optString(i), PacketIconModel.class));
            }
            v_recycler_redbags.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            v_recycler_redbags.setLayoutManager(linearLayoutManager);
            PacketIconModel packetIconModel = packIconsList.get(0);
            tv_redbag_img_provider.setText(packetIconModel.msg);
            adapter = new ChatRedBagAdapter(this, packetIconModel);
            v_recycler_redbags.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.v_send_redbag:
                sendRedBag();
                break;
            case R.id.v_more_packets:
                Intent intent = new Intent(this, GroupchatMorePacketActivity.class);
                intent.putParcelableArrayListExtra(K_MORE_PACKETS, (ArrayList<PacketIconModel>) packIconsList);
                startActivityForResult(intent, REQUEST_CODE_MORE_PACKETS);
                break;
            default:
                break;
        }
    }


    private void sendRedBag() {
        showAnimLoading();
        String api = AsynHttpClient.API_PERSONAL_PACKET_SEND;
        if (redbag_type == IMChatRedbagActivity.REDBAG_TYPE_GROUP) {
            api = AsynHttpClient.API_GROUP_MONEY_PACKET_SEND;
        }
        PacketIconModel packetIconModel = adapter.getSelectedPacketIcon();
        packetIconModel.msg = et_redbag_leaveword.getText().toString();
        ArrayList<NameValuePair> params = new ArrayList<>();

        long money_cent = Math.round((Double.valueOf(et_money_total.getText().toString()) * 10 * 10));
        params.add(new BasicNameValuePair("money", String.valueOf(money_cent)));
        params.add(new BasicNameValuePair("tuid", String.valueOf(getIntent().getIntExtra("tuid", 0))));
        params.add(new BasicNameValuePair("msg", TextUtils.isEmpty(et_redbag_leaveword.getText()) ? et_redbag_leaveword.getHint().toString() : et_redbag_leaveword.getText().toString()));
        params.add(new BasicNameValuePair("icon_url", packetIconModel.send_icon));
        ApiRequestWrapper.openAPIPOST(this, params, api, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(object -> {
                    dismissAnimLoading();
                    if (redbag_type == IMChatRedbagActivity.REDBAG_TYPE_PERSONAL) {
                        UserManager.updateMoney(IMChatRedbagActivity.this, object.optString("balance"));
                    }
                    packetIconModel.id = object.optString("packet_id");
                    Intent intent = getIntent();
                    intent.putExtra(K_SINGLE_PACKET, packetIconModel);
                    setResult(RESULT_OK, intent);
                    finish();
                });
            }

            @Override
            public void onError(int error, Object ret) {
                Object[] objects = new Object[]{error, ret};
                Observable.just(objects).observeOn(AndroidSchedulers.mainThread()).subscribe(rst -> {
                    dismissAnimLoading();
//                    10001	PARAMETER_ERROR	money，quantity，group_id没有传入，传入的quantity>money, 或者传入的group_id不存在
//                    60002	LOW_BALANCE	用户余额不足
//                    70001	PACKET_MONEY_TOO_LARGE	红包金额超过上限
//                    70002	PACKET_QUANTITY_TOO_LARGE	红包数量超过上限
                    //group error
//                    10001	PARAMETER_ERROR	score，quantity，group_id没有传入，传入的quantity>score, 或者传入的group_id不存在
//                    60002	LOW_BALANCE	群声望余额不足
//                    70001	PACKET_TOO_LARGE	声望超过上限
//                    70002	PACKET_QUANTITY_TOO_LARGE	数量超过上限
//                    10003	PERMISSION_ERROR	无权限发群声望红包
                    int err = (int) rst[0];
                    Object o = rst[1];
                    switch (err) {
                        case 60002:
                            if (redbag_type != REDBAG_TYPE_GROUP) {
                                alertCharge();
                            } else {
                                parseError(o);
                            }
                            break;
                        case 70001:
                            if (!parseError(o))
                                Snackbar.make(v_recycler_redbags, "红包金额超过上限", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 70002:
                            if (!parseError(o))
                                Snackbar.make(v_recycler_redbags, "红包数量超过上限", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 10003:
                            if (!parseError(o))
                                Snackbar.make(v_recycler_redbags, "无权限发群声望红包", Snackbar.LENGTH_SHORT).show();
                            break;

                        default:
                            if (!parseError(o))
                                Snackbar.make(v_recycler_redbags, "网络连接错误", Snackbar.LENGTH_SHORT).show();
                            break;
                    }
                });
            }
        });
    }

    private void alertCharge() {
        new AlertDialog.Builder(this)
                .setTitle("余额不足，充个值再来发吧！")
                .setPositiveButton("充值", (dialog, which) -> {
                    startActivity(new Intent(this, ChargeActivity.class));
                }).setNegativeButton("取消", null)
                .create().show();
    }

    private boolean parseError(Object o) {
        if (o instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) o;
            String msg = jsonObject.optString("msg");
            if (!TextUtils.isEmpty(msg)) {
                Snackbar.make(v_recycler_redbags, msg, Snackbar.LENGTH_SHORT).show();
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_MORE_PACKETS:
                    int g_position = data.getIntExtra(GroupchatMorePacketActivity.K_GROUP_POSITION, -1);
                    int c_position = data.getIntExtra(GroupchatMorePacketActivity.K_CHILD_POSITION, -1);
                    if (g_position >= 0 && c_position >= 0) {
                        PacketIconModel packetIconModel = packIconsList.get(g_position);
                        tv_redbag_img_provider.setText(packetIconModel.msg);
                        adapter.reload(packetIconModel, c_position);
                    }
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                int rx = (int) ev.getRawX();
                int ry = (int) ev.getRawY();
                if (!r_et_money_total.contains(rx, ry)) {
                    PWUtils.hideSoftKeyBoard(this);
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
