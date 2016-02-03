package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.TabfindGroupModel;
import me.peiwo.peiwo.model.groupchat.GroupsTitle;

import java.util.List;

/**
 * Created by fuhaidong on 16/1/18.
 */
public class GroupsAdapter extends BaseExpandableListAdapter {
    private List<List<TabfindGroupModel>> mGroups;
    private LayoutInflater inflater;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private List<GroupsTitle> groupTitles;

    public GroupsAdapter(Context context, List<List<TabfindGroupModel>> mGroups, List<GroupsTitle> groupTitles) {
        this.mGroups = mGroups;
        this.groupTitles = groupTitles;
        inflater = LayoutInflater.from(context);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_default_avatar)
                .showImageForEmptyUri(R.drawable.ic_default_avatar)
                .showImageOnFail(R.drawable.ic_default_avatar).cacheInMemory(true)
                .cacheOnDisk(true).displayer(new RoundedBitmapDisplayer(10))
                .build();
    }


    @Override
    public int getGroupCount() {

        return this.mGroups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.mGroups.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mGroups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.mGroups.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupTitleViewHolder groupTitleViewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_group_title, parent, false);
            groupTitleViewHolder = new GroupTitleViewHolder(convertView);
            convertView.setTag(groupTitleViewHolder);
        } else {
            groupTitleViewHolder = (GroupTitleViewHolder) convertView.getTag();
        }
        setUpGroupTitle(groupPosition, groupTitleViewHolder.tv_title);
        if (isExpanded) {
            groupTitleViewHolder.iv_indi.setImageResource(R.drawable.ic_expanded);
        } else {
            groupTitleViewHolder.iv_indi.setImageResource(R.drawable.ic_collapsed);
        }
        return convertView;
    }

    private void setUpGroupTitle(int groupPosition, TextView tv_title) {
        if (groupTitles != null) {
            if (groupPosition < groupTitles.size()) {
                tv_title.setText(groupTitles.get(groupPosition).group_title);
            } else {
                tv_title.setText(null);
            }
        } else {
            tv_title.setText(null);
        }
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TabfindGroupModel model = (TabfindGroupModel) getChild(groupPosition, childPosition);
        GroupListViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_my_group_item, parent, false);
            holder = new GroupListViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (GroupListViewHolder) convertView.getTag();
        }
        fetchDataForGroup(holder, model);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private void fetchDataForGroup(GroupListViewHolder holderGroup, TabfindGroupModel groupModel) {
        imageLoader.displayImage(TextUtils.isEmpty(groupModel.avatar) ? groupModel.admin.avatar : groupModel.avatar, holderGroup.iv_group_avatar, options);
        holderGroup.tv_group_name.setText(groupModel.group_name);
    }


    static class GroupListViewHolder {
        @Bind(R.id.iv_group_avatar)
        ImageView iv_group_avatar;
        @Bind(R.id.tv_group_name)
        TextView tv_group_name;

        public GroupListViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }
    }

    static class GroupTitleViewHolder {
        @Bind(R.id.tv_title)
        TextView tv_title;
        @Bind(R.id.iv_indi)
        ImageView iv_indi;

        public GroupTitleViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }
    }
}
