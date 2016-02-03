package me.peiwo.peiwo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.TitleUtil;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CreatTopicActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    public static final String KEY_TAGS = "tags";
    public static final String KEY_TYPE = "type";

    public static final int HANDLE_GET_TOPIC_SUCCESS = 0x01;
    public static final int HANDLE_GET_TOPIC_FAILURE = 0x02;

    private CreatTopicAdapter adapter;
    private List<TopicModel> topicList = new ArrayList<>();
    private EditText add_new_tags_edit;

    String title = "";
    private Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.add_tags_activity);
        adapter = new CreatTopicAdapter();
        initView();
        requestServer();
    }


    private void initView() {
        TitleUtil.setTitleBar(this, title, v -> {
            onBackPressed();
        }, "下一步", v -> {
            TopicModel model = getSelectedModel();
            if (model == null) {
                return;
            }
            startNewTopic(model.tagContent, model.id);
        });
        ListView listView = (ListView) findViewById(R.id.tags_list_view);
        View headerView = LayoutInflater.from(this).inflate(R.layout.add_tags_head, null);
        listView.addHeaderView(headerView, null, false);
        listView.setOnItemClickListener(this);
        listView.setAdapter(adapter);


        add_new_tags_edit = (EditText) headerView.findViewById(R.id.add_new_tags_edit);
        add_new_tags_edit.setHint("添加自定义话题");
        Button add_new_tags_button = (Button) headerView.findViewById(R.id.add_new_tags_button);
        add_new_tags_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String inputText = add_new_tags_edit.getText().toString();
                if (TextUtils.isEmpty(inputText)) {
                    return;
                }
                inputText = inputText.replace(" ", "");
                if (TextUtils.isEmpty(inputText)) {
                    return;
                }
                TopicModel model = new TopicModel(inputText, -1);
                model.selected = true;
                topicList.add(0, model);
                changeSelected(0);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private TopicModel getSelectedModel() {
        for (TopicModel item : topicList) {
            if (item.selected) {
                return item;
            }
        }
        return null;
    }

    private void requestServer() {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        ApiRequestWrapper.openAPIGET(this, params,
                AsynHttpClient.API_GET_SYSTEM_TOPIC, new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                        Message msg = mHandler.obtainMessage(HANDLE_GET_TOPIC_SUCCESS);
                        msg.obj = data;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onError(int error, Object ret) {
                        mHandler.sendEmptyMessage(HANDLE_GET_TOPIC_FAILURE);
                    }
                });
    }

    private void startNewTopic(String topic_content, int topic_id) {
        Intent intent = new Intent(mContext, InformationPubActivity.class);
        intent.putExtra("topic_content", topic_content);
        if (topic_id != -1) {
            intent.putExtra("topic_id", topic_id);
        }
        startActivity(intent);
        finish();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_GET_TOPIC_SUCCESS: {
                    JSONObject tagObject = (JSONObject) msg.obj;
                    JSONArray tagArray = tagObject.optJSONArray("topics");
                    if (tagArray == null) return;
                    List<TopicModel> models = new ArrayList<>();
                    for (int i = 0; i < tagArray.length(); i++) {
                        JSONObject itemJSON = tagArray.optJSONObject(i);
                        int id = itemJSON.optInt("id");
                        String content = itemJSON.optString("content");
                        TopicModel topicModel = new TopicModel(content, id);
                        if (i == 0) {
                            topicModel.selected = true;
                        } else {
                            topicModel.selected = false;
                        }
                        models.add(topicModel);
                    }
                    topicList.addAll(models);
                    adapter.notifyDataSetChanged();
                }
                break;
                case HANDLE_GET_TOPIC_FAILURE: {
                    adapter.notifyDataSetChanged();
                }
                break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListView listView = (ListView) parent;
        changeSelected(position - listView.getHeaderViewsCount());
        adapter.notifyDataSetChanged();
    }


    private class CreatTopicAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return topicList.size();
        }

        @Override
        public Object getItem(int arg0) {
            return topicList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(final int arg0, View view, ViewGroup arg2) {
            ViewHolder holder;
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.add_tags_item, arg2, false);
                holder = new ViewHolder();
                holder.tags_item_image = view.findViewById(R.id.tags_item_image);
                holder.tags_item_text = (TextView) view.findViewById(R.id.tags_item_text);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            final TopicModel model = topicList.get(arg0);
            holder.tags_item_text.setText(model.tagContent);
            holder.tags_item_image.setVisibility(model.selected ? View.VISIBLE : View.GONE);


            return view;
        }


    }

    static class ViewHolder {
        View tags_item_image;
        TextView tags_item_text;
    }

    private void changeSelected(int position) {
        for (int i = 0; i < topicList.size(); i++) {
            TopicModel model = topicList.get(i);
            model.selected = i == position;
        }
    }

    public static class TopicModel {
        public String tagContent;
        public int id = 0;
        public boolean selected = false;

        public TopicModel(String tagContent, int id) {
            this.tagContent = tagContent;
            this.id = id;
        }
    }
}
