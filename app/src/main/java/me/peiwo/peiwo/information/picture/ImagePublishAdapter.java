package me.peiwo.peiwo.information.picture;

import java.util.ArrayList;
import java.util.List;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.ImageItem;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class ImagePublishAdapter extends BaseAdapter {
	private List<ImageItem> mDataList = new ArrayList<ImageItem>();
	private Context mContext;
	public ImagePublishAdapter(Context context, List<ImageItem> dataList) {
		this.mContext = context;
		this.mDataList = dataList;
	}

	public int getCount() {
		// 多返回一个用于展示添加图标
		return mDataList.size();
	}

	public Object getItem(int position) {
		return mDataList.get(position);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// 所有Item展示不满一页，就不进行ViewHolder重用了，避免了一个拍照以后添加图片按钮被覆盖的奇怪问题
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.item_publish, null);
			viewHolder.imageIv = (ImageView) convertView.findViewById(R.id.item_grid_image);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if (parent.getChildCount() != position) {
			return  convertView;
		}
		    
		if (isShowAddItem(position)) {
			viewHolder.imageIv.setImageResource(R.drawable.icon_add_photo);
		} else {
			ImageItem item = mDataList.get(position);
			String path = "";
			if (!TextUtils.isEmpty(item.thumbnailPath)) {
				path = "file://" + item.thumbnailPath;
			} else {
				path = "file://" + item.sourcePath;
			}
			ImageLoader.getInstance().displayImage(path, viewHolder.imageIv);
		}
		return convertView;
	}

    private static class ViewHolder {
        ImageView imageIv;
    }
    
	private boolean isShowAddItem(int position) {
		return TextUtils.isEmpty(mDataList.get(position).sourcePath);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}
}
