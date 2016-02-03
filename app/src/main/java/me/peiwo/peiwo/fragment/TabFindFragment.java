package me.peiwo.peiwo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.*;
import me.peiwo.peiwo.adapter.TabFindAdapter;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.model.TabFindModel;
import me.peiwo.peiwo.model.TabfindGroupModel;
import me.peiwo.peiwo.model.TabfindGroupMoreModel;
import me.peiwo.peiwo.model.TopicFindModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.UmengStatisticsAgent;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.PWPullToRefreshListView;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * create by dfh
 *
 * @author Fuhai
 */
public class TabFindFragment extends PPBaseFragment implements
        OnItemClickListener, OnRefreshListener2<ListView> {

    private static final int ID_RECEIVE_MAINLIST = 1000;
    private static final int ID_RECEIVE_MAINLIST_ERROR = 1001;
    private static final int ID_RECEIVE_TOPIC = 1002;


    private PWPullToRefreshListView pullToRefreshListView;
    private String cursor = null;
    private List<Object> mList = new ArrayList<>();
    private TabFindAdapter adapter;
    //推荐的用户数目
    private int recommend_nums;
    //动态添加的下表哦
    private int topicLocationIndex;


    public static TabFindFragment newInstance() {
        return new TabFindFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_find, container, false);
        init(v);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
//private TextView headerTitleView;

    private void init(View v) {
        pullToRefreshListView = (PWPullToRefreshListView) v.findViewById(R.id.pullToRefreshListView);
        //codeHeaderView();
        adapter = new TabFindAdapter(mList, getActivity());
        pullToRefreshListView.setAdapter(adapter);
        pullToRefreshListView.setOnRefreshListener(this);
        pullToRefreshListView.setOnItemClickListener(this);
        pullToRefreshListView.setRefreshing();
    }

//    private void codeHeaderView() {
//        LinearLayout headerContainer = new LinearLayout(getActivity());
//        headerContainer.setOrientation(LinearLayout.VERTICAL);
//        int padding = PWUtils.getPXbyDP(getActivity(), 6);
//        headerContainer.setPadding(0, padding, 0, 0);
//        headerContainer.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        headerTitleView = new TextView(getActivity());
//        headerTitleView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
//        headerTitleView.setPadding(0, 0, PWUtils.getPXbyDP(getActivity(), 18), 0);
//        headerTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
//        headerTitleView.setTextColor(Color.parseColor("#8e8e8e"));
//        headerContainer.addView(headerTitleView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        View line = new View(getActivity());
//        line.setBackgroundColor(getResources().getColor(R.color.c_de2));
//        LinearLayout.LayoutParams line_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PWUtils.getPXbyDP(getActivity(), 0.5f));
//        line_params.setMargins(0, padding, 0, 0);
//        headerContainer.addView(line, line_params);
//        pullToRefreshListView.getRefreshableView().addHeaderView(headerContainer, null, false);
//    }

    private void requestServer() {
        ApiRequestWrapper.getMainList(getActivity(), UserManager.getUid(getActivity()), cursor, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                distributeMessage(ID_RECEIVE_MAINLIST, data);
                CustomLog.d("requestServer, data is : " + data);
            }

            @Override
            public void onError(int error, Object ret) {
                distributeMessage(ID_RECEIVE_MAINLIST_ERROR, null);
            }
        });
    }

    @Override
    protected void handle_message(int message_id, JSONObject obj) {
        switch (message_id) {
            case ID_RECEIVE_MAINLIST:
                if (adapter != null) {
                    adapter.resetToIdle();
                }
                fillData(obj);
                break;

            case ID_RECEIVE_MAINLIST_ERROR:
                pullToRefreshListView.onRefreshComplete();
                break;
            case ID_RECEIVE_TOPIC:
                addTopicModel(obj);
                break;
        }
    }

    private void addTopicModel(JSONObject obj) {
        TopicFindModel model = new TopicFindModel(obj);
        if (topicLocationIndex > mList.size() || topicLocationIndex < 0) {
            topicLocationIndex = 0;
        }
        mList.add(topicLocationIndex, model);
        adapter.notifyDataSetChanged();
    }

    private void fillData(JSONObject obj) {
        try {
            pullToRefreshListView.onRefreshComplete();
            pullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
            List<Object> objects = new ArrayList<>();
            JSONObject groups = obj.optJSONObject("groups");

            JSONArray groupArray = groups.optJSONArray("group_list");
            if (groupArray != null) {
                for (int i = 0, z = groupArray.length(); i < z; i++) {
                    objects.add(JSON.parseObject(groupArray.getString(i), TabfindGroupModel.class));
                }
            }

            boolean group_has_more = groups.optInt("has_more", 0) > 0;
            if (group_has_more) {
                objects.add(new TabfindGroupMoreModel());
            }

            JSONArray array = obj.getJSONArray("userlist");
            for (int i = 0; i < array.length(); i++) {
                objects.add(new TabFindModel(array.getJSONObject(i)));
            }
            boolean no_more = obj.optBoolean("no_more", false);
            pullToRefreshListView.end(no_more);
            if (TextUtils.isEmpty(cursor)) {
                mList.clear();
            }
            mList.addAll(objects);
            adapter.notifyDataSetChanged();
            if (TextUtils.isEmpty(cursor)) {
                recommend_nums = obj.optInt("recommend_nums");
                topicLocationIndex = groupArray == null ? 0 : groupArray.length() + (group_has_more ? +1 : +0);
                //setRecommendTitle();
                int sexMask = obj.getInt("mask");
                int price_on = obj.optInt("price_on");
                setUpNavigitionView(sexMask, price_on);
                getTopic();
            }
            cursor = obj.optString("cursor");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    private void setRecommendTitle() {
//        if (recommend_nums == 0) {
//            headerTitleView.setText(null);
//            headerTitleView.setVisibility(View.INVISIBLE);
//        } else {
//            headerTitleView.setVisibility(View.VISIBLE);
//            String recommend_str = null;
//            switch (recommend_nums) {
//                case 1:
//                    recommend_str = "一";
//                    break;
//                case 2:
//                    recommend_str = "二";
//                    break;
//                case 3:
//                    recommend_str = "三";
//                    break;
//                case 4:
//                    recommend_str = "四";
//                    break;
//                case 5:
//                    recommend_str = "五";
//                    break;
//                case 6:
//                    recommend_str = "六";
//                    break;
//                case 7:
//                    recommend_str = "七";
//                    break;
//                case 8:
//                    recommend_str = "八";
//                    break;
//                case 9:
//                    recommend_str = "九";
//                    break;
//            }
//            headerTitleView.setText(String.format("%s个推荐", recommend_str));
//        }
//    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser && adapter != null) {
            adapter.resetToIdle();
        }
        if (isVisibleToUser)
            UmengStatisticsAgent.onEvent(getActivity(), UMEventIDS.UMEFINDPAGE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.resetToIdle();
        }
    }

    private void getTopic() {
        ArrayList<NameValuePair> params = new ArrayList<>();
        ApiRequestWrapper.openAPIGET(getActivity(), params,
                AsynHttpClient.API_TAG_TOPIC, new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                        distributeMessage(ID_RECEIVE_TOPIC, data);
                    }

                    @Override
                    public void onError(int error, Object ret) {

                    }
                });
    }

    private void setUpNavigitionView(int sexMask, int price_on) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setNavgationViewOnlineStatus(sexMask, price_on == 1);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object object = parent.getAdapter().getItem(position);
        if (object instanceof TabFindModel) {
            TabFindModel uInfo = (TabFindModel) object;
            //CustomLog.d("uid is : " + uInfo.uid + ", price is : " + uInfo.price);
            Intent intent = new Intent(getActivity(), UserInfoActivity.class);
            intent.putExtra(UserInfoActivity.TARGET_UID, uInfo.uid);
            startActivity(intent);
        } else if (object instanceof TabfindGroupMoreModel) {
            startActivity(new Intent(getActivity(), GroupListMoreActivity.class));
        } else if (object instanceof TabfindGroupModel) {
            Intent intent = new Intent(getActivity(), GroupExhibitionActivity.class);
            intent.putExtra(GroupHomePageActvity.KEY_GROUP_ID, ((TabfindGroupModel) object).group_id);
            startActivity(intent);
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        cursor = null;
        requestServer();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        requestServer();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void refreshData() {
        pullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        pullToRefreshListView.setRefreshing();
    }

    @Override
    protected String getPageName() {
        return "在线列表";
    }
}
