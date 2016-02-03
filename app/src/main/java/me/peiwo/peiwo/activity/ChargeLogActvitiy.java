package me.peiwo.peiwo.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.ChargeLogAdapter;
import me.peiwo.peiwo.model.ChargeLogModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.TitleUtil;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenHao on 2014-11-10 上午10:31.
 *
 * @modify:
 */
public class ChargeLogActvitiy extends BaseActivity implements PullToRefreshBase.OnRefreshListener2<ListView> {
    private PullToRefreshListView pullToRefreshListView;
    private MyHandler mHandler;
    private int max_id = Integer.MAX_VALUE;
    private List<ChargeLogModel> mList = new ArrayList<ChargeLogModel>();
    private ChargeLogAdapter mAdapter;
    private boolean reload;

    static class MyHandler extends Handler {
        WeakReference<ChargeLogActvitiy> activity_ref;

        public MyHandler(ChargeLogActvitiy activity) {
            activity_ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ChargeLogActvitiy theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) {
                return;
            }
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    theActivity.dismissAnimLoading();
                    theActivity.doRPCComplete(msg.obj);
                    break;
                case WHAT_DATA_RECEIVE_ERROR:
                    theActivity.pullToRefreshListView.onRefreshComplete();
                    theActivity.dismissAnimLoading();
                    theActivity.showToast(theActivity, "网络连接失败");
                    break;
            }
            super.handleMessage(msg);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new MyHandler(this);
        initView();
    }


    private void initView() {
        setContentView(R.layout.activity_charge_history);
        setTitleBar();
        mAdapter = new ChargeLogAdapter(mList, this);
        pullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pullToRefreshListView);
        pullToRefreshListView.setOnRefreshListener(this);
        pullToRefreshListView.setRefreshing();
        pullToRefreshListView.setAdapter(mAdapter);
    }


    public void getChargeHistory(int id) {

        showAnimLoading();
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("max_id", String.valueOf(id)));
        params.add(new BasicNameValuePair("type", String.valueOf(1)));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_PAYMENT_HISTORY, new MsgStructure() {
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
        });
    }

    private void setTitleBar() {
        TitleUtil.setTitleBar(this, "充值记录", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        }, null);
    }

    private void doRPCComplete(Object obj) {
        pullToRefreshListView.onRefreshComplete();
        try {
            fillData((JSONObject) obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void fillData(JSONObject o) throws JSONException {
        JSONArray array = o.has("list") ? o.getJSONArray("list") : null;
        if (array == null || array.length() == 0) {
            return;
        } else {
            if (reload == true)
                mList.clear();
            max_id = array.getJSONObject(array.length() - 1).optInt("payment_id");
            List<ChargeLogModel> list = new ArrayList<ChargeLogModel>();
            for (int i = 0; i < array.length(); i++) {
                list.add(new ChargeLogModel(array.getJSONObject(i)));
            }
            mList.addAll(list);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        reload = true;
        getChargeHistory(Integer.MAX_VALUE);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        reload = false;
        getChargeHistory(max_id);
    }

}
