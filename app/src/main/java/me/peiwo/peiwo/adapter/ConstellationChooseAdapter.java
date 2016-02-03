package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.ConstellationChooseModel;

import java.util.List;

/**
 * Created by fuhaidong on 15/9/28.
 */
public class ConstellationChooseAdapter extends PPBaseAdapter<ConstellationChooseModel> {
    private LayoutInflater inflater;

    public ConstellationChooseAdapter(Context context, List<ConstellationChooseModel> mList) {
        super(mList);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ConstellationChooseModel model = (ConstellationChooseModel) getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_conste_choose, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.iv_constellation.setImageResource(model.res_id);
        holder.tv_cname.setText(model.c_name);
        return convertView;
    }

    static class ViewHolder {
        public ImageView iv_constellation;
        public TextView tv_cname;

        public ViewHolder(View convertView) {
            this.iv_constellation = (ImageView) convertView.findViewById(R.id.iv_constellation);
            this.tv_cname = (TextView) convertView.findViewById(R.id.tv_cname);
        }
    }
}
