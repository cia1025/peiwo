package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.GlobalWebViewActivity;
import me.peiwo.peiwo.model.groupchat.PacketIconModel;

import java.util.List;

/**
 * Created by fuhaidong on 15/12/21.
 */
public class GroupchatPacketsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<PacketIconModel> models;
    private LayoutInflater inflater;
    private Context context;

    public GroupchatPacketsAdapter(Context context, List<PacketIconModel> models) {
        this.context = context;
        this.models = models;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new PacketViewHolder(inflater.inflate(R.layout.layout_packets_item, parent, false));
        } else {
            return new FooterViewHolder(inflater.inflate(R.layout.layout_packets_footer, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PacketViewHolder) {
            PacketViewHolder packetViewHolder = (PacketViewHolder) holder;
            PacketIconModel packetIconModel = models.get(position);
            packetViewHolder.tv_icons_provider.setText(packetIconModel.msg);
            createPacketItem(packetIconModel, packetViewHolder, position);
        } else {
            //empty
            FooterViewHolder fooViewHolder = (FooterViewHolder) holder;
            fooViewHolder.tv_link.setOnClickListener(v -> {
                Intent intent = new Intent(context, GlobalWebViewActivity.class);
                intent.putExtra(GlobalWebViewActivity.URL, "https://h5.peiwoapi.com/h5/group/grabMoney.html");
                context.startActivity(intent);
            });
        }

    }

    private void createPacketItem(PacketIconModel packetIconModel, PacketViewHolder holder, int group_position) {
        holder.v_recycler_redbags.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        holder.v_recycler_redbags.setLayoutManager(linearLayoutManager);
        holder.v_recycler_redbags.setAdapter(new ChatPacketsAdapter(context, packetIconModel, group_position));
    }

    @Override
    public int getItemCount() {
        return models.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return 1;
        } else {
            return 0;
        }
    }

    static class PacketViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.v_recycler_redbags)
        RecyclerView v_recycler_redbags;
        @Bind(R.id.tv_icons_provider)
        TextView tv_icons_provider;

        public PacketViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_link)
        TextView tv_link;

        public FooterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
