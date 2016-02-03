package me.peiwo.peiwo.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.PWPraiseAdapter;
import me.peiwo.peiwo.model.PWPraiseModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.widget.PWPullToRefreshListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;

public class PWPraiseActivity extends PWPreCallingActivity implements
		OnRefreshListener2<ListView>, OnItemClickListener {
	private static final int REQUEST_CODE_USERINFO = 5000;
	private PWPullToRefreshListView pullToRefreshListView;
	private List<PWPraiseModel> mPraiseList = new ArrayList<PWPraiseModel>();
	private PWPraiseAdapter mPraiseAdapter;
	private MyHandler mHandler;
	private int index = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_praise_list);
		setTitleBar();
		init();
		requestServer();
	}

	public void init() {
		pullToRefreshListView = (PWPullToRefreshListView) findViewById(R.id.fansPullToRefreshListView);
		pullToRefreshListView.setRefreshing();
		pullToRefreshListView.setOnRefreshListener(this);
		mPraiseAdapter = new PWPraiseAdapter(mPraiseList, this);
		pullToRefreshListView.setAdapter(mPraiseAdapter);
		pullToRefreshListView.setOnItemClickListener(this);
		mHandler = new MyHandler(this);
	}

	public void setTitleBar() {
		TitleUtil.setTitleBar(this, "赞我的人", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		}, null);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_USERINFO:
				boolean isUnAvailable = data.getBooleanExtra(
						"usernotavailable", false);
				int tuid = data.getIntExtra("uid", 0);
				if (isUnAvailable) {
					deleteUnAvailableUser(tuid);
				}
				break;
			default:
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void deleteUnAvailableUser(int tuid) {
		PWPraiseModel praiseModel = new PWPraiseModel();
		for (int i = 0; i < mPraiseList.size(); i++) {
			praiseModel = mPraiseList.get(i);
			if (praiseModel.getUid() == tuid) {
				mPraiseList.remove(i);
				mPraiseAdapter.notifyDataSetChanged();
				break;
			}
		}
	}

	private class MyHandler extends Handler {
		WeakReference<PWPraiseActivity> acivity_ref;

		public MyHandler(PWPraiseActivity activity) {
			acivity_ref = new WeakReference<PWPraiseActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			PWPraiseActivity theActivity = acivity_ref.get();
			if (theActivity == null || theActivity.isFinishing())
				return;
			int what = msg.what;
			switch (what) {
			case WHAT_DATA_RECEIVE:
				dismissAnimLoading();
				fillData((JSONObject) msg.obj);
				pullToRefreshListView.onRefreshComplete();
				break;
			case WHAT_DATA_RECEIVE_ERROR:
				pullToRefreshListView.onRefreshComplete();
				showToast(theActivity, "网络连接失败");
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	}

	private void fillData(JSONObject o) {
		pullToRefreshListView.onRefreshComplete();
		if (index == 0) {
			pullToRefreshListView.end(true);
			return;
		}
		pullToRefreshListView.end(false);
		if (index == 1) {
			mPraiseList.clear();
		}
		try {
			index = o.getInt("next");
		} catch (JSONException e) {
			index=0;
			e.printStackTrace();
		}
		JSONArray array = null;
		try {
			array = o.getJSONArray("users");
			if (array != null && array.length() > 0) {
				List<PWPraiseModel> praiseModels = new ArrayList<PWPraiseModel>();
				for (int i = 0; i < array.length(); i++) {
					praiseModels.add(new PWPraiseModel(array.getJSONObject(i)));
				}
				mPraiseList.addAll(praiseModels);
				mPraiseAdapter.notifyDataSetChanged();
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		PWPraiseModel model = (PWPraiseModel) parent.getAdapter().getItem(
				position);
		int location = mPraiseList.indexOf(model);
		Intent intent = new Intent(this, UserInfoActivity.class);
		intent.putExtra(AsynHttpClient.KEY_TUID, model.getUid());
		intent.putExtra(AsynHttpClient.KEY_NAME, model.getName());
		intent.putExtra("position", location);
		startActivityForResult(intent, REQUEST_CODE_USERINFO);

	}

	public void requestServer() {
		PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
			@Override
			public void run() {
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
				String pub_id = getIntent().getStringExtra("pub_id");
				if (TextUtils.isEmpty(pub_id)) {
					return;
				}
				params.add(new BasicNameValuePair("pub_id", pub_id));
				params.add(new BasicNameValuePair("page", String.valueOf(index)));
				params.add(new BasicNameValuePair("ipp", "20"));
				ApiRequestWrapper.openAPIGET(PWPraiseActivity.this, params,
						AsynHttpClient.API_PRAISE_LIST, new MsgStructure() {
							@Override
							public void onReceive(JSONObject data) {
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
		});
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		index = 1;
		requestServer();
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		requestServer();
		
	}
}
