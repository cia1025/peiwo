package me.peiwo.peiwo.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import butterknife.Bind;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.presenter.AgoraWildCallPresenter;
import me.peiwo.peiwo.widget.CallSmothDragView;

public class AgoraWildCallActivity extends AgoraCallActivity {

    private AnimatorSet matchingAnim;
    //private String channel_id;
    @Bind(R.id.call_smoth_dragview)
    CallSmothDragView call_smoth_dragview;
    @Bind(R.id.iv_matching_anim)
    View iv_matching_anim;

    private AgoraWildCallPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agora_wild_call);
        presenter = new AgoraWildCallPresenter(this);
        init();
        presenter.init();
    }


    @Override
    public void onBackPressed() {
        presenter.quit();
        //super.onBackPressed();
    }


    private void init() {
        startMatchingAnim();
        call_smoth_dragview.setCallDragListener(this::handleDragState);
    }

    public void startMatchingAnim() {
        ObjectAnimator animatorXEnlarge = ObjectAnimator.ofFloat(iv_matching_anim, "scaleX", 1.0f, 1.5f);
        animatorXEnlarge.setRepeatCount(ValueAnimator.INFINITE);
        animatorXEnlarge.setRepeatMode(ValueAnimator.REVERSE);
        animatorXEnlarge.setDuration(2000);

        ObjectAnimator animatorYEnlarge = ObjectAnimator.ofFloat(iv_matching_anim, "scaleY", 1.0f, 1.5f);
        animatorYEnlarge.setRepeatCount(ValueAnimator.INFINITE);
        animatorYEnlarge.setRepeatMode(ValueAnimator.REVERSE);
        animatorYEnlarge.setDuration(2000);

        ObjectAnimator animatorXShrink = ObjectAnimator.ofFloat(iv_matching_anim, "scaleX", 1.5f, 1.0f);
        animatorXShrink.setRepeatCount(ValueAnimator.INFINITE);
        animatorXShrink.setRepeatMode(ValueAnimator.REVERSE);
        animatorXShrink.setDuration(1000);

        ObjectAnimator animatorYShrink = ObjectAnimator.ofFloat(iv_matching_anim, "scaleY", 1.5f, 1.0f);
        animatorYShrink.setRepeatCount(ValueAnimator.INFINITE);
        animatorYShrink.setRepeatMode(ValueAnimator.REVERSE);
        animatorYShrink.setDuration(1000);

//        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(iv_matching_anim, "alpha", 1.0f, 0.2f);
//        animatorAlpha.setRepeatCount(ValueAnimator.INFINITE);
//        animatorAlpha.setRepeatMode(ValueAnimator.REVERSE);
        AnimatorSet animatorSet1 = new AnimatorSet();
        animatorSet1.playTogether(animatorXEnlarge, animatorYEnlarge);
        AnimatorSet animatorSet2 = new AnimatorSet();
        animatorSet2.playTogether(animatorXShrink, animatorYShrink);

        matchingAnim = new AnimatorSet();
        matchingAnim.playSequentially(animatorSet1, animatorSet2);
        matchingAnim.setInterpolator(new DecelerateInterpolator());
        matchingAnim.start();
    }

    private void handleDragState(int state) {
        presenter.handleDragState(state);
    }


    @Override
    protected void onDestroy() {
        presenter.onDestory();
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_quit:
                onBackPressed();
//                if (v.getTag() == null) {
//                    presenter.pauseMatchingAnimator();
//                    v.setTag("haha");
//                } else {
//                    presenter.resumeMatchingAnimator();
//                    v.setTag(null);
//                }
                break;

            default:
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void pauseAnimator() {
        if (matchingAnim != null && !matchingAnim.isPaused()) {
            matchingAnim.pause();
        }
    }

    public void stopMatchingAnimator() {
        if (matchingAnim != null) {
            matchingAnim.end();
            matchingAnim.cancel();
        }
        matchingAnim = null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void resumeAnimator() {
        if (matchingAnim != null && matchingAnim.isPaused()) {
            matchingAnim.resume();
        }
    }
}
