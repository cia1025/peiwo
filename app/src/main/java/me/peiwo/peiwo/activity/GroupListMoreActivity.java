package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ListView;
import butterknife.Bind;
import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.GroupListAdapter;
import me.peiwo.peiwo.model.TabfindGroupModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.widget.PWPullToRefreshListView;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.util.ArrayList;
import java.util.List;

public class GroupListMoreActivity extends BaseActivity implements PullToRefreshBase.OnRefreshListener2<ListView> {
    @Bind(R.id.pullToRefreshListView)
    PWPullToRefreshListView pullToRefreshListView;
    private GroupListAdapter adapter;
    private List<TabfindGroupModel> mList = new ArrayList<>();

    private String next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list_more);
        init();
    }

    private void init() {
        setTitle("全部群组");
        pullToRefreshListView.setOnRefreshListener(this);
        adapter = new GroupListAdapter(this, mList);
        pullToRefreshListView.setAdapter(adapter);
        pullToRefreshListView.setRefreshing();
        pullToRefreshListView.setOnItemClickListener((parent, view, position, id) -> {
            TabfindGroupModel model = (TabfindGroupModel) parent.getAdapter().getItem(position);
            Intent intent = new Intent(this, GroupExhibitionActivity.class);
            intent.putExtra(GroupHomePageActvity.KEY_GROUP_ID, model.group_id);
            startActivity(intent);
        });
    }


    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        next = null;
        requestServer();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        requestServer();
    }


    private void requestServer() {
        ArrayList<NameValuePair> params = new ArrayList<>();
        if (!TextUtils.isEmpty(next)) {
            params.add(new BasicNameValuePair("cursor", next));
        }
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_GROUPS_RECRUIT, new MsgStructure() {

            @Override
            public void onReceive(JSONObject data) {
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(rst -> {
                    pullToRefreshListView.onRefreshComplete();
                    fetchGroupsItems(rst);
                });
            }

            @Override
            public void onError(int error, Object ret) {
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> pullToRefreshListView.onRefreshComplete());
            }
        });
    }

    private void fetchGroupsItems(JSONObject o) {
        if (o == null) {
            pullToRefreshListView.end(true);
            return;
        }
        JSONArray array = o.optJSONArray("groups");
        List<TabfindGroupModel> models = new ArrayList<>();
        for (int i = 0, z = array.length(); i < z; i++) {
            models.add(JSON.parseObject(array.optString(i), TabfindGroupModel.class));
        }
        if (TextUtils.isEmpty(next)) {
            mList.clear();
        }
        mList.addAll(models);
        adapter.notifyDataSetChanged();
        JSONObject cursor = o.optJSONObject("cursor");
        if (cursor != null) {
            next = cursor.optString("next");
            pullToRefreshListView.end(false);
        } else {
            next = null;
            pullToRefreshListView.end(true);
        }
    }
}
