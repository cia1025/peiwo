package me.peiwo.peiwo.adapter;

import java.util.List;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.PWBlockModel;
import me.peiwo.peiwo.util.ImageUtil;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by ChenHao on 2014-11-20 下午2:52.
 *
 * @modify:
 */
public class BlockListAdapter extends PPBaseAdapter<PWBlockModel> {
    private List<PWBlockModel> mList;
    private LayoutInflater inflater;
    private ImageLoader imageLoader;

    public BlockListAdapter(Context context, List<PWBlockModel> mlist) {
        super(mlist);
        this.mList = mlist;
        inflater = LayoutInflater.from(context);
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.block_list_item, parent, false);
            holder = new ViewHolder();
            holder.iv_uface = (ImageView) convertView.findViewById(R.id.iv_uface);
            holder.tv_uname = (TextView) convertView.findViewById(R.id.tv_uname);
            holder.tv_block_time = (TextView) convertView.findViewById(R.id.tv_block_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        PWBlockModel model = mList.get(position);
        imageLoader.displayImage(model.avatar_thumbnail, holder.iv_uface, ImageUtil.getRoundedOptions());
        holder.tv_uname.setText(model.name);
        holder.tv_block_time.setText(String.format("拉黑时间：%s", model.block_time));
        return convertView;
    }

    static class ViewHolder {
        ImageView iv_uface;
        TextView tv_uname;
        TextView tv_block_time;
    }
}
