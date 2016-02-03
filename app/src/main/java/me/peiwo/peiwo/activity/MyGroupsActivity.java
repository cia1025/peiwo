package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import butterknife.Bind;
import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.GroupsAdapter;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.model.TabfindGroupModel;
import me.peiwo.peiwo.model.groupchat.GroupsModel;
import me.peiwo.peiwo.model.groupchat.GroupsTitle;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.CustomLog;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.util.ArrayList;
import java.util.List;

public class MyGroupsActivity extends BaseActivity implements PullToRefreshBase.OnRefreshListener2<ExpandableListView>, ExpandableListView.OnChildClickListener, AdapterView.OnItemLongClickListener {
    private static final int REQUEST_MY_GROUPS = 0x30;
    @Bind(R.id.v_empty)
    View v_empty;
    @Bind(R.id.pullToRefreshExpandableListView)
    PullToRefreshExpandableListView pullToRefreshExpandableListView;
    private List<List<TabfindGroupModel>> mGroups = new ArrayList<>();
    private List<GroupsTitle> mGroupTitles = new ArrayList<>();
    private GroupsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups);
        setTitle(getString(R.string.my_groups));
        init();
    }

    private void init() {
        pullToRefreshExpandableListView.getRefreshableView().setGroupIndicator(null);
        pullToRefreshExpandableListView.setOnRefreshListener(this);
        mAdapter = new GroupsAdapter(this, mGroups, mGroupTitles);
        pullToRefreshExpandableListView.getRefreshableView().setAdapter(mAdapter);
        pullToRefreshExpandableListView.setRefreshing();
        pullToRefreshExpandableListView.getRefreshableView().setOnChildClickListener(this);
        pullToRefreshExpandableListView.getRefreshableView().setOnItemLongClickListener(this);
    }

    private void quiteAlert(int groupPosition, int childPosition) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.quit_this_group))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.quit), (dialog, which) -> {
                    quitGroup(groupPosition, childPosition);
                })
                .create().show();
    }

    private void quitGroup(int groupPosition, int childPosition) {
        showAnimLoading();
        TabfindGroupModel model = (TabfindGroupModel) mAdapter.getChild(groupPosition, childPosition);
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("group_id", model.group_id));
        ApiRequestWrapper.openAPIPOST(this, params, AsynHttpClient.API_QUIT_GROUP_CHAT, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                dismissAnimLoading();
                boolean b = MsgDBCenterService.getInstance().deleteMessageByMsgId(model.group_id);
                Observable.just(b).observeOn(AndroidSchedulers.mainThread()).subscribe(issucceed -> {
                    if (issucceed) {
                        List<TabfindGroupModel> group = mGroups.get(groupPosition);
                        group.remove(childPosition);
                        if (group.size() == 0) {
                            mGroups.remove(groupPosition);
                            mGroupTitles.remove(groupPosition);
                        } else {
                            GroupsTitle groupsTitle = mGroupTitles.get(groupPosition);
                            groupsTitle.group_count = group.size();
                            groupsTitle.appendTitle();
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(int error, Object ret) {
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> dismissAnimLoading());
            }
        });
    }

    private void requestServer() {
        ArrayList<NameValuePair> params = new ArrayList<>();
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_MY_GROUPS, new MsgStructure() {

            @Override
            public void onReceive(JSONObject data) {
                GroupsModel mGroupsModel = JSON.parseObject(data.toString(), GroupsModel.class);
                Observable.just(mGroupsModel).observeOn(AndroidSchedulers.mainThread()).subscribe(model -> {
                    pullToRefreshExpandableListView.onRefreshComplete();
                    mGroups.clear();
                    setUpGroupsData(model);
                    setUpGroupTitles(model);
                    mAdapter.notifyDataSetChanged();
                    if (mGroups.size() == 0) {
                        v_empty.setVisibility(View.VISIBLE);
                    } else {
                        v_empty.setVisibility(View.GONE);
                        for (int i = 0; i < mGroups.size(); i++) {
                            pullToRefreshExpandableListView.getRefreshableView().expandGroup(i);
                        }
                    }
                });
            }

            @Override
            public void onError(int error, Object ret) {
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(rerror -> {
                    pullToRefreshExpandableListView.onRefreshComplete();
                });
            }
        });
    }

    private void setUpGroupsData(GroupsModel model) {
        if (model.admin != null && model.admin.size() > 0) {
            mGroups.add(model.admin);
        }
        if (model.member != null && model.member.size() > 0) {
            mGroups.add(model.member);
        }
        if (model.newbie != null && model.newbie.size() > 0) {
            mGroups.add(model.newbie);
        }
    }

    private void setUpGroupTitles(GroupsModel model) {
        if(mGroupTitles.size() > 0)
            mGroupTitles.clear();
        if (model.admin != null && model.admin.size() > 0) {
            mGroupTitles.add(new GroupsTitle("我是群主", model.admin.size()));
        }
        if (model.member != null && model.member.size() > 0) {
            mGroupTitles.add(new GroupsTitle("我是成员", model.member.size()));
        }
        if (model.newbie != null && model.newbie.size() > 0) {
            mGroupTitles.add(new GroupsTitle("我是游客", model.newbie.size()));
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
        requestServer();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ExpandableListView> refreshView) {

    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        TabfindGroupModel model = (TabfindGroupModel) mAdapter.getChild(groupPosition, childPosition);
        Intent intent = new Intent(this, GroupChatActivity.class);
        intent.putExtra(GroupChatActivity.K_GROUP_DATA, model);
        startActivityForResult(intent, REQUEST_MY_GROUPS);
        return true;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            int groupPosition = ExpandableListView.getPackedPositionGroup(id);
            int childPosition = ExpandableListView.getPackedPositionChild(id);
            quiteAlert(groupPosition, childPosition);
            return true;
        }
        return false;
    }

    @Override
    public void click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.v_recommend_groups:
                startActivity(new Intent(this, GroupListMoreActivity.class));
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MY_GROUPS) {
            requestServer();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
