package me.peiwo.peiwo.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.SayHelloAdapter;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.eventbus.event.FocusEvent;
import me.peiwo.peiwo.model.SayHelloModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.service.PlayerService;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.util.UmengStatisticsAgent;
import me.peiwo.peiwo.widget.PWPullToRefreshListView;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SayHelloActivity extends BaseActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnClickListener {

    private PWPullToRefreshListView pullToRefreshListView;
    private List<SayHelloModel> mList = new ArrayList<SayHelloModel>();
    private SayHelloAdapter adapter;
    private MyHandler mHandler;
    private MsgDBCenterService dbService;
    boolean needresult = false;
    public static final int WHAT_DATA_DOBLOCK_REPORT = 1000;
    public static final int WHAT_DATA_REPLY_MESSAGE = 2000;
    public static final int WHAT_DATA_RECEIVE_BLOCK_SECCESS = 3000;
    public static final int WHAT_DATA_RECEIVE_REPORT_SUCCESS = 4000;
    public static final int WHAT_DATA_START_USERINFO = 5000;
    public static final int WHAT_DATA_LONGCLICK = 6000;
    public static final int WHAT_DATA_DELETE_SUCCESS = 7000;
	public static final int WHAT_DATA_ACCEPT_TO_BE_FRIENDS = 8000;
	public static final int WHAT_DATA_FOCUS_SUCCESS = 9000;

	private SayHelloModel mAcceptModel;
	public boolean isNeedReServer = false;
	public Button btn_update;
	public static boolean isActivity  = false;
	private TextView bottom_view;
	private int loaderId;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.msg_apply_list_activity);
        init();
		loaderId = hashCode();
        getSupportLoaderManager().initLoader(loaderId, null, this);
		EventBus.getDefault().register(this);
		isActivity = true;
    }

	@Override
	protected void onStop() {
		super.onStop();
		if(adapter != null){
			adapter.resetToIdle();
		}
	}

	@Override
	protected void onDestroy() {
		getSupportLoaderManager().destroyLoader(loaderId);
		super.onDestroy();
		isActivity = false;
		EventBus.getDefault().unregister(this);
	}

	private void init() {
        mHandler = new MyHandler(this);
        dbService = MsgDBCenterService.getInstance();
        pullToRefreshListView = (PWPullToRefreshListView) findViewById(R.id.pullToRefreshListView);
        btn_update = (Button) findViewById(R.id.btn_update);
        btn_update.setOnClickListener(this);
        View newEmptyView = findViewById(R.id.empty);
        String str = "去更新资料装扮主页吧!";
		SpannableStringBuilder style = new SpannableStringBuilder(str);
		style.setSpan(new ForegroundColorSpan(Color.parseColor("#00b8d0")), 1, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		TextView tvColor = (TextView) findViewById(R.id.tv_text2);
		tvColor.setText(style);
        pullToRefreshListView.setEmptyView(newEmptyView);
        pullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
		adapter = new SayHelloAdapter(mList, this, mHandler);
        pullToRefreshListView.setAdapter(adapter);
        dbService.clearBadgeByMsgId(Integer.valueOf(DfineAction.MSG_ID_SAYHELLO));
		bottom_view = (TextView) findViewById(R.id.bottom_ignore_view);
    }

	private void longclick(final SayHelloModel model) {
		if (model == null) {
        	return;
        }
		new AlertDialog.Builder(this)
				.setTitle("操作")
				.setItems(new String[] { "删除", "取消" }, (dialog, which) -> {
						switch (which) {
						case 0:
							sendMsgIdToServer(model.uid);
							doDeleteMsg(model.uid);
							break;
						}
					}
				).create().show();
    }
    
	@Override
	public void onResume() {
		super.onResume();
		dbService.cancelIMNotification();
	}

	protected void doDeleteMsg(int uid) {
		if (dbService.deleteSayHelloMessageByUid(uid)) {
			mHandler.sendEmptyMessage(WHAT_DATA_DELETE_SUCCESS);
		}
	}
    
    private class MyHandler extends Handler {
        WeakReference<SayHelloActivity> activity_ref;

        public MyHandler(SayHelloActivity activity) {
            activity_ref = new WeakReference<SayHelloActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            SayHelloActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) return;
            int what = msg.what;
			Resources res = getResources();
            switch (what) {
				case WHAT_DATA_DOBLOCK_REPORT:
					UmengStatisticsAgent.onEvent(theActivity, UMEventIDS.UMEHELLOBOXBLACKORREPORT);
					int tUid = mList.get((Integer) msg.obj).uid;
					doReportOrDoBlack(tUid);
					break;
				case WHAT_DATA_REPLY_MESSAGE:
					UmengStatisticsAgent.onEvent(theActivity, UMEventIDS.UMECLICKREPLYHELLO);
					SayHelloModel mModel = mList.get((Integer) msg.obj);
					Intent intent = new Intent(SayHelloActivity.this, MsgAcceptedMsgActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					Serializable data = mModel.userModel;
					intent.putExtra("msg_user", data);
					intent.putExtra("msg_id", mModel.msg_id);
					if (mModel.from == 4) {
						intent.putExtra("show_prompt", true);
					}
					startActivity(intent);
					break;
				case WHAT_DATA_START_USERINFO:
					Intent userInfoIntent = new Intent(SayHelloActivity.this, UserInfoActivity.class);
					userInfoIntent.putExtra(UserInfoActivity.TARGET_UID, mList.get((Integer) msg.obj).uid);
					startActivity(userInfoIntent);
					break;
				case WHAT_DATA_LONGCLICK:
					SayHelloModel mLongClickodel = mList.get((Integer) msg.obj);
					longclick(mLongClickodel);
					break;
				case WHAT_DATA_DELETE_SUCCESS:
					//删除单条消息
					isNeedReServer = true;
					showToast(theActivity, res.getString(R.string.delete_success));
					if(mList.size() <= 1)
						bottom_view.setVisibility(View.INVISIBLE);
					break;
				case WHAT_DATA_RECEIVE_BLOCK_SECCESS:
					dismissAnimLoading();
					showToast(getApplicationContext(), "拉黑成功");
					sendMsgIdToServer(msg.arg1);
					int tuid = msg.arg1;
					theActivity.dbService.deleteMessageByUid(String.valueOf(tuid));
					if(mList.size() <= 1)
						bottom_view.setVisibility(View.INVISIBLE);
					break;
				case WHAT_DATA_RECEIVE_REPORT_SUCCESS:
					showToast(theActivity, res.getString(R.string.refuse_already));
					sendMsgIdToServer(msg.arg1);
					theActivity.dbService.deleteMessageByUid(String.valueOf(msg.arg1));
					if(mList.size() <= 1)
						bottom_view.setVisibility(View.INVISIBLE);
					break;
				case WHAT_DATA_ACCEPT_TO_BE_FRIENDS:
					if (mList.size() <= 1){
						bottom_view.setVisibility(View.INVISIBLE);
					}
					if(adapter != null){
						adapter.resetToIdle();
					}
					mAcceptModel = (SayHelloModel) msg.obj;
					break;
				case WHAT_DATA_FOCUS_SUCCESS:
					if(mAcceptModel != null) {
						Serializable serializable_data = mAcceptModel.userModel;
						intent = new Intent(theActivity, MsgAcceptedMsgActivity.class);
						intent.putExtra("msg_user", serializable_data);
						intent.putExtra("msg_id", mAcceptModel.msg_id);
						startActivityForResult(intent, TabMsgFragment.REQUEST_HELLO_MESSAGE);
						UmengStatisticsAgent.onEvent(theActivity, UMEventIDS.UMECHAT);
					}
					break;
				default:
					break;
            }
            super.handleMessage(msg);
        }
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		CustomLog.d("Sayhello, onActivityResult. request code is : " + requestCode + ", result code is : " + resultCode);
		if(requestCode == TabMsgFragment.REQUEST_HELLO_MESSAGE) {
			finish();
		}
	}

	private void doBlock(final int tUid){
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEBLACKLIST);
        new AlertDialog.Builder(this)
                .setTitle("提示").setMessage("拉黑后将不再收到对方发来的消息，在“设置->黑名单”中可解除，确定拉黑？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doRealBlock(tUid);
            }
        }).setNegativeButton("取消", null).create().show();
    }
    
    private void doRealBlock(final int tUid) {
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEBLACKLISTSURE);
        showAnimLoading();
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tuid", String.valueOf(tUid)));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_CONTACT_BLOCK, new MsgStructure() {
			@Override
			public boolean onInterceptRawData(String rawStr) {
				Message msg = mHandler.obtainMessage();
				msg.what = WHAT_DATA_RECEIVE_BLOCK_SECCESS;
				msg.arg1 = tUid;
				mHandler.sendMessage(msg);
				return true;
			}

			@Override
			public void onReceive(JSONObject data) {

			}

			@Override
			public void onError(int error, Object ret) {

			}
		});
    }
    
    private void reportUser(final int tUid) {
        // DEFAULT = 0 其它
        // PRON = 1 色情
        // CHEAT = 2 欺诈
        // HARASS = 3 骚扰
        // INFRINGE = 4 侵权
//    	String[] menuArray = new String[]{"色情", "欺诈", "骚扰", "侵权", "其他", "取消"};
//        new AlertDialog.Builder(this).setTitle("举报用户")
//                .setItems(menuArray, (dialog, which) -> {
//                		if (which == 5) {
//                			return;
//                		}
//                		showToast(SayHelloActivity.this, "正在举报");
//                		ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
//                        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_TUID, String.valueOf(tUid)));
//                        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_REASON, String.valueOf(which == 4 ? 0 : which + 1)));
//                		ApiRequestWrapper.openAPIGET(getApplicationContext(), paramList, AsynHttpClient.API_REPORT_DOBLOCK, new MsgStructure() {
//
//							@Override
//							public void onReceive(JSONObject data) {
//								Message msg = mHandler.obtainMessage();
//								msg.what = WHAT_DATA_RECEIVE_REPORT_SUCCESS;
//								msg.arg1 = tUid;
//								mHandler.sendMessage(msg);
//							}
//
//							@Override
//							public void onError(int error, Object ret) {
//							}
//						});
//                }).create().show();
		ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
		paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_TUID, String.valueOf(tUid)));
		paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_REASON, "0"));
		ApiRequestWrapper.openAPIGET(getApplicationContext(), paramList, AsynHttpClient.API_REPORT_DOBLOCK, new MsgStructure() {

			@Override
			public void onReceive(JSONObject data) {
				Message msg = mHandler.obtainMessage();
				msg.what = WHAT_DATA_RECEIVE_REPORT_SUCCESS;
				msg.arg1 = tUid;
				mHandler.sendMessage(msg);
			}

			@Override
			public void onError(int error, Object ret) {
			}
		});
    }
    
    private void doReportOrDoBlack(final int tUid){
//        new AlertDialog.Builder(this)
//                .setTitle("提示")
//                .setItems(new String[]{"拉黑", "举报并拉黑"}, (dialog, which) -> {
//                                switch (which) {
//                                    case 0:
//                                    	doBlock(tUid);
//                                        break;
//                                    case 1:
//                                        reportUser(tUid);
//                                        break;
//									default:
//										break;
//                                }
//                        }).create().show();

//		Resources res = getResources();
//		new android.support.v7.app.AlertDialog.Builder(this)
//				.setTitle(res.getString(R.string.refuse_this_man))
//				.setNegativeButton(res.getString(R.string.cancel), null)
//				.setPositiveButton(res.getString(R.string.ok), (dialog, which) -> {
//					reportUser(tUid);
//				})
//				.create().show();
		reportUser(tUid);
    }
    
	private void setTitleBar(String number) {
		TitleUtil.setTitleBar(this, number, (v) -> {
				onBackPressed();
			}
		, null);
	}

	private void deleteInsideSayHello() {
		new AlertDialog.Builder(this).setTitle("确定清理所有未处理的招呼信息吗?")
				.setNegativeButton("取消", null)
				.setPositiveButton("确定", (dialog, which) -> {
						//删除所有记录前,需要先上传MsgId给服务器,服务器下次不会发送该离线消息
						sendMsgIdsToserver();
						dbService.deleteAllInsideSayHello();
						bottom_view.setVisibility(View.INVISIBLE);
						adapter.resetToIdle();
					}
				).create().show();
	}
	
	@Override
	public void finish() {
		super.finish();
		dbService.reSetSayhelloData();
	}
	
	private void sendMsgIdsToserver(){
		//发送最大msgId给服务器
		JSONArray responseArray = dbService.getMsgIds();
		if(responseArray != null && responseArray.length() != 0)
		TcpProxy.getInstance().receiveMessageResponse(responseArray);
	}
	
	private void sendMsgIdToServer(int uid){
		JSONArray responseArray = dbService.getMsgId(String.valueOf(uid));
		if(responseArray != null && responseArray.length() != 0)
		TcpProxy.getInstance().receiveMessageResponse(responseArray);
	}
	
    public void click(View v) {
		switch (v.getId()) {
		case R.id.empty:

			break;
		case R.id.bottom_ignore_view:
			UmengStatisticsAgent.onEvent(SayHelloActivity.this, UMEventIDS.UMEIGNOREALL);
			deleteInsideSayHello();
			break;
		default:
			break;
		}
    }

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String sortOrder = PWDBConfig.MessagesTable.UPDATE_TIME + " desc";
		String selection = PWDBConfig.MessagesTable.INSIDE + " = ? ";
		String[] selectionArgs = new String[1];
		selectionArgs[0] = "1";
		return new CursorLoader(this, PWDBConfig.MessagesTable.CONTENT_URI, null, selection, selectionArgs, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor mCursor) {
		boolean isSame = false;
		List<SayHelloModel> list = new ArrayList<>();
		int position = 0;
		while (mCursor.moveToNext()) {
			SayHelloModel model = new SayHelloModel(mCursor);
			if(!isSame && mList != null && mList.size() > position && mList.get(position).voice.voice_url.equals(model.voice.voice_url)) {
				isSame = true;
			} else {
				isSame = false;
			}
			list.add(model);
			++ position;
		}
		CustomLog.d("onLoadFinished. isSame is : "+isSame);
		//收到信息时也会回调次方法，此时播放语音申请不应该停止
		if(!isSame) {
			mList.clear();
			mList.addAll(list);
			setTitleBar(getString(R.string.new_voice_message, mList.size()));
			adapter.notifyDataSetChanged();
			PlayerService.getInstance().resetPlayerCommand();
		}
	}

	public void onEventMainThread(FocusEvent event) {
		CustomLog.d("onEventMainThread. event.type is : " + event.type);
		int type = event.type;
		if (type == FocusEvent.FOCUS_SUCCESS_EVENT) {
			mHandler.sendEmptyMessage(WHAT_DATA_FOCUS_SUCCESS);
			String msg = getResources().getString(R.string.apply_success);
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		CustomLog.d("onLoaderReset.");
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		    case R.id.btn_update:
			    Intent it = new Intent(this, UpdateProfileActivity.class);
				startActivity(it);
				finish();
				break;

		    default:
			    break;
		}
	}
}
