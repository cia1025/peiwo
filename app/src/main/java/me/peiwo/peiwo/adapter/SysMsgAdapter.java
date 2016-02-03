package me.peiwo.peiwo.adapter;//package me.peiwo.peiwo.adapter;
//
//import java.util.List;
//
//import me.peiwo.peiwo.R;
//import me.peiwo.peiwo.model.SysMsgModel;
//import me.peiwo.peiwo.util.PWUtils;
//import me.peiwo.peiwo.util.TimeUtil;
//import android.content.Context;
//import android.text.TextUtils;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.nostra13.universalimageloader.core.ImageLoader;
//
///**
// * Created by Dong Fuhai on 2014-07-21 14:43.
// * 
// * @modify:
// */
//public class SysMsgAdapter extends PPBaseAdapter<SysMsgModel> {
//	LayoutInflater inflater;
//
//	private static final int MAX_COUNT = 2;
//	private static final int VIEW_TYPE_SYSTEM = 0;
//	private static final int VIEW_TYPE_REDBAG = 1;
//	private ImageLoader imageLoader;
//	private Context context;
//	
//	public SysMsgAdapter(List<SysMsgModel> mList, Context context) {
//		super(mList);
//		inflater = LayoutInflater.from(context);
//		imageLoader = ImageLoader.getInstance();
//		this.context = context;
//	}
//	
//	@Override
//    public int getViewTypeCount() {
//        return MAX_COUNT;
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//    	if (mList.get(position).dialog_type == MessageModel.DIALOG_TYPE_PACKAGE) {
//    		return VIEW_TYPE_REDBAG;
//    	}
//    	return VIEW_TYPE_SYSTEM;
//    }
//	 
//
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		ViewHolder mHolder = null;
//		RedBagViewHolder redBagHolder = null;
////		int viewType = 6;
//		int viewType = getItemViewType(position);
//		if (convertView == null) {
//			switch (viewType) {
//			case VIEW_TYPE_SYSTEM:
//				convertView = inflater.inflate(R.layout.activity_msgaccept_item_other, parent, false);
//				mHolder = new ViewHolder();
//				mHolder.iv_uface = (ImageView) convertView.findViewById(R.id.iv_uface);
//				mHolder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
//				mHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
//				convertView.setTag(mHolder);
//				break;
//
//			case VIEW_TYPE_REDBAG:
//				convertView = inflater.inflate(R.layout.activity_msgsystem_item_redbag, parent, false);
//				redBagHolder = new RedBagViewHolder();
//				redBagHolder.tv_redbag_content = (TextView) convertView.findViewById(R.id.tv_redbag_content);
//				redBagHolder.tv_redbag_title = (TextView) convertView.findViewById(R.id.tv_redbag_title);
//				redBagHolder.iv_redbag_icon = (ImageView) convertView.findViewById(R.id.iv_redbag_icon);
//				redBagHolder.iv_uface =  (ImageView) convertView.findViewById(R.id.iv_uface);
//				redBagHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
//				convertView.setTag(redBagHolder);
//				break;
//			}
//		} else {
//			switch (viewType) {
//			case VIEW_TYPE_SYSTEM:
//				mHolder = (ViewHolder) convertView.getTag();
//				break;
//			case VIEW_TYPE_REDBAG:
//				redBagHolder = (RedBagViewHolder) convertView.getTag();
//				break;
//			}
//		}
//
//		SysMsgModel model = mList.get(position);
//
//		if (mHolder != null) {
//			mHolder.iv_uface.setImageResource(R.drawable.icon_sys);
//			mHolder.tv_content.setText(model.content);
//			mHolder.tv_time.setText(TimeUtil.getMsgTimeDisplay(model.update_time, true));
//		}
//		if(redBagHolder != null){
//			redBagHolder.iv_uface.setImageResource(R.drawable.icon_sys);
//			redBagHolder.tv_redbag_title.setText(model.redbag_title);
//			if (model.readStatus == 0) {
//				redBagHolder.tv_redbag_content.setText(model.redbag_content);
//			} else {
//				redBagHolder.tv_redbag_content.setText(model.redbag_extra);
//			}
//			String name = PWUtils.getFileNameNoEx(model.icon_name);
//			int icon_id = 0;
//			if (!TextUtils.isEmpty(name)) {
//				PWUtils.getResId(context, name, "drawable");
//			}
//			if (icon_id != 0) {
//				redBagHolder.iv_redbag_icon.setImageResource(icon_id);
//			} else {
//				imageLoader.displayImage(model.icon_url,redBagHolder.iv_redbag_icon);
//			}
//
//			redBagHolder.tv_time.setText(TimeUtil.getMsgTimeDisplay(model.update_time, true));
//		}
//		return convertView;
//	}
//
//	static class ViewHolder {
//		TextView tv_content;
//		TextView tv_time;
//		ImageView iv_uface;
//	}
//
//}
