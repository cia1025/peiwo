package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.callback.ExpressionItemClickListener;
import me.peiwo.peiwo.model.GIFModel;
import me.peiwo.peiwo.util.PWUtils;
import pl.droidsonroids.gif.GifImageView;

import java.util.List;

/**
 * Created by fuhaidong on 15/11/18.
 */
public class GIFFragmentAdapter extends RecyclerView.Adapter<GIFFragmentAdapter.GIFViewHolder> {
    private List<GIFModel> mList;
    private LayoutInflater inflater;
    private ExpressionItemClickListener listener;

    public GIFFragmentAdapter(Context context, List<GIFModel> mList) {
        this.mList = mList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public GIFViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GIFViewHolder(inflater.inflate(R.layout.grid_item_gif, parent, false));
    }

    @Override
    public void onBindViewHolder(GIFViewHolder holder, int position) {
        GIFModel model = mList.get(position);
        holder.iv_gif_item.setImageResource(model.res_id);
        holder.tv_gif_des.setText(model.gif_title);
        holder.iv_gif_item.setOnClickListener(v -> {
            if (this.listener != null) {
                this.listener.onExpressionItemClick(model);
            }
        });
        holder.iv_gif_item.setOnLongClickListener(v -> {
            showGifPreViewAtLocation(v, model.movie_res_id);
            return true;
        });
        holder.iv_gif_item.setOnTouchListener((v, event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    //showGifPreView(v, model.movie_res_id);
                    //v.getParent().requestDisallowInterceptTouchEvent(true);
                    //Log.i("dispatch", "ACTION_DOWN");
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    //Log.i("dispatch", "ACTION_UP");
                    hideGifPreView(v);
                    //v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    //Log.i("dispatch", "ACTION_CANCEL");
                    hideGifPreView(v);
                    break;
            }
            return false;
        });
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    private void hideGifPreView(View v) {
        v.getParent().requestDisallowInterceptTouchEvent(false);
        ViewGroup viewGroup = ((ViewGroup) v.getRootView().findViewById(android.R.id.content));
        View preView = viewGroup.findViewById(R.id.v_pre_gif_view);
        viewGroup.removeView(preView);
    }

    private void showGifPreViewAtLocation(View v, int movie_res_id) {
        v.getParent().requestDisallowInterceptTouchEvent(true);
        View preView = inflater.inflate(R.layout.msg_preview_gif_face_layout, null);
        GifImageView iv_preview_face_view = (GifImageView) preView.findViewById(R.id.iv_preview_face_view);
        iv_preview_face_view.setImageResource(movie_res_id);
        int px = PWUtils.getPXbyDP(v.getContext(), 120);
        int[] openingViewLocation = new int[2];
        v.getLocationOnScreen(openingViewLocation);
        preView.setTranslationX(openingViewLocation[0] - px / 3);
        preView.setTranslationY(openingViewLocation[1] - px - v.getHeight() / 2);
        ((ViewGroup) v.getRootView().findViewById(android.R.id.content)).addView(preView, new ViewGroup.LayoutParams(px, px));
    }

    public void setOnExpressionItemClickListener(ExpressionItemClickListener listener) {
        this.listener = listener;
    }

    static class GIFViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_gif_item;
        TextView tv_gif_des;

        public GIFViewHolder(View itemView) {
            super(itemView);
            iv_gif_item = (ImageView) itemView.findViewById(R.id.iv_gif_item);
            tv_gif_des = (TextView) itemView.findViewById(R.id.tv_gif_des);
        }
    }
}
