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
import me.peiwo.peiwo.model.groupchat.PacketIconModel;

/**
 * Created by fuhaidong on 15/12/19.
 */
public class ChatRedBagAdapter extends RecyclerView.Adapter<ChatRedBagAdapter.RedBagViewHolder> {
    private PacketIconModel model;
    private LayoutInflater inflater;
    private ImageLoader imageLoader;
    private int curr_selected = 0;

    public ChatRedBagAdapter(Context context, PacketIconModel model) {
        inflater = LayoutInflater.from(context);
        this.model = model;
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public RedBagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RedBagViewHolder(inflater.inflate(R.layout.layout_chat_redbag_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RedBagViewHolder holder, int position) {
        String uri = model.icons[position];
        imageLoader.displayImage(uri, holder.iv_picket_icon);
        if (curr_selected == position) {
            holder.iv_indi.setImageResource(R.drawable.image_selected_small_s);
        } else {
            holder.iv_indi.setImageResource(R.drawable.image_selected_small_n);
        }
        holder.itemView.setOnClickListener(v -> {
            if (curr_selected == position) {
                return;
            }
            curr_selected = position;
            notifyDataSetChanged();
//            if (curr_selected == position) {
//                curr_selected = -1;
//                notifyItemChanged(position);
//            } else {
//                curr_selected = position;
//                notifyDataSetChanged();
//            }
        });
    }

    @Override
    public int getItemCount() {
        return model.icons.length;
    }

    public void reload(PacketIconModel model, int select_index) {
        curr_selected = select_index;
        this.model = model;
        notifyDataSetChanged();
    }

    public PacketIconModel getSelectedPacketIcon() {
        if (curr_selected >= 0 && curr_selected < model.icons.length) {
            return new PacketIconModel(model.id, model.msg, model.icons[curr_selected]);
        }
        return null;
    }

    static class RedBagViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_picket_icon)
        ImageView iv_picket_icon;

        @Bind(R.id.iv_indi)
        ImageView iv_indi;

        public RedBagViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
