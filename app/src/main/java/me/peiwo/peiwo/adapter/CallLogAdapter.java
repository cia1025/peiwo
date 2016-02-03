package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.CallLogModel;
import me.peiwo.peiwo.util.TimeUtil;

import java.util.List;

public class CallLogAdapter extends PPBaseAdapter<CallLogModel> {

	private DisplayImageOptions options = getRoundOptions(true);

	private static final int CALLER = 0; // 主叫
	private static final int CALLEE = 1; // 被叫
	private static final int NO_ANWSER = 2; // 未接听
	private static final int WILDCAT = 3; // 随机呼叫

	List<CallLogModel> mList;
	Context context;
	LayoutInflater inflater;
	ImageLoader imageLoader;

	public CallLogAdapter(List<CallLogModel> mList, Context context) {
		super(mList);
		this.mList = mList;
		this.context = context;
		inflater = LayoutInflater.from(context);
		imageLoader = ImageLoader.getInstance();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.calllog_list_item, parent,
					false);
			holder = new ViewHolder();
			holder.iv_uface = (ImageView) convertView
					.findViewById(R.id.iv_uface);
			holder.tv_name_with_icon = (TextView) convertView
					.findViewById(R.id.tv_name_with_icon);
			holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		CallLogModel model = mList.get(position);
		imageLoader.displayImage(model.user.avatar_thumbnail, holder.iv_uface,
				options);
		holder.tv_time.setText(TimeUtil.getMsgTimeDisplay(model.update_time, false));
		
		// 暂时没有图片
		setCompoundDrawable(model.history_type, holder.tv_name_with_icon, model.user.name);
		return convertView;
	}

	private void setCompoundDrawable(int history_type, TextView tv, String name) {
		int resId = 0;
		switch (history_type) {
		case CALLER:
			resId = R.drawable.icon_call_outgoing;
			break;

		case CALLEE:
			resId = R.drawable.icon_call_outgoing;
			break;
		case NO_ANWSER:
			resId = 0;
			break;
		case WILDCAT:
			resId = R.drawable.icon_random_call;
			break;
		default:
			resId = 0;
			break;
		}
		if (resId != 0) {
			tv.setCompoundDrawables(null, null, getCompoundDrawable(resId),
					null);
			tv.setTextColor(Color.parseColor("#000000"));
			tv.setText(name);
		} else {
			tv.setCompoundDrawables(null, null, null, null);
			tv.setTextColor(Color.parseColor("#ff0000"));
			tv.setText(String.format("%s%s", name, "  未接"));
		}
	}

	static class ViewHolder {
		ImageView iv_uface;
		TextView tv_name_with_icon;
		TextView tv_time;
	}

	private Drawable getCompoundDrawable(int resId) {
		Drawable iconDrawable = context.getResources().getDrawable(resId);
		iconDrawable.setBounds(0, 0, iconDrawable.getMinimumWidth(),
				iconDrawable.getMinimumHeight());
		return iconDrawable;
	}

}
