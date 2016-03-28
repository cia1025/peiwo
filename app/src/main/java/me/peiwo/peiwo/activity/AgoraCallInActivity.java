package me.peiwo.peiwo.activity;

import android.os.Bundle;
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
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.agora.AgoraIntentRewardResponseEvent;
import me.peiwo.peiwo.model.agora.AgoraRewardedEvent;
import me.peiwo.peiwo.net.NetUtil;
import me.peiwo.peiwo.presenter.AgoraCallInPresenter;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.RxRangeSensor;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.CallSmothDragView;
import me.peiwo.peiwo.widget.RewardView;
import me.peiwo.peiwo.widget.RewardedView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class AgoraCallInActivity extends AgoraCallActivity {
    public static final String K_CALLED_EVENT = "called";

    private AgoraCallInPresenter presenter;

    @Bind(R.id.call_smoth_dragview_in)
    CallSmothDragView call_smoth_dragview_in;
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
    @Bind(R.id.ll_answer_call)
    LinearLayout ll_answer_call;
    @Bind(R.id.iv_answer_call)
    ImageView iv_answer_call;
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
    @Bind(R.id.tv_network_tips)
    TextView tv_network_tips;
    @Bind(R.id.iv_push_top)
    ImageView iv_push_top;
    @Bind(R.id.self_view)
    PercentFrameLayout self_view;

    @Bind(R.id.v_black_screen)
    View v_black_screen;


    private String currentDuration = "00:00:00";
    private ImageLoader imageLoader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setContentView(R.layout.activity_agora_call_in);
        presenter = new AgoraCallInPresenter(this);
        init();
        presenter.init();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.cancelNotification();
    }

    private void init() {
        call_smoth_dragview_in.setCallDragListener(this::handleDragState);
        imageLoader = ImageLoader.getInstance();
        setActionControlClicks();

        Subscription subscribe = RxRangeSensor.postEvent(this).debounce(500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean isNear) {
                if (isNear) v_black_screen.setVisibility(View.VISIBLE);
                else v_black_screen.setVisibility(View.GONE);
            }
        });

        presenter.addSubscription(subscribe);

    }

    //接通前初始化界面
    public void initConnectingView() {
        setActionContainerVisible(View.GONE);
        setAnswerCallVisibile(View.VISIBLE);
        setCallDuration(getResources().getString(R.string.is_calling));
        setDurationContainerClickable(false);
        setChargeGuide();
    }

    private void setActionControlClicks() {

        Subscription durationSubscription = RxView.clicks(fl_duration_container).throttleFirst(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                handleDurationShow();
            }
        });

        presenter.addSubscription(durationSubscription);

        Subscription userInfoSubscription = RxView.clicks(ll_enter_userinfo).throttleFirst(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                presenter.startUserInfoActivity();
            }
        });

        presenter.addSubscription(userInfoSubscription);

        Subscription feedMoneySubscription = RxView.clicks(tv_feed_money).throttleFirst(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                if (hasRewardView() || hasRewardedView()) return;
                presenter.sendCallInIntentRewardMessage();
            }
        });

        presenter.addSubscription(feedMoneySubscription);

        Subscription voiceModelSubscription = RxView.clicks(tv_voice_mode).throttleFirst(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Void>() {
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

        Subscription handupSubscription = RxView.clicks(iv_hangup).throttleFirst(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aVoid -> {
            presenter.rejectCall();
        });

        presenter.addSubscription(handupSubscription);

        Subscription answerCallSubscription = RxView.clicks(iv_answer_call).throttleFirst(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                presenter.answerCall();
            }
        });

        presenter.addSubscription(answerCallSubscription);
    }

    public void initJoinedView() {
        setAnswerCallVisibile(View.GONE);
        setActionContainerVisible(View.VISIBLE);
        setDurationOptionVisible(View.VISIBLE);
        setDurationContainerClickable(true);
        setCallDuration(getResources().getString(R.string.is_speaking));
    }

    public void setAnswerCallVisibile(int visibile) {
        ll_answer_call.setVisibility(visibile);
    }

    public void setDurationOptionVisible(int visible) {
        iv_duration_option.setVisibility(visible);
    }

    public void handleDurationShow() {
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

    private void handleDragState(int state) {
        presenter.handleDragState(state);
    }

    public void setDurationContainerClickable(boolean clickable) {
        fl_duration_container.setClickable(clickable);
    }

    public void setActionContainerVisible(int isVisible) {
        ll_action_container.setVisibility(isVisible);
    }

    public void setTags(String[] tags) {
        if (tags != null && tags.length > 0) {
            tv_tag.setText("#" + tags[0]);
        } else {
            tv_tag.setText("");
        }
    }

    public void setCalleeImg(String face_url) {
        imageLoader.displayImage(face_url, iv_start_userinfo);
    }

    public void setNickName(int callee_id, String callee_name) {
        String realName = UserManager.getRealName(callee_id, callee_name, this);
        if (!TextUtils.isEmpty(realName)) {
            tv_nickname.setText(realName);
        } else {
            tv_nickname.setText("匿名");
        }
    }

    public void setChargeGuide() {
        switch (PeiwoApp.getApplication().getNetType()) {
            case NetUtil.WIFI_NETWORK:
                tv_charge_free_guide.setText(getString(R.string.calling_outgoing_wifi));
                break;
            default:
                tv_charge_free_guide.setText(getString(R.string.calling_outgoing_mobile_network));
                break;
        }
    }

    public void animChargeGuide() {
        tv_charge_free_guide.animate().alpha(0.0f).start();
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


    @Override
    protected void onDestroy() {
        presenter.onDestory();
        super.onDestroy();
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
    }

    public void removeRewardView() {
        self_view.removeView(findViewById(R.id.rewarded_view_id));
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

    private void removeRewardedView() {
        self_view.removeView(findViewById(R.id.rewarded_view_id));
    }


    @Override
    public void refreshRewardPrice() {
        presenter.sendCallInIntentRewardMessage();
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
        presenter.sendCallInIntentRewardMessage();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
