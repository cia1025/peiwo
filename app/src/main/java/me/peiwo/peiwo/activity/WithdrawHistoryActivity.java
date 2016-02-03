package me.peiwo.peiwo.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.WithdrawHistoryAdapter;
import me.peiwo.peiwo.model.WithdrawHistoryModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.PWPullToRefreshListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

/**
 * Created by fuhaidong on 14/11/13.
 */
public class WithdrawHistoryActivity extends BaseActivity implements PullToRefreshBase.OnRefreshListener2<ListView> {

    private PWPullToRefreshListView pullToRefreshListView;
    private int mUid;
    private List<WithdrawHistoryModel> mList = new ArrayList<WithdrawHistoryModel>();
    private android.os.Handler mHandler;
    private WithdrawHistoryAdapter mAdapter;
    boolean isreload = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw_history);

        init();
    }

    private void init() {
        setTitleBar();
        mHandler = new MyHandler(this);
        mUid = UserManager.getUid(this);
        pullToRefreshListView = (PWPullToRefreshListView) findViewById(R.id.pullToRefreshListView);
        mAdapter = new WithdrawHistoryAdapter(mList, this);
        pullToRefreshListView.setAdapter(mAdapter);
        pullToRefreshListView.setOnRefreshListener(this);
        pullToRefreshListView.setRefreshing();
    }

    private void setTitleBar() {
        TitleUtil.setTitleBar(this, "提现明细", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        }, null);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        isreload = true;
        requestServer();
    }


    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        isreload = false;
        requestServer();
    }

    private void requestServer() {
        //uid=xxx&update_time=xxxxxx&type=1,2 1是充值记录 2是提现记录
        String max_id = String.valueOf(Integer.MAX_VALUE);
        if (isreload) {
            max_id = String.valueOf(Integer.MAX_VALUE);
        } else {
            if (mList.size() > 0) {
                max_id = mList.get(mList.size() - 1).withdraw_id;
            }
        }

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("uid", String.valueOf(mUid)));
        params.add(new BasicNameValuePair("max_id", max_id));
        params.add(new BasicNameValuePair("type", "2"));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_PAYMENT_HISTORY, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                //Trace.i("data == " + data.toString());
                List<WithdrawHistoryModel> models = new ArrayList<WithdrawHistoryModel>();
                try {
                    if (data != null) {
                        JSONArray array = data.getJSONArray("list");
                        for (int i = 0; i < array.length(); i++) {
                            models.add(new WithdrawHistoryModel(array.getJSONObject(i)));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    Message message = mHandler.obtainMessage();
                    message.what = WHAT_DATA_RECEIVE;
                    message.obj = models;
                    mHandler.sendMessage(message);
                }
            }

            @Override
            public void onError(int error, Object ret) {
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
            }
        });
    }

    class MyHandler extends android.os.Handler {
        WeakReference<WithdrawHistoryActivity> activity_ref;

        public MyHandler(WithdrawHistoryActivity activity) {
            activity_ref = new WeakReference<WithdrawHistoryActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            WithdrawHistoryActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) return;
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    theActivity.doRPCCommplete(msg.obj);
                    break;

                case WHAT_DATA_RECEIVE_ERROR:
                    theActivity.pullToRefreshListView.onRefreshComplete();
                    showToast(theActivity, "网络连接失败");
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    protected void doRPCCommplete(Object obj) {
        if (isreload) {
            mList.clear();
        }
        pullToRefreshListView.onRefreshComplete();
        List<WithdrawHistoryModel> models = (List<WithdrawHistoryModel>) obj;
        if (models == null || models.isEmpty()) {
            pullToRefreshListView.end(true);
            return;
        } else {
            pullToRefreshListView.end(false);
        }
        mList.addAll(models);
        mAdapter.notifyDataSetChanged();
    }
}