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


public class WildGuideStepThreeFragment extends Fragment {

    public static WildGuideStepThreeFragment newInstance() {

        return new WildGuideStepThreeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wild_guide_step_three, container, false);
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            View parent = getView();
            if (parent != null) {
                View v_ind1 = parent.findViewById(R.id.v_ind1);
                View v_src1 = parent.findViewById(R.id.v_src1);
                v_src1.setVisibility(View.VISIBLE);
                ObjectAnimator animator_src1 = ObjectAnimator.ofFloat(v_src1, "alpha", 0.0f, 1.0f);
                animator_src1.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v_ind1.setVisibility(View.VISIBLE);
                        ObjectAnimator animator_ind1 = ObjectAnimator.ofFloat(v_ind1, "alpha", 0.0f, 1.0f);
                        animator_ind1.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                anim2(parent);
                            }
                        });
                        animator_ind1.setDuration(300).start();
                    }
                });
                animator_src1.setDuration(500).start();
            }
        }
    }

    private void anim2(View parent) {
        View v_ind2 = parent.findViewById(R.id.v_ind2);
        View v_src2 = parent.findViewById(R.id.v_src2);
        v_src2.setVisibility(View.VISIBLE);
        ObjectAnimator animator_src2 = ObjectAnimator.ofFloat(v_src2, "alpha", 0.0f, 1.0f);
        animator_src2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v_ind2.setVisibility(View.VISIBLE);
                ObjectAnimator.ofFloat(v_ind2, "alpha", 0.0f, 1.0f).setDuration(300).start();
            }
        });
        animator_src2.setDuration(500).start();
    }

}
