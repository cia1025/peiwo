package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.BaseActivity;
import me.peiwo.peiwo.activity.ChargeActivity;
import me.peiwo.peiwo.activity.GroupChatActivity;
import me.peiwo.peiwo.activity.GroupHomePageActvity;
import me.peiwo.peiwo.constans.GroupConstant;
import me.peiwo.peiwo.model.TabfindGroupModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.SharedPreferencesUtil;
import me.peiwo.peiwo.util.UserManager;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fuhaidong on 15/12/10.
 */
public class GroupJoinBaseAdapter<T> extends PPBaseAdapter<T> {
    private Context context;

    public GroupJoinBaseAdapter(Context context, List<T> mList) {
        super(mList);
        this.context = context;
    }

    protected void joinGroup(View target, TabfindGroupModel groupModel) {
        if (context != null && context instanceof BaseActivity) {
            prepareJoinGroup(target, groupModel);
        }
    }

    private void prepareJoinGroup(View target, TabfindGroupModel groupModel) {
//        group_id		string	    组ID，如存在，说明为免费群,可直接入群
//        group_prefix	string	    组名
//        order_id		string	    订单ID
//        order_type	string	    订单类型，JOIN_GROUP
//        amount		number	    订单金额,单位为分
//        uid		    number	    用户ID
        ((BaseActivity) context).showAnimLoading();
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("group_id", groupModel.group_id));
        ApiRequestWrapper.openAPIPOST(context, params, AsynHttpClient.API_GROUPCHAT_JOIN, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(jsonObject -> {
                    dispatchRst(groupModel, jsonObject, target);
                });
            }

            @Override
            public void onError(int error, Object ret) {
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> {
                    ((BaseActivity) context).dismissAnimLoading();
//                    10001	PARAMETER_ERROR		group_id没有传入
//                    20004	DATA_ALREADY_EXIST		已经是群成员, 此时直接进入群组内。
//                    20005	DATA_NOT_EXIST		group不存在
//                    20011	GROUP_STOP_RECRUITING		群组已停止招新
//                    20012	GROUP_NOT_PAID              还未支付群门票, 此时有data字段，{'group_id': '121','ticket_price': 120},ticket_price单位分
                    CustomLog.d("prepareJoinGroup. onError. ret is : " + ret);
                    switch (integer) {
                        case 10001:

                            break;
                        case 20004:
                            joinGroupchatStraight(groupModel, GroupConstant.MemberType.MEMBER);
                            break;
                        case 20005:
                            Snackbar.make(target, "群组不存在", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 20011:
                            Snackbar.make(target, R.string.stop_zhaoxin, Snackbar.LENGTH_SHORT).show();
                            break;
                        case 20012:
                            JSONObject obj = (JSONObject) ret;
                            JSONObject data = obj.optJSONObject("data");
                            int ticketPrice = data.optInt("ticket_price");
                            Observable.just(ticketPrice).observeOn(AndroidSchedulers.mainThread()).subscribe(price -> {
                                ((BaseActivity) context).dismissAnimLoading();
                                showJoinGroupOrder(price, groupModel, target);
                            });
                            break;
                        case 20003:
                            Snackbar.make(target, "声望值达到50分开启入群权限，多打电话攒分吧", Snackbar.LENGTH_SHORT).show();
                            break;
                        default:
                            Snackbar.make(target, "加入群组失败", Snackbar.LENGTH_SHORT).show();
                            break;
                    }
                });
            }
        });
    }

    private void dispatchRst(TabfindGroupModel groupModel, JSONObject object, View target) {
        String group_id = object.optString(GroupHomePageActvity.KEY_GROUP_ID);
        if (!TextUtils.isEmpty(group_id)) {
            ((BaseActivity) context).dismissAnimLoading();
            joinGroupchatStraight(groupModel, GroupConstant.MemberType.NEWBIE);
        } else {
            Snackbar.make(target, context.getResources().getString(R.string.group_id_invalid), Snackbar.LENGTH_LONG).show();
        }
    }

    private void checkGroupWithPay(TabfindGroupModel groupModel, View target) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("group_id", groupModel.group_id));
        ApiRequestWrapper.openAPIPOST(context, params, AsynHttpClient.API_GROUPCHAT_JOIN_ORDER, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
//                group_id	y	string	群id
//                order_id	y	number	订单号
//                ticket_price	y	number	门票钱，单位分，因为门票价格实时变化，所以客户端拿此值显示，不能按照之前的群信息价格显示门票价格
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(jsonObject -> {
                    ((BaseActivity) context).dismissAnimLoading();
                    int ticketPrice = jsonObject.optInt("ticket_price");
                    String money = UserManager.getPWUser(context).money;
                    float moneyFloat = Float.valueOf(money);
                    int moneyInt = (int) (moneyFloat * 100);
                    if (moneyInt > ticketPrice) {
                        payJoinGroupOrder(ticketPrice, groupModel, jsonObject.optInt("order_id"), target);
                    } else {
                        charge();
                    }
                });
            }

            @Override
            public void onError(int error, Object ret) {
//                10001	PARAMETER_ERROR	group_id没有传入
//                20004	DATA_ALREADY_EXIST	已经是群成员, 无需门票.此时直接进入群组内。
//                20005	DATA_NOT_EXIST	group不存在
//                20013	GROUP_FREE	群组不收钱，无需订单
//                60002	LOW_BALANCE	余额不足
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> {
                    ((BaseActivity) context).dismissAnimLoading();
                    Resources res = context.getResources();
                    switch (error) {
                        case 20004:
                            joinGroupchatStraight(groupModel, GroupConstant.MemberType.MEMBER);
                            break;
                        case 20005:
                            Snackbar.make(target, res.getString(R.string.group_id_invalid), Snackbar.LENGTH_SHORT).show();
                            break;
                        case 20013:
                            joinGroupchatStraight(groupModel, GroupConstant.MemberType.NEWBIE);
                            break;
                        case 20014:
                            Snackbar.make(target, "该群已到达人数上限", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 60002:
                            charge();
                            break;
                        default:
                            Snackbar.make(target, "加入群组失败", Snackbar.LENGTH_SHORT).show();
                            break;
                    }
                });
            }
        });
    }

    private void showJoinGroupOrder(int amount, TabfindGroupModel groupModel, View target) {
        Resources res = context.getResources();
        new AlertDialog.Builder(context)
                .setTitle(String.format(res.getString(R.string.you_need_pay_group_ticket_with_money), amount / 100.0f))
                .setNegativeButton(res.getString(R.string.cancel), null)
                .setPositiveButton(res.getString(R.string.go_on), (dialog, which) -> {
                    checkGroupWithPay(groupModel, target);
                })
                .create().show();
    }

    private void payJoinGroupOrder(int amount, TabfindGroupModel groupModel, int order_id, View target) {
        Resources res = context.getResources();
        new AlertDialog.Builder(context)
                .setTitle(String.format(res.getString(R.string.confirm_to_pay_with_money), amount / 100.0f))
                .setNegativeButton(res.getString(R.string.cancel), null)
                .setPositiveButton(res.getString(R.string.pay), (dialog, which) -> {
                    payGroupTicket(groupModel, order_id, target);
                })
                .create().show();
    }

    private void charge() {
        Resources res = context.getResources();
        new AlertDialog.Builder(context)
                .setTitle(res.getString(R.string.your_balance_is_not_enough_for_join_group))
                .setNegativeButton(res.getString(R.string.cancel), null)
                .setPositiveButton(res.getString(R.string.charge), (dialog, which) -> {
                    context.startActivity(new Intent(context, ChargeActivity.class));
                })
                .create().show();
    }

    private void payGroupTicket(TabfindGroupModel groupModel, int order_id, View target) {
        ((BaseActivity) context).showAnimLoading();
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("group_id", groupModel.group_id));
        params.add(new BasicNameValuePair("order_id", String.valueOf(order_id)));
        ApiRequestWrapper.openAPIPOST(context, params, AsynHttpClient.API_GROUPCHAT_PAY_JOIN_ORDER, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
//                group_id	y	string	群id
//                order_id	y	number	订单号
//                balance 用户余额，单位分，请讲此值更新至数据库
                UserManager.updateMoney(context, data.optString("balance"));
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(jsonObject -> {
                    ((BaseActivity) context).dismissAnimLoading();
                    joinGroupchatStraight(groupModel, GroupConstant.MemberType.NEWBIE);
                });
            }

            @Override
            public void onError(int error, Object ret) {
//                20004	DATA_ALREADY_EXIST		已经是成员，无需支付。此时直接进入群组内。
//                60003	INVALID_TRANSACTION	该订单不存在/该订单已失效	订单在数据库里不存在，该订单的信息与传入的group_id不一致，或者该订单已经被取消
//                60004	DUPLIACTE_PAY	该订单已经被支付	该订单已经被支付
//                60001	PAYMENT_ERROR	支付失败	用户余额不足等导致的支付失败
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> {
                    ((BaseActivity) context).dismissAnimLoading();
                    switch (error) {
                        case 20004:
                            joinGroupchatStraight(groupModel, GroupConstant.MemberType.MEMBER);
                            break;
                        case 20014:
                            Snackbar.make(target, "该群已到达人数上限", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 60003:
                        case 60004:
                        case 60001:
                            Snackbar.make(target, "订单错误", Snackbar.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                });
            }
        });
    }

    private void joinGroupchatStraight(TabfindGroupModel groupModel, String curr_member_type) {
        Intent intent = new Intent(context, GroupChatActivity.class);
        if (GroupConstant.MemberType.NEWBIE.equals(curr_member_type)) {
            //新人
            boolean silent = SharedPreferencesUtil.getBooleanExtra(context, String.format("%s_%s", GroupChatActivity.K_NEED_SILENT, groupModel.group_id), true);
            intent.putExtra(GroupChatActivity.K_NEED_SILENT, silent);
        }
        intent.putExtra(GroupChatActivity.K_GROUP_DATA, groupModel);
        context.startActivity(intent);
        SharedPreferencesUtil.putBooleanExtra(context, GroupChatActivity.K_NEED_SILENT, false);
    }
}
