package me.peiwo.peiwo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.AddTagsAdapter;
import me.peiwo.peiwo.adapter.AddTagsAdapter.TagModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.TitleUtil;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

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
    private EditText add_new_tags_edit;

    private int type;
    String title = "";
    private String[] selectedArray = null;
    private Context mContext = null;

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
                title = "专属标签";
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
        }
    }

    private void initView() {
        TitleUtil.setTitleBar(this, title, v -> {
            finish();
        }, null);

        ListView listView = (ListView) findViewById(R.id.tags_list_view);
        View headView = View.inflate(this, R.layout.add_tags_head, null);
        listView.addHeaderView(headView);


        add_new_tags_edit = (EditText) headView.findViewById(R.id.add_new_tags_edit);
        add_new_tags_edit.setHint("添加我喜欢的" + title);
        Button add_new_tags_button = (Button) headView.findViewById(R.id.add_new_tags_button);
        add_new_tags_button.setOnClickListener(arg0 -> mHandler.sendEmptyMessage(HANDLE_ADD_NEW_TAGS));
        listView.setAdapter(adapter);
//		mHandle.sendEmptyMessage(AddTagsActivity.);
//		AdapterView
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
                    adapter.notifyDataSetChanged();
                }
                break;
                case HANDLE_GET_TAGS_FAILURE: {
                    mergeAllTags();
                    adapter.notifyDataSetChanged();
                }
                break;
                case HANDLE_SELECT_TAGS: {
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
                    tagsList.get(msg.arg1).isSelected = !tagsList.get(msg.arg1).isSelected;
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

                            tagsList.add(0, new TagModel(content, true));
                            adapter.notifyDataSetChanged();
                            add_new_tags_edit.setText("");
                        }
                    }
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
                tagsList.add(new TagModel(selectedArray[i], true));
            }
        }
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
        for (int i = 0; i < tagsList.size(); i++) {
            if (tagsList.get(i).isSelected) {
                if (tags.length() > 0) {
                    tags.append(",");
                }
                tags.append(tagsList.get(i).tagContent);
            }
        }
        Intent intent = getIntent();
        intent.putExtra(KEY_TAGS, tags.toString());
        setResult(RESULT_OK, intent);
        super.finish();
    }
}
