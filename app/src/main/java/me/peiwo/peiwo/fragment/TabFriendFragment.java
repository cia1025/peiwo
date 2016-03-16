package me.peiwo.peiwo.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.*;
import me.peiwo.peiwo.adapter.PWContactsAdapter;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.db.BriteDBHelperHolder;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.eventbus.event.RedPonitVisibilityEvent;
import me.peiwo.peiwo.model.PWContactExtraData;
import me.peiwo.peiwo.model.PWContactsModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.UmengStatisticsAgent;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.PWPullToRefreshListView;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.lang.ref.WeakReference;
import java.util.*;

public class TabFriendFragment extends PPBaseFragment implements
        OnRefreshListener<ListView>, OnItemClickListener,
        OnItemLongClickListener,
        OnClickListener, TextView.OnEditorActionListener, AbsListView.OnScrollListener {

    public static final String ACTION_REFRESH = "me.peiwo.peiwo.ACTION_REFRESH_CONTACT";
    private boolean hasCallDuration = false;
    //次逻辑可以去掉
    private Map<String, PWContactExtraData> mExtraDataMap = new HashMap<>();

    public static TabFriendFragment newInstance() {
        return new TabFriendFragment();
    }

    private PWPullToRefreshListView pullToRefreshListView;
    private PWContactsAdapter mAdapter;
    private List<PWContactsModel> models = new ArrayList<>();
    private MyHandler mHandler;


    private static final int WHAT_DATA_RECEIVE_ERROR_DATA_NOEXISTS = 10001;
    private static final int WHAT_DATA_RECEIVE_NOT_AVAILABLE = 10002;
    private static final int WHAT_DATA_RECEIVE_PWSEARCH_NO = 10003;

    private static final int WHAT_DATA_RECEIVE_DELCONTACT = 3000;
    private static final int WHAT_DATA_RECEIVE_SORT_BY_SIGIN_TIME = 7000;
    private static final int WHAT_RED_POINT_VISIBILITY = 8000;
    private static final int REQUEST_CODE_USERINFO = 5000;


    private static final int WHAT_TAG_RECEIVE = 3001;
    private static final int WHAT_TAG_RECEIVE_ERROR = 3002;


    private EditText et_search_pwnum;
    private View v_action_search;
    private View iv_feed_ind;
    private TextView tv_feed_des;
    private SORT_TYPE curr_sort_type = SORT_TYPE.SOUR_BY_ACTIVE_TIME;
    private View emptyView;

    private enum SORT_TYPE {
        SOUR_BY_ACTIVE_TIME,
        SOUR_BY_CALL_DURATION
    }

    private CompositeSubscription mCompositeSubscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UmengStatisticsAgent.onEvent(getActivity(), UMEventIDS.UMECOBBERPAGE);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_pw_contacts, container, false);
        emptyView = v.findViewById(R.id.layout_empty);
        emptyView.setOnClickListener(v1 -> startActivity(new Intent(getActivity(), WildCatCallActivity.class)));
//        View iv_icon_friends_blank = emptyView.findViewById(R.id.iv_icon_friends_blank);
//        iv_icon_friends_blank.setOnClickListener(v1 -> startActivity(new Intent(getActivity(), WildCatCallActivity.class)));
        pullToRefreshListView = (PWPullToRefreshListView) v.findViewById(R.id.pullToRefreshListView);
        //pullToRefreshListView.getRefreshableView().setEmptyView(emptyView);
        pullToRefreshListView.setOnRefreshListener(this);

        View header = View.inflate(getActivity(), R.layout.activity_friend_circle_head, null);
        View v_my_group = header.findViewById(R.id.v_my_group);
        v_my_group.setOnClickListener(v_group -> startActivity(new Intent(getActivity(), MyGroupsActivity.class)));
        iv_feed_ind = header.findViewById(R.id.iv_feed_ind);
        tv_feed_des = (TextView) header.findViewById(R.id.tv_feed_des);
        View v_header_pic = header.findViewById(R.id.v_header_pic);
        setHeaderPicSize(v_header_pic);
        v_header_pic.setOnClickListener(this);
        et_search_pwnum = (EditText) header.findViewById(R.id.et_search_pwnum);
        et_search_pwnum.setOnEditorActionListener(this);
        listenSearchTextChanged();
        header.findViewById(R.id.iv_sort_action).setOnClickListener(this);
        v_action_search = header.findViewById(R.id.v_action_search);
        v_action_search.setOnClickListener(this);

        pullToRefreshListView.getRefreshableView().addHeaderView(header, null, false);
        pullToRefreshListView.setOnScrollListener(this);
        mAdapter = new PWContactsAdapter(models, getActivity());
        pullToRefreshListView.setAdapter(mAdapter);
        pullToRefreshListView.setOnItemClickListener(this);
        pullToRefreshListView.setOnItemLongClickListener(this);
        mCompositeSubscription = new CompositeSubscription();
        loadDataFromLocal(true);

        mHandler = new MyHandler(this);
        getTagTopic();
        EventBus.getDefault().register(this);
        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        TcpProxy.getInstance().requestFriendPubFlow();
        if(isVisibleToUser)
            UmengStatisticsAgent.onEvent(getActivity(), UMEventIDS.UMECOBBERPAGE);
    }

    private void setHeaderPicSize(View v_header_pic) {
        ViewGroup.LayoutParams params = v_header_pic.getLayoutParams();
        params.height = 245 * PWUtils.getWindowWidth(getActivity()) / 750;
        v_header_pic.setLayoutParams(params);
    }

    private void listenSearchTextChanged() {
        et_search_pwnum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    v_action_search.setVisibility(View.INVISIBLE);
                } else {
                    v_action_search.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getTagTopic() {
        ArrayList<NameValuePair> params = new ArrayList<>();
        ApiRequestWrapper.openAPIGET(getActivity(), params,
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


    @Override
    public void onResume() {
        //PWUtils.getUserInfo(getActivity());
        super.onResume();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    private void requestServer() {
//        try {
        BriteDatabase briteDatabase = BriteDBHelperHolder.getInstance().getBriteDatabase(getActivity());
        if(briteDatabase == null) return;
        String sql = String.format("select sync_id from %s order by sync_id desc limit 1", PWDBConfig.TB_PW_CONTACTS);
        Subscription subscription = briteDatabase.createQuery(PWDBConfig.TB_PW_CONTACTS, sql)
                .map(query -> {
                    int sync_id = 0;
                    Cursor cursor = query.run();
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            sync_id = cursor.getInt(0);
                        }
                        cursor.close();
                    }
                    return sync_id;
                }).subscribe(this::getPWContactsBySyncId);
        //briteDatabase.close();
        subscription.unsubscribe();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void getPWContactsBySyncId(int sync_id) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("max_id", String.valueOf(sync_id)));
        ApiRequestWrapper.openAPIGET(getActivity(), params,
                AsynHttpClient.API_CONTACT_LIST, new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                        Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(TabFriendFragment.this::fetchPWContactsToDB);
                    }

                    @Override
                    public void onError(int error, Object ret) {
                        Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> {
                            pullToRefreshListView.onRefreshComplete();
                        });
                    }
                });
    }

    private void fetchPWContactsToDB(JSONObject o) {
        List<PWContactsModel> childs = new ArrayList<>();
        try {
            JSONArray array = o.getJSONArray("contacts");
            for (int i = 0, z = array.length(); i < z; i++) {
                JSONObject object = array.getJSONObject(i);
                //==0是好友==1被删
                childs.add(new PWContactsModel(object));
            }
            if (childs.size() != 0) {
                mergeDB(childs);
            } else {
                pullToRefreshListView.onRefreshComplete();
                requestActiveTimeAndCallDuration(false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void mergeDB(List<PWContactsModel> childs) {
        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    //Log.i("mergedb", "call == " + (Looper.myLooper() == Looper.getMainLooper()));
                    BriteDatabase database = BriteDBHelperHolder.getInstance().getBriteDatabase(getActivity());
                    if(database == null) return;
                    BriteDatabase.Transaction transaction = database.newTransaction();
                    String insert_sql = String.format("insert or replace into %s (uid, sync_id, avatar, avatar_thumbnail, birthday, price, gender, name, slogan, city, province, contact_id, signin_time, contact_state, call_duration) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", PWDBConfig.TB_PW_CONTACTS);
                    for (PWContactsModel model : childs) {
                        database.execute(insert_sql, model.uid, model.sync_id, model.avatar, model.avatar_thumbnail, model.birthday, model.price, model.gender, model.name, model.slogan, model.city, model.province, model.contact_id, model.signin_time, model.contact_state, model.call_duration);
                    }
                    transaction.markSuccessful();
                    transaction.end();
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(new Exception("db error"));
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {
                //Log.i("mergedb", "onCompleted == " + (Looper.myLooper() == Looper.getMainLooper()));
                loadDataFromLocal(false);
            }

            @Override
            public void onError(Throwable e) {
                //Log.i("mergedb", e.getMessage());
                pullToRefreshListView.onRefreshComplete();
            }

            @Override
            public void onNext(Boolean aBoolean) {

            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //CustomLog.i("syf", "onScrollStateChanged arg1 = " + arg1);
        switch (scrollState) {
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                //debugLog("SCROLL_STATE_IDLE");
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                //debugLog("SCROLL_STATE_TOUCH_SCROLL");
                PWUtils.hideSoftInput(et_search_pwnum, getActivity());
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                //debugLog("SCROLL_STATE_FLING");
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }


    private static class MyHandler extends Handler {
        WeakReference<TabFriendFragment> fragment_ref;

        public MyHandler(TabFriendFragment fragment) {
            fragment_ref = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            TabFriendFragment theFragment = fragment_ref.get();
            if (theFragment == null || theFragment.isDetached())
                return;
            int what = msg.what;
            switch (what) {

                case WHAT_DATA_RECEIVE_ERROR:
                    theFragment.pullToRefreshListView.onRefreshComplete();
                    theFragment.dismissAnimLoading();
                    theFragment.showToast(theFragment.getActivity(), "网络连接失败");
                    break;

                case WHAT_DATA_RECEIVE_SORT_BY_SIGIN_TIME:
                    theFragment.dismissAnimLoading();
                    break;
                case WHAT_RED_POINT_VISIBILITY:
                    //CustomLog.i("TabFriendFragment.MyHandler.handleMessage(), redpoint == " + redPoint);
//                    if (redPoint == null) {
//                        return;
//                    }
//                    RedPonitVisibilityEvent event = (RedPonitVisibilityEvent) msg.obj;
//                    if (event.count > 0) {
//                        redPoint.setVisibility(View.VISIBLE);
//                    } else {
//                        redPoint.setVisibility(View.GONE);
//                    }
                    break;
                case WHAT_TAG_RECEIVE:
                    JSONObject object = (JSONObject) (msg.obj);
                    String topic = object.optString("content");
                    theFragment.tv_feed_des.setText("今日话题 #" + topic + "#");
                    break;
                case WHAT_TAG_RECEIVE_ERROR:
                    CustomLog.i("no data!");
                    //tv_topic.setText("#如果制作照片#");
                    break;
                case WHAT_DATA_RECEIVE_PWSEARCH_NO:
                    theFragment.dismissAnimLoading();
                    Intent intent = new Intent(theFragment.getActivity(), UserInfoActivity.class);
                    intent.putExtra(UserInfoActivity.TARGET_UID, Integer.valueOf((String) msg.obj));
                    intent.putExtra(UserInfoActivity.MESSAGE_FROM, 2);
                    theFragment.startActivity(intent);
                    break;
                case WHAT_DATA_RECEIVE_ERROR_DATA_NOEXISTS:
                    theFragment.dismissAnimLoading();
                    theFragment.showToast(theFragment.getActivity(), "此账号不存在，请输入正确的陪我号");
                    break;
                case WHAT_DATA_RECEIVE_NOT_AVAILABLE:
                    theFragment.dismissAnimLoading();
                    theFragment.showToast(theFragment.getActivity(), "此账号已被封禁 ");
                    break;
            }
            super.handleMessage(msg);
        }
    }


    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
        new AlertDialog.Builder(getActivity())
                .setTitle("删除好友")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog1, which) -> {
                    PWContactsModel model = (PWContactsModel) parent.getAdapter().getItem(position);
                    deletePWContact(model.uid);
                })
                .create().show();
        return true;
    }

    public void deleteSingleFriendFromDB(String uid) {
        //Log.i("friend", "deleteSingleFriendFromDB");
        BriteDatabase database = BriteDBHelperHolder.getInstance().getBriteDatabase(getActivity());
        if(database == null) return;
        //int rows =
        database.delete(PWDBConfig.TB_PW_CONTACTS, "uid = ?", uid);
        //Log.i("friend", "rows == " + rows);
    }


    private void deletePWContact(final String tuid) {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tuids", tuid));
        showAnimLoading("", false, false, false);
        ApiRequestWrapper.openAPIGET(getActivity(), params, AsynHttpClient.API_CONTACT_DELETE, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Observable.just(tuid).observeOn(AndroidSchedulers.mainThread()).subscribe(uid -> {
                    dismissAnimLoading();
                    deleteSingleFriendFromDB(uid);
                    MsgDBCenterService.getInstance().deleteSayHelloMessageByUid(Integer.valueOf(uid));
                });
            }

            @Override
            public void onError(int error, Object ret) {
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PWContactsModel model = (PWContactsModel) parent.getAdapter().getItem(position);
        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
        intent.putExtra(AsynHttpClient.KEY_TUID, Integer.valueOf(model.uid));
        intent.putExtra(AsynHttpClient.KEY_NAME, model.name);
        startActivityForResult(intent, REQUEST_CODE_USERINFO);
        UmengStatisticsAgent.onEvent(getActivity(), UMEventIDS.UMEFRIENDLISTVISITHOME);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_sort_action:
                startSortPWContacts();
                UmengStatisticsAgent.onEvent(getActivity(), UMEventIDS.UMECLICKRANK);
                break;
            case R.id.friends_empey_layout:
                break;
            case R.id.v_action_search:
                startSearch();
                break;
            case R.id.v_header_pic:
                Intent intent = new Intent(getActivity(), FeedFlowActivity.class);
                intent.putExtra("is_firend", true);
                startActivity(intent);
                break;
        }
    }

    private void startSortPWContacts() {
        new AlertDialog.Builder(getActivity())
                .setTitle("排序")
                .setItems(new String[]{"与我通话最多", "登录时间", "取消"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            sortByCallDuration();
                            break;
                        case 1:
                            sortBySignInTimeFromLocal();
                            break;
                        default:
                            break;
                    }
                })
                .create().show();
    }

    private void sortBySignInTimeFromLocal() {
        curr_sort_type = SORT_TYPE.SOUR_BY_ACTIVE_TIME;
        Collections.sort(models, (p1, p2) -> p2.signin_time.compareTo(p1.signin_time));
        notifyDataChanged();
    }

    private void sortByCallDuration() {
        if (!hasCallDuration) {
            requestActiveTimeAndCallDuration(true);
        } else {
            sortCallDurationFromLocal();
        }
    }

    private void sortCallDurationFromLocal() {
        curr_sort_type = SORT_TYPE.SOUR_BY_CALL_DURATION;
        Collections.sort(models, (p1, p2) -> p2.call_duration - p1.call_duration);
        notifyDataChanged();
    }

    private void requestActiveTimeAndCallDuration(boolean isshowloading) {
        if (isshowloading) showAnimLoading("", false, false, false);
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("order_type", "1"));
        ApiRequestWrapper.openAPIGET(getActivity(), params, AsynHttpClient.API_CONTACT_ORDER, new MsgStructure() {
            @Override
            public boolean onInterceptRawData(String rawStr) {
                Map<String, PWContactExtraData> extraDataMap = new HashMap<>();
                try {
                    JSONObject object = new JSONObject(rawStr);
                    JSONArray array = object.optJSONArray("data");
                    if (array == null) {
                        array = new JSONArray();
                    }
                    for (int i = 0, z = array.length(); i < z; i++) {
                        PWContactExtraData data = new PWContactExtraData(array.getJSONObject(i));
                        extraDataMap.put(data.uid, data);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Observable.just(extraDataMap).observeOn(AndroidSchedulers.mainThread()).subscribe(map -> {
                    dismissAnimLoading();
                    updateActiveTimeAndCallDuration(map);
                });
                return true;
            }

            @Override
            public void onReceive(JSONObject data) {

            }

            @Override
            public void onError(int error, Object ret) {
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(e -> dismissAnimLoading());
            }
        });
    }

    private void updateActiveTimeAndCallDuration(Map<String, PWContactExtraData> extraDataMap) {
        mExtraDataMap.clear();
        mExtraDataMap.putAll(extraDataMap);
        for (PWContactsModel model : models) {
            PWContactExtraData data = extraDataMap.get(model.uid);
            if (data != null) {
                model.call_duration = data.contact_call_duration;
                model.signin_time = data.user_active_time;
            }
        }
        hasCallDuration = true;
        if (curr_sort_type == SORT_TYPE.SOUR_BY_ACTIVE_TIME) {
            sortBySignInTimeFromLocal();
        } else {
            sortCallDurationFromLocal();
        }
    }

    private void notifyDataChanged() {
        mAdapter.notifyDataSetChanged();
        if (models.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }


    @Override
    public void onRefresh(PullToRefreshBase<ListView> refreshView) {
        this.mExtraDataMap.clear();
        requestServer();
    }


    private void loadDataFromLocal(boolean auto_load_from_server) {
        try {
            BriteDatabase briteDatabase = BriteDBHelperHolder.getInstance().getBriteDatabase(getActivity());
            if(briteDatabase == null) return;
            String sql = String.format("select * from %s where contact_state = 0 order by signin_time desc", PWDBConfig.TB_PW_CONTACTS);
            Observable<SqlBrite.Query> observable = briteDatabase.createQuery(PWDBConfig.TB_PW_CONTACTS, sql);
            //loadDataFromLocal这个方法被调用多少次就会产生多少个subscription，db改变时就会调用多少次observable.subscribe，就会执行query
            Subscription subscription = observable.subscribe(query -> {
                //Log.i("friend", "loadDataFromLocal");
                List<PWContactsModel> childs = new ArrayList<>();
                Cursor cursor = query.run();
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        PWContactsModel model = new PWContactsModel(cursor);
                        if (this.mExtraDataMap.size() > 0) {
                            PWContactExtraData data = this.mExtraDataMap.get(model.uid);
                            if (data != null) {
                                model.call_duration = data.contact_call_duration;
                                model.signin_time = data.user_active_time;
                            }
                        }
                        childs.add(model);
                    }
                    cursor.close();
                }
                models.clear();
                models.addAll(childs);
                if (this.mExtraDataMap.size() > 0) {
                    sortBySignInTimeFromLocal();
                } else {
                    notifyDataChanged();
                }
            });
            if (mCompositeSubscription.hasSubscriptions()) {
                mCompositeSubscription.clear();
            }
            mCompositeSubscription.add(subscription);
            //briteDatabase.close();
            if (auto_load_from_server) {
                pullToRefreshListView.setRefreshing();
            } else {
                pullToRefreshListView.onRefreshComplete();
                requestActiveTimeAndCallDuration(false);
            }
            hasCallDuration = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            if (((MainActivity) activity).getCurrentFragmentIndex() == MainActivity.TAB_FRIENDS) {
                ((MainActivity) activity).updateNavgationViewControllerUI();
            }
        }
    }

    @Override
    public void onDetach() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription.unsubscribe();
        }
        super.onDetach();
    }

    public void onEventMainThread(RedPonitVisibilityEvent event) {
        if (event.count > 0) {
            iv_feed_ind.setVisibility(View.VISIBLE);
        } else {
            iv_feed_ind.setVisibility(View.GONE);
        }
    }

    public void onEventMainThread(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_REFRESH.equals(action)) {
                pullToRefreshListView.setRefreshing();
            }
        }
    }

    public void hideSoftKeyBoard() {
        PWUtils.hideSoftInput(et_search_pwnum, getActivity());
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (event == null || event.getAction() == KeyEvent.ACTION_UP) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                if (TextUtils.isEmpty(et_search_pwnum.getText())) {
                    showToast(getActivity(), "陪我号不能为空");
                } else {
                    startSearch();
                }
                return true;
            }
        }
        return true;
    }

    private void startSearch() {
        hideSoftKeyBoard();
        final String tuid = et_search_pwnum.getText().toString();
        if (tuid.length() >= 10) {
            showToast(getActivity(), "您输入的陪我号过长");
            return;
        }
        showAnimLoading("", false, false);
        ApiRequestWrapper.getUserInfo(getActivity(), UserManager.getUid(getActivity()), tuid, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Message message = mHandler.obtainMessage();
                message.what = WHAT_DATA_RECEIVE_PWSEARCH_NO;
                message.obj = tuid;
                mHandler.sendMessage(message);
            }

            @Override
            public void onError(int error, Object ret) {
                if (error == AsynHttpClient.DATA_NOT_EXISTS) {
                    mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR_DATA_NOEXISTS);
                } else if (error == AsynHttpClient.PW_RESPONSE_DATA_NOT_AVAILABLE) {
                    mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_NOT_AVAILABLE);
                } else {
                    mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
                }
            }
        });
    }

    @Override
    protected String getPageName() {
        return "好友列表";
    }
}
