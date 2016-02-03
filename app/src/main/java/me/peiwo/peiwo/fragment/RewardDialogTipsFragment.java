package me.peiwo.peiwo.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import me.peiwo.peiwo.R;

/**
 * Created by fuhaidong on 15/9/15.
 */
public class RewardDialogTipsFragment extends DialogFragment implements View.OnClickListener {

    public static RewardDialogTipsFragment newInstance() {

        return new RewardDialogTipsFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.layout_rewardtips_dialog, null);


        v.findViewById(R.id.iv_reward_close).setOnClickListener(this);
        v.findViewById(R.id.btn_close).setOnClickListener(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        return builder.create();
    }


    @Override
    public void onClick(View v) {
//        int id = v.getId();
//        switch (id) {
//            case R.id.iv_reward_close:
//                break;
//            case R.id.btn_reward_action:
//                break;
//        }
        dismiss();
    }

}
