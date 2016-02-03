package me.peiwo.peiwo.adapter;

import java.util.List;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.ChargeLogModel;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by ChenHao on 2014-11-12 下午8:08.
 *
 * @modify:
 */
public class ChargeLogAdapter extends PPBaseAdapter<ChargeLogModel> {
    private LayoutInflater inflater;
    private List<ChargeLogModel> mList;

    public ChargeLogAdapter(List<ChargeLogModel> mList, Context context) {
        super(mList);
        this.mList = mList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.charge_log_list_item, null);
            holder = new ViewHolder();
            holder.tv_money = (TextView) convertView.findViewById(R.id.tv_money);
            holder.tv_charge_time = (TextView) convertView.findViewById(R.id.tv_charge_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ChargeLogModel model = mList.get(position);
        holder.tv_money.setText(String.format("陪我充值%s元", model.money));
        holder.tv_charge_time.setText(model.update_time);

        return convertView;
    }

    static class ViewHolder {
        TextView tv_money;
        TextView tv_charge_time;

    }
}
