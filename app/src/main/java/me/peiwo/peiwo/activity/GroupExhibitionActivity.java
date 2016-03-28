package me.peiwo.peiwo.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import com.alibaba.fastjson.JSON;
import com.nostra13.universalimageloader.core.ImageLoader;
import de.hdodenhof.circleimageview.CircleImageView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.GroupMembersNewbiesAdapter;
import me.peiwo.peiwo.constans.GroupConstant;
import me.peiwo.peiwo.model.FeedFlowModel;
import me.peiwo.peiwo.model.GroupMemberModel;
import me.peiwo.peiwo.model.ImageModel;
import me.peiwo.peiwo.model.TabfindGroupModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.SharedPreferencesUtil;
import me.peiwo.peiwo.util.UserManager;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.util.ArrayList;

/**
 * Created by gaoxiang on 2015/12/22.
 */
public class GroupExhibitionActivity extends BaseActivity {
    @Bind(R.id.civ_group_avatar)
    CircleImageView civ_group_avatar;
    @Bind(R.id.tv_group_name)
    TextView tv_group_name;
    @Bind(R.id.tv_group_announcement)
    TextView tv_group_announcement;
    @Bind(R.id.recycler_friends_list)
    RecyclerView mRecyclerview;
    @Bind(R.id.layout_show_more)
    RelativeLayout layout_show_more;
    @Bind(R.id.tv_show_all_members)
    TextView tv_show_all_members;
    @Bind(R.id.iv_group_feed_flow)
    ImageView iv_group_feed_flow;
    @Bind(R.id.tv_group_feed_content)
    TextView tv_group_feed_content;
    @Bind(R.id.tv_join_group)
    TextView tv_join_group;
    @Bind(R.id.tv_reputation_value)
    TextView tv_reputation_value;

    private static final int REQUEST_GROUP_DISPLAY_OR_DELETE_MEMBERS = 0x10;
    public ArrayList<GroupMemberModel> mMemberList = new ArrayList<>();
    private static final int MEMBER_LIMIT_COUNT = 3;
    private String group_id;
    private TabfindGroupModel mGroupModel;
    private GroupMembersNewbiesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_exhibition);
        init();
    }

    private void init() {
        group_id = getIntent().getStringExtra(GroupHomePageActvity.KEY_GROUP_ID);
        if (TextUtils.isEmpty(group_id)) {
            finish();
            return;
        }
        fetchGroupData();
        fetchMembers();
        fetchFeedFlow();
        getPrestige();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerview.setLayoutManager(layoutManager);
    }

    private void fetchGroupData() {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(GroupHomePageActvity.KEY_GROUP_ID, group_id));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_CREATE_GROUP_CHAT, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("fetchGroupData. data is : " + data);
                JSONObject groupData = data.optJSONObject("group");
                Observable.just(groupData).observeOn(AndroidSchedulers.mainThread()).subscribe(GroupExhibitionActivity.this::fillGroupData);
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("fetchGroupData. error is : " + error);
            }
        });
    }

    private void fetchMembers() {
        showAnimLoading();
        mMemberList.clear();
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(GroupHomePageActvity.KEY_GROUP_ID, group_id));
        CustomLog.d("fetchMembers, group_id is : " + group_id);
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_GROUP_MEMBERS, new MsgStructure() {

            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("fetchMembers. data is : " + data);
                JSONArray members = data.optJSONArray("members");
                int length = members.length();
                ArrayList<GroupMemberModel> homepageMembers = new ArrayList<>();
                ArrayList<GroupMemberModel> wholeMembers = new ArrayList<>();
                int row_num = 0;
                //更新最新的群人数，群成员人数
//                mGroupModel.member_number = 0;
                for (int i = 0; i < length; i++) {
                    JSONObject member_json = members.optJSONObject(i);
                    GroupMemberModel member = JSON.parseObject(member_json.toString(), GroupMemberModel.class);
//                    if(!member.member_type.equals(GroupConstant.MemberType.NEWBIE)) {
//                        ++mGroupModel.member_number;
//                    }
                    wholeMembers.add(member);
                    if (i < MEMBER_LIMIT_COUNT) {
                        homepageMembers.add(member);
                        ++row_num;
                    }
                }
//                mGroupModel.total_number = members.length();
                mMemberList.addAll(wholeMembers);
                final int finalRow_num = row_num;
//                mGroupModel.member_counts = members.length()
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                    setRecyclerLayout(finalRow_num);
                    mAdapter = new GroupMembersNewbiesAdapter(GroupExhibitionActivity.this, homepageMembers, mGroupModel);
                    mAdapter.setOnMemberSelectedListener(this::handleOnMemberSelected);
                    mRecyclerview.setAdapter(mAdapter);
                    tv_show_all_members.setText(getString(R.string.show_all_group_members, members.length()));
                });
            }

            private void handleOnMemberSelected(GroupMemberModel memberModel) {
                Intent it = new Intent(GroupExhibitionActivity.this, UserInfoActivity.class);
                it.putExtra(UserInfoActivity.TARGET_UID, memberModel.uid);
                startActivity(it);
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("fetchMembers. error is : " + error + ", ret is : " + ret);
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    showToast(GroupExhibitionActivity.this, getResources().getString(R.string.load_failed));
                    dismissAnimLoading();
                });
            }
        });
    }


    private void setRecyclerLayout(int row_num) {
        MyLinearLayoutManager layoutManager = new MyLinearLayoutManager(this, row_num);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerview.setLayoutManager(layoutManager);
    }

    static class MyLinearLayoutManager extends LinearLayoutManager {

        private int rowCount;

        public MyLinearLayoutManager(Context context, int rowCount) {
            super(context);
            this.rowCount = rowCount;
        }


        @Override
        public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
            View view = recycler.getViewForPosition(0);
            if (view != null) {
                measureChild(view, widthSpec, heightSpec);
                int measuredHeight = view.getMeasuredHeight();
                setMeasuredDimension(widthSpec, measuredHeight * rowCount);
            } else {
                super.onMeasure(recycler, state, widthSpec, heightSpec);
            }
        }
    }

    private void getPrestige() {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("group_id", group_id));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_GROUP_PRESTIGE, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(object -> {
//                    total_prestige_value	y	number	声值.
//                    prestige_value	y	number	声值.
//                    update_time
                    float total_prestige_value = Math.round(object.optDouble("total_prestige_value") * 100) / 100.0f;
                    float prestige_value = Math.round(object.optDouble("prestige_value") * 100) / 100.0f;

                    tv_reputation_value.setText(String.valueOf(total_prestige_value));
                    tv_reputation_value.setTag(prestige_value);
                });
            }

            @Override
            public void onError(int error, Object ret) {

            }
        });
    }

    private void fetchFeedFlow() {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(GroupHomePageActvity.KEY_GROUP_ID, group_id));
        CustomLog.d("fetchFeedFlow. group id is : " + group_id);
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_GROUP_FEED_FLOW, new MsgStructure() {

            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("fetchFeedFlow. data is : " + data);
                JSONArray pubsArray = data.optJSONArray("pubs");
                if (pubsArray != null && pubsArray.length() > 0) {
                    JSONObject pubsObject = pubsArray.optJSONObject(0);
                    FeedFlowModel model = new FeedFlowModel(pubsObject);
                    String thumbnail_url = "";
                    if (model.getImageList() != null) {
                        ImageModel imgModel = model.getImageList().get(0);
                        thumbnail_url = imgModel != null ? imgModel.thumbnail_url : "";
                    }
                    String content = model.getContent();
                    final String finalThumbnail_url = thumbnail_url;
                    Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                        if (!TextUtils.isEmpty(finalThumbnail_url)) {
                            ImageLoader.getInstance().displayImage(finalThumbnail_url, iv_group_feed_flow);
                        }
                        tv_group_feed_content.setText(content);
                    });
                }
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("fetchFeedFlow. error is : " + error);
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    showToast(GroupExhibitionActivity.this, getResources().getString(R.string.load_failed));
                });
            }
        });
    }

    private void fillGroupData(JSONObject groupData) {
        mGroupModel = JSON.parseObject(groupData.toString(), TabfindGroupModel.class);
        tv_group_announcement.setText(TextUtils.isEmpty(mGroupModel.notice) ? getResources().getString(R.string.group_announcement_none) : mGroupModel.notice);
        tv_group_name.setText(mGroupModel.group_name);
        ImageLoader.getInstance().displayImage(mGroupModel.avatar, civ_group_avatar);
    }

    private void prepareJoinGroup(TabfindGroupModel groupModel) {
//        group_id		string	    组ID，如存在，说明为免费群,可直接入群
//        group_prefix	string	    组名
//        order_id		string	    订单ID
//        order_type	string	    订单类型，JOIN_GROUP
//        amount		number	    订单金额,单位为分
//        uid		    number	    用户ID
        if (groupModel == null) {
            return;
        }
        showAnimLoading();
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("group_id", groupModel.group_id));
        ApiRequestWrapper.openAPIPOST(this, params, AsynHttpClient.API_GROUPCHAT_JOIN, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("prepareJoinGroup. data is : " + data);
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(jsonObject -> {
                    dispatchRst(groupModel, jsonObject);
                });
            }

            @Override
            public void onError(int error, Object ret) {
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> {
                    dismissAnimLoading();
//                    10001	PARAMETER_ERROR		group_id没有传入
//                    20004	DATA_ALREADY_EXIST		已经是群成员, 此时直接进入群组内。
//                    20005	DATA_NOT_EXIST		group不存在
//                    20011	GROUP_STOP_RECRUITING		群组已停止招新
//                    20012	GROUP_NOT_PAID              还未支付群门票, 此时有data字段，{'group_id': '121','ticket_price': 120},ticket_price单位分
                    CustomLog.d("prepareJoinGroup. error is : " + error + ", ret is : " + ret);
                    switch (integer) {
                        case 10001:

                            break;
                        case 20004:
                            joinGroupchatStraight(groupModel, GroupConstant.MemberType.MEMBER);
                            break;
                        case 20005:
                            Toast.makeText(GroupExhibitionActivity.this, "failed", Toast.LENGTH_SHORT).show();
                            break;
                        case 20011:
                            Toast.makeText(GroupExhibitionActivity.this, R.string.stop_zhaoxin, Toast.LENGTH_SHORT).show();
                            break;
                        case 20012:
                            JSONObject obj = (JSONObject) ret;
                            JSONObject data = obj.optJSONObject("data");
                            int ticketPrice = data.optInt("ticket_price");
                            Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(integer1 -> {
                                dismissAnimLoading();
                                showJoinGroupOrder(ticketPrice, groupModel);
                            });
                            break;
                        case 20003:
                            Toast.makeText(GroupExhibitionActivity.this, "声望值达到50分开启入群权限，多打电话攒分吧", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(GroupExhibitionActivity.this, "failed", Toast.LENGTH_SHORT).show();
                            break;
                    }
                });
            }
        });
    }

    private void dispatchRst(TabfindGroupModel groupModel, JSONObject object) {
        String group_id = object.optString("group_id");
        if (!TextUtils.isEmpty(group_id)) {
            //直接入群
            dismissAnimLoading();
            joinGroupchatStraight(groupModel, GroupConstant.MemberType.NEWBIE);
        } else {
            checkGroupWithPay(groupModel);
        }
    }

    private void checkGroupWithPay(TabfindGroupModel groupModel) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("group_id", groupModel.group_id));
        ApiRequestWrapper.openAPIPOST(this, params, AsynHttpClient.API_GROUPCHAT_JOIN_ORDER, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
//                group_id	y	string	群id
//                order_id	y	number	订单号
//                ticket_price	y	number	门票钱，单位分，因为门票价格实时变化，所以客户端拿此值显示，不能按照之前的群信息价格显示门票价格
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(jsonObject -> {
                    dismissAnimLoading();
                    int ticketPrice = jsonObject.optInt("ticket_price");
                    String money = UserManager.getPWUser(GroupExhibitionActivity.this).money;
                    float moneyFloat = Float.valueOf(money);
                    int moneyInt = (int) (moneyFloat * 100);
                    CustomLog.d("checkGroupWithPay moneyInt is : " + moneyInt + ", ticketPrice is : " + ticketPrice);
//                    if (moneyInt > ticketPrice) {
                    payJoinGroupOrder(moneyInt, ticketPrice, groupModel, jsonObject.optInt("order_id"));
//                    } else {
//                        charge();
//                    }
                });
            }

            @Override
            public void onError(int error, Object ret) {
//                10001	PARAMETER_ERROR	group_id没有传入
//                20004	DATA_ALREADY_EXIST	已经是群成员, 无需门票.此时直接进入群组内。
//                20005	DATA_NOT_EXIST	group不存在
//                20013	GROUP_FREE	群组不收钱，无需订单
//                60002	LOW_BALANCE	余额不足
                Resources res = getResources();
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> {
                    dismissAnimLoading();
                    switch (error) {
                        case 20004:
                            joinGroupchatStraight(groupModel, GroupConstant.MemberType.MEMBER);
                            break;
                        case 20005:
                            Snackbar.make(tv_join_group, res.getString(R.string.group_id_invalid), Snackbar.LENGTH_SHORT).show();
                            break;
                        case 20013:
                            joinGroupchatStraight(groupModel, GroupConstant.MemberType.NEWBIE);
                            break;
                        case 20014:
                            Snackbar.make(tv_join_group, "该群已到达人数上限", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 60002:
                            charge();
                            break;
                        default:
                            Snackbar.make(tv_join_group, res.getString(R.string.join_group_failed), Snackbar.LENGTH_SHORT).show();
                            break;
                    }
                });
            }
        });
    }

    private void showJoinGroupOrder(int amount, TabfindGroupModel groupModel) {
        Resources res = getResources();
        new AlertDialog.Builder(this)
                .setTitle(String.format(res.getString(R.string.you_need_pay_group_ticket_with_money), amount / 100.0f))
                .setNegativeButton(res.getString(R.string.cancel), null)
                .setPositiveButton(res.getString(R.string.go_on), (dialog, which) -> {
                    checkGroupWithPay(groupModel);
                })
                .create().show();
    }

    private void payJoinGroupOrder(int money, int amount, TabfindGroupModel groupModel, int order_id) {
        Resources res = getResources();
        new AlertDialog.Builder(this)
                .setTitle(String.format(res.getString(R.string.confirm_to_pay_with_money), amount / 100.0f))
                .setNegativeButton(res.getString(R.string.cancel), null)
                .setPositiveButton(res.getString(R.string.pay), (dialog, which) -> {
                    if (money > amount) {
                        payGroupTicket(groupModel, order_id);
                    } else {
                        charge();
                    }
                })
                .create().show();
    }

    private void charge() {
        Resources res = getResources();
        new AlertDialog.Builder(this)
                .setTitle(res.getString(R.string.your_balance_is_not_enough_for_join_group))
                .setNegativeButton(res.getString(R.string.cancel), null)
                .setPositiveButton(res.getString(R.string.charge), (dialog, which) -> {
                    this.startActivity(new Intent(this, ChargeActivity.class));
                })
                .create().show();
    }

    private void payGroupTicket(TabfindGroupModel groupModel, int order_id) {
        showAnimLoading();
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("group_id", groupModel.group_id));
        params.add(new BasicNameValuePair("order_id", String.valueOf(order_id)));
        ApiRequestWrapper.openAPIPOST(this, params, AsynHttpClient.API_GROUPCHAT_PAY_JOIN_ORDER, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("payGroupTicket. data is : " + data);
//                group_id	y	string	群id
//                order_id	y	number	订单号
//                balance 用户余额，单位分，请讲此值更新至数据库
                UserManager.updateMoney(GroupExhibitionActivity.this, data.optString("balance"));
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(jsonObject -> {
                    dismissAnimLoading();
                    joinGroupchatStraight(groupModel, GroupConstant.MemberType.NEWBIE);
                });
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("payGroupTicket. error is : " + error);
//                20004	DATA_ALREADY_EXIST		已经是成员，无需支付。此时直接进入群组内。
//                60003	INVALID_TRANSACTION	该订单不存在/该订单已失效	订单在数据库里不存在，该订单的信息与传入的group_id不一致，或者该订单已经被取消
//                60004	DUPLIACTE_PAY	该订单已经被支付	该订单已经被支付
//                60001	PAYMENT_ERROR	支付失败	用户余额不足等导致的支付失败
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> {
                    dismissAnimLoading();
                    Resources res = getResources();
                    switch (error) {
                        case 20004:
                            joinGroupchatStraight(groupModel, GroupConstant.MemberType.MEMBER);
                            break;
                        case 20014:
                            Snackbar.make(tv_join_group, "该群已到达人数上限", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 60003:
                        case 60004:
                        case 60001:
                            Snackbar.make(tv_join_group, res.getString(R.string.group_join_order_error), Snackbar.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                });
            }
        });
    }

    private void joinGroupchatStraight(TabfindGroupModel groupModel, String curr_member_type) {
        Intent intent = new Intent(this, GroupChatActivity.class);
        if (GroupConstant.MemberType.NEWBIE.equals(curr_member_type)) {
            //新人
            boolean silent = SharedPreferencesUtil.getBooleanExtra(this, GroupChatActivity.K_NEED_SILENT, true);
            intent.putExtra(GroupChatActivity.K_NEED_SILENT, silent);
        }
        intent.putExtra(GroupChatActivity.K_GROUP_DATA, groupModel);
        startActivity(intent);
    }

    public void click(View v) {
        switch (v.getId()) {
            case R.id.layout_show_more:
                Intent it = new Intent(this, GroupMembersNewbiesActivity.class);
                it.putExtra(GroupHomePageActvity.K_GROUP_DATA, mGroupModel);
                it.putExtra("show_edit_button", false);
                startActivityForResult(it, REQUEST_GROUP_DISPLAY_OR_DELETE_MEMBERS);
                break;
            case R.id.layout_group_feed:
                it = new Intent(this, FeedFlowActivity.class);
                it.putExtra(FeedFlowActivity.KEY_GROUP_FEED_FLOW, true);
                it.putExtra(GroupHomePageActvity.KEY_GROUP_ID, group_id);
                startActivity(it);
                break;
            case R.id.tv_join_group:
                prepareJoinGroup(mGroupModel);
                break;
            case R.id.v_group_notice:
                Intent intent = new Intent(this, UpdateGroupNoticeActivity.class);
                //String announcement = tv_group_announcement.getText().toString();
                intent.putExtra(UpdateGroupNoticeActivity.K_GROUP_NOTICE, mGroupModel.notice);
                //intent.putExtra(UpdateGroupNoticeActivity.K_MEMBER_TYPE, mGroupModel.member_type);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
