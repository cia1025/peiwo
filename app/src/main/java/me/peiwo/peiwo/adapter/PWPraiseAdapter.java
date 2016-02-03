package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.PWPraiseModel;
import me.peiwo.peiwo.util.ImageUtil;
import me.peiwo.peiwo.util.TimeUtil;
import me.peiwo.peiwo.util.UserManager;

import java.util.List;

public class PWPraiseAdapter extends PPBaseAdapter<PWPraiseModel> {
    LayoutInflater inflater;
    List<PWPraiseModel> mPraiseList;
    ImageLoader imageLoader;
    private Context context;

    private DisplayImageOptions options = ImageUtil.getRoundedOptions();

    public PWPraiseAdapter(List<PWPraiseModel> list, Context context) {
        super(list);
        this.context = context;
        mPraiseList = list;
        inflater = LayoutInflater.from(context);
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.pwpraise_item, parent,
                    false);
            holder = new ViewHolder();
            holder.iv_uface = (ImageView) convertView
                    .findViewById(R.id.iv_uface);
            holder.tv_uname = (TextView) convertView
                    .findViewById(R.id.tv_uname);
            holder.tv_praise_time = (TextView) convertView
                    .findViewById(R.id.tv_praise_time);
//			holder.v_gender_age = (GenderWithAgeView) convertView
//					.findViewById(R.id.v_gender_age);
//			holder.tv_des = (TextView) convertView.findViewById(R.id.tv_des);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final PWPraiseModel model = mList.get(position);
        imageLoader.displayImage(model.getAvatarThumbnail(), holder.iv_uface, options);
        holder.tv_uname.setText(UserManager.getRealName(model.getUid(),
                model.getName(), context));
//		holder.v_gender_age.displayGenderWithAge(model.getGender(),
//				TimeUtil.getAgeByBirthday(model.getBirthday()));
//		holder.tv_des.setText(model.getSlogan());
        holder.tv_praise_time.setText(TimeUtil.getMsgTimeDisplay(model.getPraiseTime()));
        return convertView;
    }

    static class ViewHolder {
        ImageView iv_uface;
        TextView tv_uname;
        TextView tv_praise_time;
    }
}
