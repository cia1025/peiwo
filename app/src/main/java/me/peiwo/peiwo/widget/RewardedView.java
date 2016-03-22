package me.peiwo.peiwo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.AgoraCallActivity;
import me.peiwo.peiwo.model.agora.AgoraRewardedEvent;

/**
 * Created by wallace on 16/3/18.
 */
public class RewardedView extends FrameLayout {
    private ImageView iv_avatar;
    private TextView tv_reward_from;
    private TextView tv_reward_message;
    private TextView tv_reward_price;

    public RewardedView(Context context, AgoraRewardedEvent event) {
        super(context);
        init(event);
    }

    public RewardedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RewardedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(AgoraRewardedEvent event) {
        setId(R.id.rewarded_view_id);
        setBackgroundColor(getResources().getColor(R.color.white));
        LayoutInflater.from(getContext()).inflate(R.layout.layout_rewarded_dialog, this, true);
        post(() -> {
            iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
            tv_reward_from = (TextView) findViewById(R.id.tv_reward_from);
            tv_reward_message = (TextView) findViewById(R.id.tv_reward_message);
            tv_reward_price = (TextView) findViewById(R.id.tv_reward_price);
            display(event);
            findViewById(R.id.iv_reward_close).setOnClickListener(v -> removeThis());
            findViewById(R.id.btn_reward_toyou).setOnClickListener(v -> {
                AgoraCallActivity activity = (AgoraCallActivity) getContext();
                activity.returnASalute();
                removeThis();
            });
        });
    }

    public void display(AgoraRewardedEvent event) {
        ImageLoader.getInstance().displayImage(event.remote_avatar, iv_avatar);
        tv_reward_from.setText(event.nick_name);
        tv_reward_message.setText(event.msg);
        tv_reward_price.setText(event.money_format);
    }

    private void removeThis() {
        ViewGroup viewGroup = (ViewGroup) getParent();
        viewGroup.removeView(this);
    }
}
