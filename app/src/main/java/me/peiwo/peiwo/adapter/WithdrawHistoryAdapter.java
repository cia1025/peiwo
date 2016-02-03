package me.peiwo.peiwo.adapter;

import java.util.List;

import android.content.res.Resources;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.WithdrawHistoryModel;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by fuhaidong on 14/11/14.
 */
public class WithdrawHistoryAdapter extends PPBaseAdapter<WithdrawHistoryModel> {
    private List<WithdrawHistoryModel> mList;
    private LayoutInflater inflater;

    public WithdrawHistoryAdapter(List<WithdrawHistoryModel> mList, Context context) {
        super(mList);
        this.mList = mList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_withdraw_history, parent, false);
            holder = new ViewHolder();
            holder.tv_withdraw_money = (TextView) convertView.findViewById(R.id.tv_withdraw_money);
            holder.tv_withdraw_time = (TextView) convertView.findViewById(R.id.tv_withdraw_time);
            holder.tv_withdraw_state = (TextView) convertView.findViewById(R.id.tv_withdraw_state);
            holder.tv_withdraw_reason = (TextView) convertView.findViewById(R.id.tv_withdraw_reason);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        WithdrawHistoryModel model = mList.get(position);
        Resources res = convertView.getResources();
        holder.tv_withdraw_money.setText(String.format(res.getString(R.string.withdraw_money_for_how_much), model.money));
        if (model.state == 2) {
            holder.tv_withdraw_time.setText(model.pay_time);
        } else {
            holder.tv_withdraw_time.setText(model.update_time);
        }
        holder.tv_withdraw_state.setText(model.state_str);
        holder.tv_withdraw_reason.setText(model.reason_str);
        return convertView;
    }

    static class ViewHolder {
        TextView tv_withdraw_money;
        TextView tv_withdraw_time;
        TextView tv_withdraw_state;
        TextView tv_withdraw_reason;
    }
}
