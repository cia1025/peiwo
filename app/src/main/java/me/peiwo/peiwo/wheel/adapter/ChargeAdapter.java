package me.peiwo.peiwo.wheel.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.PaymentItemModel;

import java.util.List;

/**
 * Created by fuhaidong on 15/11/17.
 */
public class ChargeAdapter extends RecyclerView.Adapter<ChargeAdapter.ChargeViewHolder> {
    private List<PaymentItemModel> mList;
    private LayoutInflater inflater;

    public ChargeAdapter(Context context, List<PaymentItemModel> mList) {
        inflater = LayoutInflater.from(context);
        this.mList = mList;
    }

    @Override
    public ChargeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ChargeViewHolder(inflater.inflate(R.layout.charge_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ChargeViewHolder holder, final int position) {
        PaymentItemModel model = mList.get(position);
        holder.tv_charge_item.setSelected(model.isselected);
        holder.tv_charge_item.setText(model.money + "元");
        holder.tv_charge_item.setOnClickListener(v -> changeItemStatus(position));
    }

    private void changeItemStatus(int position) {
        int i = 0;
        for (PaymentItemModel model : mList) {
            model.isselected = i == position;
            i++;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public PaymentItemModel getCheckedPaymentEntity() {
        for (PaymentItemModel model : mList) {
            if (model.isselected) {
                return model;
            }
        }
        return null;
    }

    static class ChargeViewHolder extends RecyclerView.ViewHolder {
        TextView tv_charge_item;
        //View v_bg_parent;

        public ChargeViewHolder(View itemView) {
            super(itemView);
            //this.v_bg_parent = itemView.findViewById(R.id.v_bg_parent);
            tv_charge_item = (TextView) itemView.findViewById(R.id.tv_charge_item);
        }
    }
}
