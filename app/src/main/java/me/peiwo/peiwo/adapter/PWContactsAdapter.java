package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;
import java.util.Locale;

import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.PWPreCallingActivity;
import me.peiwo.peiwo.activity.RealCallActivity;
import me.peiwo.peiwo.activity.UserInfoActivity;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.model.PWContactsModel;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.TimeUtil;
import me.peiwo.peiwo.util.UmengStatisticsAgent;
import me.peiwo.peiwo.util.UserManager;

/**
 * Created by fuhaidong on 14-8-27.
 */
public class PWContactsAdapter extends PPBaseAdapter<PWContactsModel> {
    private LayoutInflater inflater;
    private List<PWContactsModel> mList;
    private ImageLoader imageLoader;
    private Context context;
    private int color_male;
    private int color_female;


    private final DisplayImageOptions OPTIONS_F = getRoundOptions(true);

    public PWContactsAdapter(List<PWContactsModel> mList, Context context) {
        super(mList);
        this.context = context;
        this.mList = mList;
        inflater = LayoutInflater.from(context);
        imageLoader = ImageLoader.getInstance();
        color_male = context.getResources().getColor(R.color.c_list_male);
        color_female = context.getResources().getColor(R.color.c_list_female);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.pwcontacts_item, parent, false);
            holder = new ViewHolder();
            holder.iv_uface = (ImageView) convertView.findViewById(R.id.iv_uface);
            holder.iv_quick_call = (ImageView) convertView.findViewById(R.id.iv_quick_call);
            holder.tv_uname = (TextView) convertView.findViewById(R.id.tv_uname);
            holder.tv_signtime = (TextView) convertView.findViewById(R.id.tv_signtime);
            holder.tv_des = (TextView) convertView.findViewById(R.id.tv_des);
            holder.tv_gender_constellation = (TextView) convertView.findViewById(R.id.tv_gender_constellation);
            holder.iv_price_icon = (ImageView) convertView.findViewById(R.id.iv_price_icon);
            holder.v_gender_indicator = convertView.findViewById(R.id.v_gender_indicator);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //赋值
        final PWContactsModel model = mList.get(position);
        imageLoader.displayImage(model.avatar_thumbnail, holder.iv_uface, OPTIONS_F);
        if (model.gender == AsynHttpClient.GENDER_MASK_FEMALE) {
            holder.v_gender_indicator.setBackgroundColor(color_female);
            //mHolder.tv_gender_constellation.setBackgroundResource(R.drawable.bg_gender_f);
        } else {
            holder.v_gender_indicator.setBackgroundColor(color_male);
            //mHolder.tv_gender_constellation.setBackgroundResource(R.drawable.bg_gender_m);
        }

        holder.tv_uname.setText(UserManager.getRealName(Integer.valueOf(model.uid), model.name, context));
        if (UserManager.getRealName(Integer.valueOf(model.uid), model.name, context).contains("系统消息") && model.slogan.contains("有人@我：")) {
            holder.tv_des.setText(model.slogan.substring(model.slogan.indexOf("有人@我：") + 5));
        } else {
            holder.tv_des.setText(model.slogan);
        }
//        holder.tv_des.setText(model.slogan);
        holder.tv_gender_constellation.setText(TimeUtil.getConstellation(model.birthday));
        //充当显示登录时间
        holder.tv_signtime.setText(TimeUtil.getSignInTime(model.signin_time));

        if (TextUtils.isEmpty(model.price)) {
            holder.iv_price_icon.setVisibility(View.GONE);
        } else {
            if (Float.valueOf(model.price) > 0) {
                holder.iv_price_icon.setVisibility(View.VISIBLE);
            } else {
                holder.iv_price_icon.setVisibility(View.GONE);
            }
        }

        holder.iv_quick_call.setOnClickListener((v) -> {
                    call(model);
                    UmengStatisticsAgent.onEvent(context, UMEventIDS.UMEFRIENDLISTCALL);

                }
        );
        return convertView;
    }

    static class ViewHolder {
        ImageView iv_uface;
        ImageView iv_quick_call;
        TextView tv_uname;
        TextView tv_signtime;
        TextView tv_des;
        ImageView iv_price_icon;
        TextView tv_gender_constellation;
        View v_gender_indicator;
    }

    public void call(final PWContactsModel model) {
        if (context == null) return;
        if (!PWUtils.isNetWorkAvailable(context)) {
            ((PWPreCallingActivity) context).showToast(context, "网络连接失败");
            return;
        }
        if (context instanceof PWPreCallingActivity) {
            final PWPreCallingActivity activity = (PWPreCallingActivity) context;
            Intent intent = new Intent(activity, RealCallActivity.class);
            intent.putExtra("face_url", model.avatar_thumbnail);
            intent.putExtra("gender", model.gender); // == 1 ? "男" : "女"
            intent.putExtra("address", String.format(Locale.getDefault(), "%s %s", model.province, model.city));
            intent.putExtra("age", TimeUtil.getAgeByBirthday(model.birthday));
            intent.putExtra("tid", Integer.valueOf(model.uid));
            intent.putExtra("uname", model.name);
            intent.putExtra("slogan", model.slogan);
            intent.putExtra("flag", DfineAction.OUTGOING_CALL);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            PWUserModel mUser = UserManager.getPWUser(activity);

            float src_price = Float.valueOf(String.format("%.1f", Double.valueOf(model.price)));
            activity.prepareCalling(mUser.uid, Integer.valueOf(model.uid), UserInfoActivity.RELATION_FRIENDS, src_price, intent, new PWPreCallingActivity.OnCallPreparedListener() {
                @Override
                public void onCallPreparedSuccess(int permission, final float price) {
                }

                @Override
                public void onCallPreparedError(int error, Object ret) {

                }
            });
        }
    }
}
