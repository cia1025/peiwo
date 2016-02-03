package me.peiwo.peiwo.adapter;/*package me.peiwo.peiwo.adapter;

import java.util.List;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.MsgApplyListActivity;
import me.peiwo.peiwo.model.SayHelloModel;
import me.peiwo.peiwo.util.TimeUtil;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MsgApplyAdapter extends PPBaseAdapter<SayHelloModel> {
    LayoutInflater inflater;
    ImageLoader imageLoader;
    DisplayImageOptions options;
    Context context;
    Handler mHandler;

    public MsgApplyAdapter(List<SayHelloModel> mList, Context context, Handler handler) {
        super(mList);
        this.context = context;
        this.mHandler = handler;
        inflater = LayoutInflater.from(context);
        imageLoader = ImageLoader.getInstance();
        options = getRoundOptions();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.call_request_list_item, parent, false);
            mHolder = new ViewHolder();
            mHolder.iv_uface = (ImageView) convertView.findViewById(R.id.iv_uface);
            mHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            mHolder.tv_msg = (TextView) convertView.findViewById(R.id.tv_msg);
            mHolder.tv_badge = (TextView) convertView.findViewById(R.id.tv_badge);
            mHolder.tv_find_search = (TextView) convertView.findViewById(R.id.tv_find_search);
            mHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            mHolder.btn_doblock_report = (TextView) convertView.findViewById(R.id.btn_doblock_report);
            mHolder.btn_re_msg = (TextView) convertView.findViewById(R.id.btn_re_msg);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        SayHelloModel model = mList.get(position);
        imageLoader.displayImage(model.user.avatar_thumbnail, mHolder.iv_uface, options);
        mHolder.tv_name.setText(model.user.name);
        mHolder.tv_msg.setText(model.dialogContent);
        mHolder.tv_time.setText(TimeUtil.getMsgTimeDisplay(model.update_time, false));
        mHolder.tv_badge.setText(String.valueOf(model.dialogBadge));

//        mHolder.tv_badge.setText(model.unread_count);
        
        mHolder.btn_doblock_report.setOnClickListener(new OnApplyClickListener(position));
        mHolder.btn_re_msg.setOnClickListener(new OnApplyClickListener(position));
        return convertView;
    }

    class OnApplyClickListener implements View.OnClickListener {
        private int position;
        public OnApplyClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
        	switch (v.getId()) {
			case R.id.btn_doblock_report:
	            if (mHandler != null) {
	                Message message = mHandler.obtainMessage();
	                message.what = MsgApplyListActivity.WHAT_DOBLOCK_REPORT;
	                message.obj = position;
	                mHandler.sendMessage(message);
	            }
				break;

			case R.id.btn_re_msg:
	            if (mHandler != null) {
	                Message message = mHandler.obtainMessage();
	                message.what = MsgApplyListActivity.WHAT_REPLY_MESSAGE;
	                message.obj = position;
	                mHandler.sendMessage(message);
	            }
				break;

			}
        }
    }

    static class ViewHolder {
        ImageView iv_uface;
        TextView tv_name;
        TextView tv_msg;
        TextView tv_badge;
        TextView tv_find_search;
        TextView tv_time;
        TextView btn_doblock_report;
        TextView btn_re_msg;
    }
}
*/