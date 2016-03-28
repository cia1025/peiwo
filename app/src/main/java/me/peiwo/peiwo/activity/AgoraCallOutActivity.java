package me.peiwo.peiwo.activity;

import android.os.Bundle;
<<<<<<< HEAD
import android.support.percent.PercentFrameLayout;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import de.hdodenhof.circleimageview.CircleImageView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.agora.AgoraIntentRewardResponseEvent;
import me.peiwo.peiwo.model.agora.AgoraRewardedEvent;
import me.peiwo.peiwo.presenter.AgoraCallOutPresenter;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.RxRangeSensor;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.CallSmothDragView;
import me.peiwo.peiwo.widget.RewardView;
import me.peiwo.peiwo.widget.RewardedView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
=======
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import com.jakewharton.rxbinding.view.RxView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.presenter.AgoraCallOutPresenter;
import rx.android.schedulers.AndroidSchedulers;

import java.util.concurrent.TimeUnit;
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198

public class AgoraCallOutActivity extends AgoraCallActivity {
    public static final String K_CALLEE_ID = "callee_id";
    public static final String K_CHANNEL = "channel";
    public static final String K_CHANNEL_ID = "channel_id";
    public static final String K_PRICE = "price";

    private AgoraCallOutPresenter presenter;

<<<<<<< HEAD
    @Bind(R.id.call_smoth_dragview_out)
    CallSmothDragView call_smoth_dragview_out;
    @Bind(R.id.tv_nickname)
    TextView tv_nickname;
    @Bind(R.id.tv_tag)
    TextView tv_tag;
    @Bind(R.id.fl_duration_container)
    LinearLayout fl_duration_container;
    @Bind(R.id.tv_call_duration)
    TextView tv_call_duration;
    @Bind(R.id.iv_duration_option)
    ImageView iv_duration_option;
    @Bind(R.id.tv_charge_free_guide)
    TextView tv_charge_free_guide;
    @Bind(R.id.tv_callstate)
    TextView tv_callstate;
    @Bind(R.id.ll_hangup_container)
    LinearLayout ll_hangup_container;
    @Bind(R.id.iv_hangup)
    ImageView iv_hangup;
    @Bind(R.id.ll_action_container)
    LinearLayout ll_action_container;
    @Bind(R.id.ll_enter_userinfo)
    LinearLayout ll_enter_userinfo;
    @Bind(R.id.iv_start_userinfo)
    CircleImageView iv_start_userinfo;
    @Bind(R.id.tv_feed_money)
    TextView tv_feed_money;
    @Bind(R.id.tv_voice_mode)
    TextView tv_voice_mode;
    @Bind(R.id.iv_push_top)
    ImageView iv_push_top;
    @Bind(R.id.self_view)
    PercentFrameLayout self_view;

    @Bind(R.id.v_black_screen)
    View v_black_screen;

    private String currentDuration = "00:00:00";
    private ImageLoader imageLoader;
=======
    @Bind(R.id.btn_hungup)
    Button btn_hungup;
    @Bind(R.id.tv_caller)
    TextView tv_caller;
    @Bind(R.id.tv_callee)
    TextView tv_callee;
    @Bind(R.id.btn_audio_mode)
    Button btn_audio_mode;
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
<<<<<<< HEAD
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
=======
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
        setContentView(R.layout.activity_call_out);
        presenter = new AgoraCallOutPresenter(this);
        init();
        presenter.init();
    }

<<<<<<< HEAD
    @Override
    public void onResume() {
        super.onResume();
        presenter.cancelNotification();
    }


    private void init() {
        call_smoth_dragview_out.setCallDragListener(AgoraCallOutActivity.this::handleDragState);
        imageLoader = ImageLoader.getInstance();
        setActionControlClicks();
        RxRangeSensor.postEvent(this).debounce(500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean isNear) {
                if (isNear) v_black_screen.setVisibility(View.VISIBLE);
                else v_black_screen.setVisibility(View.GONE);
            }
        });
    }

    public void initConnectingView() {
        setActionContainerVisible(View.GONE);
        setCallDuration(getResources().getString(R.string.is_calling));
        setDurationContainerClickable(false);
    }

    private void handleDragState(int state) {
        presenter.handleDragState(state);
    }


    private void setActionControlClicks() {

        Subscription handupSubscription = RxView.clicks(iv_hangup).throttleFirst(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aVoid -> {
            presenter.hungUp(true);
        });

        presenter.addSubscription(handupSubscription);

        Subscription durationSubscription = RxView.clicks(fl_duration_container).throttleFirst(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                handleDurationShow();
            }
        });

        presenter.addSubscription(durationSubscription);

        Subscription userInfoSubscription = RxView.clicks(ll_enter_userinfo).throttleFirst(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                presenter.startUserInfoActivity();
            }
        });

        presenter.addSubscription(userInfoSubscription);

        Subscription feedMoneySubscription = RxView.clicks(tv_feed_money).throttleFirst(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                if (hasRewardView() || hasRewardedView()) return;
                presenter.sendCallOutIntentRewardMessage();
            }
        });

        presenter.addSubscription(feedMoneySubscription);

        Subscription voiceModelSubscription = RxView.clicks(tv_voice_mode).throttleFirst(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                tv_voice_mode.setSelected(!tv_voice_mode.isSelected());
                if (tv_voice_mode.isSelected()) {
                    presenter.handsOff();
                } else {
                    presenter.handsFree();
                }
            }
        });

        presenter.addSubscription(voiceModelSubscription);

        Subscription pushTopSubscription = RxView.clicks(iv_push_top).throttleFirst(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {

            }
        });

        presenter.addSubscription(pushTopSubscription);
    }


    private void handleDurationShow() {
        boolean selected = iv_duration_option.isSelected();
        iv_duration_option.setSelected(!selected);
        if (selected) {
            iv_duration_option.setImageResource(R.drawable.icon_hide_duration);
            tv_call_duration.setText(getResources().getString(R.string.is_speaking));
            tv_call_duration.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        } else {
            iv_duration_option.setImageResource(R.drawable.icon_show_duration);
            tv_call_duration.setText(currentDuration);
            tv_call_duration.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        }
    }

    public void setCallDuration(String durationText) {
        currentDuration = durationText;
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (hasRewardView()) {
            removeRewardView();
        } else if (hasRewardedView()) {
            removeRewardedView();
        } else {
            presenter.quit();
        }
    }

    private void removeRewardedView() {
        self_view.removeView(findViewById(R.id.rewarded_view_id));
=======
    private void init() {
        setUpInitView();
    }

    private void setUpInitView() {
        setAudioModeText("现在是扬声器模式");
        setAudioViewEnable(false);
        RxView.clicks(btn_hungup).throttleFirst(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aVoid -> {
            presenter.hungUp(true);
        });
        RxView.clicks(btn_audio_mode).throttleFirst(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aVoid -> {
            Object tag = btn_audio_mode.getTag();
            presenter.handleAudioMode(tag);
        });
    }

    @Override
    public void left_click(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        presenter.hungUp(true);
        //super.onBackPressed();
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
    }

    @Override
    protected void onDestroy() {
        presenter.onDestory();
        super.onDestroy();
    }

<<<<<<< HEAD

    public void setDurationContainerClickable(boolean clickable) {
        fl_duration_container.setClickable(clickable);
    }

    public void setActionContainerVisible(int isVisible) {
        ll_action_container.setVisibility(isVisible);
    }

    public void setNickName(int callee_id, String callee_name) {
        String realName = UserManager.getRealName(callee_id, callee_name, this);
        if (!TextUtils.isEmpty(realName)) {
            tv_nickname.setText(realName);
        } else {
            tv_nickname.setText("匿名");
        }
    }

    public void setTags(String[] tags) {
        if (tags != null && tags.length > 0) {
            tv_tag.setText("#" + tags[0]);
        } else {
            tv_tag.setText("");
        }
    }


    public void setFreeGuardText(String text) {
        tv_charge_free_guide.setText(text);
    }


    public void animateChargeGuide() {
        tv_charge_free_guide.animate().alpha(0.0f).start();
    }


    public void setCalleeImg(String imgUrl) {
        imageLoader.displayImage(imgUrl, iv_start_userinfo);
    }


    public boolean hasRewardView() {
        return findViewById(R.id.reward_view_id) != null;
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
    }

    public void removeRewardView() {
        self_view.removeView(findViewById(R.id.reward_view_id));
    }

    public boolean hasRewardedView() {
        return findViewById(R.id.rewarded_view_id) != null;
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void initJoinedView() {
        setActionContainerVisible(View.VISIBLE);
        setDurationOptionVisible(View.VISIBLE);
        setDurationContainerClickable(true);
        setCallDuration(getResources().getString(R.string.is_speaking));
    }

    private void setDurationOptionVisible(int visible) {
        iv_duration_option.setVisibility(visible);
    }

    @Override
    public void refreshRewardPrice() {
        presenter.sendCallOutIntentRewardMessage();
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
        presenter.sendCallOutIntentRewardMessage();
    }


=======
    public void setCallerText(String text) {
        tv_caller.setText(text);
    }

    public void setCalleeText(String text) {
        tv_callee.setText(text);
    }

    public void setAudioModeText(String text) {
        btn_audio_mode.setText(text);
    }

    public void setAudioViewEnable(boolean enable) {
        btn_audio_mode.setEnabled(enable);
    }

    public void setHungUpViewEnable(boolean enable) {
        btn_hungup.setEnabled(enable);
    }


    public void setAudioTag(Object o) {
        btn_audio_mode.setTag(o);
    }
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
}
