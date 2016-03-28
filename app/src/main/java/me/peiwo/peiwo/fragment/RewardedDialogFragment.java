package me.peiwo.peiwo.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.R;

/**
 * Created by fuhaidong on 15/9/15.
 */
public class RewardedDialogFragment extends DialogFragment implements View.OnClickListener {

    private OnRewardToYouListener listener;

    public static RewardedDialogFragment newInstance(String avatar, String reward_from, String reward_message, String reward_price) {
        RewardedDialogFragment fragment = new RewardedDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("avatar", avatar);
        bundle.putString("reward_from", reward_from);
        bundle.putString("msg", reward_message);
        bundle.putString("price", reward_price);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        RelativeLayout rootView = new RelativeLayout(getActivity());
        View v = getActivity().getLayoutInflater().inflate(R.layout.layout_rewarded_dialog, rootView, true);
        //rootView.addView(v);
        ImageView iv_avatar = (ImageView) v.findViewById(R.id.iv_avatar);
        TextView tv_reward_from = (TextView) v.findViewById(R.id.tv_reward_from);
        tv_reward_from.setText(getArguments().getString("reward_from") + "给我打赏");
        ImageLoader.getInstance().displayImage(getArguments().getString("avatar"), iv_avatar);
        TextView tv_reward_message = (TextView) v.findViewById(R.id.tv_reward_message);
        TextView tv_reward_price = (TextView) v.findViewById(R.id.tv_reward_price);
        Button btn_reward_toyou = (Button) v.findViewById(R.id.btn_reward_toyou);
        v.findViewById(R.id.iv_reward_close).setOnClickListener(this);
        btn_reward_toyou.setOnClickListener(this);
        if (getArguments() != null) {
            tv_reward_message.setText(getArguments().getString("msg"));
            float money = Float.valueOf(getArguments().getString("price")) / 100;
            tv_reward_price.setText("￥" + String.format("%.2f", money));
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);
        return builder.create();
    }

    public void setOnRewardToYouListener(OnRewardToYouListener listener) {
        this.listener = listener;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_reward_close:
                break;
            case R.id.btn_reward_toyou:
                if (listener != null) {
                    listener.onRewardToYouClick();
                }
                break;
        }
        dismiss();
    }

    public interface OnRewardToYouListener {
        void onRewardToYouClick();
    }
}
