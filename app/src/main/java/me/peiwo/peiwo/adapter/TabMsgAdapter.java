package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.TabMsgFragment;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.model.TabMsgModel;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.TimeUtil;
import me.peiwo.peiwo.util.UserManager;

import java.util.List;

public class TabMsgAdapter extends PPBaseAdapter<TabMsgModel> {

    private DisplayImageOptions options = getRoundOptions(true);

    private List<TabMsgModel> mList;
    private LayoutInflater inflater;
    private Context mContext;

    private int sayHelloCount = 0;
    //private int faceSize = 0;
    MsgDBCenterService dbCenterService;

    public TabMsgAdapter(List<TabMsgModel> mList, Context mContext) {
        super(mList);
        this.mContext = mContext;
        this.mList = mList;
        inflater = LayoutInflater.from(mContext);
        dbCenterService = MsgDBCenterService.getInstance();
        //faceSize = PWUtils.getFaceSizeFromScreen(mContext);
    }

    public void setSayHelloCount(int count) {
        this.sayHelloCount = count;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.msg_list_item, parent, false);
            holder = new ViewHolder();
            holder.iv_uface = (ImageView) convertView.findViewById(R.id.iv_uface);
            holder.tv_badge = (TextView) convertView.findViewById(R.id.tv_badge);
            holder.tv_uname = (TextView) convertView.findViewById(R.id.tv_uname);
            holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            holder.tv_des = (TextView) convertView.findViewById(R.id.tv_des);
            holder.tv_at_user = (TextView) convertView.findViewById(R.id.tv_at_user);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        TabMsgModel model = mList.get(position);
        fillData(model, holder);
        return convertView;
    }

    private void fillData(TabMsgModel model, ViewHolder holder) {
        //CustomLog.d("TabMsgAdapter fillData, model is : " + model);
        int msg_type = model.msg_type;
        switch (msg_type) {
            case TabMsgFragment.GROUP_MESSAGE:
                holder.tv_uname.setText(model.userModel.name);
                ImageLoader.getInstance().displayImage(model.userModel.avatar_thumbnail, holder.iv_uface, options);
                holder.tv_time.setText(TimeUtil.getMsgTimeDisplay(model.update_time, false));
                break;
            case TabMsgFragment.USER_MESSAGE:
                // 用户通过权限或取消权限消息
                if (model.uid.equals(DfineAction.MSG_ID_SAYHELLO)) {
                    holder.tv_uname.setText(mContext.getString(R.string.voices_need_you_confirm, sayHelloCount));//每一个人就是一个msgTable中的一条记录
                    holder.iv_uface.setImageResource(R.drawable.icon_voice_request);
                    holder.tv_time.setText(TimeUtil.getMsgTimeDisplay(model.update_time, false));
                } else {
                    holder.tv_uname.setText(UserManager.getRealName(Integer.valueOf(model.uid), model.userModel.name, mContext));
                    ImageLoader.getInstance().displayImage(model.userModel.avatar_thumbnail, holder.iv_uface, options);
                    holder.tv_time.setText(TimeUtil.getMsgTimeDisplay(model.update_time, false));
                }
                break;
            case TabMsgFragment.SYS_MESSAGE:
                // 系统消息
                holder.tv_uname.setText("系统消息");
                holder.iv_uface.setImageResource(R.drawable.ic_system_msg);
                holder.tv_time.setText(TimeUtil.getMsgTimeDisplay(model.update_time, false));
                break;
            case TabMsgFragment.CALL_HISTORY:
                // 通话记录
                holder.tv_uname.setText("通话记录");
                holder.iv_uface.setImageResource(R.drawable.ic_calllog);
                holder.tv_time.setText(TimeUtil.getMsgTimeDisplay(model.update_time, false));
                break;

            default:
                break;
        }
        if (TabMsgFragment.GROUP_MESSAGE == msg_type) {
            String extra = dbCenterService.getAtUserMapping().get(model.uid);
            holder.tv_at_user.setText(extra);
        }
        holder.tv_des.setText(model.content);
        //if (!TextUtils.isEmpty(model.content)) {
        //SpannableString spannableString = ExpressionUtil.getInstance().getExpressionString(model.content, faceSize);
        //CustomLog.d("spannableStr is : "+spannableString);
        //holder.tv_des.setText(model.content);
//        }else {
//
//        }

        if (model.unread_count > 0) {
            holder.tv_badge.setVisibility(View.VISIBLE);
            if (model.unread_count > 99) {
                holder.tv_badge.setText("99+");
            } else {
                holder.tv_badge.setText(String.valueOf(model.unread_count));
            }
        } else {
            holder.tv_badge.setText(null);
            holder.tv_badge.setVisibility(View.INVISIBLE);
        }
    }

    static class ViewHolder {
        ImageView iv_uface;
        TextView tv_badge;
        TextView tv_uname;
        TextView tv_time;
        TextView tv_des;
        TextView tv_at_user;
    }

}
