package me.peiwo.peiwo.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.TabFindAdapter;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.model.TabFindModel;
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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

/**
 * Created by chenhao on 2014-11-05 16:56.
 *
 * @modify:
 */
public class SearchContactByTagActivity extends BaseActivity implements View.OnClickListener, PullToRefreshBase.OnRefreshListener2<ListView>, AdapterView.OnItemClickListener {
    public static final String SEARCH_TAG = "tag";
    private MyHandler mHandler;
    private String tag;
    private PWPullToRefreshListView pullToRefreshListView;
    private List<TabFindModel> mList = new ArrayList<TabFindModel>();
    private TabFindAdapter adapter;
    private int mCurGender;
    private boolean isreload = false;
    private int mIndex;

    //    private static final String
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_tag);
        tag = getIntent().getStringExtra(SEARCH_TAG);
        mHandler = new MyHandler(this);
        PWUserModel model = UserManager.getPWUser(this);
        mCurGender = model.gender == AsynHttpClient.GENDER_MASK_MALE ? AsynHttpClient.GENDER_MASK_FEMALE : AsynHttpClient.GENDER_MASK_MALE;  //默认选择异性
        init();
    }

    class MyHandler extends Handler {
        WeakReference<SearchContactByTagActivity> activity_ref;

        public MyHandler(SearchContactByTagActivity activity) {
            activity_ref = new WeakReference<SearchContactByTagActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            SearchContactByTagActivity theActivity = activity_ref.get();
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
            }
            super.handleMessage(msg);
        }
    }


    private void requestServer() {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(SEARCH_TAG, tag));
        params.add(new BasicNameValuePair(AsynHttpClient.KEY_INDEX, String
                .valueOf(mIndex)));
        params.add(new BasicNameValuePair(AsynHttpClient.KEY_GENDER_MASK,
                String.valueOf(mCurGender)));
        if (isreload) {
            params.add(new BasicNameValuePair(AsynHttpClient.KEY_RELOAD, "1"));
        }
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_USERLIST_SEARCHBYTAG, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Message msg = mHandler.obtainMessage();
                msg.what = WHAT_DATA_RECEIVE;
                msg.obj = data;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onError(int error, Object ret) {
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
            }
        });

    }

    private void doRPCComplete(Object obj) {
        try {
            dismissAnimLoading();
            pullToRefreshListView.onRefreshComplete();
            JSONObject o = (JSONObject) obj;
            fillData(o);
            if (o.optBoolean("no_more")) {
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

    public void fillData(JSONObject o) throws JSONException {
        JSONArray array = o.getJSONArray("userlist");
        List<TabFindModel> models = new ArrayList<TabFindModel>();
        for (int i = 0; i < array.length(); i++) {
            models.add(new TabFindModel(array.getJSONObject(i)));
        }
        if (mIndex == 0)
            mList.clear();
        mList.addAll(models);
        adapter.notifyDataSetChanged();
    }

    private void init() {
        setTitleBar();
        createPopWindow();
        pullToRefreshListView = (PWPullToRefreshListView) findViewById(R.id.pullToRefreshListView);
        //adapter = new TabFindAdapter(mList, this);
        pullToRefreshListView.setAdapter(adapter);
        pullToRefreshListView.setOnRefreshListener(this);
        pullToRefreshListView.setRefreshing();
        pullToRefreshListView.setOnItemClickListener(this);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TabFindModel uInfo = (TabFindModel) parent.getAdapter().getItem(
                position);
        Intent intent = new Intent(this, UserInfoActivity.class);
        intent.putExtra(UserInfoActivity.TARGET_UID, uInfo.uid);
        intent.putExtra(UserInfoActivity.TARGET_NAME, uInfo.name);
        intent.putExtra(UserInfoActivity.MESSAGE_FROM,  2);
        startActivity(intent);
    }

    private void setTitleBar() {
        TitleUtil.setTitleBar(this, tag, this, "筛选", this);
    }

    private View iv_bg_all;
    private View iv_bg_male;
    private View iv_bg_famle;
    private PopupWindow mPopupWindow;

    @SuppressLint("InflateParams")
    private void createPopWindow() {
        View v = LayoutInflater.from(this).inflate(R.layout.layout_pop_swich_sex, null);
        View ll_all = v.findViewById(R.id.ll_all);
        ll_all.setTag(AsynHttpClient.GENDER_MASK_ALL);
        ll_all.setOnClickListener(this);
        View tv_male = v.findViewById(R.id.ll_male);
        tv_male.setTag(AsynHttpClient.GENDER_MASK_MALE);
        tv_male.setOnClickListener(this);
        View tv_famle = v.findViewById(R.id.ll_famle);
        tv_famle.setTag(AsynHttpClient.GENDER_MASK_FEMALE);
        tv_famle.setOnClickListener(this);
        iv_bg_all = v.findViewById(R.id.iv_bg_all);
        iv_bg_male = v.findViewById(R.id.iv_bg_male);
        iv_bg_famle = v.findViewById(R.id.iv_bg_famle);
        setDefultSelect();
        mPopupWindow = new PopupWindow(v, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(),
                (Bitmap) null));
    }

    private void setDefultSelect() {
        switch (mCurGender) {
            case AsynHttpClient.GENDER_MASK_ALL:
                iv_bg_all.setVisibility(View.VISIBLE);
                break;
            case AsynHttpClient.GENDER_MASK_FEMALE:
                iv_bg_famle.setVisibility(View.VISIBLE);
                break;
            case AsynHttpClient.GENDER_MASK_MALE:
                iv_bg_male.setVisibility(View.VISIBLE);
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_left:
                finish();
                break;
            case R.id.btn_right:
                showPop(v);
                break;
            case R.id.ll_all:
            case R.id.ll_male:
            case R.id.ll_famle:
                onFilter(v);
                break;
        }
    }

    private void onFilter(View v) {
        if (mPopupWindow != null)
            mPopupWindow.dismiss();
        if (mCurGender == (Integer) v.getTag())
            return;
        mCurGender = (Integer) v.getTag();
        mIndex = 0;
        showAnimLoading();
        switch (v.getId()) {
            case R.id.ll_male:
                mCurGender = AsynHttpClient.GENDER_MASK_MALE;
                iv_bg_all.setVisibility(View.INVISIBLE);
                iv_bg_famle.setVisibility(View.INVISIBLE);
                iv_bg_male.setVisibility(View.VISIBLE);
                break;
            case R.id.ll_famle:
                mCurGender = AsynHttpClient.GENDER_MASK_FEMALE;
                iv_bg_all.setVisibility(View.INVISIBLE);
                iv_bg_male.setVisibility(View.INVISIBLE);
                iv_bg_famle.setVisibility(View.VISIBLE);
                break;
            default:
                mCurGender = AsynHttpClient.GENDER_MASK_ALL;
                iv_bg_famle.setVisibility(View.INVISIBLE);
                iv_bg_male.setVisibility(View.INVISIBLE);
                iv_bg_all.setVisibility(View.VISIBLE);
                break;
        }
        requestServer();
    }

    private void showPop(View v) {
        if (mPopupWindow == null)
            return;
        mPopupWindow.showAsDropDown(v, 0, 0);
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