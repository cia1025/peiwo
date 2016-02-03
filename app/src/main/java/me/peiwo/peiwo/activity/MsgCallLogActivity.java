package me.peiwo.peiwo.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.CallLogAdapter;
import me.peiwo.peiwo.model.CallLogModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.PWPullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;

public class MsgCallLogActivity extends BaseFragmentActivity implements
        OnRefreshListener2<ListView>, OnItemClickListener,
        OnItemLongClickListener {

    private static final int WHAT_DELETE_ERROR = 3000;
    private static final int WHAT_DELETE_SUCCESS = 4000;

    private PWPullToRefreshListView pullToRefreshListView;
    private MyHandler mHandler;
    private MyMsgStructure mStructure;
    private int mIndex;
    private boolean isreload = false;
    private boolean no_more = false;
    private List<CallLogModel> mList = new ArrayList<CallLogModel>();
    private CallLogAdapter adapter;
    private int mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_msg_frag);

        init();
    }

    private void init() {
        setBarTitle();
        mUid = UserManager.getUid(this);
        mHandler = new MyHandler(this);
        mStructure = new MyMsgStructure();
        pullToRefreshListView = (PWPullToRefreshListView) findViewById(R.id.pullToRefreshListView);
        adapter = new CallLogAdapter(mList, this);
        pullToRefreshListView.setAdapter(adapter);
        pullToRefreshListView.setOnRefreshListener(this);
        pullToRefreshListView.setOnItemClickListener(this);
        pullToRefreshListView.setOnItemLongClickListener(this);
        pullToRefreshListView.setRefreshing();

        //cleanMsgBadge();
    }

    private boolean isclean = false;

    @Override
    public void finish() {
        if (isclean)
            setResult(RESULT_OK);
        super.finish();
    }

//    private void cleanMsgBadge() {
//        Intent intent = getIntent();
//        int badge = intent.getIntExtra("badge", 0);
//        if (badge == 0)
//            return;
//        int msg_id = intent.getIntExtra(AsynHttpClient.KEY_MSG_ID, 0);
//        if (msg_id == 0)
//            return;
//        ApiRequestWrapper.messageClean(this, mUid, String.valueOf(msg_id),
//                new MsgStructure() {
//
//                    @Override
//                    public void onReceive(JSONObject data) {
//                        isclean = true;
//                        Trace.i("clean msg badge");
//                    }
//
//                    @Override
//                    public void onError(int error, Object ret) {
//                    }
//                });
//    }

    private void requestServer() {
        ApiRequestWrapper.callHistoryList(this, mUid, mIndex, isreload,
                mStructure);
    }

    class MyHandler extends Handler {
        WeakReference<MsgCallLogActivity> activity_ref;

        public MyHandler(MsgCallLogActivity activity) {
            activity_ref = new WeakReference<MsgCallLogActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MsgCallLogActivity theActivity = activity_ref.get();
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    theActivity.doRPCCommplete(msg.obj);
                    break;

                case WHAT_DATA_RECEIVE_ERROR:
                    theActivity.pullToRefreshListView.onRefreshComplete();
                    showToast(theActivity, "网络连接失败");
                    break;
                case WHAT_DELETE_ERROR:
                    showToast(theActivity, "删除失败");
                    theActivity.dismissAnimLoading();
                    break;
                case WHAT_DELETE_SUCCESS:
                    showToast(theActivity, "删除成功");
                    theActivity.dismissAnimLoading();
                    theActivity.mIndex = 0;
                    theActivity.isreload = true;
                    theActivity.requestServer();
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    protected void doRPCCommplete(Object obj) {
        try {
            pullToRefreshListView.onRefreshComplete();
            JSONObject o = (JSONObject) obj;
            fillData(o);
            no_more = o.has("no_more") ? o.getBoolean("no_more") : false;
            if (no_more) {
                pullToRefreshListView.end(true);
                return;
            } else {
                pullToRefreshListView.end(false);
            }
            mIndex = mList.size();
            isreload = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fillData(JSONObject o) throws JSONException {
        JSONArray array = o.getJSONArray("historylist");
        List<CallLogModel> models = new ArrayList<CallLogModel>();
        for (int i = 0; i < array.length(); i++) {
            models.add(new CallLogModel(array.getJSONObject(i)));
        }
        if (mIndex == 0)
            mList.clear();
        mList.addAll(models);
        adapter.notifyDataSetChanged();
    }

    class MyMsgStructure extends MsgStructure {

        @Override
        public void onReceive(JSONObject data) {
            // 子线程
            Message message = mHandler.obtainMessage();
            message.what = WHAT_DATA_RECEIVE;
            message.obj = data;
            mHandler.sendMessage(message);
        }

        @Override
        public void onError(int error, Object ret) {
            mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
        }

    }

    private void setBarTitle() {
        TitleUtil.setTitleBar(this, "通话记录", new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        }, null);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View v, int position,
                                   final long id) {
        final CallLogModel model = (CallLogModel) parent.getAdapter().getItem(
                position);
        new AlertDialog.Builder(this).setTitle("确认删除吗")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int which) {
                        doDeleteMsg(model.history_id);
                    }
                }).setNegativeButton("取消", null).create().show();
        return true;
    }

    protected void doDeleteMsg(int msg_id) {
        // 删除msg消息
        showAnimLoading();
        ApiRequestWrapper.messageDelete(this, mUid, msg_id,
                new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                        mHandler.sendEmptyMessage(WHAT_DELETE_SUCCESS);
                    }

                    @Override
                    public void onError(int error, Object ret) {
                        mHandler.sendEmptyMessage(WHAT_DELETE_ERROR);
                    }
                });
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        CallLogModel model = (CallLogModel) arg0.getAdapter().getItem(arg2);
        Intent intent = new Intent(this, UserInfoActivity.class);
        intent.putExtra(AsynHttpClient.KEY_TUID, model.user.uid);
        intent.putExtra(AsynHttpClient.KEY_NAME, model.user.name);
        startActivity(intent);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        mIndex = 0;
        isreload = true;
        requestServer();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        requestServer();
    }

}
