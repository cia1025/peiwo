package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import butterknife.Bind;
import com.alibaba.fastjson.JSON;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.GroupMembersNewbiesAdapter;
import me.peiwo.peiwo.constans.GroupConstant;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.model.GroupMemberModel;
import me.peiwo.peiwo.model.TabfindGroupModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.PinYin;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.util.ArrayList;

/**
 * Created by gaoxiang on 2015/12/16.
 */
public class GroupMembersNewbiesActivity extends BaseActivity implements TextWatcher {
    @Bind(R.id.recycler_group_members_list)
    RecyclerView mRecyclerview;
    @Bind(R.id.et_search_name)
    EditText et_search_name;
    @Bind(R.id.layout_empty)
    RelativeLayout layout_empty;
    public GroupMembersNewbiesAdapter mAdapter;
    private ArrayList<GroupMemberModel> mGroupList = new ArrayList<>();
    private TabfindGroupModel mGroupModel;

    public static final String ACTION_AT_MEMBER = "me.peiwo.peiwo.ACTION_AT_MEMBER";

    public enum State {
        EDIT,
        DEFAULT
    }

    public State mState = State.DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members_list);
        EventBus.getDefault().register(this);
        init();
    }

    private void init() {
        mGroupModel = getIntent().getParcelableExtra(GroupHomePageActvity.K_GROUP_DATA);
        if (mGroupModel == null) {
            return;
        }
        boolean showEditBtn = getIntent().getBooleanExtra("show_edit_button", true);
        if (!ACTION_AT_MEMBER.equals(getIntent().getAction()) && showEditBtn
                && ((mGroupModel.member_type.equals(GroupConstant.MemberType.ADMIN) && mGroupModel.total_number > 1)
                || (mGroupModel.member_type.equals(GroupConstant.MemberType.MEMBER) && mGroupModel.total_number - mGroupModel.member_number >= 1))) {
            //hide right text
            setRightText(getResources().getString(R.string.edit));
        }
        et_search_name.addTextChangedListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerview.setLayoutManager(layoutManager);
        mAdapter = new GroupMembersNewbiesAdapter(this, mGroupList, mGroupModel);
        mAdapter.setOnMemberSelectedListener(this::handleOnMemberSelected);
        mRecyclerview.setAdapter(mAdapter);
        fetchMembers();
    }

    private void handleOnMemberSelected(GroupMemberModel memberModel) {
        Intent data = getIntent();
        String action = data.getAction();
        if (TextUtils.isEmpty(action)) {
            Intent it = new Intent(this, UserInfoActivity.class);
            it.putExtra(UserInfoActivity.TARGET_UID, memberModel.uid);
            startActivity(it);
        } else if (ACTION_AT_MEMBER.equals(action)) {
            //@ member
            Intent rst = new Intent();
            rst.putExtra(GroupChatActivity.K_MEMBER_DATA, memberModel);
            setResult(RESULT_OK, rst);
            finish();
        }
    }

    private void fetchMembers() {
        showAnimLoading();
        String group_id = mGroupModel.group_id;
        CustomLog.d("fetchMembers group id is : " + group_id);
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(GroupHomePageActvity.KEY_GROUP_ID, group_id));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_GROUP_MEMBERS, new MsgStructure() {

            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("fetchMembers. data is : " + data);
                JSONArray members = data.optJSONArray(GroupHomePageActvity.KEY_MEMBERS);
                boolean isFirstNewbie = false;
                for (int i = 0; i < members.length(); i++) {
                    JSONObject member_json = members.optJSONObject(i);
                    GroupMemberModel member = JSON.parseObject(member_json.toString(), GroupMemberModel.class);
                    mGroupList.add(member);
                    if (member.member_type.equals(GroupConstant.MemberType.ADMIN) || member.member_type.equals(GroupConstant.MemberType.MEMBER)) {
//                        mGroupModel.member_counts++;
                    } else if (member.member_type.equals(GroupConstant.MemberType.NEWBIE)) {
//                        mGroupModel.newbie_counts++;
                        if (!isFirstNewbie) {
                            isFirstNewbie = true;
                            member.isFirstNewbie = true;
                        }
                    }
                }
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    setTitle(getResources().getString(R.string.group_members_list, members.length()));
                    dismissAnimLoading();
                    mAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("fetchMembers. error is : " + error);
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                });
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String result = s.toString();
        if (!TextUtils.isEmpty(result.trim())) {
            doSearch(result);
        } else {
            mAdapter = new GroupMembersNewbiesAdapter(this, mGroupList, mGroupModel);
            mRecyclerview.setAdapter(mAdapter);
            layout_empty.setVisibility(View.GONE);
        }
    }

    private void doSearch(String result) {
        String input_str = PinYin.getAllPinYin(result).toLowerCase();
        if (TextUtils.isEmpty(input_str)) return;
        ArrayList<GroupMemberModel> findOutList = new ArrayList<>();
        boolean isFirstNewbie = false;
        for (int i = 0; i < mGroupList.size(); i++) {
            GroupMemberModel model = mGroupList.get(i);
            String pinyin_name = PinYin.getAllPinYin(model.name).toLowerCase();
            if (pinyin_name.contains(input_str)) {
                if (model.member_type.equals(GroupConstant.MemberType.NEWBIE)) {
                    if (!isFirstNewbie) {
                        isFirstNewbie = true;
                        model.isFirstNewbie = true;
                    }
                }
                findOutList.add(model);
            }
        }
        updateFindResult(findOutList);
    }

    private void updateFindResult(ArrayList<GroupMemberModel> findOutList) {
        if (findOutList.size() > 0) {
            mAdapter = new GroupMembersNewbiesAdapter(this, mGroupList, findOutList, mGroupModel);
            layout_empty.setVisibility(View.GONE);
        } else {
            ArrayList emptyList = new ArrayList<>();
            mAdapter = new GroupMembersNewbiesAdapter(this, emptyList, emptyList, mGroupModel);
            layout_empty.setVisibility(View.VISIBLE);
        }
        mRecyclerview.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    public void onEventMainThread(Intent intent) {
        if (intent == null) {
            return;
        }

        int groupPeopleNum = intent.getIntExtra("total_number", 0);
        if (groupPeopleNum > 0)
            setTitle(getResources().getString(R.string.group_members_list, groupPeopleNum));
    }

    @Override
    public void right_click(View v) {
        if (mState == State.DEFAULT) {
            mState = State.EDIT;
            setRightText(getResources().getString(R.string.done));
        } else {
            mState = State.DEFAULT;
            setRightText(getResources().getString(R.string.edit));
        }
        mAdapter.setState(mState);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
