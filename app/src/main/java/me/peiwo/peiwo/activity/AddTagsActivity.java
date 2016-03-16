package me.peiwo.peiwo.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.AddTagsAdapter;
import me.peiwo.peiwo.adapter.AddTagsAdapter.TagModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.widget.FlowLayout;

public class AddTagsActivity extends BaseActivity {
    public static final String KEY_TAGS = "tags";
    public static final String KEY_TYPE = "type";

    public static final int HANDLE_GET_TAGS_SUCCESS = 0x01;
    public static final int HANDLE_GET_TAGS_FAILURE = 0x02;

    public static final int HANDLE_SELECT_TAGS = 0x03;

    public static final int HANDLE_ADD_NEW_TAGS = 0x04;
    private int max_tag_can_add = 5;

    private AddTagsAdapter adapter;
    private ArrayList<TagModel> tagsList = new ArrayList<TagModel>();
    private ArrayList<TagModel> selectedTagsList = new ArrayList<TagModel>();
    private EditText add_new_tags_edit;

    private int type;
    String title = "";
    private String[] selectedArray = null;
    private Context mContext = null;
    private FlowLayout tags_list_selected_container;
    private Button add_new_tags_button;
    private int maxNum = 16;
    private RelativeLayout tagsRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.add_tags_activity);
        adapter = new AddTagsAdapter(this, tagsList, mHandler);
        initData();
        initView();
        requestServer();
    }

    private void initData() {
        type = getIntent().getIntExtra(KEY_TYPE, 0);
        switch (type) {
            case 0:
                max_tag_can_add = 10;
//                title = "专属标签";
                title = "自我评价";
                break;
            case 1:
                title = "美食";
                break;
            case 2:
                title = "音乐";
                break;
            case 3:
                title = "影视";
                break;
            case 4:
                title = "书籍";
                break;
            case 5:
                title = "旅行";
                break;
            case 6:
                title = "运动";
                break;
            case 7:
                title = "应用";
                break;
        }
        String tags = getIntent().getStringExtra(KEY_TAGS);
        if (!TextUtils.isEmpty(tags)) {
            selectedArray = tags.split(",");
            for (int i = 0; i < selectedArray.length; i++) {
                if (!TextUtils.isEmpty(selectedArray[i])) {
                    selectedTagsList.add(new TagModel(selectedArray[i], true));
                }
            }
        }
    }

    private void initView() {
        TitleUtil.setTitleBar(this, title, v -> {
            finish();
        }, null);

        ListView listView = (ListView) findViewById(R.id.tags_list_view);
        View headView = View.inflate(this, R.layout.add_tags_head, null);
        listView.addHeaderView(headView);

        tags_list_selected_container = (FlowLayout) findViewById(R.id.tags_list_selected_container);
        addTagsToContainer();
        tagsRelativeLayout = (RelativeLayout) headView.findViewById(R.id.add_new_tags_container);
        add_new_tags_button = (Button) headView.findViewById(R.id.add_new_tags_button);
        add_new_tags_edit = (EditText) headView.findViewById(R.id.add_new_tags_edit);
        add_new_tags_button.setVisibility(View.INVISIBLE);
        if (type == 0) {
            add_new_tags_edit.setHint("添加自我评价");
        } else {
            add_new_tags_edit.setHint("添加我喜欢的" + title);
        }
//        add_new_tags_edit.setHint("添加我喜欢的" + title);
        add_new_tags_edit.setOnFocusChangeListener((v, hasFocus) -> {
            EditText editText = (EditText) v;
            if (!hasFocus) {
//                editText.setHint("添加我喜欢的" + title);
                if (type == 0) {
                    add_new_tags_edit.setHint("添加自我评价");
                } else {
                    add_new_tags_edit.setHint("添加我喜欢的" + title);
                }
//                if (isShow.get()) {
//                    isShow.set(false);
//                } else {
//                    add_new_tags_button.setVisibility(View.INVISIBLE);
//                }

            } else {
                editText.setHint("");
//                add_new_tags_button.setVisibility(View.VISIBLE);
            }
        });

        add_new_tags_edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxNum)});

        add_new_tags_button.setOnClickListener(arg0 -> mHandler.sendEmptyMessage(HANDLE_ADD_NEW_TAGS));
//        tagsRelativeLayout.setOnClickListener(v -> {
//            add_new_tags_edit.requestFocus();
//            add_new_tags_button.setVisibility(View.VISIBLE);
//        });

        listView.setAdapter(adapter);
//		mHandle.sendEmptyMessage(AddTagsActivity.);
//		AdapterView
    }

    private void addTagsToContainer() {
        tags_list_selected_container.removeAllViews();
        int size = selectedTagsList.size();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        for (int i = 0; i < selectedTagsList.size(); i++) {
            String tagContent = selectedTagsList.get(i).tagContent;
            if (!TextUtils.isEmpty(tagContent)) {
                TextView tag_transp_item = (TextView) layoutInflater.inflate(R.layout.tag_transp_item, null);
                if ("自我评价".equals(title)) {
                    tag_transp_item.setText("#" + tagContent);
                } else {
                    tag_transp_item.setText(tagContent);
                }
                tags_list_selected_container.addView(tag_transp_item);
            }
        }
        //tags_list_selected_container.invalidate();
    }

    Rect touchRect = new Rect();

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                tagsRelativeLayout.getGlobalVisibleRect(touchRect);
                if (touchRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    add_new_tags_edit.requestFocus();
                    showSoftKeyboard();
                    add_new_tags_button.setVisibility(View.VISIBLE);
                } else {
                    add_new_tags_edit.clearFocus();
                    add_new_tags_button.setVisibility(View.INVISIBLE);
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void requestServer() {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("type", String.valueOf(type)));
        ApiRequestWrapper.openAPIGET(this, params,
                AsynHttpClient.API_GET_SYSTEM_TAGS, new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                        Message msg = mHandler.obtainMessage(HANDLE_GET_TAGS_SUCCESS);
                        msg.obj = data;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onError(int error, Object ret) {
                        mHandler.sendEmptyMessage(HANDLE_GET_TAGS_FAILURE);
                    }
                });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_GET_TAGS_SUCCESS: {
                    JSONObject tagObject = (JSONObject) msg.obj;
                    JSONArray tagArray = tagObject.optJSONArray("tags");
                    for (int i = 0; i < tagArray.length(); i++) {
                        tagsList.add(new TagModel(tagArray.optString(i), false));
                    }
                    mergeAllTags();
                    addTagsToContainer();
                    adapter.notifyDataSetChanged();
                }
                break;
                case HANDLE_GET_TAGS_FAILURE: {
                    mergeAllTags();
                    addTagsToContainer();
                    adapter.notifyDataSetChanged();
                }
                break;
                case HANDLE_SELECT_TAGS: {
                    TagModel tempTagModel;
                    if (!tagsList.get(msg.arg1).isSelected) {
                        int selectCount = 0;
                        for (int i = 0; i < tagsList.size(); i++) {
                            if (tagsList.get(i).isSelected) {
                                selectCount++;
                            }
                        }
                        if (selectCount >= max_tag_can_add) {
                            showToast(mContext, String.format("最多添加%s个", max_tag_can_add));
                            return;
                        }
                    }
                    tempTagModel = tagsList.get(msg.arg1);
                    if (!tempTagModel.isSelected) {
                        tempTagModel.isSelected = true;
                        selectedTagsList.add(tempTagModel);
                    } else {
                        Iterator<TagModel> iterator = selectedTagsList.iterator();
                        while (iterator.hasNext()) {
                            TagModel tagModel = iterator.next();
                            if (tempTagModel.tagContent.equals(tagModel.tagContent)) {
                                iterator.remove();
                            }
                        }

                        tempTagModel.isSelected = false;
                    }
                    // tagsList.get(msg.arg1).isSelected = !tagsList.get(msg.arg1).isSelected;
                    addTagsToContainer();
//                    add_new_tags_edit.clearFocus();
                    adapter.notifyDataSetChanged();
                }
                break;
                case HANDLE_ADD_NEW_TAGS:
                    if (add_new_tags_edit.getText().length() != 0) {
                        String content = add_new_tags_edit.getText().toString();
                        content = content.replace(" ", "");
                        if (!TextUtils.isEmpty(content)) {
                            int selectCount = 0;
                            for (int i = 0; i < tagsList.size(); i++) {
                                if (tagsList.get(i).isSelected) {
                                    selectCount++;
                                }
                            }
                            if (selectCount >= max_tag_can_add) {
                                showToast(mContext, String.format("最多添加%s个", max_tag_can_add));
                                return;
                            }
                            if (isContains(content)) {
                                showToast(AddTagsActivity.this, "已存在相同标签");
                            } else {
                                selectedTagsList.add(new TagModel(content, true));
                                tagsList.add(0, new TagModel(content, true));
                                addTagsToContainer();
                            }

                            adapter.notifyDataSetChanged();
                            add_new_tags_edit.setText("");
                        }
                    }
                    add_new_tags_edit.clearFocus();
                    add_new_tags_button.setVisibility(View.INVISIBLE);
                    hideSoftKeyboard(add_new_tags_button);
                    break;
                default:
                    break;
            }
        }
    };

    private void mergeAllTags() {
        if (selectedArray == null)
            return;
        for (int i = 0; i < tagsList.size(); i++) {
            for (int j = 0; j < selectedArray.length; j++) {
                if (tagsList.get(i).tagContent.equals(selectedArray[j])) {
                    tagsList.get(i).isSelected = true;
                    selectedArray[j] = "";
                    break;
                }
            }
        }
        for (int i = selectedArray.length - 1; i >= 0; i--) {
            if (!TextUtils.isEmpty(selectedArray[i])) {
                tagsList.add(0, new TagModel(selectedArray[i], true));
            }
        }


//        if(selectedTagsList.isEmpty())
//            return;
//        ArrayList<TagModel> extraTagsList=new ArrayList<TagModel>();
//        for(int i=0;i<selectedTagsList.size();i++){
//            for(int j=0;j<tagsList.size();j++){
//                if(selectedTagsList.get(i).tagContent.equals(tagsList.get(j).tagContent)){
//                    tagsList.get(j).isSelected=true;
//                }else{
//                    extraTagsList.add
//                }
//            }
//        }
    }

    private boolean isContains(String content) {
        boolean isContains = false;
        Iterator<TagModel> iterator = tagsList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().tagContent.equals(content)) {
                isContains = true;
                break;
            }
        }
        return isContains;
    }

//    @Override
//    public void onBackPressed() {
//        StringBuffer tags = new StringBuffer();
//        for (int i = 0; i < tagsList.size(); i++) {
//            if (tagsList.get(i).isSelected) {
//                if (tags.length() > 0) {
//                    tags.append(",");
//                }
//                tags.append(tagsList.get(i).tagContent);
//            }
//        }
//        Intent intent = getIntent();
//        intent.putExtra(KEY_TAGS, tags.toString());
//        setResult(RESULT_OK, intent);
//        finish();
//    }

    @Override
    public void finish() {
        StringBuffer tags = new StringBuffer();
//        for (int i = 0; i < tagsList.size(); i++) {
//            if (tagsList.get(i).isSelected) {
//                if (tags.length() > 0) {
//                    tags.append(",");
//                }
//                tags.append(tagsList.get(i).tagContent);
//            }
//        }
        for (int j = 0; j < selectedTagsList.size(); j++) {
            if (tags.length() > 0) {
                tags.append(",");
            }
            tags.append(selectedTagsList.get(j).tagContent);
        }
        Intent intent = getIntent();
        intent.putExtra(KEY_TAGS, tags.toString());
        setResult(RESULT_OK, intent);
        super.finish();
    }

    private void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) AddTagsActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) AddTagsActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0,InputMethodManager.SHOW_FORCED);
    }

}
