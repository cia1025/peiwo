package me.peiwo.peiwo.presenter;

<<<<<<< HEAD
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.widget.ViewDragHelper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

=======
import android.content.Intent;
import android.util.Log;
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
import io.agora.rtc.Constants;
import me.peiwo.peiwo.BuildConfig;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
<<<<<<< HEAD
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.RxBus;
import me.peiwo.peiwo.activity.AgoraCallOutActivity;
import me.peiwo.peiwo.activity.ChargeActivity;
import me.peiwo.peiwo.activity.UserInfoActivity;
import me.peiwo.peiwo.model.agora.AgoraCallEvent;
import me.peiwo.peiwo.model.agora.AgoraCallResponseEvent;
import me.peiwo.peiwo.model.agora.AgoraConnectionInterruptedEvent;
import me.peiwo.peiwo.model.agora.AgoraConnectionLostEvent;
import me.peiwo.peiwo.model.agora.AgoraHungUpByServEvent;
import me.peiwo.peiwo.model.agora.AgoraIntentRewardResponseEvent;
import me.peiwo.peiwo.model.agora.AgoraJoinChannelSuccessEvent;
import me.peiwo.peiwo.model.agora.AgoraOnLeaveChannelEvent;
import me.peiwo.peiwo.model.agora.AgoraPayRewardResponseEvent;
import me.peiwo.peiwo.model.agora.AgoraReJoinChannelSuccessEvent;
import me.peiwo.peiwo.model.agora.AgoraRewardedEvent;
import me.peiwo.peiwo.model.agora.AgoraStopCallResponseEvent;
import me.peiwo.peiwo.model.agora.AgoraUser;
import me.peiwo.peiwo.model.agora.AgoraUserJoinedEvent;
import me.peiwo.peiwo.model.agora.AgoraUserMuteAudioEvent;
import me.peiwo.peiwo.model.agora.AgoraUserOffineEvent;
import me.peiwo.peiwo.net.NetUtil;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.service.PlayerService;
import me.peiwo.peiwo.util.SharedPreferencesUtil;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.CallSmothDragView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
=======
import me.peiwo.peiwo.RxBus;
import me.peiwo.peiwo.activity.AgoraCallOutActivity;
import me.peiwo.peiwo.model.agora.*;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.util.UserManager;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import java.util.concurrent.TimeUnit;
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198

/**
 * Created by wallace on 16/3/15.
 */
public class AgoraCallOutPresenter extends AgoraCallPresenter {
    private AgoraCallOutActivity activity;
    private int caller_id;
    private int callee_id;
    private int call_id;
    private int call_stop_reason = DfineAction.REAL_STOP_CALL_NORMAL;
<<<<<<< HEAD
=======
    private int channel;
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
    private String channel_id;

    private boolean joined = false;
    private Subscription timer_callee_subscription;
    private Subscription timer_caller_subscription;

<<<<<<< HEAD
    public static final int CALLEE_BUSY = 40005;
    public static final int CALLER_MONEY_LOW = 40001;
    private int callee_gender;
    private boolean canInvalidate = true;
    private boolean isCalling = false;
    private AgoraUser calleeUser;


=======
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
    public AgoraCallOutPresenter(AgoraCallOutActivity activity) {
        super(activity);
        this.activity = activity;
    }

    public void init() {
        setCalling(true, PeiwoApp.CALL_TYPE.CALL_REAL);
        DfineAction.CURRENT_CALL_STATUS = DfineAction.CURRENT_CALL_REAL;
        setUp();
        if (finishIfNeed()) return;
        setUpNextEvent();
        joinChannel(channel_id, "i am " + caller_id, caller_id);
    }

    private void setUp() {
        caller_id = UserManager.getUid(activity);
        Intent intent = activity.getIntent();
        callee_id = intent.getIntExtra(AgoraCallOutActivity.K_CALLEE_ID, 0);
<<<<<<< HEAD
        if (BuildConfig.DEBUG)
            Log.i("agora", "call out caller_id==" + caller_id + "--callee_id==" + callee_id);
        channel = intent.getIntExtra(AgoraCallOutActivity.K_CHANNEL, 0);
        channel_id = intent.getStringExtra(AgoraCallOutActivity.K_CHANNEL_ID);
        activity.initConnectingView();

=======
        if (BuildConfig.DEBUG) Log.i("agora", "call out caller_id==" + caller_id + "--callee_id==" + callee_id);
        activity.setCallerText("caller is " + caller_id);
        activity.setCalleeText("callee is " + callee_id);
        channel = intent.getIntExtra(AgoraCallOutActivity.K_CHANNEL, 0);
        channel_id = intent.getStringExtra(AgoraCallOutActivity.K_CHANNEL_ID);
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
    }

    private void setUpNextEvent() {
        Subscription subscription = RxBus.provider().toObserverable().filter(o -> o instanceof AgoraCallEvent).observeOn(AndroidSchedulers.mainThread()).subscribe(event -> {
            dispatcCallEvent((AgoraCallEvent) event);
        });
        mCompositeSubscription.add(subscription);
    }

    private void dispatcCallEvent(AgoraCallEvent event) {
        if (event instanceof AgoraJoinChannelSuccessEvent) {
            setUpOnJoinChannelSuccess((AgoraJoinChannelSuccessEvent) event);
        } else if (event instanceof AgoraCallResponseEvent) {
            AgoraCallResponseEvent callResponseEvent = (AgoraCallResponseEvent) event;
            call_id = callResponseEvent.call_id;
            setUpCalleeView(callResponseEvent);
        } else if (event instanceof AgoraUserJoinedEvent) {
            setUpUserJoined((AgoraUserJoinedEvent) event);
        } else if (event instanceof AgoraOnLeaveChannelEvent) {
            setUpOnLeave((AgoraOnLeaveChannelEvent) event);
        } else if (event instanceof AgoraUserOffineEvent) {
            setUpOnUserOffine((AgoraUserOffineEvent) event);
        } else if (event instanceof AgoraStopCallResponseEvent) {
            //stop call response
            hungUp(true);
        } else if (event instanceof AgoraHungUpByServEvent) {
            hungUp(false);
        } else if (event instanceof AgoraReJoinChannelSuccessEvent) {
            setUpReJoinChannelSuccess((AgoraReJoinChannelSuccessEvent) event);
        } else if (event instanceof AgoraConnectionInterruptedEvent) {
            setUpConnectionInterruped((AgoraConnectionInterruptedEvent) event);
        } else if (event instanceof AgoraConnectionLostEvent) {
            //lost connection
            setUpConnectLost((AgoraConnectionLostEvent) event);
        } else if (event instanceof AgoraUserMuteAudioEvent) {
            setUpUserMute((AgoraUserMuteAudioEvent) event);
<<<<<<< HEAD
        } else if (event instanceof AgoraIntentRewardResponseEvent) {
            setUpIntentRewardResponse((AgoraIntentRewardResponseEvent) event);
        } else if (event instanceof AgoraPayRewardResponseEvent) {
            setUpPayRewardResponse((AgoraPayRewardResponseEvent) event);
        } else if (event instanceof AgoraRewardedEvent) {
            setUpAgoraRewarded((AgoraRewardedEvent) event);
        }
    }

    private void setUpAgoraRewarded(AgoraRewardedEvent event) {
        if (event.balance != 0)
            UserManager.updateMoney(activity, String.valueOf(event.balance / 100.00f));
        activity.removeRewardView();
        event.remote_avatar = calleeUser.avatar_thumbnail;
        event.nick_name = calleeUser.name;
        event.money_format = String.format("￥%d.%02d", event.money / 100, event.money % 100);
        if (activity.hasRewardedView())
            activity.reAssignRewardedViewValue(event);
        else
            activity.showRewardedView(event);
    }

    private void setUpPayRewardResponse(AgoraPayRewardResponseEvent event) {
        if (event.balance != 0)
            UserManager.updateMoney(activity, String.valueOf(event.balance / 100.00f));
        if (60002 == event.code)
            activity.toast("余额不足");
        else if (event.code == 0)
            activity.toast("打赏成功");
    }

    private void setUpIntentRewardResponse(AgoraIntentRewardResponseEvent event) {
        event.money_format = String.format("￥%d.%02d", event.money / 100, event.money % 100);
        if (activity.hasRewardView())
            activity.reAssignRewardViewValue(event);
        else
            activity.showRewardView(event);
    }

=======
        }
    }

>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
    private void setUpUserMute(AgoraUserMuteAudioEvent event) {
        if (event.uid == caller_id) {
            activity.toast(String.format("caller %s", event.muted ? "开启静音" : "关闭静音"));
        } else {
            activity.toast(String.format("callee %s", event.muted ? "开启静音" : "关闭静音"));
        }
    }

    private void setUpConnectLost(AgoraConnectionLostEvent event) {
        //自己掉线
        //timer delay
        timerCallerTimeout();
    }

    private void timerCallerTimeout() {
<<<<<<< HEAD
        if (timer_caller_subscription != null && !timer_caller_subscription.isUnsubscribed())
            return;
=======
        if (timer_caller_subscription != null && !timer_caller_subscription.isUnsubscribed()) return;
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
        if (BuildConfig.DEBUG) Log.i("agora", "caller " + caller_id + "计时开始");
        timer_caller_subscription = Observable.timer(MAX_DELAY, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            //自己连接超时，退出
            hungUp(true);
        });
        mCompositeSubscription.add(timer_caller_subscription);
    }

    public void onDestory() {
        setCalling(false, PeiwoApp.CALL_TYPE.CALL_NONE);
        DfineAction.CURRENT_CALL_STATUS = DfineAction.CURRENT_CALL_NOT;
<<<<<<< HEAD
        releasePlayer();
        cancelNotification();
//        activity.cancelMonitorTelState();
    }

    private void releasePlayer() {
        PlayerService.getInstance().releasePlayerCommand();
    }


=======
    }

>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
    public boolean finishIfNeed() {
        if (callee_id == 0) {
            activity.finish();
            return true;
        }
        return false;
    }


    public synchronized void hungUp(boolean send_stopcall_message) {
<<<<<<< HEAD
//        activity.setHungUpViewEnable(false);
=======
        activity.setHungUpViewEnable(false);
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
        unsubscribe();
        if (send_stopcall_message)
            sendStopCallMessage();
        leaveChannel();
<<<<<<< HEAD
        isCalling = false;
        activity.finish();
    }

    public void hungUpDealy(long secondTime) {
        Observable.timer(secondTime, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        hungUp(true);
                    }
                });
    }

=======
        activity.finish();
    }

>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
    private void setUpConnectionInterruped(AgoraConnectionInterruptedEvent event) {

    }

    private void sendStopCallMessage() {
        try {
            JSONObject o = new JSONObject();
            o.put("msg_type", DfineAction.MSG_STOPCALL_MESSAGE);
            o.put("call_id", call_id);
            o.put("reason", call_stop_reason);
            TcpProxy.getInstance().sendTCPMessage(o.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setUpCalleeView(AgoraCallResponseEvent event) {
<<<<<<< HEAD
        if (event == null || event.user == null)
            return;
        initConnectedView(event);

        int code = event.code;
        callee_gender = calleeUser.gender;
        handleRingWithCode(code);
    }

    private void initConnectedView(AgoraCallResponseEvent event) {
        calleeUser = event.user;
        String[] tags = calleeUser.tags;
        activity.setTags(tags);
        String face_url = calleeUser.avatar_thumbnail;
        activity.setCalleeImg(face_url);
        String name = calleeUser.name;
        activity.setNickName(callee_id, name);
        setChargeGuide();
        playMusicByPath("call_music/call_ring.mp3", true);
    }

    private void handleRingWithCode(int code) {
        switch (code) {
            case CALLEE_BUSY:
                playCalleeBusyVoice();
                hungUpDealy(1);
                break;
            case CALLER_MONEY_LOW:
                break;
        }
    }

    private void playCalleeBusyVoice() {
        if (callee_gender == 2) {
            playMusicByPath("call_music/busy1.mp3", false);
        } else {
            playMusicByPath("call_music/busy2.mp3", false);
        }
    }


=======

    }

>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
    private void setUpUserJoined(AgoraUserJoinedEvent event) {
        //对方离线又连线会重复调用这个方法
        if (timer_callee_subscription != null) {
            //对方连上，停止定时器
            mCompositeSubscription.remove(timer_callee_subscription);
        }
        if (!joined) {
            joined = true;
            try {
                String message = new JSONObject().put("msg_type", DfineAction.MSG_CallBeginMessage).toString();
                TcpProxy.getInstance().sendTCPMessage(message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //changeUI
<<<<<<< HEAD
            activity.initJoinedView();
            startChargeGuideAnim();
            countTime();
            resetPlayer();
            SharedPreferencesUtil.putBooleanExtra(activity, "called_with" + callee_id, true);
            call_stop_reason = DfineAction.REAL_STOP_CALL_CALLING_NORMAL;
        }

=======
            call_stop_reason = DfineAction.REAL_STOP_CALL_CALLING_NORMAL;
        }
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
    }

    private void setUpOnLeave(AgoraOnLeaveChannelEvent event) {

    }

    private void setUpOnJoinChannelSuccess(AgoraJoinChannelSuccessEvent event) {
<<<<<<< HEAD
//        activity.setAudioViewEnable(true);
=======
        activity.setAudioViewEnable(true);
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
        try {
            JSONObject o = new JSONObject();
            o.put("msg_type", DfineAction.MSG_Call);
            o.put("tuid", callee_id);
            o.put("payload", String.valueOf(System.currentTimeMillis()));
            o.put("channel", channel);
            o.put("channel_id", channel_id);
            TcpProxy.getInstance().sendTCPMessage(o.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setUpOnUserOffine(AgoraUserOffineEvent event) {
        if (callee_id == event.uid && Constants.USER_OFFLINE_QUIT == event.reason) {
            //对方主动退出
            hungUp(true);
        } else if (callee_id == event.uid && Constants.USER_OFFLINE_DROPPED == event.reason) {
            //对方掉线，设置超时时间
            timerCalleeTimeout();
        }
    }

    private void setUpReJoinChannelSuccess(AgoraReJoinChannelSuccessEvent event) {
        if (caller_id == event.uid) {
            //自己掉线后重新加入channel
            if (timer_caller_subscription != null)
                mCompositeSubscription.remove(timer_caller_subscription);
        }
        //dismiss timer
    }

    private void timerCalleeTimeout() {
<<<<<<< HEAD
        if (timer_callee_subscription != null && !timer_callee_subscription.isUnsubscribed())
            return;
=======
        if (timer_callee_subscription != null && !timer_callee_subscription.isUnsubscribed()) return;
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
        if (BuildConfig.DEBUG) Log.i("agora", "callee " + callee_id + "计时开始");
        timer_callee_subscription = Observable.timer(MAX_DELAY, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            //已超时，退出
            hungUp(true);
        });
        mCompositeSubscription.add(timer_callee_subscription);
    }

<<<<<<< HEAD
    public void handleDragState(int state) {
        if (state == CallSmothDragView.OUTSIDE) {
            //quit();
            backgroundRunning();
        } else if (state == ViewDragHelper.STATE_DRAGGING || state == ViewDragHelper.STATE_SETTLING) {
            //禁止所有界面刷新操作
            pauseAllViewInvalidate();
        } else if (state == ViewDragHelper.STATE_IDLE) {
            //回复所有界面刷新操作
            resumeAllViewInvalidate();
        }
    }

    private void backgroundRunning() {
        String content = "";
        if (isCalling) {
            content = activity.getString(R.string.calling_in_background);
        } else {
            content = activity.getString(R.string.you_are_calling_with_whom, UserManager.getRealName(callee_id, calleeUser.name, activity));
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, new Intent(activity, AgoraCallOutActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
        showNotification(activity, pendingIntent, content);
        activity.moveTaskToBack(true);
    }

    private void resumeAllViewInvalidate() {
        canInvalidate = true;
    }

    private void pauseAllViewInvalidate() {
        canInvalidate = false;
    }

    @Override
    public synchronized void quit() {
        super.quit();
    }

    public void countTime() {

        isCalling = true;
        Subscription subscribe = Observable.interval(1, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                if (canInvalidate) {
                    if (aLong < 60 * 60) {
                        activity.setCallDuration(String.format("%02d:%02d", aLong / 60, aLong % 60));
                    } else {
                        activity.setCallDuration(String.format("%02d:%02d:%02d", aLong / 3600, aLong / 60, aLong % 60));
                    }
                }
            }
        });

        mCompositeSubscription.add(subscribe);
    }

    private void resetPlayer() {
        PlayerService.getInstance().resetPlayerCommand();
    }

    public void handsOff() {
        setEnableSpeakerphone(false);
    }

    public void handsFree() {
        setEnableSpeakerphone(true);
        //Todo
    }

    public void startUserInfoActivity() {
        Intent userinfoIntent = new Intent(activity, UserInfoActivity.class);
        userinfoIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        userinfoIntent.putExtra(UserInfoActivity.TARGET_UID, callee_id);
        activity.startActivity(userinfoIntent);
        //Todo run in background
    }

    public void sendCallOutIntentRewardMessage() {
        sendIntentRewardMessage(caller_id, callee_id, REWARD_TYPE_CALL);
    }


    /***********
     * new change
     ******/

    private void playMusicByPath(String musicPath, boolean loop) {
        setAudioModeMegaphone();
        PlayerService playerService = PlayerService.getInstance();
        playerService.playAssetFileCommand(playerService.getMusicAssetPath(activity, musicPath), loop);
    }

    private void setChargeGuide() {
        switch (PeiwoApp.getApplication().getNetType()) {
            case NetUtil.WIFI_NETWORK:
                activity.setFreeGuardText(activity.getResources().getString(R.string.calling_outgoing_wifi));
                break;
            default:
                activity.setFreeGuardText(activity.getResources().getString(R.string.calling_outgoing_wifi));
                break;
        }
    }

    private void startChargeGuideAnim() {
        Subscription animSubscription = Observable.timer(5, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    activity.animateChargeGuide();
                });
        mCompositeSubscription.add(animSubscription);
    }

    public void charge() {
        Intent intent = new Intent(activity, ChargeActivity.class);
        activity.startActivity(intent);
    }


    public void sendPayRewardMessage(int transaction) {
        try {
            JSONObject o = new JSONObject();
            o.put("msg_type", DfineAction.PayRewardMessage);
            o.put("uid", caller_id);
            o.put("tuid", callee_id);
            o.put("call_id", call_id);
            o.put("transaction", transaction);
            TcpProxy.getInstance().sendTCPMessage(o.toString());
        } catch (JSONException e) {
            e.printStackTrace();
=======
    public void handleAudioMode(Object tag) {
        if (tag == null) {
            activity.setAudioTag("audio");
            setEnableSpeakerphone(false);
            activity.setAudioModeText("现在是听筒模式");
        } else {
            activity.setAudioTag(null);
            setEnableSpeakerphone(true);
            activity.setAudioModeText("现在是扬声器模式");
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
        }
    }
}
