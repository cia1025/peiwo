package me.peiwo.peiwo.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.db.BriteDBHelperHolder;
import me.peiwo.peiwo.fragment.RecorderDialogFragment;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.*;
import me.peiwo.peiwo.widget.PWPullToRefreshListView;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.Subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WildcatCallRecordActivity extends BaseActivity implements
        OnRefreshListener2<ListView> {

    private static final int HANDLE_GETDATA_SUCCESS = 0x01;
    private static final int HANDLE_GETDATA_FAILURE = 0x02;
    private static final int HANDLE_APPLY_FRIEND = 0x10;
    private static final int HANDLE_GET_SHAREDATA_SUCCESS = 0x20;
    private static final int HANDLE_GET_SHAREDATA_FAILURE = 0x21;
    private static final int UPDATE_APPLY_BUTTON = 0x22;
    private static final int LAZY_GUY_VOICE_RECORDER_DONE = 0X23;
    private static final int ID_RECEIVE_REPORT = 1001;
    private static final int ID_RECEIVE_REPORT_ERROR = 1002;

    private PWPullToRefreshListView listView;
    private WildcatCallRecordAdapter adapter;
    private Context mContext;
    private ArrayList<RecordModel> recordList = new ArrayList<RecordModel>();

    private TextView headView;
    private ViewHolder mCurHolder;

    private Map<String, Integer> reportMapping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wildcat_record);
        mContext = this;
        TextView title = (TextView) findViewById(R.id.title);
        title.setText("匿名记录");
        findViewById(R.id.btn_left).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.btn_right).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (PWUtils.isMultiClick()) {
                    return;
                }
                getWildcatShareData();
            }
        });

        getReportMap();

        headView = (TextView) findViewById(R.id.call_record_head_text);


        listView = (PWPullToRefreshListView) findViewById(R.id.call_record_list);
        listView.setOnRefreshListener(this);
        listView.setRefreshing();

        adapter = new WildcatCallRecordAdapter();
        listView.setAdapter(adapter);
    }

    private void getReportMap() {
        reportMapping = new HashMap<>();
        //Subscription subscription = null;
//        try {
        BriteDatabase briteDatabase = BriteDBHelperHolder.getInstance().getBriteDatabase(this);
        String sql = String.format("select * from %s", PWDBConfig.TB_PW_WILDRLOG_REPORT_MAP);
        Observable<SqlBrite.Query> observable = briteDatabase.createQuery(PWDBConfig.TB_PW_WILDRLOG_REPORT_MAP, sql);
        Subscription subscription = observable.subscribe(query -> {
            Cursor c = query.run();
            if (c != null) {
                while (c.moveToNext()) {
                    reportMapping.put(c.getString(c.getColumnIndex("uid")), c.getInt(c.getColumnIndex("report")));
                }
                c.close();
            }
        });
        subscription.unsubscribe();
        //briteDatabase.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (subscription != null) {
//                subscription.unsubscribe();
//            }
//        }
    }

    private String WILDCAT_HAS_SEND_REQUEST = "wildcat_call_requested_ids_";


    private void sendVoiceRequest(final RecordModel model) {
        final int uid = model.remote_uid;
        String name = model.remote_nickname;
        String avatar_url = model.remote_pic;
        int msgFrom = Constans.PW_MESSAGE_FROM_WILDCAT_LOG;
        RecorderDialogFragment mRecorderDialogFrag = RecorderDialogFragment.newInstance(uid, name, avatar_url, msgFrom);
        mRecorderDialogFrag.show(getSupportFragmentManager(), mRecorderDialogFrag.toString());
        mRecorderDialogFrag.setOnUploadListener(() -> {
            SharedPreferencesUtil.putBooleanExtra(WildcatCallRecordActivity.this, WILDCAT_HAS_SEND_REQUEST + uid, true);
            distributeMessage(UPDATE_APPLY_BUTTON, null);
            showToast(mContext, getResources().getString(R.string.apply_success));
        });
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        requestCallRecord();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

    }

    private void requestCallRecord() {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        ApiRequestWrapper.openAPIGET(this, params,
                AsynHttpClient.API_GET_WILDCAT_RECORD, new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                        CustomLog.i("syf", "data = " + data);
//                        Message msg = mHandler.obtainMessage(HANDLE_GETDATA_SUCCESS);
//                        msg.obj = data;
//                        mHandler.sendMessage(msg);
                        distributeMessage(HANDLE_GETDATA_SUCCESS, data);
                    }

                    @Override
                    public void onError(int error, Object ret) {
                        CustomLog.i("syf", "ret = " + ret + ",,   error = " + error);
//                        mHandler.sendEmptyMessage(HANDLE_GETDATA_FAILURE);
                        distributeMessage(HANDLE_GETDATA_FAILURE, null);
                    }
                });
    }

    public void getWildcatShareData() {
        showAnimLoading();
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMECALLRECORD);
        PWUtils.getWildcatShareData(mContext, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                SharedPreferencesUtil.putStringExtra(mContext, "wildcat_share_content" + UserManager.getUid(mContext), data.toString());
//                Message message = mHandler.obtainMessage(HANDLE_GET_SHAREDATA_SUCCESS);
//                message.obj = data;
//                mHandler.sendMessage(message);
                distributeMessage(HANDLE_GET_SHAREDATA_SUCCESS, data);
            }

            @Override
            public void onError(int error, Object ret) {
//                mHandler.sendEmptyMessage(HANDLE_GET_SHAREDATA_FAILURE);
                distributeMessage(HANDLE_GET_SHAREDATA_FAILURE, null);
            }
        });
    }

    private DisplayImageOptions options = ImageUtil.getRoundedOptions();
    private ImageLoader imageLoader;
    private int c_gray;
    private int c_light;

    private class WildcatCallRecordAdapter extends BaseAdapter {
        private LayoutInflater inflater = null;

        public WildcatCallRecordAdapter() {
            inflater = LayoutInflater.from(mContext);
            imageLoader = ImageLoader.getInstance();
            c_gray = Color.parseColor("#8e8e8e");
            c_light = Color.parseColor("#00b8d0");
        }

        @Override
        public int getCount() {
            return recordList.size();
        }

        @Override
        public Object getItem(int position) {
            return recordList.get(position);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.activity_wildcat_record_item, parent, false);
                holder.call_record_image = (ImageView) convertView.findViewById(R.id.call_record_image);
                holder.call_record_name = (TextView) convertView.findViewById(R.id.call_record_name);
                holder.call_record_duration = (TextView) convertView.findViewById(R.id.call_record_duration);
                holder.call_record_apply_btn = (Button) convertView.findViewById(R.id.call_record_apply_btn);
                holder.v_report_user = (Button) convertView.findViewById(R.id.v_report_user);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final ViewHolder tempHolder = holder;
            final RecordModel model = (RecordModel) getItem(position);
            imageLoader.displayImage(model.remote_pic, holder.call_record_image, options);
            holder.call_record_name.setText(model.remote_nickname);
            holder.call_record_duration.setText(model.duration);
            CustomLog.d("this model.id (" + model.remote_uid + ") has been requested ? " + model.isHasRequested);
            if (!model.isHasRequested) {
                holder.call_record_apply_btn.setBackgroundResource(R.drawable.bg_rect_blue);
                holder.call_record_apply_btn.setText(getResources().getString(R.string.apply));
                holder.call_record_apply_btn.setTextColor(c_light);
                holder.call_record_apply_btn.setEnabled(true);
                holder.call_record_apply_btn.setSelected(false);
                holder.call_record_apply_btn.setOnClickListener(arg0 -> {
//                        Message msg = mHandler.obtainMessage(HANDLE_APPLY_FRIEND);
//                        msg.obj = model;
//                        mHandler.sendMessage(msg);
                    mCurHolder = tempHolder;
                    boolean netAvailable = PWUtils.isNetWorkAvailable(WildcatCallRecordActivity.this);
                    if (netAvailable) {
                        sendVoiceRequest(model);
                    } else {
                        showToast(WildcatCallRecordActivity.this, getResources().getString(R.string.umeng_common_network_break_alert));
                    }
                    CustomLog.d("getView(), current holder is : " + mCurHolder.call_record_name.getText());
                });
            } else {
                holder.call_record_apply_btn.setBackgroundResource(R.drawable.bg_rect_gray);
                holder.call_record_apply_btn.setTextColor(c_gray);
                holder.call_record_apply_btn.setText("已申请");
                holder.call_record_apply_btn.setSelected(true);
                holder.call_record_apply_btn.setEnabled(false);
            }

            if (!reportMapping.containsKey(String.valueOf(model.remote_uid))) {
                holder.v_report_user.setBackgroundResource(R.drawable.bg_rect_blue);
                holder.v_report_user.setText("讨厌");
                holder.v_report_user.setTextColor(c_light);
                holder.v_report_user.setEnabled(true);
                holder.v_report_user.setSelected(false);
                holder.v_report_user.setOnClickListener(v -> reportUser(model.remote_uid, position));
            } else {
                holder.v_report_user.setOnClickListener(null);
                holder.v_report_user.setBackgroundResource(R.drawable.bg_rect_gray);
                holder.v_report_user.setTextColor(c_gray);
                holder.v_report_user.setText("已讨厌");
                holder.v_report_user.setSelected(true);
                holder.v_report_user.setEnabled(false);
            }

            return convertView;
        }
    }

    private void reportUser(int remote_uid, int position) {
        new AlertDialog.Builder(this)
                .setTitle("讨厌此人")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> {
                    showAnimLoading("", false, false, false);
                    ArrayList<NameValuePair> paramList = new ArrayList<>();
                    paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_TUID, String.valueOf(remote_uid)));
                    paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_REASON, String.valueOf(which == 4 ? 0 : which + 1)));
                    ApiRequestWrapper.openAPIGET(getApplicationContext(), paramList, AsynHttpClient.API_REPORT_DOBLOCK, new MsgStructure() {
                        @Override
                        public void onReceive(JSONObject data) {
                            try {
                                distributeMessage(ID_RECEIVE_REPORT, new JSONObject().put("position", position));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(int error, Object ret) {
                            distributeMessage(ID_RECEIVE_REPORT_ERROR, null);
                        }
                    });
                })
                .create().show();
    }

    private class RecordModel {
        public String duration;
        public String remote_pic;
        public String remote_nickname;
        public int remote_uid;
        public String _id;
        public boolean isHasRequested;
    }

    private static class ViewHolder {
        ImageView call_record_image;
        TextView call_record_duration;
        TextView call_record_name;
        Button call_record_apply_btn;
        Button v_report_user;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LAZY_GUY_VOICE_RECORDER_DONE && resultCode == RESULT_OK) {
            int uid = data.getIntExtra("uid", 0);
            CustomLog.d("onActivityResult, uid is : " + uid);
            SharedPreferencesUtil.putBooleanExtra(WildcatCallRecordActivity.this, WILDCAT_HAS_SEND_REQUEST + uid, true);
            distributeMessage(UPDATE_APPLY_BUTTON, null);
        }
    }

    @Override
    protected void handle_message(int message_id, JSONObject obj) {
        switch (message_id) {
            case ID_RECEIVE_REPORT:
                dismissAnimLoading();
                Snackbar.make(listView, "此人已被花式吊打!", Snackbar.LENGTH_SHORT).show();
                int position = obj.optInt("position", -1);
                RecordModel _model = recordList.get(position);
                if (position >= 0) {
                    reportMapping.put(String.valueOf(_model.remote_uid), 1);
                    adapter.notifyDataSetChanged();
                    addReportMapping(String.valueOf(_model.remote_uid));
                }
                break;
            case ID_RECEIVE_REPORT_ERROR:
                dismissAnimLoading();
                break;
            case UPDATE_APPLY_BUTTON:
                CustomLog.d("UPDATE_APPLY_BUTTON, current holder is : " + mCurHolder.call_record_name.getText());
                Button button = mCurHolder.call_record_apply_btn;
                button.setBackgroundResource(R.drawable.bg_rect_gray);
                button.setTextColor(c_gray);
                button.setText(getResources().getString(R.string.applied_already));
                button.setSelected(true);
                button.setEnabled(false);
                listView.setRefreshing();
                break;
            case HANDLE_GETDATA_SUCCESS: {
                headView.setVisibility(View.VISIBLE);
                listView.onRefreshComplete();
                JSONObject data = obj;
                String extra_msg = data.optString("extra_msg");
                headView.setText(extra_msg);
                JSONArray recordArray = data.optJSONArray("user_list");

                recordList.clear();
//                String hasRequestedId = SharedPreferencesUtil.getStringExtra(mContext, WILDCAT_HAS_SEND_REQUEST + UserManager.getUid(mContext), "");
//                String[] requestedIds = null;
//                if (!TextUtils.isEmpty(hasRequested)) {
//                    requestedIds = hasRequested.split("_");
//                    req_uid = hasRequested.substring(hasRequested.lastIndexOf("-") + 1);
//                }
//                CustomLog.d("HANDLE_GETDATA_SUCCESS, requestedIds is : "+requestedIds);
                CustomLog.d("");
                for (int i = 0; i < recordArray.length(); i++) {
                    RecordModel model = new RecordModel();
                    JSONObject recordObject = recordArray.optJSONObject(i);
                    CustomLog.d("recordArray is : " + recordArray);
                    model.duration = recordObject.optString("duration");
                    model.remote_pic = recordObject.optString("remote_pic");
                    model.remote_nickname = recordObject.optString("remote_nickname");
                    model.remote_uid = recordObject.optInt("remote_uid");
                    model._id = recordObject.optString("_id");
                    model.isHasRequested = SharedPreferencesUtil.getBooleanExtra(mContext, WILDCAT_HAS_SEND_REQUEST + model.remote_uid, false);
                    CustomLog.d("id is : " + model.remote_uid + ", \t hasRequested is : " + model.isHasRequested);
//                    if (requestedIds != null) {
//                        for (int j = 0; j < requestedIds.length; j++) {
//                            if (!TextUtils.isEmpty(requestedIds[j]) && requestedIds[j].equals(model._id)) {
//                                model.isHasRequested = true;
//                                break;
//                            }
//                        }
//                    }
                    recordList.add(model);
                }
                adapter.notifyDataSetChanged();
            }
            break;
            case HANDLE_GETDATA_FAILURE: {
                listView.onRefreshComplete();
                showToast(mContext, "请求失败，请稍候再试");
            }
            break;
//            case HANDLE_APPLY_FRIEND: {
//                RecordModel model = (RecordModel) msg.obj;
//                showApplyDialog(model);
//            }
//            break;
            case HANDLE_GET_SHAREDATA_SUCCESS:
                dismissAnimLoading();
                PWUtils.showWildcatShareActivity(mContext, false);
                break;
            case HANDLE_GET_SHAREDATA_FAILURE:
                dismissAnimLoading();
                showToast(mContext, "请求失败，请稍候再试");
                break;
            default:
                break;
        }
    }

    private void addReportMapping(String uid) {
//        try {
        BriteDatabase briteDatabase = BriteDBHelperHolder.getInstance().getBriteDatabase(this);
        ContentValues values = new ContentValues();
        values.put("uid", uid);
        values.put("report", 1);
        briteDatabase.insert(PWDBConfig.TB_PW_WILDRLOG_REPORT_MAP, values);
        //briteDatabase.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
