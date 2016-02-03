package me.peiwo.peiwo.activity;//package me.peiwo.peiwo.activity;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import me.peiwo.peiwo.PeiwoApp;
//import me.peiwo.peiwo.R;
//import me.peiwo.peiwo.adapter.SysMsgAdapter;
//import me.peiwo.peiwo.constans.PWDBConfig;
//import me.peiwo.peiwo.db.MsgDBCenterService;
//import me.peiwo.peiwo.model.SysMsgModel;
//import me.peiwo.peiwo.util.TitleUtil;
//import android.content.Intent;
//import android.database.Cursor;
//import android.os.Bundle;
//import android.support.v4.app.LoaderManager;
//import android.support.v4.content.CursorLoader;
//import android.support.v4.content.Loader;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.AbsListView;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.ListView;
//
//public class MsgSystemMsgActivity extends BaseActivity implements
//		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {
//
//	private List<SysMsgModel> mList = new ArrayList<SysMsgModel>();
//	private SysMsgAdapter adapter;
//	private MsgDBCenterService dbService;
//	private ListView listView;
//	private int uid;
//	private static final int VIEW_TYPE_REDBAG = 5; // 红包消息
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_sysmsg);
//		init(getIntent());
//		getSupportLoaderManager().initLoader(0, null, this);
//	}
//
//	private void init(Intent intent) {
//		setTitleBar();
//		// mMsgId = intent.getIntExtra(AsynHttpClient.KEY_MSG_ID, 0);
//		dbService = MsgDBCenterService.getInstance();
//		listView = (ListView) findViewById(R.id.pullToRefreshListView);
//		adapter = new SysMsgAdapter(mList, this);
//		// 底部留点空间就添加了个footview
//		View footView = new View(this);
//		footView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, 60));
//		listView.setFooterDividersEnabled(true);
//		listView.addFooterView(footView);
//		listView.setAdapter(adapter);
//		listView.setOnItemClickListener(this);
//		uid = intent.getIntExtra("uid", 0);
//	}
//
//	
//	@Override
//	public void onResume() {
//		for (int i = 0; i < dbService.msgNotifiList.size(); i++) {
//			if (dbService.msgNotifiList.get(i).uid == uid) {
//				dbService.msgNotifiList.remove(i--);
//			}
//		}
//		dbService.cancelIMNotification();
//		if (uid != 0) {
//			PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
//				@Override
//				public void run() {
//					dbService.clearBadgeByUid(uid);	
//				}
//			});
//		}
//		super.onResume();
//	}
//	
//	private void setTitleBar() {
//		TitleUtil.setTitleBar(this, "系统消息", new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				onBackPressed();
//			}
//		}, null);
//	}
//
//	@Override
//	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//		String selection = PWDBConfig.DialogsTable.UID + " = ?";
//		String[] selectionArgs = new String[] { String.valueOf(uid) };
//		String sortOrder = PWDBConfig.DialogsTable.UPDATE_TIME + " ASC";
//		return new CursorLoader(this, PWDBConfig.DialogsTable.CONTENT_URI,null, selection, selectionArgs, sortOrder);
//	}
//
//	@Override
//	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//		List<SysMsgModel> list = new ArrayList<SysMsgModel>();
//		while (cursor.moveToNext()) {
//			SysMsgModel model = new SysMsgModel(cursor);
//			list.add(model);
//		}
//		mList.clear();
//		mList.addAll(list);
//		adapter.notifyDataSetChanged();
//		listView.setSelection(adapter.getCount() - 1);
//	}
//
//	@Override
//	public void onLoaderReset(Loader<Cursor> loader) {
//		adapter.notifyDataSetChanged();
//	}
//
//	@Override
//	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//		if (position >= parent.getCount()) {
//			return;
//		}
//		SysMsgModel model = (SysMsgModel) parent.getAdapter().getItem(position);
//		if (model == null) {
//			return;
//		}
//		int viewType = model.dialog_type;
//		switch (viewType) {
//		case VIEW_TYPE_REDBAG:
//			Intent intent = new Intent(this, RedBagActivity.class);
//			intent.putExtra("url", model.url);
//			intent.putExtra("id", model.id);
//			intent.putExtra("redbag_extra", model.redbag_extra);
//			startActivity(intent);
//			break;
//		}
//	}
//}
