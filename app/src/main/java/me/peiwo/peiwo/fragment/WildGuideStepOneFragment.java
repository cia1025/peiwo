package me.peiwo.peiwo.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.WildcatGuideActivity;
import me.peiwo.peiwo.net.AsynHttpClient;


public class WildGuideStepOneFragment extends Fragment {

    public static WildGuideStepOneFragment newInstance() {

        return new WildGuideStepOneFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View subView = inflater.inflate(R.layout.fragment_wild_guide_step_one, container, false);
        init(subView);
        return subView;
    }

    private void init(View subView) {
        ImageView iv_bg_new_voice = (ImageView) subView.findViewById(R.id.iv_bg_new_voice);
        View btn_match_constellation = subView.findViewById(R.id.btn_match_constellation);
        Activity activity = getActivity();
        if (activity instanceof WildcatGuideActivity) {
            int gender = ((WildcatGuideActivity) activity).getGender();
            if (gender == AsynHttpClient.GENDER_MASK_FEMALE) {
                iv_bg_new_voice.setImageResource(R.drawable.bg_new_voice_female);
                btn_match_constellation.setVisibility(View.VISIBLE);
            } else {
                iv_bg_new_voice.setImageResource(R.drawable.bg_new_voice_male);
                btn_match_constellation.setVisibility(View.INVISIBLE);
            }
        }
    }

}
