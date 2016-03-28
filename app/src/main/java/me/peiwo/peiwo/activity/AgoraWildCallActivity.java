package me.peiwo.peiwo.activity;

import android.animation.*;
import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.percent.PercentFrameLayout;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import com.jakewharton.rxbinding.view.RxView;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.agora.AgoraIntentRewardResponseEvent;
import me.peiwo.peiwo.model.agora.AgoraRewardedEvent;
import me.peiwo.peiwo.model.agora.AgoraWildCallReadyEvent;
import me.peiwo.peiwo.presenter.AgoraWildCallPresenter;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.widget.CallSmothDragView;
import me.peiwo.peiwo.widget.RewardView;
import me.peiwo.peiwo.widget.RewardedView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import java.util.concurrent.TimeUnit;

public class AgoraWildCallActivity extends AgoraCallActivity {

    private Animator matchingAnim;
    //private String channel_id;
    @Bind(R.id.call_smoth_dragview)
    CallSmothDragView call_smoth_dragview;
    @Bind(R.id.iv_matching_anim)
    View iv_matching_anim;
    @Bind(R.id.tv_network_tips)
    TextView tv_network_tips;
    @Bind(R.id.v_call_control)
    View v_call_control;
    @Bind(R.id.tv_request_friend)
    View tv_request_friend;
    @Bind(R.id.iv_remote_avatar)
    ImageView iv_remote_avatar;
    @Bind(R.id.v_connecting)
    View v_connecting;
    @Bind(R.id.tv_timing)
    TextView tv_timing;
    @Bind(R.id.tv_nickname)
    TextView tv_nickname;
    @Bind(R.id.tv_tag)
    TextView tv_tag;
    @Bind(R.id.v_tag_and_timer)
    View v_tag_and_timer;
    @Bind(R.id.v_wild_style)
    View v_wild_style;
    @Bind(R.id.tv_prompt)
    TextView tv_prompt;
    @Bind(R.id.iv_wild_tag)
    ImageView iv_wild_tag;
    @Bind(R.id.tv_like_control)
    View tv_like_control;
    @Bind(R.id.tv_mute_control)
    View tv_mute_control;
    @Bind(R.id.tv_audio_control)
    View tv_audio_control;
    @Bind(R.id.tv_timing_indi)
    TextView tv_timing_indi;
    @Bind(R.id.iv_ads)
    ImageView iv_ads;
    @Bind(R.id.v_iv_ads_container)
    View v_iv_ads_container;
    @Bind(R.id.v_pay_phone)
    View v_pay_phone;
    @Bind(R.id.self_view)
    PercentFrameLayout self_view;

    @Bind(R.id.v_black_screen)
    View v_black_screen;

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
    public void onResume() {
        super.onResume();
        //call_smoth_dragview.reset();
        //call_smoth_dragview.invalidate();
        presenter.cancelNotification();
    }

    @Override
    public void onBackPressed() {
        if (hasRewardView()) {
            removeRewardView();
        } else if (hasRewardedView()) {
            removeRewardedView();
        } else {
            presenter.quit();
        }
        //super.onBackPressed();
    }


    private void init() {
        LayoutTransition transition = new LayoutTransition();
        transition.setAnimator(LayoutTransition.APPEARING, transition.getAnimator(LayoutTransition.APPEARING));
        self_view.setLayoutTransition(transition);

        v_iv_ads_container.setVisibility(View.GONE);
        startMatchingAnim();
        call_smoth_dragview.setCallDragListener(this::handleDragState);
        changeViewWithMatching();
        setActionControlClicks();
    }

    private void setActionControlClicks() {
        Subscription subscription1 = RxView.clicks(tv_mute_control).throttleFirst(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aVoid -> {
            tv_mute_control.setSelected(!tv_mute_control.isSelected());
            if (tv_mute_control.isSelected()) {
                presenter.mute();
            } else {
                presenter.unmute();
            }
        });
        presenter.addSubscription(subscription1);
        Subscription subscription2 = RxView.clicks(tv_audio_control).throttleFirst(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aVoid -> {
            tv_audio_control.setSelected(!tv_audio_control.isSelected());
            if (tv_audio_control.isSelected()) {
                presenter.handsOff();
            } else {
                presenter.handsFree();
            }
        });
        presenter.addSubscription(subscription2);
        View tv_dashang_control = findViewById(R.id.tv_dashang_control);
        Subscription subscription3 = RxView.clicks(tv_dashang_control).throttleFirst(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aVoid -> {
            if (hasRewardView() || hasRewardedView()) return;
            presenter.sendWildCallIntentRewardMessage();
        });
        presenter.addSubscription(subscription3);
    }

    public void changeViewWithMatching() {
        call_smoth_dragview.enableDrag(false);
        removeRewardView();
        removeRewardedView();
        iv_matching_anim.setVisibility(View.VISIBLE);
        v_connecting.setVisibility(View.VISIBLE);
        v_call_control.setVisibility(View.GONE);
        v_tag_and_timer.setVisibility(View.GONE);
        tv_network_tips.setVisibility(View.GONE);
        v_wild_style.setVisibility(View.GONE);
        tv_request_friend.setEnabled(true);
        tv_like_control.setEnabled(true);
    }

    private void computeLayoutAvatar() {
        v_call_control.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                v_call_control.getViewTreeObserver().removeOnPreDrawListener(this);
                int width = tv_request_friend.getWidth();
                int pad = tv_request_friend.getLeft() + width / 2;
                int marginLeft = pad - iv_remote_avatar.getWidth() / 2;
                PercentRelativeLayout.LayoutParams params = (PercentRelativeLayout.LayoutParams) iv_remote_avatar.getLayoutParams();
                params.setMargins(marginLeft, 0, 0, 0);
                iv_remote_avatar.setLayoutParams(params);
                return true;
            }
        });
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

            case R.id.iv_wild_tag:
                animWildStyle();
                break;
            case R.id.tv_quit_control:
                quitControl();
                break;
            case R.id.tv_like_control:
                likeRemoteUser(v);
                break;
            case R.id.iv_remote_avatar:
                presenter.requestFriend();
                break;
            case R.id.v_close_ads:
                hideWildCallAds();
                break;
            case R.id.v_pay_phone:
                presenter.quit();
                toast("收费电话");
                break;
        }
    }

    private void likeRemoteUser(View v) {
        v.setSelected(true);
        v.setEnabled(false);
        presenter.sendLikeMessage();
    }

    private void quitControl() {
        new AlertDialog.Builder(this).setTitle("再见")
                .setItems(new CharSequence[]{"枪毙", "换人", "取消"}, (dialog, which) -> {
                    if (which == 0) {
                        presenter.accusationUser();
                        presenter.substitutionOfUser();
                    } else if (which == 1) {
                        presenter.substitutionOfUser();
                    }
                }).create().show();
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

    public void showNetWorkTips(String tips) {
        if (tv_network_tips.getVisibility() == View.VISIBLE) {
            tv_network_tips.setText(tips);
            return;
        }
        tv_network_tips.setVisibility(View.VISIBLE);
        tv_network_tips.setAlpha(1.0f);
        tv_network_tips.setText(tips);
        tv_network_tips.animate().alpha(0.0f).setDuration(2000).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                tv_network_tips.setVisibility(View.GONE);
            }
        }).start();
    }

    public void changeViewWithMatched(String remote_avatar) {
        v_connecting.setVisibility(View.GONE);
        iv_matching_anim.setVisibility(View.GONE);
        v_call_control.setVisibility(View.VISIBLE);
        v_tag_and_timer.setVisibility(View.VISIBLE);
        v_wild_style.setVisibility(View.VISIBLE);
        if (tv_request_friend.getLeft() == 0)
            computeLayoutAvatar();
        ImageLoader.getInstance().displayImage(remote_avatar, iv_remote_avatar);
        call_smoth_dragview.enableDrag(true);
    }

    public void setTimerText(String time) {
        tv_timing.setText(time);
    }

    public void setRemoteNickName(String nickname) {
        tv_nickname.setText(nickname);
    }

    public void setRemoteTags(String tag) {
        tv_tag.setText(tag);
    }

    public void setWildStyle(AgoraWildCallReadyEvent.Hint hint) {
        v_wild_style.setTranslationX(0f);
        switch (hint.style) {
            case 1:
                tv_prompt.setBackgroundResource(R.drawable.bg_wild_tag_yellow);
                iv_wild_tag.setImageResource(R.drawable.ic_wild_tag_bad);
                break;
            case 2:
                tv_prompt.setBackgroundResource(R.drawable.bg_wild_tag_green);
                iv_wild_tag.setImageResource(R.drawable.ic_wild_tag_good);
                break;
        }
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        iv_wild_tag.measure(w, h);
        int pad = iv_wild_tag.getMeasuredWidth();
        tv_prompt.setPadding(pad, 0, pad / 3, 0);
        tv_prompt.setText(hint.msg);
        Subscription subscription = Observable.timer(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            animWildStyle();
        });
        presenter.addSubscription(subscription);
    }

    private void animWildStyle() {
        int offset = v_wild_style.getMeasuredWidth() - iv_wild_tag.getMeasuredWidth();
        if (v_wild_style.getTranslationX() == 0) {
            ObjectAnimator.ofFloat(v_wild_style, "translationX", 0f, offset).start();
        } else {
            ObjectAnimator.ofFloat(v_wild_style, "translationX", offset, 0f).start();
        }
    }

    public boolean hasRewardView() {
        return findViewById(R.id.reward_view_id) != null;
    }

    public boolean hasRewardedView() {
        return findViewById(R.id.rewarded_view_id) != null;
    }

    public void reAssignRewardViewValue(AgoraIntentRewardResponseEvent event) {
        View v = findViewById(R.id.reward_view_id);
        if (v != null) {
            RewardView rewardView = (RewardView) v;
            rewardView.display(event);
        }
    }

    public void showRewardView(AgoraIntentRewardResponseEvent event) {
        RewardView rewardView = new RewardView(this, event);
        PercentFrameLayout.LayoutParams layoutParams = new PercentFrameLayout.LayoutParams((int) (PWUtils.getWindowWidth(this) * 0.9), ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        self_view.addView(rewardView, layoutParams);
        //rewardView.display(event);
    }

    public void removeRewardView() {
        self_view.removeView(findViewById(R.id.reward_view_id));
    }

    public void removeRewardedView() {
        self_view.removeView(findViewById(R.id.rewarded_view_id));
    }

    @Override
    public void refreshRewardPrice() {
        presenter.sendWildCallIntentRewardMessage();
    }

    @Override
    public void charge() {
        presenter.charge();
    }

    @Override
    public void payReward(int transaction) {
        presenter.sendPayRewardMessage(transaction);
    }

    @Override
    public void returnASalute() {
        presenter.sendWildCallIntentRewardMessage();
    }

    public void reAssignRewardedViewValue(AgoraRewardedEvent event) {
        View v = findViewById(R.id.rewarded_view_id);
        if (v != null) {
            RewardedView rewardedView = (RewardedView) v;
            rewardedView.display(event);
        }
    }

    public void showRewardedView(AgoraRewardedEvent event) {
        RewardedView rewardedView = new RewardedView(this, event);
        PercentFrameLayout.LayoutParams layoutParams = new PercentFrameLayout.LayoutParams((int) (PWUtils.getWindowWidth(this) * 0.9), ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        self_view.addView(rewardedView, layoutParams);
    }

    public void showMuteControl() {
        tv_mute_control.setVisibility(View.VISIBLE);
    }

    public void hideMuteControl() {
        tv_mute_control.setVisibility(View.INVISIBLE);
    }

    public void setHeadSetOnView() {
        tv_audio_control.setSelected(true);
    }

    public void setHeadSetOffView() {
        tv_audio_control.setSelected(false);
    }

    public void setTimeIndiText(String text) {
        tv_timing_indi.setText(text);
    }

    @Override
    public void showRangeBlack() {
        v_black_screen.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideRangeBlack() {
        v_black_screen.setVisibility(View.GONE);
    }

    @Override
    public void enableDrag(boolean enable) {
        call_smoth_dragview.enableDrag(enable);
    }

    public void showWildCallAd(String ad) {
        v_iv_ads_container.setVisibility(View.VISIBLE);
        ImageLoader.getInstance().displayImage(ad, iv_ads);
    }

    public void hideWildCallAds() {
        iv_ads.setImageBitmap(null);
        v_iv_ads_container.setVisibility(View.GONE);
    }

    public void showPayPhoneView() {
        v_pay_phone.setVisibility(View.VISIBLE);
    }

    public void hidePayPhoneView() {
        v_pay_phone.setVisibility(View.GONE);
    }
}
