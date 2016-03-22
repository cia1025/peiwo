package me.peiwo.peiwo.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.BaseCallActivity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fuhaidong on 15/9/15.
 */
public class RewardDialogFragment extends DialogFragment implements View.OnClickListener {
    private OnRewardActionListener listener;

    private String code;
    private int tuid;
    private int transaction;
    private Button btn_reward_action;
    private TextView tv_reward_message;
    private TextView tv_reward_price;

    public static RewardDialogFragment newInstance(int tuid, String type, int transaction, String reward_message, String reward_price, String code, int gender) {
        RewardDialogFragment fragment = new RewardDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("msg", reward_message);
        bundle.putString("price", reward_price);
        bundle.putString("code", code);
        bundle.putString("type", type);
        bundle.putInt("tuid", tuid);
        bundle.putInt("transaction", transaction);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        RelativeLayout rootView = new RelativeLayout(getActivity());
        View v = getActivity().getLayoutInflater().inflate(R.layout.layout_reward_dialog, rootView, true);
        //rootView.addView(v);
        tv_reward_message = (TextView) v.findViewById(R.id.tv_reward_message);
        tv_reward_price = (TextView) v.findViewById(R.id.tv_reward_price);
        btn_reward_action = (Button) v.findViewById(R.id.btn_reward_action);
        View tv_refresh_reward_price = v.findViewById(R.id.tv_refresh_reward_price);
        tv_refresh_reward_price.setOnClickListener(this);
//        if (getArguments().getInt("gender") == 2) {
//            if (!"0".equals(getArguments().getString("code"))) {
//                btn_reward_action.setText("取消");
//            } else {
//                btn_reward_action.setText("打赏");
//            }
//        } else {
//            btn_reward_action.setText("0".equals(getArguments().getString("code")) ? "打赏" : "充值");
//        }
        code = getArguments().getString("code");
        tuid = getArguments().getInt("tuid");
        transaction = getArguments().getInt("transaction");
        btn_reward_action.setText("0".equals(code) ? "激发钱能" : "充值");

        v.findViewById(R.id.iv_reward_close).setOnClickListener(this);
        v.findViewById(R.id.iv_reward_tips).setOnClickListener(this);
        btn_reward_action.setOnClickListener(this);
        if (getArguments() != null) {
            tv_reward_message.setText(getArguments().getString("msg"));
            float money = Float.valueOf(getArguments().getString("price")) / 100;
            tv_reward_price.setText("￥" + String.format("%.2f", money));
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);
        return builder.create();
    }

    public void setOnRewardActionListener(OnRewardActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_reward_close:
                break;
            case R.id.btn_reward_action:
                if (listener != null) {
                    listener.onRewardActionClick(this.code, this.tuid, this.transaction);
                }
                break;
            case R.id.iv_reward_tips:
                RewardDialogTipsFragment rewardDialogTipsFragment = RewardDialogTipsFragment.newInstance();
                rewardDialogTipsFragment.show(getActivity().getSupportFragmentManager(), "RewardDialogTipsFragment");
                return;
            case R.id.tv_refresh_reward_price:
                Activity activity = getActivity();
                if (activity instanceof BaseCallActivity) {
                    ((BaseCallActivity) activity).sendIntentRewardMessage(getArguments().getInt("tuid"), getArguments().getString("type"), getArguments().getString("price"));
                }
                return;
        }
        dismiss();
    }

    public interface OnRewardActionListener {
        void onRewardActionClick(String code, int tuid, int transaction);
    }

    public void refreshStatus(String json) {
        try {
            //o.optString("msg"), o.optString("money"), code
            JSONObject o = new JSONObject(json);
            this.code = o.optString("code");
            this.transaction = o.optInt("transaction");
            btn_reward_action.setText("0".equals(code) ? "激发钱能" : "充值");
            tv_reward_message.setText(o.optString("msg"));
            float money = Float.valueOf(o.optString("money")) / 100;
            tv_reward_price.setText("￥" + String.format("%.2f", money));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
