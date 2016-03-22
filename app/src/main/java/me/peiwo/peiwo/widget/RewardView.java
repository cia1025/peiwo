package me.peiwo.peiwo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.AgoraCallActivity;
import me.peiwo.peiwo.activity.BaseActivity;
import me.peiwo.peiwo.fragment.RewardDialogTipsFragment;
import me.peiwo.peiwo.model.agora.AgoraIntentRewardResponseEvent;

/**
 * Created by wallace on 16/3/18.
 */
public class RewardView extends RelativeLayout {
    private TextView tv_reward_message;
    private TextView tv_reward_price;
    private Button btn_reward_action;
    private int code;
    private int transaction;

    public RewardView(Context context, AgoraIntentRewardResponseEvent event) {
        super(context);
        init(event);
    }

    public RewardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init();
    }

    public RewardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //init();
    }

    private void init(AgoraIntentRewardResponseEvent event) {
        setId(R.id.reward_view_id);
        setBackgroundColor(getResources().getColor(R.color.white));
        LayoutInflater.from(getContext()).inflate(R.layout.layout_reward_dialog, this, true);
        post(() -> {
            tv_reward_message = (TextView) findViewById(R.id.tv_reward_message);
            tv_reward_price = (TextView) findViewById(R.id.tv_reward_price);
            btn_reward_action = (Button) findViewById(R.id.btn_reward_action);
            display(event);
            findViewById(R.id.iv_reward_close).setOnClickListener(v -> removeThis());
            findViewById(R.id.iv_reward_tips).setOnClickListener(v -> {
                RewardDialogTipsFragment rewardDialogTipsFragment = RewardDialogTipsFragment.newInstance();
                BaseActivity activity = (BaseActivity) getContext();
                rewardDialogTipsFragment.show(activity.getSupportFragmentManager(), "RewardDialogTipsFragment");
            });
            findViewById(R.id.tv_refresh_reward_price).setOnClickListener(v -> {
                AgoraCallActivity activity = (AgoraCallActivity) getContext();
                activity.refreshRewardPrice();
            });
            findViewById(R.id.btn_reward_action).setOnClickListener(v -> {
                AgoraCallActivity activity = (AgoraCallActivity) getContext();
                if (code == 60002) {
                    activity.charge();
                    removeThis();
                } else {
                    activity.payReward(transaction);
                    removeThis();
                }
            });
        });
    }

    private void removeThis() {
        ViewGroup viewGroup = (ViewGroup) getParent();
        viewGroup.removeView(this);
    }

    public void display(AgoraIntentRewardResponseEvent event) {
        code = event.code;
        transaction = event.transaction;
        if (event.code == 60002)
            btn_reward_action.setText("充值");
        else btn_reward_action.setText("激发钱能");
        tv_reward_message.setText(event.msg);
        tv_reward_price.setText(event.money_format);
    }
}
