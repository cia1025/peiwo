package me.peiwo.peiwo.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import me.peiwo.peiwo.R;


public class WildGuideStepFourFragment extends Fragment {

    public static WildGuideStepFourFragment newInstance() {

        return new WildGuideStepFourFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wild_guide_step_four, container, false);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            View parent = getView();
            if (parent != null) {
                View v_ind = parent.findViewById(R.id.v_ind);
                View v_src = parent.findViewById(R.id.v_src);
                v_src.setVisibility(View.VISIBLE);
                ObjectAnimator animator_src = ObjectAnimator.ofFloat(v_src, "alpha", 0.0f, 1.0f);
                animator_src.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v_ind.setVisibility(View.VISIBLE);
                        ObjectAnimator.ofFloat(v_ind, "alpha", 0.0f, 1.0f).setDuration(300).start();
                    }
                });
                animator_src.setDuration(500).start();
            }
        }
    }

}
