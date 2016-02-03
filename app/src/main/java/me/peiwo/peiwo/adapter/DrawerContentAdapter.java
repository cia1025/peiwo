package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.callback.RecyclerViewItemClickListener;
import me.peiwo.peiwo.model.DrawerContentModel;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.widget.DrawerContentView;

import java.util.List;

/**
 * Created by fuhaidong on 15/10/19.
 */
public class DrawerContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<DrawerContentModel> mList;
    private LayoutInflater inflater;
    private Context context;
    private ImageLoader imageLoader;

    private RecyclerViewItemClickListener onDrawerItemClickListener;


    public void setOnItemClickListener(RecyclerViewItemClickListener onDrawerItemClickListener) {
        this.onDrawerItemClickListener = onDrawerItemClickListener;
    }

    public DrawerContentAdapter(Context context, List<DrawerContentModel> mList) {
        inflater = LayoutInflater.from(context);
        this.mList = mList;
        this.context = context;
        this.imageLoader = ImageLoader.getInstance();
    }

    public DrawerContentModel getItemModel(int index) {
        return mList.get(index);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == DrawerContentView.VIEW_TYPE_HEADER) {
            return new DrawerContentHeaderViewHolder(inflater.inflate(R.layout.layout_drawer_content_header, parent, false));
        } else if (viewType == DrawerContentView.VIEW_TYPE_ITEM) {
            return new DrawerContentViewHolder(inflater.inflate(R.layout.layout_drawer_content_item, parent, false));
        } else if (viewType == DrawerContentView.VIEW_TYPE_DECORATION) {
            return new DrawerContentDecorationViewHolder(inflater.inflate(R.layout.layout_drawer_item_decoration, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        DrawerContentModel model = mList.get(position);
        if (holder instanceof DrawerContentHeaderViewHolder) {
            DrawerContentHeaderViewHolder headerHolder = (DrawerContentHeaderViewHolder) holder;
            imageLoader.displayImage(model.avatar_thumbnail, headerHolder.iv_drawer_avatar);
            headerHolder.tv_drawer_uname.setText(model.uname);
            headerHolder.tv_drawer_pwnum.setText(model.pwnum);
            //headerHolder.tv_drawer_voice_var.setText(model.voice_var);
            headerHolder.v_update_profile.setOnClickListener(v -> {
                if (this.onUpdateProfileListener != null) {
                    this.onUpdateProfileListener.onUpdateProfile();
                }
            });
            headerHolder.itemView.setOnClickListener(v -> {
                if (DrawerContentAdapter.this.onDrawerItemClickListener != null) {
                    DrawerContentAdapter.this.onDrawerItemClickListener.onRecyclerViewItemClick(v, position);
                }
            });
        } else if (holder instanceof DrawerContentViewHolder) {
            DrawerContentViewHolder contentHolder = (DrawerContentViewHolder) holder;
            setCompoundDrawable(contentHolder.tv_drawercontent_text, position);
            contentHolder.tv_drawercontent_text.setText(model.drawer_lable);
            if (position == DrawerContentView.ITEM_INDEX_UPDATE_VOICE_VAR) {
                contentHolder.tv_extra.setText(model.voice_var);
            } else {
                contentHolder.tv_extra.setText(null);
            }
            contentHolder.itemView.setOnClickListener(v -> {
                if (DrawerContentAdapter.this.onDrawerItemClickListener != null) {
                    DrawerContentAdapter.this.onDrawerItemClickListener.onRecyclerViewItemClick(v, position);
                }
            });
        }
    }

    private void setCompoundDrawable(TextView tv_drawercontent_text, int position) {
        int resId = 0;
        switch (position) {
            case DrawerContentView.ITEM_INDEX_UPDATE_MYWALLET:
                resId = R.drawable.ic_drawer_want_money;
                break;
            case DrawerContentView.ITEM_INDEX_UPDATE_WILDLOGS:
                resId = R.drawable.ic_drawer_wildcat;
                break;
            case DrawerContentView.ITEM_INDEX_UPDATE_VOICE_VAR:
                resId = R.drawable.ic_drawer_voice_var;
                break;
            case DrawerContentView.ITEM_INDEX_UPDATE_LAZYGUY:
                resId = R.drawable.ic_drawer_lazy_recoder;
                break;
            case DrawerContentView.ITEM_INDEX_UPDATE_NEWGUIDE:
                resId = R.drawable.ic_drawer_peiwo_xuetang;
                break;
            case DrawerContentView.ITEM_INDEX_UPDATE_SETTING:
                resId = R.drawable.ic_drawer_setting;
                break;
        }
        if (resId != 0) {
            tv_drawercontent_text.setCompoundDrawables(PWUtils.getCompoundDrawable(resId, context), null, null, null);
        }
    }

    @Override
    public int getItemViewType(int position) {
        DrawerContentModel model = mList.get(position);
        return model.view_type;
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class DrawerContentViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_drawercontent_text)
        TextView tv_drawercontent_text;
        @Bind(R.id.tv_extra)
        TextView tv_extra;

        public DrawerContentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class DrawerContentHeaderViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_drawer_avatar)
        ImageView iv_drawer_avatar;
        @Bind(R.id.tv_drawer_uname)
        TextView tv_drawer_uname;
        @Bind(R.id.tv_drawer_pwnum)
        TextView tv_drawer_pwnum;
        @Bind(R.id.v_update_profile)
        View v_update_profile;

        public DrawerContentHeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class DrawerContentDecorationViewHolder extends RecyclerView.ViewHolder {

        public DrawerContentDecorationViewHolder(View itemView) {
            super(itemView);
        }
    }


    private OnUpdateProfileListener onUpdateProfileListener;

    public void setOnUpdateProfileListener(OnUpdateProfileListener onUpdateProfileListener) {
        this.onUpdateProfileListener = onUpdateProfileListener;
    }

    public interface OnUpdateProfileListener {
        void onUpdateProfile();
    }
}
