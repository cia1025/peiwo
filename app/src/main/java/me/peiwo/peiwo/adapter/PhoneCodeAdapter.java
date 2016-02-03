package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.PhoneCodeModel;

import java.util.List;

/**
 * Created by fuhaidong on 14/11/7.
 */
public class PhoneCodeAdapter extends BaseExpandableListAdapter {
    private LayoutInflater inflater;
    private String[] groups;
    private List<List<PhoneCodeModel>> mList;

    public PhoneCodeAdapter(String[] groups, List<List<PhoneCodeModel>> mList, Context context) {
        this.groups = groups;
        this.mList = mList;
        inflater = LayoutInflater.from(context);
    }

    public List<List<PhoneCodeModel>> getAllChilds() {
        return mList;
    }


    @Override
    public int getGroupCount() {
        return mList.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return mList.get(i).size();
    }

    @Override
    public Object getGroup(int i) {
        return mList.get(i);
    }

    @Override
    public Object getChild(int i, int i2) {
        return mList.get(i).get(i2);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i2) {
        return i2;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        TextView tv_title;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_group_title, parent, false);
            tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            convertView.setTag(tv_title);
        } else {
            tv_title = (TextView) convertView.getTag();
        }
        tv_title.setText(groups[groupPosition]);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_phonecode_item, parent, false);
            holder = new ViewHolder();
            holder.tv_country = (TextView) convertView.findViewById(R.id.tv_country);
            holder.tv_pcode = (TextView) convertView.findViewById(R.id.tv_pcode);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        PhoneCodeModel model = (PhoneCodeModel) getChild(groupPosition, childPosition);
        holder.tv_country.setText(model.country);
        holder.tv_pcode.setText(String.format("(+%s)", model.p_code));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i2) {
        return true;
    }

    static class ViewHolder {
        public TextView tv_country;
        public TextView tv_pcode;
    }
}
