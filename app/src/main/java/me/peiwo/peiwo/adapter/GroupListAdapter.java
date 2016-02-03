package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.TabfindGroupModel;

import java.util.List;

/**
 * Created by fuhaidong on 15/12/9.
 */
public class GroupListAdapter extends GroupJoinBaseAdapter<TabfindGroupModel> {
    private LayoutInflater inflater;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public GroupListAdapter(Context context, List<TabfindGroupModel> mList) {
        super(context, mList);
        inflater = LayoutInflater.from(context);
        imageLoader = ImageLoader.getInstance();
        options = getRoundOptions(true);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TabfindGroupModel model = (TabfindGroupModel) getItem(position);
        GroupListViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_find_group_item, parent, false);
            holder = new GroupListViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (GroupListViewHolder) convertView.getTag();
        }
        fetchDataForGroup(holder, model);
        return convertView;
    }

    private void fetchDataForGroup(GroupListViewHolder holderGroup, TabfindGroupModel groupModel) {
        imageLoader.displayImage(TextUtils.isEmpty(groupModel.admin.avatar) ? groupModel.avatar : groupModel.admin.avatar, holderGroup.iv_group_avatar, options);
        holderGroup.tv_group_name.setText(groupModel.admin.name);
        holderGroup.tv_group_des.setText(groupModel.group_name);
        if (groupModel.ticket_price > 0) {
            holderGroup.iv_price_icon.setVisibility(View.VISIBLE);
        } else {
            holderGroup.iv_price_icon.setVisibility(View.INVISIBLE);
        }
        holderGroup.v_add_group_action.setOnClickListener(v -> joinGroup(v, groupModel));
    }


    static class GroupListViewHolder {
        @Bind(R.id.iv_group_avatar)
        ImageView iv_group_avatar;
        @Bind(R.id.tv_group_name)
        TextView tv_group_name;
        @Bind(R.id.tv_group_des)
        TextView tv_group_des;
        @Bind(R.id.iv_price_icon)
        View iv_price_icon;
        @Bind(R.id.v_add_group_action)
        View v_add_group_action;

        public GroupListViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }
    }
}
