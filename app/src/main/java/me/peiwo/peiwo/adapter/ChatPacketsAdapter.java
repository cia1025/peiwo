package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.GroupchatMorePacketActivity;
import me.peiwo.peiwo.model.groupchat.PacketIconModel;

/**
 * Created by fuhaidong on 15/12/19.
 */
public class ChatPacketsAdapter extends RecyclerView.Adapter<ChatPacketsAdapter.RedBagViewHolder> {
    private PacketIconModel model;
    private LayoutInflater inflater;
    private Context context;
    private int group_position;
    private ImageLoader imageLoader;

    public ChatPacketsAdapter(Context context, PacketIconModel model, int group_position) {
        inflater = LayoutInflater.from(context);
        this.model = model;
        this.context = context;
        this.group_position = group_position;
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public RedBagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RedBagViewHolder(inflater.inflate(R.layout.layout_chat_packets_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RedBagViewHolder holder, int position) {
        String uri = model.icons[position];
        imageLoader.displayImage(uri, holder.iv_single_packet_icon);
        holder.iv_single_packet_icon.setOnClickListener(v -> {
            if (context instanceof GroupchatMorePacketActivity) {
                GroupchatMorePacketActivity activity = (GroupchatMorePacketActivity) context;
                activity.resultSelectedPicket(group_position, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return model.icons.length;
    }

    static class RedBagViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_single_packet_icon)
        ImageView iv_single_packet_icon;

        public RedBagViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
