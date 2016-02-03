package me.peiwo.peiwo.activity;/*package me.peiwo.peiwo.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.SayHelloAdapter;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.model.SayHelloModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.util.UmengStatisticsAgent;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.PWPullToRefreshListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

public class MsgApplyListActivity extends BaseActivity implements
		AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener,
		LoaderManager.LoaderCallbacks<Cursor> {

    private PWPullToRefreshListView pullToRefreshListView;
    private List<SayHelloModel> mList = new ArrayList<SayHelloModel>();
    private SayHelloAdapter adapter;
    private MyHandler mHandler;
    private int mUid;
    private int tUid;
    private MsgDBCenterService dbService;
    boolean needresult = false;
    public static final int WHAT_DOBLOCK_REPORT = 1000;
    public static final int WHAT_REPLY_MESSAGE = 2000;
    public static final int WHAT_DATA_RECEIVE_BLOCK_SECCESS = 3000;
    public static final int WHAT_DATA_RECEIVE_REPORT_SUCCESS = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.msg_apply_list_activity);

        mUid = UserManager.getUid(this);
        init();
        getSupportLoaderManager().initLoader(0, null, this);
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEUMEAPPLYCALLPOP);
    }

    private void init() {
        setTitleBar("5");
        mHandler = new MyHandler(this);
        dbService = MsgDBCenterService.getInstance();
        pullToRefreshListView = (PWPullToRefreshListView) findViewById(R.id.pullToRefreshListView);
        View newEmptyView = findViewById(R.id.empty);
        pullToRefreshListView.setEmptyView(newEmptyView);
        pullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
        pullToRefreshListView.setOnItemClickListener(this);
        pullToRefreshListView.setOnItemLongClickListener(this);
        adapter = new SayHelloAdapter(mList,this,mHandler);
        pullToRefreshListView.setAdapter(adapter);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return true;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    }
    
    private class MyHandler extends Handler {
        WeakReference<MsgApplyListActivity> activity_ref;

        public MyHandler(MsgApplyListActivity activity) {
            activity_ref = new WeakReference<MsgApplyListActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MsgApplyListActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) return;
            int what = msg.what;
            switch (what) {
            case WHAT_DOBLOCK_REPORT:
            	doReportOrDoBlack();
            	break;
            case WHAT_REPLY_MESSAGE:
            	
            	break;
            case WHAT_DATA_RECEIVE_REPORT_SUCCESS:
            	showToast(theActivity, "举报成功");
            	break;
            }
            super.handleMessage(msg);
        }
    }

    private void doBlock(){

        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEBLACKLIST);
        new AlertDialog.Builder(this)
                .setTitle("提示").setMessage("拉黑后将不再收到对方发来的消息，在“设置->黑名单”中可解除，确定拉黑？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doRealBlock(false);
            }
        }).setNegativeButton("取消", null).create().show();
    
    }
    
    private void doRealBlock(final boolean isReoprt) {
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEBLACKLISTSURE);
        showAnimLoading("", false, false);
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tuid", String.valueOf(2002)));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_CONTACT_BLOCK, new MsgStructure() {
            @Override
            public boolean onInterceptRawData(String rawStr) {
            	
            	Message msg = mHandler.obtainMessage();
            	msg.obj = isReoprt;
            	msg.what = WHAT_DATA_RECEIVE_BLOCK_SECCESS;
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
    
    private void reportUser() {
        // DEFAULT = 0 其它
        // PRON = 1 色情
        // CHEAT = 2 欺诈
        // HARASS = 3 骚扰
        // INFRINGE = 4 侵权
    	String[] menuArray = new String[]{"色情", "欺诈", "骚扰", "侵权", "其他", "取消"};
        new AlertDialog.Builder(this).setTitle("举报用户")
                .setItems(menuArray, new DialogInterface.OnClickListener() {
                	@Override
                	public void onClick(DialogInterface dialog, int which) {
                		if (which == 5) {
                			return;
                		}
                		showToast(MsgApplyListActivity.this, "正在举报");
                		ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
                        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_TUID, String.valueOf(tUid)));
                        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_REASON, String.valueOf(which == 4 ? 0 : which + 1)));
                		ApiRequestWrapper.openAPIGET(getApplicationContext(), paramList, AsynHttpClient.API_REPORT_DOBLOCK, new MsgStructure() {
							
							@Override
							public void onReceive(JSONObject data) {
								mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_REPORT_SUCCESS);
							}
							
							@Override
							public void onError(int error, Object ret) {
							}
						});
                	}
                }).create().show();
    }
    
    private void doReportOrDoBlack(){

        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setItems(new String[]{"拉黑", "举报并拉黑"},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                switch (which) {
                                    case 0:
                                    	doBlock();
                                        break;
                                    case 1:
                                        reportUser();
                                        break;
                                }
                            }
                        }).create().show();
        
    }
    
	private void setTitleBar(String number) {
		TitleUtil.setTitleBar(this, "打招呼的人", new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		}, "忽略", new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
	}

    public void click(View v) {
		switch (v.getId()) {
		case R.id.empty:
	        if (PeiwoApp.getApplication().getCallType() == PeiwoApp.CALL_TYPE.CALL_REAL) {
	            showToast(this, "您当前正在通话");
	            return;
	        }
			Intent intent = new Intent(this, WildCatCallActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			intent.putExtra(WildCatCallActivity.START_MAIN, 1);
			startActivity(intent);
			finish();
			break;
		}
    }

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String sortOrder = PWDBConfig.MessagesTable.UPDATE_TIME + " desc";
		String selection = PWDBConfig.MessagesTable.INSIDE + " == ? or "+PWDBConfig.MessagesTable.MSG_TYPE + " = ?";
		String[] selectionArgs = new String[2];
		selectionArgs[0] = "1";
		selectionArgs[1] = "5";
		return new CursorLoader(this, PWDBConfig.MessagesTable.CONTENT_URI, null, selection, selectionArgs, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor mCursor) {
		List<SayHelloModel> list = new ArrayList<SayHelloModel>();
        while (mCursor.moveToNext()) {
        	SayHelloModel model = new SayHelloModel(mCursor);
        	list.add(model);
        }
        mList.clear();
        mList.addAll(list);
        adapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.notifyDataSetChanged();
	}
}
*/