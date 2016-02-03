package me.peiwo.peiwo.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.BlockListAdapter;
import me.peiwo.peiwo.model.PWBlockModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.widget.PWPullToRefreshListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;


/**
 * Created by ChenHao on 2014-11-06 上午11:29.
 *
 * @modify:
 */
public class BlockListActivity extends BaseActivity implements AdapterView.OnItemLongClickListener, PullToRefreshBase.OnRefreshListener2<ListView>, AdapterView.OnItemClickListener {
    private PWPullToRefreshListView pullToRefreshListView;
    private List<PWBlockModel> mList = new ArrayList<PWBlockModel>();
    private BlockListAdapter mAdapter;
    private String min_time;
    private MyHandler mHandler;
    private boolean reload;
    private static final int WHAT_RELIEVE_BLOCK_COMPLETE = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_list);
        mHandler = new MyHandler(this);
        init();
    }

    private void init() {
        setTitleBar();
        pullToRefreshListView = (PWPullToRefreshListView) findViewById(R.id.pullToRefreshListView);
        mAdapter = new BlockListAdapter(this, mList);
        pullToRefreshListView.setAdapter(mAdapter);
        pullToRefreshListView.setOnRefreshListener(this);
        pullToRefreshListView.setOnItemClickListener(this);
        pullToRefreshListView.setOnItemLongClickListener(this);
        pullToRefreshListView.setRefreshing();
    }


    class MyHandler extends Handler {
        WeakReference<BlockListActivity> activity_ref;

        public MyHandler(BlockListActivity activity) {
            activity_ref = new WeakReference<BlockListActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BlockListActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) {
                return;
            }
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    theActivity.doRPCComplete(msg.obj);
                    break;
                case WHAT_DATA_RECEIVE_ERROR:
                    theActivity.pullToRefreshListView.onRefreshComplete();
                    showToast(theActivity, "网络连接失败");
                    break;
                case WHAT_RELIEVE_BLOCK_COMPLETE:
                    if (msg.obj instanceof PWBlockModel) {
                        theActivity.mList.remove(msg.obj);
                        theActivity.mAdapter.notifyDataSetChanged();
                        showToast(theActivity, "解除黑名单成功");
                    }
            }
        }
    }

    private void doRPCComplete(Object obj) {
        pullToRefreshListView.onRefreshComplete();
        try {
            JSONObject o = (JSONObject) obj;
            JSONArray array = o.optJSONArray("blocklist");
            if (array == null || array.length() == 0) return;
            List<PWBlockModel> models = new ArrayList<PWBlockModel>();
            for (int i = 0; i < array.length(); i++) {
                models.add(new PWBlockModel(array.getJSONObject(i)));
            }
            min_time = models.get(models.size() - 1).block_time;  //最小的时间
            if (reload)
                mList.clear();
            mList.addAll(models);
            mAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestServer() {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        if (min_time != null)
            params.add(new BasicNameValuePair("min_time", min_time));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_CONTACT_BLOCKLIST, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Message msg = mHandler.obtainMessage();
                msg.obj = data;
                msg.what = WHAT_DATA_RECEIVE;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onError(int error, Object ret) {
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
            }
        });
    }

    private void setTitleBar() {
        TitleUtil.setTitleBar(this, "黑名单", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        }, null);
    }


    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {

        new AlertDialog.Builder(this)
                .setTitle("选择操作")
                .setItems(new String[]{"解除黑名单", "取消"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                PWBlockModel model = (PWBlockModel) parent.getAdapter().getItem(position);
                                relieveBlock(model);
                                break;
                            case 2:
                                break;
                        }
                    }
                }).create().show();
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PWBlockModel model = (PWBlockModel) parent.getAdapter().getItem(position);
        Intent intent = new Intent(BlockListActivity.this, UserInfoActivity.class);
        intent.putExtra(AsynHttpClient.KEY_TUID, model.uid);
        intent.putExtra(AsynHttpClient.KEY_NAME, model.name);
        startActivity(intent);
    }

    private void relieveBlock(final PWBlockModel model) {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(AsynHttpClient.KEY_TUID, String.valueOf(model.uid)));
        ApiRequestWrapper.openAPIPOST(this, params, AsynHttpClient.API_CONTACT_UNBLOCK, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Message msg = mHandler.obtainMessage();
                msg.what = WHAT_RELIEVE_BLOCK_COMPLETE;
                msg.obj = model;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onError(int error, Object ret) {
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
            }
        });
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        reload = true;
        requestServer();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        reload = false;
        requestServer();
    }
}
