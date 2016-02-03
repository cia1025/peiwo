package me.peiwo.peiwo.activity;

import java.util.ArrayList;
import java.util.List;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.TrendNoticeAdapter;
import me.peiwo.peiwo.model.TrendNoticeModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.widget.PWPullToRefreshListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;

public class TrendNoticeActivity extends BaseActivity implements
		OnClickListener, OnRefreshListener2<ListView>, OnItemClickListener {

	private PWPullToRefreshListView pullToRefreshListView;
	private TrendNoticeAdapter adapter;
	private List<TrendNoticeModel> mList;
	private int mIndex = -1;
	private static final int HANDLE_MSG_REQUEST_LIST_SUCCESS = 0x100;
	private static final int HANDLE_MSG_REQUEST_LIST_FAILURE = 0x101;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trend_notice);
        TitleUtil.setTitleBar(this, "动态通知", new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        }, null);
        
		pullToRefreshListView = (PWPullToRefreshListView) findViewById(R.id.pullToRefreshListView);
		mList = new ArrayList<TrendNoticeModel>();
		pullToRefreshListView.setOnRefreshListener(this);
		pullToRefreshListView.setRefreshing();
		adapter = new TrendNoticeAdapter(mList,this);
		pullToRefreshListView.setAdapter(adapter);
		pullToRefreshListView.setOnItemClickListener(this);
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		mIndex = 1;
		requestServer();
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		requestServer();
	}

	@Override
	public void onClick(View v) {
	}

	private void requestServer() {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		String apiMethod = AsynHttpClient.API_FEED_FLOW_LIKERS;
		params.add(new BasicNameValuePair("page", String.valueOf(mIndex)));
		params.add(new BasicNameValuePair("ipp", "10"));
		ApiRequestWrapper.openAPIGET(this, params, apiMethod, new MsgStructure() {
			@Override
			public void onReceive(JSONObject data) {
				Message msg = mHandler.obtainMessage(HANDLE_MSG_REQUEST_LIST_SUCCESS);
				msg.obj = data;
				System.out.println(" data = " + data);
				mHandler.sendMessage(msg);
				TcpProxy.getInstance().feedFlowRead();
			}

			@Override
			public void onError(int error, Object ret) {
				mHandler.sendEmptyMessage(HANDLE_MSG_REQUEST_LIST_FAILURE);
			}
		});
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case HANDLE_MSG_REQUEST_LIST_SUCCESS: {
				pullToRefreshListView.onRefreshComplete();
				JSONObject dataObject = (JSONObject) msg.obj;
				JSONArray likersArray = dataObject.optJSONArray("dynamics");
				if (likersArray != null && likersArray.length() > 0) {
					if (mIndex == 1) {
						mList.clear();
					}
					for (int i = 0; i < likersArray.length(); i++) {
						JSONObject pubsObject = likersArray.optJSONObject(i);
						mList.add(new TrendNoticeModel(pubsObject));
					}
				}
				adapter.notifyDataSetChanged();
				if ("null".equals(dataObject.optString("next"))) {
					pullToRefreshListView.end(true);
				} else {
					pullToRefreshListView.end(false);
				}
				mIndex++;
				setResult(Activity.RESULT_OK);
//				if (likersArray == null || likersArray.length() == 0) {
//					showToast(TrendNoticeActivity.this, "内容被移除");
//					finish();
//				}
			}
				break;
			case HANDLE_MSG_REQUEST_LIST_FAILURE: {
				pullToRefreshListView.onRefreshComplete();
			}
				break;
			}
		};
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		TrendNoticeModel mModel = (TrendNoticeModel) parent.getAdapter().getItem(position);
		Intent intent = new Intent(TrendNoticeActivity.this, FeedFlowActivity.class);
		intent.putExtra("feed_id", mModel.id);
		startActivity(intent);
	}
}
