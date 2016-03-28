package me.peiwo.peiwo.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.Bind;
import com.alibaba.fastjson.JSON;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.qiniu.android.http.ResponseInfo;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.GroupMembersNewbiesAdapter;
import me.peiwo.peiwo.callback.UploadCallback;
import me.peiwo.peiwo.constans.GroupConstant;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.fragment.TabFriendFragment;
import me.peiwo.peiwo.model.FeedFlowModel;
import me.peiwo.peiwo.model.GroupMemberModel;
import me.peiwo.peiwo.model.ImageModel;
import me.peiwo.peiwo.model.TabfindGroupModel;
import me.peiwo.peiwo.model.groupchat.PacketIconModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.PWUploader;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.ImageUtil;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.UserManager;
import net.simonvt.numberpicker.NumberPicker;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

/**
 * Created by gaoxiang on 2015/12/11.
 */
public class GroupHomePageActvity extends BaseActivity {
    public static final int RESULT_GROUP_REDBAG = 2;
    public static final int RESULT_GROUP_REPU_REDBAG = 3;
    public static final int RESULT_GROUP_UPDATE = 4;
    public static final int RESULT_QUIT_AND_DELETE_GROUP = 5;
    /***************************/
    public static final String KEY_IS_GROUP = "is_group";
    public static final String KEY_GROUP_ID = "group_id";
    public static final String KEY_ADMIN_ID = "admin_id";
    public static final String KEY_GROUP_MEMBER_ID = "member_ids";
    public static final String KEY_GROUP_NAME = "group_name";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_MEMBER_TYPE = "member_type";
    public static final String KEY_GROUP_AVATAR = "avatar";
    public static final String KEY_TICKET_PRICE = "ticket_price";
    public static final String KEY_IS_RECRUITING = "is_recruiting";
    public static final String KEY_MEMBERS = "members";
    public static final String KEY_IPP = "ipp";
    public static final String KEY_NICK_NAME = "nickname";
    public static final String KEY_SHOW_NICK_NAME = "show_nickname";
    public static final String KEY_NOTIFY_FLAG = "notify_flag";
    public static final String K_GROUP_DATA = "group_data";
    public static final String KEY_MEMBER_LIST = "member_list";
    public static final String KEY_MEMBER_IDS = "member_ids";
    private static final int REQUEST_GROUP_DISPLAY_OR_DELETE_MEMBERS = 0x10;
    private static final int REQUEST_GROUP_ADD_FRIENDS_TO_BE_MEMBERS = 0x11;
    private static final int REQUEST_CODE_START_ALBUM = 0x12;
    private static final int REQUEST_GROUP_ANNOUNCEMENT = 0x13;
    private static final int MEMBER_LIMIT_COUNT = 3;
    private static final double PICKER_STAGE_VALUE = 0.1;
    private static final int REQUEST_CODE_GROUP_REPU = 1000;
    private static final int REQUEST_CODE_GROUP_REDBAG = 1001;
    private static String mImageKey;
    public ArrayList<GroupMemberModel> mMemberList = new ArrayList<>();
    public GroupMembersNewbiesAdapter mAdapter;
    @Bind(R.id.civ_group_avatar)
    ImageView civ_group_avatar;
    @Bind(R.id.et_group_name)
    EditText et_group_name;
    @Bind(R.id.tv_ticket_price)
    TextView tv_ticket_price;
    @Bind(R.id.recycler_friends_list)
    RecyclerView mRecyclerview;
    @Bind(R.id.switch_recruition)
    SwitchCompat switch_recruition;
    @Bind(R.id.et_my_nickname)
    EditText et_my_nickname;
    @Bind(R.id.tv_group_feed_content)
    TextView tv_group_feed_content;
    @Bind(R.id.iv_group_feed_flow)
    ImageView iv_group_feed_flow;
    @Bind(R.id.tv_show_all_members)
    TextView tv_show_all_members;
    @Bind(R.id.switch_nodisturb)
    SwitchCompat switch_nodisturb;
    @Bind(R.id.switch_show_nickname)
    SwitchCompat switch_show_nickname;
    @Bind(R.id.layout_recrution)
    RelativeLayout layout_recrution;
    @Bind(R.id.ll_set_price)
    LinearLayout ll_set_price;
    @Bind(R.id.layout_recrution_about)
    LinearLayout layout_recrution_about;
    @Bind(R.id.tv_group_recruition_tips)
    TextView tv_group_recruition_tips;
    @Bind(R.id.layout_set_price)
    RelativeLayout layout_set_price;
    @Bind(R.id.tv_group_ticket_announcement)
    TextView tv_group_ticket_announcement;
    @Bind(R.id.tv_group_announcement)
    TextView tv_group_announcement;
    @Bind(R.id.np_set_ticket_price)
    NumberPicker np_set_ticket_price;
    @Bind(R.id.tv_quit_group)
    TextView tv_quit_group;
    @Bind(R.id.tv_repu_value)
    TextView tv_repu_value;
    @Bind(R.id.layout_group_redbag)
    RelativeLayout layout_group_redbag;
    @Bind(R.id.tv_reputation_tips)
    TextView tv_reputation_tips;
    @Bind(R.id.tv_involve_friend)
    TextView tv_involve_friend;
    @Bind(R.id.layout_group_announcement)
    RelativeLayout layout_group_announcement;
    @Bind(R.id.layout_group_repu)
    RelativeLayout layout_group_repu;
    @Bind(R.id.tv_group_balance_tips)
    TextView tv_group_balance_tips;
    @Bind(R.id.tv_group_balance)
    TextView tv_group_balance;
    @Bind(R.id.tv_remark_tips)
    TextView tv_remark_tips;
    @Bind(R.id.layout_involve_friend)
    RelativeLayout layout_involve_friend;
    @Bind(R.id.layout_reputation_tips)
    RelativeLayout layout_reputation_tips;
    @Bind(R.id.arrow_group_balance)
    ImageView arrow_group_balance;
    @Bind(R.id.arrow_repu_value)
    ImageView arrow_repu_value;
    @Bind(R.id.arrow_group_announcement)
    ImageView arrow_group_announcement;
    private float[] price_range;
    private String[] price_labels;
    private String group_id;
    private TabfindGroupModel mGroupModel;
    private int mCurTicketPrice;
    private String group_image_url = "";
    private Rect rect_et_group_name = new Rect();
    private String mGroupNotice;
    private String mNickName;
    //    @Bind(R.id.pen_group_name)
//    ImageView pen_group_name;
    private boolean nodisturb = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_home_page);
        init();
        //触发联系人刷新机制，解决添加成员列表刷新
        EventBus.getDefault().post(new Intent(TabFriendFragment.ACTION_REFRESH));
    }

    private void init() {
        mGroupModel = getIntent().getParcelableExtra(GroupChatActivity.K_GROUP_DATA);
        if (mGroupModel == null) {
            finish();
            return;
        }
        group_id = mGroupModel.group_id;
        setTitle(getString(R.string.group_settings));

        fetchGroupData();
        fetchMembers();
        fetchFeedFlow();
        fetchMySetting();
        initRecruitionSwitch();

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
                    double total_prestige_value = Math.round(object.optDouble("total_prestige_value") * 100) / 100.0;
                    double prestige_value = Math.round(object.optDouble("prestige_value") * 100) / 100.0;
                    if (total_prestige_value < 0.0) {
                        switch_recruition.setClickable(false);
                    }
                    if (GroupConstant.MemberType.NEWBIE.equals(mGroupModel.member_type)) {
                        //游客
                        tv_repu_value.setText(String.valueOf(total_prestige_value));
                    } else {
                        tv_repu_value.setText(String.format("当前%s(%s)", prestige_value, total_prestige_value));
                    }
                    tv_repu_value.setTag(prestige_value);
                });
            }

            @Override
            public void onError(int error, Object ret) {

            }
        });
    }

    private void getBalance() {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("group_id", group_id));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_GROUP_BALANCE, new MsgStructure() {

            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("getBalance(). data is : " + data);
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    int temp = data.optInt("amount");
                    float balance = temp / 100.0f;
                    tv_group_balance.setText(getString(R.string.yuan_unit, balance));
                    mGroupModel.amount = temp;
                });
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("getBalance(). error is : " + error);
            }
        });
    }

    private void initRecruitionSwitch() {
        switch_recruition.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layout_recrution_about.setVisibility(View.VISIBLE);
            } else {
                layout_recrution_about.setVisibility(View.GONE);
                if (ll_set_price.getVisibility() == View.VISIBLE) {
                    ll_set_price.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setPicker(JSONArray price_array) {
        String yuan = getString(R.string.yuan_unit);
        try {
            double low_d = price_array.getDouble(0);
            double high_d = price_array.getDouble(1);
            int length = 1;
            float low = Math.round(low_d * 10) / 10.0f;
            float high = Math.round(high_d * 10) / 10.0f;
            float temp = low;
            while (temp != high) {
                temp += PICKER_STAGE_VALUE;
                temp = Math.round(temp * 10) / 10.0f;
                length++;
            }
            price_range = new float[length + 1];
            price_labels = new String[length + 1];
            price_range[0] = 0;
            price_labels[0] = getString(R.string.free);
            for (int i = 1; i < length + 1; i++) {
                price_range[i] = Math.round(low * 10) / 10.0f;
                price_labels[i] = String.format(yuan, price_range[i]);
                low += PICKER_STAGE_VALUE;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        np_set_ticket_price.setMaxValue(price_labels.length - 1);
        np_set_ticket_price.setMinValue(0);
        np_set_ticket_price.setFocusable(true);
        np_set_ticket_price.setFocusableInTouchMode(true);
        np_set_ticket_price.setDisplayedValues(price_labels);
    }

    private void fetchGroupData() {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(KEY_GROUP_ID, group_id));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_CREATE_GROUP_CHAT, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("fetchGroupData. data is : " + data);
                JSONObject groupData = data.optJSONObject("group");
                JSONArray price_array = data.optJSONArray("price");
                Observable.just(price_array).observeOn(AndroidSchedulers.mainThread()).subscribe(GroupHomePageActvity.this::setPicker);
                Observable.just(groupData).observeOn(AndroidSchedulers.mainThread()).subscribe(GroupHomePageActvity.this::fillGroupData);
                //获取群声望值
                getPrestige();
                getBalance();
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
        params.add(new BasicNameValuePair(KEY_GROUP_ID, group_id));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_GROUP_MEMBERS, new MsgStructure() {

            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("fetchMembers. data is : " + data);
                JSONArray members = data.optJSONArray(KEY_MEMBERS);
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
                    if (i < MEMBER_LIMIT_COUNT && !GroupConstant.MemberType.NEWBIE.equals(member.member_type)) {
                        homepageMembers.add(member);
                        ++row_num;
                    }
                }
                CustomLog.d("fetchMembers. row num is : " + row_num);
//                mGroupModel.total_number = members.length();
                mMemberList.addAll(wholeMembers);
                final int finalRow_num = row_num;
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    setRecyclerLayout(finalRow_num);
                    mAdapter = new GroupMembersNewbiesAdapter(GroupHomePageActvity.this, homepageMembers, mGroupModel);
                    mAdapter.setOnMemberSelectedListener(this::handleOnMemberSelected);
                    mRecyclerview.setAdapter(mAdapter);
                    tv_show_all_members.setText(getString(R.string.show_all_group_members, members.length()));
                    dismissAnimLoading();
                });
            }

            private void handleOnMemberSelected(GroupMemberModel memberModel) {
                Intent it = new Intent(GroupHomePageActvity.this, UserInfoActivity.class);
                it.putExtra(UserInfoActivity.TARGET_UID, memberModel.uid);
                startActivity(it);
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("fetchMembers. error is : " + error + ", ret is : " + ret);
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    showToast(GroupHomePageActvity.this, getString(R.string.load_failed));
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

    private void fetchFeedFlow() {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(KEY_GROUP_ID, group_id));
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
                    showToast(GroupHomePageActvity.this, getString(R.string.load_failed));
                });
            }
        });
    }

    private void fetchMySetting() {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(KEY_GROUP_ID, group_id));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_GET_GROUP_MEMBER_EXTRA, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("fetchMySetting onReceive. data is : " + data);
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    mNickName = o.optString(KEY_NICK_NAME);
                    int show_nickname = o.optInt(KEY_SHOW_NICK_NAME);
                    //int notify_flag = o.optInt(KEY_NOTIFY_FLAG);
                    et_my_nickname.setText(mNickName);
                    Map<String, Integer> map = MsgDBCenterService.getInstance().getNoDisturbMapping();
                    if (map.containsKey(group_id)) {
                        switch_nodisturb.setChecked(false);
                    } else {
                        switch_nodisturb.setChecked(true);
                    }
                    switch_show_nickname.setChecked(show_nickname == 1);
                    nodisturb = switch_nodisturb.isChecked();
                });
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("fetchMySetting onError. error is : " + error);
            }
        });
    }

    private void fillGroupData(JSONObject groupData) {
        mGroupModel = JSON.parseObject(groupData.toString(), TabfindGroupModel.class);
        mGroupNotice = mGroupModel.notice;
        boolean isRecruiting = mGroupModel.is_recruiting == 1;
        if (mGroupModel.member_type.equals(GroupConstant.MemberType.NEWBIE)) {
            layout_recrution.setVisibility(View.GONE);
            layout_recrution_about.setVisibility(View.GONE);
            layout_group_redbag.setVisibility(View.GONE);
            layout_group_repu.setClickable(false);
            et_group_name.setFocusable(false);
            layout_involve_friend.setVisibility(View.GONE);
            layout_reputation_tips.setVisibility(View.GONE);
            tv_group_balance_tips.setVisibility(View.GONE);
            tv_group_recruition_tips.setVisibility(View.GONE);
            tv_remark_tips.setVisibility(View.GONE);
            civ_group_avatar.setClickable(false);
            arrow_group_balance.setVisibility(View.GONE);
            arrow_repu_value.setVisibility(View.GONE);
            arrow_group_announcement.setVisibility(View.GONE);
            View v = findViewById(R.id.v_group_nickname_start);
            ViewGroup.LayoutParams params = v.getLayoutParams();
            if (params instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) params).topMargin = PWUtils.getPXbyDP(this, 20);
                v.setLayoutParams(params);
            }

//            pen_group_name.setVisibility(View.GONE);
        } else {
            if (isRecruiting) {
                layout_recrution_about.setVisibility(View.VISIBLE);
            } else {
                layout_recrution_about.setVisibility(View.GONE);
            }
            switch_recruition.setChecked(isRecruiting);
        }
        ImageLoader.getInstance().displayImage(mGroupModel.avatar, civ_group_avatar);
        double ticket_price = Math.round(mGroupModel.ticket_price) / 100.0;
        if (mCurTicketPrice == 0) {
            mCurTicketPrice = mGroupModel.ticket_price;
        }
        tv_ticket_price.setText(mGroupModel.ticket_price == 0 ? getString(R.string.free) : String.format(getString(R.string.yuan_unit), ticket_price));
        et_group_name.setText(mGroupModel.group_name);
        tv_group_announcement.setText(TextUtils.isEmpty(mGroupModel.notice) ? getString(R.string.group_announcement_none) : mGroupModel.notice);
        for (int i = 0; i < price_range.length; i++) {
            if (price_range[i] == (float) ticket_price) {
                np_set_ticket_price.setValue(i);
            }
        }
    }

    public void click(View v) {
        switch (v.getId()) {
            case R.id.layout_group_announcement:
                Intent it = new Intent(this, UpdateGroupNoticeActivity.class);
                it.putExtra(UpdateGroupNoticeActivity.K_GROUP_NOTICE, mGroupNotice);
                it.putExtra(UpdateGroupNoticeActivity.K_MEMBER_TYPE, mGroupModel.member_type);
                startActivityForResult(it, REQUEST_GROUP_ANNOUNCEMENT);
                break;
            case R.id.civ_group_avatar:
                addPhoto();
                break;
            case R.id.tv_involve_friend:
                it = new Intent(this, CreateChatGroupActivity.class);
                it.putExtra(CreateChatGroupActivity.KEY_NEED_SHOW_AVATAR_LAYOUT, false);
                it.putExtra(KEY_MEMBER_LIST, mMemberList);
                it.putExtra(K_GROUP_DATA, mGroupModel);
                startActivityForResult(it, REQUEST_GROUP_ADD_FRIENDS_TO_BE_MEMBERS);
                break;
            case R.id.layout_show_more:
                it = new Intent(this, GroupMembersNewbiesActivity.class);
                it.putExtra(K_GROUP_DATA, mGroupModel);
                startActivityForResult(it, REQUEST_GROUP_DISPLAY_OR_DELETE_MEMBERS);
                break;
            case R.id.layout_group_feed:
                it = new Intent(this, FeedFlowActivity.class);
                it.putExtra(FeedFlowActivity.KEY_GROUP_FEED_FLOW, true);
                it.putExtra(KEY_GROUP_ID, group_id);
                startActivity(it);
                break;
            case R.id.tv_quit_group:
                showQuitDialog();
                break;
            case R.id.layout_set_price:
                showPriceSelector(ll_set_price.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                break;
            case R.id.tv_clear_history_msg:
                clearHistoryMsg(false);
                break;
            case R.id.tv_group_report:
                showReportDialog();
                break;
            case R.id.tv_ok:
                if (price_range == null) {
                    ll_set_price.setVisibility(View.INVISIBLE);
                    return;
                }
                float price = price_range[np_set_ticket_price.getValue()];
                price = (float) (Math.round(price * 10) / 10.0);
                tv_ticket_price.setText(price == 0f ? getString(R.string.free) : String.format(Locale.getDefault(), getString(R.string.yuan_unit), price));
                mCurTicketPrice = (int) (np_set_ticket_price.getValue() * PICKER_STAGE_VALUE * 100);
                ll_set_price.setVisibility(View.INVISIBLE);
                break;
            case R.id.tv_cancel:
                ll_set_price.setVisibility(View.INVISIBLE);
                break;
            case R.id.layout_group_repu:
                if (GroupConstant.MemberType.NEWBIE.equals(mGroupModel.member_type) || GroupConstant.MemberType.ALIEN.equals(mGroupModel.member_type)) {
                    Snackbar.make(tv_group_announcement, getString(R.string.no_permission_distribute_group_reputation), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                double value = (double) tv_repu_value.getTag();
                if (value <= 0 || value / mGroupModel.member_number < 0.01) {
                    Snackbar.make(tv_group_announcement, getString(R.string.group_reputation_not_enough), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(this, ChatRepuRedbagActivity.class);
                intent.putExtra(ChatRepuRedbagActivity.K_TOTAL_PRESTIGE_VALUE, value);
                intent.putExtra(ChatRepuRedbagActivity.K_TOTAL_MEMBERS, mGroupModel.member_number);
                intent.putExtra(ChatRepuRedbagActivity.K_GROUP_ID, mGroupModel.group_id);
                startActivityForResult(intent, REQUEST_CODE_GROUP_REPU);
                break;
            case R.id.layout_group_redbag:
                //群红包
                if (GroupConstant.MemberType.NEWBIE.equals(mGroupModel.member_type) || GroupConstant.MemberType.ALIEN.equals(mGroupModel.member_type)) {
                    Snackbar.make(tv_group_announcement, getString(R.string.no_permission_distribute_group_profit), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (mGroupModel.amount == 0 || mGroupModel.amount / mGroupModel.member_number < 0.01) {
                    Snackbar.make(tv_group_announcement, getString(R.string.group_profit_not_enough), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Intent redbag_intent = new Intent(this, GroupChatRedbagActivity.class);
                redbag_intent.putExtra(GroupChatRedbagActivity.K_REDBAG_TYPE, GroupChatRedbagActivity.REDBAG_TYPE_GROUP);
                redbag_intent.putExtra(GroupChatActivity.K_GROUP_DATA, mGroupModel);
                startActivityForResult(redbag_intent, REQUEST_CODE_GROUP_REDBAG);
                break;
            default:
                break;
        }
    }

    private void chooceImageByAlbum() {
        Intent intent = new Intent(this, AlbumCompatActivity.class);
        intent.putExtra(AlbumCompatActivity.K_ALBUM_RST_COUNT, 1);
        startActivityForResult(intent, REQUEST_CODE_START_ALBUM);
    }

    private void showPriceSelector(int visibility) {
        ll_set_price.setVisibility(visibility);
    }

    private void showReportDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.report_group))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    reportGroup();
                })
                .create().show();
    }

    private void clearHistoryMsg(boolean isquit) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.clear_chat_history))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    clearGroupMessages(isquit);
                })
                .create().show();
    }

    private void clearGroupMessages(boolean isquit) {
        RongIMClient.getInstance().clearMessages(Conversation.ConversationType.GROUP, mGroupModel.group_id, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                CustomLog.d("clearGroupMessages onSuccess. group id is : " + mGroupModel.group_id);
                if (isquit) {
                    setResult(RESULT_QUIT_AND_DELETE_GROUP);
//                    Intent it = new Intent(PWActionConfig.ACTION_DELETE_GROUP_MSG_ITEM);
//                    it.putExtra("msg_id", mGroupModel.group_id);
//                    EventBus.getDefault().post(it);
                    boolean success = MsgDBCenterService.getInstance().deleteMessageByMsgId(mGroupModel.group_id);
                    if (success) {
                        showToast(GroupHomePageActvity.this, getString(R.string.delete_success));
                    }
                    finish();
                } else {
                    //删除聊天记录
                    EventBus.getDefault().post(new Intent(GroupChatActivity.ACTION_CLEAR_MESSAGE));
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                CustomLog.d("clearGroupMessages onError. ");
            }
        });
    }

    public void left_click(View v) {
        onBackPressed();
    }

    @Override
    public void finish() {
        if (nodisturb != switch_nodisturb.isChecked()) {
            MsgDBCenterService.getInstance().updateNodisturbWithGroup(group_id, switch_nodisturb.isChecked());
        }
        super.finish();
    }

    @Override
    public void onBackPressed() {
        updateGroupSettings();
    }

    private void updateGroupSettings() {
        showAnimLoading();
        String groupName = et_group_name.getText().toString();
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(KEY_GROUP_ID, group_id));
        if (!mGroupModel.group_name.equals(groupName)) {
            params.add(new BasicNameValuePair(KEY_GROUP_NAME, groupName));
        }
        CustomLog.d("updateGroupSettings. isRecruiting = " + mGroupModel.is_recruiting + ", isChecked : " + switch_recruition.isChecked());
        if ((mGroupModel.is_recruiting == 1 && !switch_recruition.isChecked())
                || (mGroupModel.is_recruiting == 0 && switch_recruition.isChecked())) {
            params.add(new BasicNameValuePair(KEY_IS_RECRUITING, switch_recruition.isChecked() ? "1" : "0"));
        }
        if (mCurTicketPrice != mGroupModel.ticket_price) {
            params.add(new BasicNameValuePair(KEY_TICKET_PRICE, String.valueOf(mCurTicketPrice)));
        }
        if (mGroupNotice != null && GroupConstant.MemberType.ADMIN.equals(mGroupModel.member_type) && !mGroupModel.notice.equals(mGroupNotice)) {
            params.add(new BasicNameValuePair("notice", mGroupNotice));
        }
        if (!TextUtils.isEmpty(group_image_url) && !group_image_url.equals(mGroupModel.avatar)) {
            params.add(new BasicNameValuePair(KEY_GROUP_AVATAR, group_image_url));
        }
        ApiRequestWrapper.openAPIPOST(this, params, AsynHttpClient.API_UPDATE_GROUP_SETTING, new MsgStructure() {

            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("updateGroupSettings. data is : " + data);
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    Snackbar.make(tv_quit_group, getString(R.string.save_successful), Snackbar.LENGTH_SHORT).show();
                    dismissAnimLoading();
//                    "group": {
//                        "is_recruiting": 0,
//                        "admin_id": 1005,
//                        "group_prefix": "\u4e00\u4e2a\u7fa4",
//                        "avatar": "http://avatar.peiwo.cn/qiniu3_1005_a5caa61c4df46b982cd480c25188a102/thumbnail",
//                        "group_id": "1005_44cb3c2caa2811e5",
//                        "ticket_price": 0
//                    }
                    String group = o.optString("group");
                    TabfindGroupModel groupModel = JSON.parseObject(group, TabfindGroupModel.class);
                    if (!et_group_name.getText().toString().equals(mGroupModel.group_name)) {
                        groupModel.msg1 = o.optString("msg1");
                    }
                    if (!tv_group_announcement.getText().toString().equals(mGroupModel.notice)) {
                        groupModel.msg2 = o.optString("msg2");
                    }
                    Intent intent = new Intent();
                    intent.putExtra(GroupChatActivity.K_GROUP_DATA, groupModel);
                    setResult(RESULT_GROUP_UPDATE, intent);
                    finish();
                });
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("updateGroupSettings. error is : " + error + ", ret is : " + ret);
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    Snackbar.make(tv_quit_group, getString(R.string.save_failed), Snackbar.LENGTH_SHORT).show();
                    dismissAnimLoading();
                    finish();
                });
            }
        });

        String nickname = et_my_nickname.getText().toString();
        ArrayList<NameValuePair> param2 = new ArrayList<>();
        param2.add(new BasicNameValuePair(KEY_GROUP_ID, group_id));
        if (!nickname.equals(mNickName))
            param2.add(new BasicNameValuePair(KEY_NICK_NAME, nickname));
        param2.add(new BasicNameValuePair(KEY_NOTIFY_FLAG, switch_nodisturb.isChecked() ? "0" : "1"));
        param2.add(new BasicNameValuePair(KEY_SHOW_NICK_NAME, switch_show_nickname.isChecked() ? "1" : "0"));
        ApiRequestWrapper.openAPIPOST(this, param2, AsynHttpClient.API_GET_GROUP_MEMBER_EXTRA, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    Intent intent = new Intent(GroupChatActivity.ACTION_SHOW_NICKNAME);
                    intent.putExtra(GroupChatActivity.K_SHOW_NICK_NAME, switch_show_nickname.isChecked());
                    intent.putExtra(GroupChatActivity.K_NICK_NAME, nickname);
                    EventBus.getDefault().post(intent);
                });
                CustomLog.d("update personal settings. data is : " + data);
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("update personal settings. error is : " + error);
            }
        });
    }

    private void reportGroup() {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(KEY_GROUP_ID, group_id));
        ApiRequestWrapper.openAPIPOST(this, params, AsynHttpClient.API_GROUP_CHAT_REPORT, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("reportGroup, data is :" + data);
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    showToast(GroupHomePageActvity.this, getString(R.string.report_success));
                });
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("reportGroup, error is : " + error);
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    showToast(GroupHomePageActvity.this, getString(R.string.report_failed));
                });
            }
        });
    }

    private void addPhoto() {
        new AlertDialog.Builder(this).setTitle("添加照片").setItems(new String[]{"拍照", "相册", "取消"},
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            mImageKey = PWUploader.getInstance().getKey(Integer.valueOf(mGroupModel.admin_id));
                            ImageUtil.startImgPickerCamera(
                                    GroupHomePageActvity.this,
                                    ImageUtil.PICK_FROM_CAMERA,
                                    ImageUtil.getPathForCameraCrop(mImageKey));
                            break;
                        case 1:
                            chooceImageByAlbum();
                            break;
                        default:
                            break;
                    }
                }).create().show();
    }

    private void showQuitDialog() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.quit_this_group))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    quitAndDelete();
                })
                .create().show();
    }

    private void quitAndDelete() {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(KEY_GROUP_ID, group_id));
        CustomLog.d("showQuitDialog group_id is : " + group_id);
        ApiRequestWrapper.openAPIPOST(this, params, AsynHttpClient.API_QUIT_GROUP_CHAT, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                clearGroupMessages(true);
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("showQuitDialog. error is : " + error + ", ret is : " + ret);
            }
        });
    }

    private void uploadImgByCameraCrop(String imagePath) {
        if (imagePath == null) {
            showToast(this, "获取图片出错");
        } else {
            String imageKey = PWUploader.getInstance().getKey(UserManager.getUid(this));
            ImageModel imgModel = new ImageModel(imagePath, imageKey);
            uploadImgBySCS(imgModel);
        }
    }

    private void uploadImgBySCS(final ImageModel imgModel) {
        showAnimLoading("", false, false, false);
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(PWUploader.K_UPLOAD_TYPE, PWUploader.UPLOAD_TYPE_AVATAR));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_QINIU_TOKEN, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("uploadImgBySCS. onReiceve. data is : " + data);
                PWUploader uploader = PWUploader.getInstance();
                uploader.add(imgModel.uploadpath, data.optString("key"), data.optString("token"), new UploadCallback() {
                    @Override
                    public void onComplete(String key, ResponseInfo responseInfo, JSONObject jsonObject) {
                        group_image_url = data.optString("thumbnail_url");
                        CustomLog.d("uploadImgBySCS onComplete. group_image_url : " + group_image_url);
                        dismissAnimLoading();
                    }

                    @Override
                    public void onFailure(String key, ResponseInfo responseInfo, JSONObject jsonObject) {
                        CustomLog.d("uploadImgBySCS onFailure.");
                        Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                            dismissAnimLoading();
                            showToast(GroupHomePageActvity.this, getString(R.string.load_failed));
                        });
                    }
                });
            }

            @Override
            public void onError(int error, Object ret) {
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                int rx = (int) ev.getRawX();
                int ry = (int) ev.getRawY();
                if (!rect_et_group_name.contains(rx, ry)) {
                    PWUtils.hideSoftKeyBoard(this);
                    et_group_name.clearFocus();
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_GROUP_REPU:
                    Intent intent = new Intent();
                    PacketIconModel packetIconModel = data.getParcelableExtra(GroupChatRedbagActivity.K_SINGLE_PACKET);
                    if (packetIconModel != null) {
                        intent.putExtra(GroupChatRedbagActivity.K_SINGLE_PACKET, packetIconModel);
                        setResult(RESULT_GROUP_REPU_REDBAG, intent);
                        finish();
                    }
                    break;
                case REQUEST_GROUP_ANNOUNCEMENT:
                    String announcement = data.getStringExtra(UpdateGroupNoticeActivity.K_GROUP_NOTICE);
                    if (announcement.equals("")) {
                        announcement = getString(R.string.group_announcement_none);
                    }
                    tv_group_announcement.setText(announcement);
                    mGroupNotice = announcement;
                    break;
                case REQUEST_CODE_START_ALBUM:
                    ArrayList<String> items = data.getStringArrayListExtra(AlbumCompatActivity.K_ALBUM_RST);
                    String path = items.get(0);
                    uploadImgByCameraCrop(path);
                    CustomLog.d("onActivityResult REQUEST_CODE_START_ALBUM. image path is : " + path);
                    ImageLoader.getInstance().displayImage("file://" + path, civ_group_avatar);
                    break;
                case ImageUtil.PICK_FROM_CAMERA:
                    File src = ImageUtil.getPathForCameraCrop(mImageKey);
                    path = src.getAbsolutePath();
                    uploadImgByCameraCrop(path);
                    CustomLog.d("onActivityResult PICK_FROM_CAMERA. path : " + path);
                    ImageLoader.getInstance().displayImage("file://" + path, civ_group_avatar);
                    break;
                case REQUEST_CODE_GROUP_REDBAG:
                    //群红包回调
                    PacketIconModel callbackPacketIconModel = data.getParcelableExtra(GroupChatRedbagActivity.K_SINGLE_PACKET);
                    if (callbackPacketIconModel != null) {
                        Intent callback_intent = new Intent();
                        callback_intent.putExtra(GroupChatRedbagActivity.K_SINGLE_PACKET, callbackPacketIconModel);
                        setResult(RESULT_GROUP_REDBAG, callback_intent);
                        finish();
                    }
                    break;
                default:
                    break;
            }
        }

        if (requestCode == REQUEST_GROUP_DISPLAY_OR_DELETE_MEMBERS ||
                requestCode == REQUEST_GROUP_ADD_FRIENDS_TO_BE_MEMBERS) {
            fetchMembers();
            //更新member_number, total_number
            fetchGroupData();
        }
    }

    static class MyLinearLayoutManager extends LinearLayoutManager {

        private int rowCount;

        public MyLinearLayoutManager(Context context, int rowCount) {
            super(context);
            this.rowCount = rowCount;
        }


        @Override
        public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
            if (rowCount == 0) {
                setMeasuredDimension(widthSpec, 0);
                return;
            }
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
}
