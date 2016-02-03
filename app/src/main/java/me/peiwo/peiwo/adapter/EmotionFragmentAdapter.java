package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.callback.ExpressionItemClickListener;
import me.peiwo.peiwo.model.EmotionModel;

import java.util.List;

/**
 * Created by fuhaidong on 15/11/18.
 */
public class EmotionFragmentAdapter extends RecyclerView.Adapter<EmotionFragmentAdapter.EmotionViewHolder> {
    private List<EmotionModel> mList;
    private LayoutInflater inflater;

    private ExpressionItemClickListener listener;

    public EmotionFragmentAdapter(Context context, List<EmotionModel> mList) {
        this.mList = mList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public EmotionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new EmotionViewHolder(inflater.inflate(R.layout.grid_item_face, parent, false));
    }

    @Override
    public void onBindViewHolder(EmotionViewHolder holder, int position) {
        EmotionModel model = mList.get(position);
        holder.iv_emotion_item.setImageResource(model.res_id);
        holder.iv_emotion_item.setOnClickListener(v -> {
            if (this.listener != null) {
                this.listener.onExpressionItemClick(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public void setOnExpressionItemClickListener(ExpressionItemClickListener listener) {
        this.listener = listener;
    }

    static class EmotionViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_emotion_item;

        public EmotionViewHolder(View itemView) {
            super(itemView);
            iv_emotion_item = (ImageView) itemView.findViewById(R.id.iv_emotion_item);
        }
    }

}
