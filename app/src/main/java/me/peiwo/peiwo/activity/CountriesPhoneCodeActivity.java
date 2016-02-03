package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.PhoneCodeAdapter;
import me.peiwo.peiwo.model.PhoneCodeModel;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.PinYin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fuhaidong on 14/11/6.
 */
public class CountriesPhoneCodeActivity extends BaseActivity implements View.OnTouchListener, ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupClickListener, AbsListView.OnScrollListener {

    public static final String COUNTRY = "country";
    public static final String PHONE_CODE = "phone_code";
    private Handler mHandler = new Handler();
    //    private List<String> groups = new ArrayList<String>();
//    private List<List<PhoneCodeModel>> code_list = new ArrayList<List<PhoneCodeModel>>();
    private ExpandableListView lv_content;
    private LinearLayout ll_sort_key_container;
    private PhoneCodeAdapter mAdapter;
    private TextView tv_title;
    private EditText et_serach;
    private View btn_scancel;
    private View ic_search_action;
    private boolean needfinish = false;

    private static final String[] SORT_KEY = new String[]{"A", "B", "C", "D", "E", "F", "G", "H",
            "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T",
            "W", "X", "Y", "Z"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_code);

        init();
    }

    private void init() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        et_serach = (EditText) findViewById(R.id.et_search);
        btn_scancel = findViewById(R.id.btn_scancel);
        et_serach.addTextChangedListener(new MyTextWatcher());
        ic_search_action = findViewById(R.id.ic_search_action);
        lv_content = (ExpandableListView) findViewById(R.id.lv_content);
        ll_sort_key_container = (LinearLayout) findViewById(R.id.ll_sort_key_container);
        ll_sort_key_container.setOnTouchListener(this);
        lv_content.setGroupIndicator(null);
        lv_content.setOnGroupClickListener(this);
        lv_content.setOnChildClickListener(this);
        lv_content.setOnScrollListener(this);
        showAnimLoading("", false, false, false);
        //decodePhoneCode();
        readCodes();
    }

    private void readCodes() {
        PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
            @Override
            public void run() {

                InputStream ins = null;
                InputStreamReader inReader = null;
                BufferedReader reader = null;
                try {
                    ins = getAssets().open("txt/p_code.txt");
                    inReader = new InputStreamReader(ins, "utf-8");
                    reader = new BufferedReader(inReader);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    //Trace.i("sb == " + sb.toString());
                    JSONObject o = new JSONObject(sb.toString());
                    final List<List<PhoneCodeModel>> mList = new ArrayList<List<PhoneCodeModel>>();
                    for (String sort_key : SORT_KEY) {
                        String key = sort_key.toLowerCase();
                        JSONArray array = o.getJSONArray(key);
                        List<PhoneCodeModel> list = new ArrayList<PhoneCodeModel>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject oo = array.getJSONObject(i);
                            PhoneCodeModel model = new PhoneCodeModel();
                            model.country = oo.optString("country_name_cn");
                            model.p_code = oo.optString("dialingcode");
                            model.search_key = PinYin.getAllPinYin(model.country);
                            //model.sort_key = String.valueOf(model.search_key.charAt(0)).toUpperCase();
                            list.add(model);
                        }
                        mList.add(list);
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            fetchData(SORT_KEY, mList);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (reader != null)
                            reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (inReader != null)
                            inReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (ins != null)
                            ins.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    private void fetchData(String[] _groups, List<List<PhoneCodeModel>> _code_list) {
        //groups.addAll(_groups);
        //code_list.addAll(_code_list);
//        for (int i = 0; i < groups.size(); i++) {
//            Trace.i("group == " + groups.get(i));
//            for (PhoneCodeModel model : code_list.get(i)) {
//                Trace.i(String.format("name == %s and code == %s", model.country, model.p_code));
//            }
//        }
        dismissAnimLoading();
        ic_search_action.setVisibility(View.VISIBLE);
        sortSize = _groups.length;
        mAdapter = new PhoneCodeAdapter(_groups, _code_list, this);
        lv_content.setAdapter(mAdapter);
        int length = _groups.length;
        for (int i = 0; i < length; i++) {
            lv_content.expandGroup(i);
        }
        createSortKeyView(_groups);
    }

    private int sortKeyHeight;

    private void createSortKeyView(String[] groups) {
        ll_sort_key_container.removeAllViews();
        int color = Color.parseColor("#4d4d4d");
        for (String s : groups) {
            TextView tv = new TextView(this);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tv.setTextColor(color);
            tv.setText(s);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.weight = 1.0f;
            tv.setLayoutParams(params);
            ll_sort_key_container.addView(tv);
        }
        ll_sort_key_container.post(new Runnable() {
            @Override
            public void run() {
                sortKeyHeight = ll_sort_key_container.getMeasuredHeight();
            }
        });
    }

    private int sortSize;

    @Override
    public boolean onTouch(View v, MotionEvent motionEvent) {
        if (sortKeyHeight == 0)
            return true;
        int index = (int) (motionEvent.getY() / (sortKeyHeight / sortSize));
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (index >= 0 && index < sortSize) {
                    lv_content.setSelectedGroup(index);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (index >= 0 && index < sortSize) {
                    lv_content.setSelectedGroup(index);
                }
                break;

            case MotionEvent.ACTION_UP:
                break;
        }

        return true;
    }


    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        PhoneCodeModel model = (PhoneCodeModel) mAdapter.getChild(groupPosition, childPosition);
        //PPAlert.showToast(this, String.format("country == %s, code == %s", model.country, model.p_code));
        Intent data = new Intent();
        data.putExtra(COUNTRY, model.country);
        data.putExtra(PHONE_CODE, model.p_code);
        setResult(RESULT_OK, data);
        needfinish = true;
        finish();
        return true;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        return true;
    }

    public void click(View v) {
        switch (v.getId()) {
            case R.id.btn_left:
                finish();
                break;
            case R.id.ic_search_action:
                changeViewForSearching();
                et_serach.requestFocus();
                PWUtils.showSoftInput(this);
                break;
            case R.id.btn_scancel:
                finish();
                break;
        }
    }

    @Override
    public void finish() {
        if (!needfinish && et_serach.getVisibility() == View.VISIBLE) {
            et_serach.clearFocus();
            changeViewForNomalSearch();
            PWUtils.hideSoftInput(et_serach, this);
            return;
        }
        super.finish();
    }

    private void changeViewForNomalSearch() {
        ic_search_action.setVisibility(View.VISIBLE);
        tv_title.setVisibility(View.VISIBLE);
        et_serach.setVisibility(View.GONE);
        btn_scancel.setVisibility(View.GONE);
    }

    private void changeViewForSearching() {
        ic_search_action.setVisibility(View.GONE);
        tv_title.setVisibility(View.GONE);
        et_serach.setVisibility(View.VISIBLE);
        btn_scancel.setVisibility(View.VISIBLE);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                PWUtils.hideSoftInput(et_serach, this);
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    private class MyTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s)) {
                return;
            } else {
                String result = s.toString();
                if (!TextUtils.isEmpty(result.trim())) {
                    doSearch(result);
                }
            }
        }
    }

    private void doSearch(String result) {
        String real = PinYin.getAllPinYin(result).toLowerCase();
        //Log.i("search_key", real);
        if (TextUtils.isEmpty(real)) return;
        List<List<PhoneCodeModel>> mList = mAdapter.getAllChilds();
        OK:
        for (int i = 0; i < mList.size(); i++) {
            for (int j = 0; j < mList.get(i).size(); j++) {
                PhoneCodeModel model = mList.get(i).get(j);
                if (model.search_key != null && model.search_key.contains(real)) {
                    //Log.i("search_key", model.search_key);
                    lv_content.setSelectedChild(i, j, true);
                    break OK;
                }
            }
        }
    }
}