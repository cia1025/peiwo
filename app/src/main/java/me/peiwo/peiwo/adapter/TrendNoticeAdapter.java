package me.peiwo.peiwo.adapter;

import java.util.List;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.UserInfoActivity;
import me.peiwo.peiwo.model.TrendNoticeModel;
import me.peiwo.peiwo.util.TimeUtil;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class TrendNoticeAdapter extends PPBaseAdapter<TrendNoticeModel> {
	private LayoutInflater inflater;
	private ImageLoader imageLoader;
	private Context context = null;
	public TrendNoticeAdapter(List<? extends TrendNoticeModel> mList,
			Context context) {
		super(mList);
		this.context = context;
		this.mList = mList;
		inflater = LayoutInflater.from(context);
		imageLoader = ImageLoader.getInstance();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.activity_trend_notice_item, parent,false);
			holder.iv_uface = (ImageView) convertView.findViewById(R.id.iv_uface);
			holder.iv_pub_image = (ImageView) convertView.findViewById(R.id.iv_pub_image);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final TrendNoticeModel model = mList.get(position);
		imageLoader.displayImage(model.image_url, holder.iv_uface);
		if("null".equals(model.pub_image_url) || TextUtils.isEmpty(model.pub_image_url)){
			holder.iv_pub_image.setBackgroundResource(R.drawable.bg_font);
		}else{
			imageLoader.displayImage(model.pub_image_url, holder.iv_pub_image);
		}
		holder.tv_name.setText(model.name);
		holder.tv_time.setText(TimeUtil.getMsgTimeDisplay(model.time, false));
		holder.iv_uface.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(context, UserInfoActivity.class);
		        intent.putExtra(UserInfoActivity.TARGET_UID, model.uid);
		        intent.putExtra(UserInfoActivity.TARGET_NAME, model.name);
		        intent.putExtra(UserInfoActivity.MESSAGE_FROM, 3);
		        context.startActivity(intent);
			}
		});

		return convertView;
	}

	private static class ViewHolder {
		ImageView iv_uface;
		TextView tv_name;
		TextView tv_time;
		ImageView iv_pub_image;
	}
}
