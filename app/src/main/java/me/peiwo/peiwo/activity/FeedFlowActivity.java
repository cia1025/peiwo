package me.peiwo.peiwo.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.FeedFlowAdapter;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.eventbus.event.CreateFeedFlowEvent;
import me.peiwo.peiwo.eventbus.event.RedPonitVisibilityEvent;
import me.peiwo.peiwo.fragment.RecorderDialogFragment;
import me.peiwo.peiwo.model.FeedFlowModel;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.util.*;
import me.peiwo.peiwo.widget.CircleBitmapDisplayer;
import me.peiwo.peiwo.widget.PWPullToRefreshListView;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FeedFlowActivity extends BaseActivity implements OnClickListener,
        OnRefreshListener2<ListView>, PullToRefreshBase.OnLastItemVisibleListener {

    private static final int WHAT_TAG_RECEIVE = 1000;
    private static final int WHAT_TAG_RECEIVE_ERROR = 1001;
    private static final int WHAT_RECEIVE_RELATION = 1002;


    enum TShowType {
        EMainTopic,  //话题信息流
        EMainUser,   //个人动态
        EMainId,      //单条信息
        EMainAll,       //往期内容
        EFriendDynamic,    //好友动态
        EGroupFeed   //群组动态
        ;
    }

    public static final int VIEW_DETAILS_CODE = 0x1000;

    private static final int HANDLE_MSG_REQUEST_LIST_SUCCESS = 0x100;
    private static final int HANDLE_MSG_REQUEST_LIST_FAILURE = 0x101;
    public static final int HANDLE_MSG_LIKE = 0x201;

    public static final int HANDLE_MSG_SAY_HELLO = 0x202;
    public static final int HANDLE_MSG_DELETE_FEED_FLOW = 0x203;
    public static final int HANDLE_MSG_WHO_LIKE_LIST = 0x204;
    public static final int HANDLE_MSG_REPORT_FEED_FLOW = 0x205;
    public static final int HANDLE_MSG_REPORT_FEED_FLOW_SUCCESS = 0x206;
    public static final int HANDLE_MSG_REPORT_FEED_FLOW_ERROR = 0x207;
    public static final int HANDLE_MSG_DELETE_FEED_FLOW_SUCCESS = 0x208;
    public static final int HANDLE_MSG_DELETE_FEED_FLOW_ERROR = 0x209;
    private static final int WHAT_DATA_RECEIVE_RED_POINT_VISIBLE = 0x210;
    public static final String KEY_GROUP_FEED_FLOW = "group_feed";
    public static final String KEY_FRIEND_FEED_FLOW = "is_firend";
    public static final String KEY_MY_FEED_FLOW = "creator_uid";
    public static final String KEY_TOPIC_FEED_FLOW = "topic_id";
    public static final String KEY_PREV_TIME = "prev_time";

    public static final int HANDLE_VIEW_TOPIC = 0x211;

    //private static List<FeedFlowModel> mCachelist = new ArrayList<FeedFlowModel>();
    private static TShowType cacheShowType;
    private static int cacheCreatorUid = -1;
    private static HashMap<String, Boolean> cacheLikerHandleMap = new HashMap<String, Boolean>();


    private TShowType showType = TShowType.EMainTopic;

    private int creatorUid = -1;
    private String feedId;
    private boolean show_bar;
    private HashMap<String, Boolean> likerHandleMap = new HashMap<String, Boolean>();
    private PWPullToRefreshListView pullToRefreshListView;
    private FeedFlowAdapter adapter;
    private List<FeedFlowModel> mlist;
    private ImageView mEditImage;
    private TextView mExitTv;
    private DisplayImageOptions options;
    private View headView;
    private RecorderDialogFragment mRecorderDialogFrag;

    private int topicId = -1;
    private int mIndex = 1;
    //拉倒底部自动请求，不设置标识会使mIndex的值错乱
    private boolean isloading = true;
    private String topicContent;

    private String topicTag;

    private TextView tv_new_message;

    //private FeedFlowModel createTopicModel;
    private boolean isDelete = false;

    private boolean hasmore = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_flow);
        EventBus.getDefault().register(this);
        mlist = new ArrayList<FeedFlowModel>();
        mEditImage = (ImageView) findViewById(R.id.image_edit);
        mEditImage.setOnClickListener(this);
        mExitTv = (TextView) findViewById(R.id.btn_left);
        mExitTv.setOnClickListener(this);

        TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setOnClickListener(this);
        tv_new_message = (TextView) findViewById(R.id.tv_new_message);
        tv_new_message.setOnClickListener(this);
        topicContent = getIntent().getStringExtra("topic_content");
        //createTopicModel = new FeedFlowModel();
        //createTopicModel.view_type = 1;
        creatorUid = UserManager.getUid(this);

        pullToRefreshListView = (PWPullToRefreshListView) findViewById(R.id.pullToRefreshListView);
        pullToRefreshListView.setOnRefreshListener(this);
        pullToRefreshListView.setOnLastItemVisibleListener(this);
        adapter = new FeedFlowAdapter(mlist, this, mHandler);
        pullToRefreshListView.setAdapter(adapter);
        Resources res = getResources();
        if (getIntent().getBooleanExtra("feed_single", false)) {
            topicId = getIntent().getIntExtra("topic_id", -1);
            topicTag = getIntent().getStringExtra("topic_content");
            //titleView.setText("#" + topicTag + "#");
            titleView.setText(topicTag);
            adapter.setShowTopicContent(false);
        } else {
            if (getIntent().hasExtra(KEY_TOPIC_FEED_FLOW)) {
                showType = TShowType.EMainTopic;
                if (cacheShowType != showType) {
                    //mCachelist.clear();
                }
                topicId = getIntent().getIntExtra(KEY_TOPIC_FEED_FLOW, -1);
                topicTag = getIntent().getStringExtra("topic_content");
                //titleView.setText("#" + topicTag + "#");
                titleView.setText("新鲜事");
                adapter.setShowTopicContent(true);
            } else if (getIntent().hasExtra(KEY_MY_FEED_FLOW)) {
                creatorUid = getIntent().getIntExtra(KEY_MY_FEED_FLOW, -1);
                showType = TShowType.EMainUser;
                if (cacheShowType != showType) {
                    //mCachelist.clear();
                }
                if (cacheCreatorUid != creatorUid) {
                    //mCachelist.clear();
                }
                titleView.setText("三两小事");
                if (UserManager.getUid(this) != creatorUid) {
                    mEditImage.setVisibility(View.GONE);
                }
                UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEPERSONALDYNAMIC);
            } else if (getIntent().hasExtra("feed_id")) {
                showType = TShowType.EMainId;
                feedId = getIntent().getStringExtra("feed_id");
                adapter.setMainId(true);
                mEditImage.setVisibility(View.GONE);
                adapter.setShowTopicContent(false);
                titleView.setText("小事详见");
            } else if (getIntent().hasExtra(KEY_FRIEND_FEED_FLOW)) {
                showType = TShowType.EFriendDynamic;
                if (cacheShowType != showType) {
                    //mCachelist.clear();
                }
                adapter.setShowTopicContent(true);
                titleView.setText("陪我的圈");
                UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEPERSONALDYNAMIC);
            } else if (getIntent().hasExtra(KEY_GROUP_FEED_FLOW)) {
                showType = TShowType.EGroupFeed;
                mEditImage.setVisibility(View.GONE);
                adapter.setShowTopicContent(true);
                titleView.setText(res.getString(R.string.group_feed_flow));
            } else {
                showType = TShowType.EMainAll;
                adapter.setShowTopicContent(true);
                titleView.setText("往期内容");
            }
//        if (showType != TShowType.EMainId && showType != TShowType.EMainAll && mCachelist.size() > 0) {
//            mlist.addAll(mCachelist);
//            notifyDataSetChanged();
//        }
        }

        pullToRefreshListView.setRefreshing();
        getFeedFlowPrise();
        boolean netAvailable = PWUtils.isNetWorkAvailable(this);
        if (!netAvailable) {
            showToast(this, getResources().getString(R.string.umeng_common_network_break_alert));
        }
    }


    @Override
    public void onLastItemVisible() {
        if (hasmore && !isloading) {
            UmengStatisticsAgent.onEvent(FeedFlowActivity.this, UMEventIDS.UMEPULLUPLOADING);
            if (showType == TShowType.EGroupFeed) {
                requestGroupFeedFlow();
            } else {
                requestServer();
            }
        }
    }


    private void addReleaseHeaderView() {
        if (getIntent().getBooleanExtra("feed_single", false)) {
            return;
        }
        if (showType != TShowType.EMainTopic)
            return;
        if (!show_bar) {
            removeReleaseHeaderView();
            return;
        }
        if (headView != null && headView.getTag() != null && Boolean.valueOf(headView.getTag().toString())) {
            return;
        }
        if (options == null) {
            options = new DisplayImageOptions.Builder().cacheInMemory(true)
                    .cacheOnDisk(true).displayer(new CircleBitmapDisplayer())
                    .build();
        }
        if (headView == null) {
            headView = View.inflate(this, R.layout.feed_flow_head_view, null);
        }
        headView.findViewById(R.id.iv_uface_layout).setOnClickListener(this);
        ImageView v = (ImageView) headView.findViewById(R.id.iv_uface);
        PWUserModel model = UserManager.getPWUser(this);
        ImageLoader.getInstance().displayImage(model.avatar_thumbnail, v, options);
        headView.findViewById(R.id.btn_pub_feed_flow).setOnClickListener(this);
        pullToRefreshListView.getRefreshableView().addHeaderView(headView);
        headView.setTag(true);
    }

    private void removeReleaseHeaderView() {
        if (headView != null) {
            pullToRefreshListView.getRefreshableView().removeHeaderView(headView);
            headView.setTag(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_edit:
            case R.id.btn_pub_feed_flow:
            case R.id.iv_uface_layout:
                UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEEDITFEED);
                if ((showType == TShowType.EMainUser && UserManager.getUid(this) == creatorUid)
                        || showType == TShowType.EMainAll || showType == TShowType.EFriendDynamic) {
                    Intent topicIntent = new Intent(this, CreatTopicActivity.class);
                    startActivity(topicIntent);
                } else {
                    Intent intent = new Intent(this, InformationPubActivity.class);
                    intent.putExtra("topic_id", topicId);
                    intent.putExtra("topic_content", topicContent);
                    startActivity(intent);
                }
                break;
            case R.id.btn_left:
                finish();
                break;
            case R.id.tv_new_message:
                EventBus.getDefault().post(new RedPonitVisibilityEvent(0));
                SharedPreferencesUtil.putIntExtra(PeiwoApp.getApplication(),
                        "like_num_" + UserManager.getUid(PeiwoApp.getApplication()), 0);
                tv_new_message.setVisibility(View.GONE);
                Intent praiseIntent = new Intent(this, TrendNoticeActivity.class);
                startActivity(praiseIntent);
                break;
            case R.id.title:
                if (System.currentTimeMillis() - clickTitleTime < 500) {
                    pullToRefreshListView.getRefreshableView().smoothScrollToPosition(0);
                    clickTitleTime = 0L;
                } else {
                    clickTitleTime = System.currentTimeMillis();
                }
                break;
        }
    }

    private long clickTitleTime = 0L;

    @Override
    public void finish() {
        if (showType == TShowType.EMainId) {
            Intent intent = null;
            if (isDelete) {
                intent = new Intent();
                intent.putExtra("is_delete", true);
            } else {
                if (likerHandleMap.size() > 0) {
                    intent = new Intent();
                    intent.putExtra("is_like", likerHandleMap.get(feedId));
                }
            }
            if (intent != null) {
                intent.putExtra("feed_id", feedId);
                setResult(RESULT_OK, intent);
            }
        }
        int current_item = getIntent().getIntExtra("current_item", -1);
        if (current_item >= 0) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("current_item", current_item);
            startActivity(intent);
        }
        TcpProxy.getInstance().requestFriendPubFlow();
        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case VIEW_DETAILS_CODE:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        String feed_id = data.getStringExtra("feed_id");
                        boolean is_like = data.getBooleanExtra("is_like", true);
                        boolean is_delete = data.getBooleanExtra("is_delete", false);
                        if (!TextUtils.isEmpty(feed_id)) {
                            for (int i = 0; i < mlist.size(); i++) {
                                FeedFlowModel model = mlist.get(i);
                                if (feed_id.equals(model.getId())) {
                                    if (is_delete) {
                                        mlist.remove(i);
                                    } else {
                                        if (model.getIs_like() == 1 && !is_like) {
                                            model.setIs_like(0);
                                            model.setLike_number(model.getLike_number() - 1);
                                        }
                                        if (model.getIs_like() == 0 && is_like) {
                                            model.setIs_like(1);
                                            model.setLike_number(model.getLike_number() + 1);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        notifyDataSetChanged();
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void requestGroupFeedFlow() {
        isloading = true;
        String group_id = getIntent().getStringExtra(GroupHomePageActvity.KEY_GROUP_ID);
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(GroupHomePageActvity.KEY_GROUP_ID, group_id));
        String update_time;
        if (mIndex != 1) {
            update_time = mlist.get(mlist.size() - 1).getUpdate_time();
            params.add(new BasicNameValuePair(KEY_PREV_TIME, update_time));
            CustomLog.d("requestGroupFeedFlow update_time is : " + update_time);
        }
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_GROUP_FEED_FLOW, new MsgStructure() {

            @Override
            public void onReceive(JSONObject data) {
                Message msg = mHandler.obtainMessage(HANDLE_MSG_REQUEST_LIST_SUCCESS);
                msg.obj = data;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onError(int error, Object ret) {
                Message msg = mHandler.obtainMessage(HANDLE_MSG_REQUEST_LIST_FAILURE);
                msg.obj = error;
                mHandler.sendMessage(msg);
            }
        });
    }

    private void requestServer() {
        isloading = true;
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        String apiMethod = AsynHttpClient.API_TAG_TOPIC_LIST;
        if (getIntent().getBooleanExtra("feed_single", false)) {
            params.add(new BasicNameValuePair("topic_id", String.valueOf(topicId)));
            params.add(new BasicNameValuePair("page", String.valueOf(mIndex)));
            params.add(new BasicNameValuePair("ipp", "10"));
        } else {
            if (showType == TShowType.EMainTopic) {
                //全部的信息流换2.0接口，其余类型信息流不变
                apiMethod = AsynHttpClient.API_TAG_MAIN_TOPIC_LIST;
                params.add(new BasicNameValuePair("topic_id", String.valueOf(topicId)));
            } else if (showType == TShowType.EMainUser) {
                params.add(new BasicNameValuePair("creator_id", String.valueOf(creatorUid)));
            }
            if (showType == TShowType.EMainId) {
                params.add(new BasicNameValuePair("id", feedId));
                apiMethod = AsynHttpClient.API_TOPIC_PUB;
            } else if (showType == TShowType.EMainAll) {
                apiMethod = AsynHttpClient.API_FEED_PUB_HISTORY;
                params.add(new BasicNameValuePair("page", String.valueOf(mIndex)));
                params.add(new BasicNameValuePair("ipp", "10"));
            } else if (showType == TShowType.EFriendDynamic) {
                apiMethod = AsynHttpClient.API_FEED_PUB_FRIEND;
                params.add(new BasicNameValuePair("page", String.valueOf(mIndex)));
                params.add(new BasicNameValuePair("ipp", "10"));
                if (mlist.size() > 0 && mIndex > 1) {
                    params.add(new BasicNameValuePair("prev_time", String.valueOf(mlist.get(mlist.size() - 1).prev_time)));
                }
            } else {
                params.add(new BasicNameValuePair("page", String.valueOf(mIndex)));
                params.add(new BasicNameValuePair("ipp", "10"));
            }
        }


        ApiRequestWrapper.openAPIGET(this, params,
                apiMethod, new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
//                            //去重
//                            JSONArray resultArray = new JSONArray();
//                            JSONArray pubsArray = data.optJSONArray("pubs");
//                            for (int i = 0; i < pubsArray.length(); i++) {
//                                JSONObject item = pubsArray.getJSONObject(i);
//                                if (needPut(item.optString("id", ""))) {
//                                    resultArray.put(item);
//                                }
//                            }
//                            data.put("pubs", resultArray);
                        Message msg = mHandler.obtainMessage(HANDLE_MSG_REQUEST_LIST_SUCCESS);
                        msg.obj = data;
                        mHandler.sendMessage(msg);

                    }

                    @Override
                    public void onError(int error, Object ret) {
                        Message msg = mHandler.obtainMessage(HANDLE_MSG_REQUEST_LIST_FAILURE);
                        msg.arg1 = error;
                        mHandler.sendMessage(msg);
                    }
                });
    }

//    private boolean needPut(String id) {
//        for (FeedFlowModel model : mlist) {
//            if (id.equals(model.getId())) {
//                return false;
//            }
//        }
//        return true;
//    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (mlist.size() == 0) {
            showAnimLoading();
        }
        mIndex = 1;
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEPULLDOWNLOADING);
        if (showType == TShowType.EGroupFeed) {
            requestGroupFeedFlow();
        } else {
            requestServer();
        }
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEPULLUPLOADING);
        if (showType == TShowType.EGroupFeed) {
            requestGroupFeedFlow();
        } else {
            requestServer();
        }
    }

    private void getTagTopic() {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        ApiRequestWrapper.openAPIGET(PeiwoApp.getApplication(), params,
                AsynHttpClient.API_TAG_TOPIC, new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                        Message message = mHandler.obtainMessage();
                        message.what = WHAT_TAG_RECEIVE;
                        message.obj = data;
                        mHandler.sendMessage(message);
                    }

                    @Override
                    public void onError(int error, Object ret) {
                        mHandler.sendEmptyMessage(WHAT_TAG_RECEIVE_ERROR);
                    }
                });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dismissAnimLoading();
            int what = msg.what;
            switch (what) {
                case WHAT_RECEIVE_RELATION:
                    handleRelation(msg);
                    break;
                case WHAT_TAG_RECEIVE:
                    JSONObject o = (JSONObject) msg.obj;
                    topicId = o.optInt("id");
                    topicContent = o.optString("content");
                    findViewById(R.id.ll_start_topic).setVisibility(View.VISIBLE);
                    TextView tv_topic_today = (TextView) findViewById(R.id.tv_topic_today);
                    tv_topic_today.setText("#" + topicContent + "#");
                    break;
                case HANDLE_MSG_REQUEST_LIST_SUCCESS: {
                    isloading = false;
                    pullToRefreshListView.onRefreshComplete();
                    JSONObject dataObject = (JSONObject) msg.obj;
                    show_bar = dataObject.optInt("show_bar") == 1;
                    boolean isEnd = !dataObject.has("next") || "null".equals(dataObject.optString("next"));
                    if (showType != TShowType.EMainId) {
                        JSONArray pubsArray = dataObject.optJSONArray("pubs");
                        CustomLog.d("requestGroupFeedflow HANDLE_MSG_REQUEST_LIST_SUCCESS. has more : " + pubsArray.length());
                        if (pubsArray.length() == 0) {
                            hasmore = false;
                        }
                        if (mIndex == 1) {
                            mlist.clear();
                        }
                        if (pubsArray != null && pubsArray.length() > 0) {
                            for (int i = 0; i < pubsArray.length(); i++) {
                                JSONObject pubsObject = pubsArray.optJSONObject(i);
                                FeedFlowModel model = new FeedFlowModel(pubsObject);
                                if (likerHandleMap.containsKey(model.getId())) {
                                    boolean isLike = likerHandleMap.get(model.getId());
                                    if (model.getIs_like() == 1 && !isLike) {
                                        model.setIs_like(0);
                                    }
                                    if (model.getIs_like() == 0 && isLike) {
                                        model.setIs_like(1);
                                    }
                                }
                                CustomLog.d("handleMessage. requestGroupFeedflow update time is : " + model.getUpdate_time());
                                mlist.add(model);
                            }
                        }
                    } else {
                        mlist.clear();
                        FeedFlowModel model = new FeedFlowModel(dataObject);
                        if (likerHandleMap.containsKey(model.getId())) {
                            boolean isLike = likerHandleMap.get(model.getId());
                            if (model.getIs_like() == 1 && !isLike) {
                                model.setIs_like(0);
                            }
                            if (model.getIs_like() == 0 && isLike) {
                                model.setIs_like(1);
                            }
                        }
                        mlist.add(model);
                    }

//                    if (showType == TShowType.EMainTopic && !mlist.contains(createTopicModel)) {
//                        if (isEnd) {
//                            if (mlist.size() > 3) {
//                                mlist.add(3, createTopicModel);
//                            } else {
//                                mlist.add(createTopicModel);
//                            }
//                        } else {
//                            if (mlist.size() > 3) {
//                                mlist.add(3, createTopicModel);
//                            }
//                        }
//                    }
                    addReleaseHeaderView();
                    notifyDataSetChanged();
                    if (!hasmore) {
                        pullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
                        pullToRefreshListView.end(true);
                    } else {
                        pullToRefreshListView.end(false);
                    }
                    mIndex++;
                    if (showType == TShowType.EFriendDynamic) {
                        ListView listView = pullToRefreshListView.getRefreshableView();
                        if (listView.getAdapter().getCount() - listView.getHeaderViewsCount() - listView.getHeaderViewsCount() == 0) {
                            getTagTopic();
                        }
                    }
                }
                break;
                case HANDLE_MSG_REQUEST_LIST_FAILURE: {
                    pullToRefreshListView.onRefreshComplete();
                    if (showType == TShowType.EMainId) {
                        if (msg.arg1 == 20003) {
                            showToast(FeedFlowActivity.this, "内容被移除");
                            finish();
                        }
                    }
                }
                break;
                case HANDLE_MSG_LIKE: {
                    UmengStatisticsAgent.onEvent(FeedFlowActivity.this, UMEventIDS.UMECLICKPRAISE);
                    int positon = (Integer) msg.obj;
                    FeedFlowModel model = mlist.get(positon);
                    boolean needAdd = false;
                    if (likerHandleMap.containsKey(model.getId())) {
                        likerHandleMap.remove(model.getId());
                    } else {
                        needAdd = true;
                    }
                    if (model.getIs_like() == 1) {
                        //取消点赞
                        model.setIs_like(0);
                        model.setLike_number(model.getLike_number() - 1);
                        if (needAdd) {
                            likerHandleMap.put(model.getId(), false);
                        }
                    } else {
                        model.setIs_like(1);
                        model.setLike_number(model.getLike_number() + 1);
                        if (needAdd) {
                            likerHandleMap.put(model.getId(), true);
                        }
                    }
                    notifyDataSetChanged();
                }
                break;
                case HANDLE_MSG_SAY_HELLO: {
                    sendSayHelloMsg((FeedFlowModel) msg.obj);
                }
                break;
                case HANDLE_MSG_DELETE_FEED_FLOW: {
                    int position = (Integer) msg.obj;
                    deleteFeedFlow(position);
                }
                break;
                case HANDLE_MSG_REPORT_FEED_FLOW: {
                    int position = (Integer) msg.obj;
                    reportFeedFlow(mlist.get(position));
                }
                break;
                case HANDLE_MSG_WHO_LIKE_LIST: {
                    Intent praiseIntent = new Intent(FeedFlowActivity.this, PWPraiseActivity.class);
                    String id = (String) msg.obj;
                    praiseIntent.putExtra("pub_id", id);
                    startActivity(praiseIntent);
                }
                break;
                case HANDLE_MSG_REPORT_FEED_FLOW_ERROR: {
                    showToast(FeedFlowActivity.this, "举报失败");
                }
                break;
                case HANDLE_MSG_REPORT_FEED_FLOW_SUCCESS: {
                    showToast(FeedFlowActivity.this, "举报成功");
                }
                break;
                case HANDLE_MSG_DELETE_FEED_FLOW_SUCCESS: {
                    isDelete = true;
                    notifyDataSetChanged();
                    showToast(FeedFlowActivity.this, "删除成功");
                    if (showType == TShowType.EMainId) {
                        finish();
                    }
                }
                break;
                case HANDLE_MSG_DELETE_FEED_FLOW_ERROR: {
                    showToast(FeedFlowActivity.this, "删除失败");
                }
                break;
                case WHAT_DATA_RECEIVE_RED_POINT_VISIBLE: {
                    int count = (Integer) msg.obj;
                    if (count > 0) {
                        tv_new_message.setVisibility(View.VISIBLE);
                        tv_new_message.setText(count + "条新消息");
                    } else {
                        tv_new_message.setVisibility(View.GONE);
                        tv_new_message.setText(null);
                    }
                }
                break;
                case HANDLE_VIEW_TOPIC:
                    int topicId = msg.arg1;
                    String topicContent = String.valueOf(msg.obj);
                    Intent intent = new Intent(FeedFlowActivity.this, FeedFlowActivity.class);
                    intent.putExtra("topic_id", topicId);
                    intent.putExtra("topic_content", topicContent);
                    intent.putExtra("feed_single", true);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    };

    private void handleRelation(Message msg) {
        int relation = msg.arg1;
        FeedFlowModel model = (FeedFlowModel) msg.obj;
        CustomLog.d("handleRelation, relation is : " + relation);
        switch (relation) {
            case UserInfoActivity.RELATION_FRIENDS:
                startChat(model);
                break;
            case UserInfoActivity.RELATION_FOLLOW:
            case UserInfoActivity.RELATION_STRANGER:
                boolean netAvailable = PWUtils.isNetWorkAvailable(this);
                if (netAvailable) {
                    sendVoiceRequest(model);
                } else {
                    showToast(this, getResources().getString(R.string.umeng_common_network_break_alert));
                }
                break;
            case UserInfoActivity.RELATION_FANS:
                acceptRequest(model);
                break;
            case UserInfoActivity.RELATION_TROUBLESOME_PERSON:
                break;
            default:
                break;
        }
    }

    private void sendVoiceRequest(final FeedFlowModel model) {
        PWUserModel userModel = model.userModel;
        mRecorderDialogFrag = RecorderDialogFragment.newInstance(userModel.uid, userModel.name, userModel.avatar_thumbnail, Constans.PW_MESSAGE_FROM_FEED);
        mRecorderDialogFrag.show(getSupportFragmentManager(), mRecorderDialogFrag.toString());
    }

    private void acceptRequest(final FeedFlowModel model) {
        JSONObject jobj = new JSONObject();
        int msg_from = Constans.PW_MESSAGE_FROM_FEED;
        try {
            jobj.put("from", msg_from);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TcpProxy.getInstance().focusUser(model.userModel.uid, jobj);
        startChat(model);
    }

    private void startChat(final FeedFlowModel model) {
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMESAYHELLO);
        Intent intent = new Intent(this, MsgAcceptedMsgActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Serializable data = model.userModel;
        intent.putExtra("msg_user", data);
        intent.putExtra("feed_id", model.getId());
        if (model.getImageList() != null && model.getImageList().size() > 0) {
            intent.putExtra("icon_url", model.getImageList().get(0).thumbnail_url);
        }
        if (TextUtils.isEmpty(model.getTopicContent())) {
            intent.putExtra("topic_title", topicContent);
        } else {
            intent.putExtra("topic_title", model.getTopicContent());
        }

        intent.putExtra("topic_content", model.getContent());
        intent.putExtra(UserInfoActivity.MESSAGE_FROM, Constans.PW_MESSAGE_FROM_FEED);
        startActivity(intent);
    }

    private void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
        if (showType == TShowType.EMainUser && UserManager.getUid(this) == creatorUid) {
            if (adapter.getCount() == 0) {
                findViewById(R.id.empty_list_layout).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.empty_list_layout).setVisibility(View.GONE);
            }
        }
    }

    private void deleteFeedFlow(final int position) {
        new AlertDialog.Builder(this).setTitle("删除")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("id", mlist.get(position).getId()));
                        ApiRequestWrapper.openAPIPOST(FeedFlowActivity.this, params, AsynHttpClient.API_DELETE_FEED_FLOW, new MsgStructure() {

                            @Override
                            public void onReceive(JSONObject data) {
                                mlist.remove(position);
                                mHandler.sendEmptyMessage(HANDLE_MSG_DELETE_FEED_FLOW_SUCCESS);
                            }

                            @Override
                            public void onError(int error, Object ret) {
                                mHandler.sendEmptyMessage(HANDLE_MSG_DELETE_FEED_FLOW_ERROR);
                            }
                        });
                    }
                }).setNegativeButton("取消", null).create().show();
    }

    private void reportFeedFlow(final FeedFlowModel model) {
        new AlertDialog.Builder(this).setTitle("举报")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("pub_id", model.getId()));
                        ApiRequestWrapper.openAPIPOST(FeedFlowActivity.this, params, AsynHttpClient.API_REPORT_FEED_FLOW, new MsgStructure() {

                            @Override
                            public void onReceive(JSONObject data) {
                                mHandler.sendEmptyMessage(HANDLE_MSG_REPORT_FEED_FLOW_SUCCESS);
                            }

                            @Override
                            public void onError(int error, Object ret) {
                                mHandler.sendEmptyMessage(HANDLE_MSG_REPORT_FEED_FLOW_ERROR);
                            }
                        });
                    }
                }).setNegativeButton("取消", null).create().show();

    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);

        if (showType != TShowType.EMainId && showType != TShowType.EMainAll) {
            cacheShowType = showType;
            if (showType == TShowType.EMainUser) {
                cacheCreatorUid = creatorUid;
            }
            //mCachelist.addAll(mlist);
        }
        super.onDestroy();
    }

    ;

    @Override
    public void onPause() {
        if (TcpProxy.getInstance().isLoginStauts()) {
            if (!TcpProxy.getInstance().feedFlowLikeHandle(likerHandleMap)) {
                cacheLikerHandleMap.putAll(likerHandleMap);
            }
            likerHandleMap.clear();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        likerHandleMap.putAll(cacheLikerHandleMap);
        super.onResume();
    }

    public void getFeedFlowPrise() {
        if (/*showType != TShowType.EMainUser ||*/ UserManager.getUid(this) != creatorUid) {
            return;
        }
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_PRISE_COUNT, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                int count = data.optInt("count");
                if (count > 0) {
                    Message msg = mHandler.obtainMessage();
                    msg.obj = count;
                    msg.what = WHAT_DATA_RECEIVE_RED_POINT_VISIBLE;
                    mHandler.sendMessage(msg);
                }
            }

            @Override
            public void onError(int error, Object ret) {

            }
        });
    }

    private void sendSayHelloMsg(final FeedFlowModel model) {
        showAnimLoading();
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("tuid", String.valueOf(model.getUserModel().uid)));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_RELATION, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                int relation = data.optInt("relation");
                Message msg = mHandler.obtainMessage(WHAT_RECEIVE_RELATION);
                msg.obj = model;
                msg.arg1 = relation;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onError(int error, Object ret) {
                mHandler.post(() -> {
                            dismissAnimLoading();
                        }
                );
            }
        });
    }

    public void onEventMainThread(RedPonitVisibilityEvent event) {
        if (/*showType != TShowType.EMainUser ||*/ UserManager.getUid(this) == creatorUid) {
            return;
        }
        if (event.count > 0) {
            tv_new_message.setText(event.count + "条新消息");
            tv_new_message.setVisibility(View.VISIBLE);
        } else {
            tv_new_message.setVisibility(View.GONE);
        }
    }

    public void onEventMainThread(CreateFeedFlowEvent event) {
        if (event == null) {
            return;
        }
        FeedFlowModel mModel = event.model;
        if (mModel != null) {
            mlist.add(0, mModel);
            notifyDataSetChanged();
        } else {
            pullToRefreshListView.setRefreshing();
        }
    }

    @Override
    public void click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.ll_start_topic:
                Intent intent = new Intent(this, FeedFlowActivity.class);
                intent.putExtra("topic_id", this.topicId);
                intent.putExtra("topic_content", this.topicContent);
                intent.putExtra("current_item", 0);
                startActivity(intent);
                finish();
                break;
        }
    }
}
