package me.peiwo.peiwo.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import butterknife.Bind;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.presenter.AgoraWildCallPresenter;
import me.peiwo.peiwo.widget.CallSmothDragView;

public class AgoraWildCallActivity extends AgoraCallActivity {

    private Animator matchingAnim;
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
        try {
            matchingAnim = AnimatorInflater.loadAnimator(this, R.animator.anim_wild_breath);
            matchingAnim.setTarget(iv_matching_anim);
            matchingAnim.start();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
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
                presenter.quit();
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
