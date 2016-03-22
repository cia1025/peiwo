package me.peiwo.peiwo.presenter;

import android.content.Intent;
import android.util.Log;
import io.agora.rtc.Constants;
import me.peiwo.peiwo.BuildConfig;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
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

/**
 * Created by wallace on 16/3/15.
 */
public class AgoraCallOutPresenter extends AgoraCallPresenter {
    private AgoraCallOutActivity activity;
    private int caller_id;
    private int callee_id;
    private int call_id;
    private int call_stop_reason = DfineAction.REAL_STOP_CALL_NORMAL;
    private int channel;
    private String channel_id;

    private boolean joined = false;
    private Subscription timer_callee_subscription;
    private Subscription timer_caller_subscription;

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
        if (BuildConfig.DEBUG) Log.i("agora", "call out caller_id==" + caller_id + "--callee_id==" + callee_id);
        activity.setCallerText("caller is " + caller_id);
        activity.setCalleeText("callee is " + callee_id);
        channel = intent.getIntExtra(AgoraCallOutActivity.K_CHANNEL, 0);
        channel_id = intent.getStringExtra(AgoraCallOutActivity.K_CHANNEL_ID);
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
        }
    }

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
        if (timer_caller_subscription != null && !timer_caller_subscription.isUnsubscribed()) return;
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
    }

    public boolean finishIfNeed() {
        if (callee_id == 0) {
            activity.finish();
            return true;
        }
        return false;
    }


    public synchronized void hungUp(boolean send_stopcall_message) {
        activity.setHungUpViewEnable(false);
        unsubscribe();
        if (send_stopcall_message)
            sendStopCallMessage();
        leaveChannel();
        activity.finish();
    }

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

    }

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
            call_stop_reason = DfineAction.REAL_STOP_CALL_CALLING_NORMAL;
        }
    }

    private void setUpOnLeave(AgoraOnLeaveChannelEvent event) {

    }

    private void setUpOnJoinChannelSuccess(AgoraJoinChannelSuccessEvent event) {
        activity.setAudioViewEnable(true);
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
        if (timer_callee_subscription != null && !timer_callee_subscription.isUnsubscribed()) return;
        if (BuildConfig.DEBUG) Log.i("agora", "callee " + callee_id + "计时开始");
        timer_callee_subscription = Observable.timer(MAX_DELAY, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            //已超时，退出
            hungUp(true);
        });
        mCompositeSubscription.add(timer_callee_subscription);
    }

    public void handleAudioMode(Object tag) {
        if (tag == null) {
            activity.setAudioTag("audio");
            setEnableSpeakerphone(false);
            activity.setAudioModeText("现在是听筒模式");
        } else {
            activity.setAudioTag(null);
            setEnableSpeakerphone(true);
            activity.setAudioModeText("现在是扬声器模式");
        }
    }
}
