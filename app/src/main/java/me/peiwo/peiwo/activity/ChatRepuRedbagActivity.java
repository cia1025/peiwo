package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import butterknife.Bind;
import com.alibaba.fastjson.JSON;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.TabfindGroupModel;
import me.peiwo.peiwo.model.groupchat.PacketIconModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.util.ArrayList;

public class ChatRepuRedbagActivity extends BaseActivity {
    public static final String K_TOTAL_PRESTIGE_VALUE = "total_prestige_value";
    public static final String K_TOTAL_MEMBERS = "total_member";
    public static final String K_GROUP_ID = "group_id";
    @Bind(R.id.tv_total_repu)
    TextView tv_total_repu;
    @Bind(R.id.tv_repuredbag_count)
    TextView tv_repuredbag_count;
    private double total_prestige_value;
    private int total_member;
    private String group_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_repu_redbag);
        init();
        getGroupInfo();
    }

    private void getGroupInfo() {
        //API_CREATE_GROUP_CHAT
        ArrayList<NameValuePair> parpms = new ArrayList<>();
        parpms.add(new BasicNameValuePair("group_id", group_id));
        ApiRequestWrapper.openAPIGET(this, parpms, AsynHttpClient.API_CREATE_GROUP_CHAT, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(object -> {
                    String group = object.optString("group");
                    TabfindGroupModel groupModel = JSON.parseObject(group, TabfindGroupModel.class);
                    total_member = groupModel.member_number;
                    tv_repuredbag_count.setText(String.valueOf(total_member));
                });
            }

            @Override
            public void onError(int error, Object ret) {

            }
        });
    }

    private void init() {
        setTitle("群声望包");
        Intent data = getIntent();
        group_id = data.getStringExtra(K_GROUP_ID);
        total_prestige_value = data.getDoubleExtra(K_TOTAL_PRESTIGE_VALUE, 0);
        total_member = data.getIntExtra(K_TOTAL_MEMBERS, 1);
        tv_total_repu.setText(String.valueOf(total_prestige_value));
        //最大数目的红包个数
        tv_repuredbag_count.setText(String.valueOf(total_member));
    }

    @Override
    public void click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_repuredbag_send:
                if (check(v)) {
                    sendRepuRedbag();
                }
                break;

            default:
                break;
        }
    }

    private boolean check(View v) {
        if (total_prestige_value < total_member * 0.01) {
            Snackbar.make(v, "当前群声望不足", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void sendRepuRedbag() {
        showAnimLoading();
//        score	y	number	群声望
//        quantity	y	number	红包数,红包数
//        group_id	y	string	群组id
//        msg	n	string	留言
//        icon_url
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("score", String.valueOf(total_prestige_value)));
        params.add(new BasicNameValuePair("quantity", String.valueOf(total_member)));
        params.add(new BasicNameValuePair("group_id", group_id));
        params.add(new BasicNameValuePair("msg", "点击图片抢声望"));
        params.add(new BasicNameValuePair("icon_url", ""));
        ApiRequestWrapper.openAPIPOST(this, params, AsynHttpClient.API_GROUP_SCORE_PACKET_SEND, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(object -> {
                    dismissAnimLoading();
                    PacketIconModel packetIconModel = new PacketIconModel();
                    packetIconModel.msg = object.optString("msg");
                    packetIconModel.id = object.optString("packet_id");
                    packetIconModel.send_icon = object.optString("icon_url");
                    Intent intent = new Intent();
                    intent.putExtra(ChatRedbagActivity.K_SINGLE_PACKET, packetIconModel);
                    setResult(RESULT_OK, intent);
                    finish();
                });
            }

            @Override
            public void onError(int error, Object ret) {
//                10001	PARAMETER_ERROR	score，quantity，group_id没有传入，传入的quantity>score, 或者传入的group_id不存在
//                60002	LOW_BALANCE	群声望余额不足
//                70001	PACKET_TOO_LARGE	声望超过上限
//                70002	PACKET_QUANTITY_TOO_LARGE	数量超过上限
//                10003	PERMISSION_ERROR	无权限发群声望红包
                Object[] objects = new Object[]{error, ret};
                Observable.just(objects).observeOn(AndroidSchedulers.mainThread()).subscribe(rst -> {
                    dismissAnimLoading();
                    int err = (int) rst[0];
                    Object o = rst[1];
                    switch (err) {
                        case 60002:
                            if (!parseError(o))
                                Snackbar.make(tv_repuredbag_count, "群声望余额不足", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 70001:
                            if (!parseError(o))
                                Snackbar.make(tv_repuredbag_count, "声望超过上限", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 70002:
                            if (!parseError(o))
                                Snackbar.make(tv_repuredbag_count, "数量超过上限", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 10003:
                            if (!parseError(o))
                                Snackbar.make(tv_repuredbag_count, "无权限发群声望红包", Snackbar.LENGTH_SHORT).show();
                            break;
                        default:
                            if (!parseError(o))
                                Snackbar.make(tv_repuredbag_count, "未知错误", Snackbar.LENGTH_SHORT).show();
                            break;
                    }
                });
            }
        });
    }

    private boolean parseError(Object o) {
        if (o instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) o;
            String msg = jsonObject.optString("msg");
            if (!TextUtils.isEmpty(msg)){
                Snackbar.make(tv_repuredbag_count, msg, Snackbar.LENGTH_SHORT).show();
                return true;
            }
            return false;
        }
        return false;
    }
}
