package me.peiwo.peiwo.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.TabMsgAdapter;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.eventbus.event.MessagePushEvent;
import me.peiwo.peiwo.eventbus.event.MsgTitleChangedEvent;
import me.peiwo.peiwo.fragment.PPBaseFragment;
import me.peiwo.peiwo.model.TabMsgModel;
import me.peiwo.peiwo.model.TabfindGroupModel;
import me.peiwo.peiwo.net.*;
import me.peiwo.peiwo.service.NetworkConnectivityListener.NetworkCallBack;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.TimeUtil;
import me.peiwo.peiwo.util.UmengStatisticsAgent;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.PWPullToRefreshListView;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TabMsgFragment extends PPBaseFragment implements
        OnItemClickListener, OnItemLongClickListener,
        PullToRefreshBase.OnRefreshListener<ListView>, View.OnClickListener,
        NetworkCallBack, Observable.OnSubscribe<List<TabMsgModel>> {

    private static final String PAGE_NAME = "消息页面";
    /**
     * 查询向你申请通话的人接口，成功返回
     */
    public static final String LOCATION = "location";
    public static final String PRICE = "price";
    public static final String NAME = "name";
    public static final String AVATAR = "avatar";
    public static final String REMARK = "remark";

    private static final int WHAT_DATA_RECEIVE_FOCUS_LIST_SUCCESS = 20000;
    public static final int REQUEST_HELLO_MESSAGE = 0x10;
    private static final int REQUEST_GROUP_CHAT_ACTIVITY = 0X11;

    private int loaderId;

    public static TabMsgFragment newInstance(String tuid, int msg_type) {
        TabMsgFragment f = new TabMsgFragment();
        Bundle bundle = new Bundle();
        bundle.putString("tuid", tuid);
        bundle.putInt("msg_type", msg_type);
        f.setArguments(bundle);
        return f;
    }

    public static TabMsgFragment newInstance() {
        return new TabMsgFragment();
    }

    private PWPullToRefreshListView pullToRefreshListView;
    private MyHandler mHandler;
    private MyMsgStructure mStructure;
    private TabMsgAdapter adapter;

    private View mNetStateView;

    private List<TabMsgModel> mList = new ArrayList<TabMsgModel>();
    public static final int USER_MESSAGE = 2;// 用户通过权限或取消权限消息
    public static final int SYS_MESSAGE = 3; // 系统消息
    public static final int CALL_HISTORY = 4; // 通话记录
    public static final int GROUP_MESSAGE = 5;// 创建组消息

    private static final int WHAT_DELETE_ERROR = 3000;
    private static final int WHAT_DELETE_SUCCESS = 4000;

    private MsgDBCenterService dbService;
    private ImageView myBar;
    private LinearLayout barLayout;

    private Subscription subscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UmengStatisticsAgent.onEvent(getActivity(), UMEventIDS.UMEINFOMATION);
        PeiwoApp.getApplication().addNetworkCallBack(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
            //Log.i("debounce", "onAttach()");
            loaderId = hashCode();
            subscription = Observable.create(this).debounce(150, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(tabMsgModels -> {
                //Log.i("debounce", "debounce 1500");
                //Log.i("debounce", "observeOn " + System.currentTimeMillis());
                Collections.sort(tabMsgModels, (m1, m2) -> {
                    if (m1.update_time == null) {
                        m1.update_time = "";
                    }
                    if (m2.update_time == null) {
                        m2.update_time = "";
                    }
                    return m2.update_time.compareTo(m1.update_time);
                });
                mList.clear();
                mList.addAll(tabMsgModels);
                adapter.setSayHelloCount(dbService.getSayHelloListCount());
                adapter.notifyDataSetChanged();
                int unreadCount = 0;
                for (int i = 0; i < mList.size(); i++) {
                    unreadCount += mList.get(i).unread_count;
                }

                updateBadge(unreadCount);


                Bundle bundle = getArguments();
                if (bundle != null) {
                    String suid = bundle.getString("tuid");
                    if (TextUtils.isEmpty(suid)) {
                        return;
                    }
                    if (TabMsgFragment.GROUP_MESSAGE == bundle.getInt("msg_type")) {
                        inGroupChatActivity(suid);
                    } else {
                        int tuid = Integer.valueOf(suid);
                        if (tuid > 0) {
                            inChatActivity(tuid);
                        }
                    }
                    bundle.clear();
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_msg_frag, container, false);
        mNetStateView = v.findViewById(R.id.layout_network);
        init(v);

        return v;
    }

    private void init(View v) {
        mHandler = new MyHandler(this);
        mStructure = new MyMsgStructure();
        myBar = (ImageView) v.findViewById(R.id.net_bar);
        barLayout = (LinearLayout) v.findViewById(R.id.bar_layout);
        pullToRefreshListView = (PWPullToRefreshListView) v.findViewById(R.id.pullToRefreshListView);
        pullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        adapter = new TabMsgAdapter(mList, getActivity());
        pullToRefreshListView.setAdapter(adapter);
        pullToRefreshListView.setOnRefreshListener(this);
        pullToRefreshListView.setOnItemClickListener(this);
        pullToRefreshListView.setOnItemLongClickListener(this);

        v.findViewById(R.id.layout_network).setOnClickListener(this);
        dbService = MsgDBCenterService.getInstance();
        requestServer();

        if (PeiwoApp.getApplication().getNetType() != NetUtil.NO_NETWORK) {
            mNetStateView.setVisibility(View.GONE);
            if (!TcpProxy.getInstance().isLoginStauts()) {
                showAnimation();
            }
        }
    }

    /**
     * 请求消息数据，包含（所有通话记录，系统消息）
     */
    public void requestServer() {
        PeiwoApp.getApplication().mExecutorService.execute(() -> {
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            ApiRequestWrapper.openAPIGET(PeiwoApp.getApplication(), params,
                    AsynHttpClient.API_MESSAGE_DIALOGS, mStructure);
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.layout_network:
                startActivity(new Intent(getActivity(), NetworkOfflineHelpActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    public void onRefresh(PullToRefreshBase<ListView> refreshView) {
        requestServer();
    }


    @Override
    public void call(Subscriber<? super List<TabMsgModel>> subscriber) {
        //Log.i("debounce", "call()");
        //Log.i("debounce", "call thread == " + (Looper.myLooper() == Looper.getMainLooper()));
        getLoaderManager().destroyLoader(loaderId);

        getLoaderManager().initLoader(loaderId, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String sortOrder = PWDBConfig.MessagesTable.UPDATE_TIME + " desc";
                String selection = PWDBConfig.MessagesTable.INSIDE + " = ? and " + PWDBConfig.MessagesTable.MSG_TYPE + " != ?  and " + PWDBConfig.MessagesTable.IS_HIDE + " = ?";
                String[] selectionArgs = new String[3];
                selectionArgs[0] = "0";
                selectionArgs[1] = "1";
                selectionArgs[2] = "0";
                return new CursorLoader(getActivity(), PWDBConfig.MessagesTable.CONTENT_URI, null, selection, selectionArgs, sortOrder);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor mCursor) {
                //Log.i("debounce", "onLoadFinished()");
                //Log.i("debounce", "onLoadFinished thread == " + (Looper.myLooper() == Looper.getMainLooper()));
                if (!subscriber.isUnsubscribed()) {
                    List<TabMsgModel> tempData = new ArrayList<>();
                    if (mCursor != null && !mCursor.isClosed()) {
                        while (mCursor.moveToNext()) {
                            TabMsgModel model = new TabMsgModel(mCursor);
                            //CustomLog.d("onLoadFinished. content is : "+model.content);
                            tempData.add(model);
                        }
                    }
                    //Log.i("debounce", "onLoadFinished " + System.currentTimeMillis());
                    subscriber.onNext(tempData);
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                //Log.i("debounce", "onLoaderReset");
                adapter.notifyDataSetChanged();
            }
        });
    }

    private static class MyHandler extends Handler {
        WeakReference<TabMsgFragment> fragment_ref;

        public MyHandler(TabMsgFragment fragment) {
            fragment_ref = new WeakReference<TabMsgFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            TabMsgFragment theFragment = fragment_ref.get();
            if (theFragment == null || theFragment.isDetached()) return;
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:// 请求所有消息数据成功
                    theFragment.pullToRefreshListView.onRefreshComplete();
                    theFragment.doRPCCommplete(msg.obj);
                    break;
                case WHAT_DELETE_ERROR:
                    theFragment.dismissAnimLoading();
                    theFragment.showToast(theFragment.getActivity(), "删除失败");
                    break;
                case WHAT_DELETE_SUCCESS:
                    theFragment.dismissAnimLoading();
                    Bundle b = msg.getData();
                    String uid = b.getString("uid");
                    if (theFragment.dbService.deleteMessageByUid(uid)) {
                        theFragment.showToast(theFragment.getActivity(), "删除成功");
                    }
                    break;
                case WHAT_DATA_RECEIVE_FOCUS_LIST_SUCCESS:

                    break;
            }
            super.handleMessage(msg);
        }
    }

    private void updateBadge(int count) {
        FragmentActivity fa = getActivity();
        if (fa == null)
            return;
        if (fa instanceof MainActivity) {
            MainActivity activity = (MainActivity) fa;
            activity.updateMsgBadge(count);
        }
    }

    protected void doRPCCommplete(Object obj) {
        try {
            String rawStr = (String) obj;
            JSONArray array = new JSONObject(rawStr).getJSONArray("data");
            if (array != null && array.length() > 0) {
                dbService.insertDialogsWithMessages(array, false, -1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class MyMsgStructure extends MsgStructure {
        @Override
        public boolean onInterceptRawData(String rawStr) {
            Message message = mHandler.obtainMessage();
            message.what = WHAT_DATA_RECEIVE;
            message.obj = rawStr;
            mHandler.sendMessage(message);
            return true;
        }

        @Override
        public void onReceive(JSONObject data) {
        }

        @Override
        public void onError(int error, Object ret) {
            mHandler.post(() -> pullToRefreshListView.onRefreshComplete());
        }
    }

    private void AlertDialogWithCall(final TabMsgModel model) {
        new AlertDialog.Builder(getActivity()).setTitle("操作")
                .setItems(new String[]{"打电话", "删除", "取消"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            call(model);
                            break;
                        case 1:
                            doDeleteMsg(String.valueOf(model.uid));
                            break;
                    }
                }).create().show();
    }

    private void AlertDialogWithOutCall(final TabMsgModel model) {
        new AlertDialog.Builder(getActivity()).setTitle("操作")
                .setItems(new String[]{"删除", "取消"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            if (model.msg_type == GROUP_MESSAGE) {
                                doDeleteMsg(model.msg_id);
                            } else {
                                doDeleteMsg(String.valueOf(model.uid));
                            }
                            break;
                    }
                }).create().show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View v, int position, final long id) {
        if (position >= parent.getCount())
            return true;
        final TabMsgModel model = (TabMsgModel) parent.getAdapter().getItem(position);
        if (model == null)
            return true;
        if (model.msg_type == USER_MESSAGE) {
            if (model.uid.equals(DfineAction.MSG_ID_SAYHELLO)) {
                AlertDialogWithOutCall(model);
                //同步删除所有的MsgId
                JSONArray msgIds = dbService.getMsgIds();
                if (msgIds != null && msgIds.length() != 0) {
                    TcpProxy.getInstance().receiveMessageResponse(msgIds);
                }
            } else {
                AlertDialogWithCall(model);
            }
        } else {
            AlertDialogWithOutCall(model);
        }

        return true;
    }

    protected void doDeleteMsg(String uid) {
        Message message = mHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("uid", uid);
        message.setData(b);
        message.what = WHAT_DELETE_SUCCESS;
        mHandler.sendMessage(message);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        if (position >= parent.getCount())
            return;
        TabMsgModel model = (TabMsgModel) parent.getAdapter().getItem(position);
        if (model == null)
            return;
        switch (model.msg_type) {
            case USER_MESSAGE:
                if (model.uid.equals(DfineAction.MSG_ID_SAYHELLO)) {
                    //打招呼盒子
                    Intent sayHelloIntent = new Intent(getActivity(), SayHelloActivity.class);
                    getActivity().startActivityForResult(sayHelloIntent, REQUEST_HELLO_MESSAGE);
                } else {
                    // 用户通过权限或取消权限消息
                    Intent intent = new Intent(getActivity(), MsgAcceptedMsgActivity.class);
                    Serializable data = model.userModel;
                    intent.putExtra("msg_user", data);
                    intent.putExtra("msg_id", model.msg_id);
                    startActivity(intent);
                    UmengStatisticsAgent.onEvent(getActivity(), UMEventIDS.UMECHAT);
                }
                break;
            case SYS_MESSAGE:// 系统消息
                Intent sysIntent = new Intent(getActivity(), MsgAcceptedMsgActivity.class);
                Serializable data = model.userModel;
                sysIntent.putExtra("msg_user", data);
                sysIntent.putExtra("msg_id", model.msg_id);
                startActivity(sysIntent);
                UmengStatisticsAgent.onEvent(getActivity(), UMEventIDS.UMESYSTEMMESSAGES);
                break;
            case CALL_HISTORY:
                // 通话记录
                Intent intent2 = new Intent(getActivity(), MsgCallLogActivity.class);
                intent2.putExtra(AsynHttpClient.KEY_MSG_ID, model.msg_id);
                startActivity(intent2);
                break;
            case GROUP_MESSAGE:
                Intent intent = new Intent(getActivity(), GroupChatActivity.class);
                TabfindGroupModel groupModel = new TabfindGroupModel();
                groupModel.group_id = model.msg_id;
                groupModel.group_name = model.userModel.name;
                groupModel.avatar = model.userModel.avatar_thumbnail;
                intent.putExtra(GroupChatActivity.K_GROUP_DATA, groupModel);
                intent.putExtra(GroupChatActivity.K_UNREAD_COUNT, model.unread_count);
                startActivityForResult(intent, REQUEST_GROUP_CHAT_ACTIVITY);
                UmengStatisticsAgent.onEvent(getActivity(), UMEventIDS.UMECHAT);
                break;
            default:
                break;
        }
        if (model.msg_type != GROUP_MESSAGE) {
            if (model.unread_count > 0) {
                clearBadge(model.msg_id, Integer.valueOf(model.uid));
            }
            sendMsgIdToServer(Integer.valueOf(model.uid));
        }
    }

    private void sendMsgIdToServer(int uid) {
        JSONArray responseArray = dbService.getMsgId(String.valueOf(uid));
        if (responseArray != null && responseArray.length() != 0)
            TcpProxy.getInstance().receiveMessageResponse(responseArray);
    }

    private void clearBadge(final String msg_id, final int uid) {
        if (msg_id.equals("0")) {
            return;
        }
        //先更改本地数据库数目
        dbService.clearBadgeByMsgId(uid);
    }

    @Override
    public void onResume() {
        super.onResume();
        dbService.cancelIMNotification();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PeiwoApp.getApplication().removeNetworkCallBack(this);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        getLoaderManager().destroyLoader(loaderId);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void scrollToTop() {
        if (pullToRefreshListView != null) {
            pullToRefreshListView.getRefreshableView().setSelectionFromTop(0, 0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        CustomLog.d("TabMsgFragment, onActivityResult() requstcode is : " + requestCode + ", resultcode is : " + resultCode);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GROUP_CHAT_ACTIVITY:
                    /**华为手机退出群组，无法收到onActivityResult()，所以用EventBus方案解决*/
//                    String msg_id = data.getStringExtra("msg_id");
//                    doDeleteMsg(msg_id);
                    break;
                default:
                    break;
//        	case REQUESTCODE_MSG_ACCEPT:
//        		float price = data.getFloatExtra(PRICE, 0.0f);
//        		String name = data.getStringExtra(REGULAR);
//        		String avatar = data.getStringExtra(AVATAR);
//
//        		// 重新取数据,location可能会被改变，导致下标出错
//        		dbService.updateMsgModel(price, name, avatar, currMsgId);
//        		break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void call(final TabMsgModel model) {
        if (getActivity() instanceof PWPreCallingActivity) {
            final PWPreCallingActivity activity = (PWPreCallingActivity) getActivity();
            Intent intent = new Intent(activity, RealCallActivity.class);
            intent.putExtra("face_url", model.userModel.avatar_thumbnail);
            intent.putExtra("gender", model.userModel.gender); // == 1 ? "男" : "女"
            intent.putExtra("address", String.format(Locale.getDefault(), "%s %s",
                    model.userModel.province, model.userModel.city));
            intent.putExtra("age", TimeUtil.getAgeByBirthday(model.userModel.birthday));
            intent.putExtra("tid", Integer.valueOf(model.uid));
            intent.putExtra("uname", model.userModel.name);
            intent.putExtra("slogan", model.userModel.slogan);
            intent.putExtra("flag", DfineAction.OUTGOING_CALL);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            int meUid = UserManager.getUid(activity);
            activity.prepareCalling(meUid, Integer.valueOf(model.uid), 1, model.userModel.getPriceFloat(), intent, new PWPreCallingActivity.OnCallPreparedListener() {
                @Override
                public void onCallPreparedSuccess(int permission, final float price) {
                    mHandler.post(() -> {
                        model.userModel.price = String.valueOf(price);
                        adapter.notifyDataSetChanged();
                        dbService.updateMsgPrice(price, Integer.valueOf(model.msg_id));
                    });
                }

                @Override
                public void onCallPreparedError(int error, Object ret) {

                }
            }, true);
        }
    }

    public void toTranslate() {
        if (myBar != null && myBar.getVisibility() == View.VISIBLE) {
            Animation anim = AnimationUtils.loadAnimation(getActivity(),
                    R.anim.translate_anim_connect);
            myBar.startAnimation(anim);
        }
    }


    public void showAnimation() {
        barLayout.setVisibility(View.VISIBLE);
        myBar.setVisibility(View.VISIBLE);
        toTranslate();
    }

    public void cancelAnimation() {
        myBar.clearAnimation();
        myBar.setVisibility(View.GONE);
        barLayout.setVisibility(View.GONE);
    }


    public void onEventMainThread(MsgTitleChangedEvent event) {
        if (event == null) {
            return;
        }
        if (event.netType == NetUtil.NO_NETWORK) {
            cancelAnimation();
            return;
        }
        if (event.isLogin) {
            cancelAnimation();
        } else {
            showAnimation();
        }
    }

    public void onEventMainThread(MessagePushEvent event) {
        requestServer();
    }

//    public void onEventMainThread(Intent it) {
//        CustomLog.d("TabMsgFragment onEventMainThread. action : "+it.getAction());
//        if (PWActionConfig.ACTION_DELETE_GROUP_MSG_ITEM.equals(it.getAction())) {
//            String msg_id = it.getStringExtra("msg_id");
////            doDeleteMsg(msg_id);
//        }
//    }

    @Override
    public void getSelfNetworkType(int type) {
        if (type == NetUtil.NO_NETWORK) {
            mNetStateView.setVisibility(View.VISIBLE);
            cancelAnimation();
        } else {
            mNetStateView.setVisibility(View.GONE);
            if (TcpProxy.getInstance().isLoginStauts()) {
                cancelAnimation();
            } else {
                showAnimation();
            }
        }
    }

    public void inChatActivity(int uid) {
        for (TabMsgModel model : mList) {
            if (uid == model.userModel.uid) {
                Intent intent = new Intent(getActivity(), MsgAcceptedMsgActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Serializable data = model.userModel;
                intent.putExtra("msg_user", data);
                intent.putExtra("msg_id", model.msg_id);
                startActivity(intent);
                break;
            }
        }
    }

    public void inGroupChatActivity(String uid) {
        for (TabMsgModel model : mList) {
            if (uid.equals(model.msg_id)) {
                Intent intent = new Intent(getActivity(), GroupChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                TabfindGroupModel groupModel = new TabfindGroupModel();
                groupModel.group_id = model.msg_id;
                groupModel.group_name = model.userModel.name;
                groupModel.avatar = model.userModel.avatar_thumbnail;
                intent.putExtra(GroupChatActivity.K_GROUP_DATA, groupModel);
                startActivityForResult(intent, REQUEST_GROUP_CHAT_ACTIVITY);
                break;
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser)
            UmengStatisticsAgent.onEvent(getActivity(), UMEventIDS.UMEINFOMATION);
    }
}
