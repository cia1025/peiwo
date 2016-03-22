package me.peiwo.peiwo.presenter;

import android.content.Intent;
import android.util.Log;
import io.agora.rtc.Constants;
import me.peiwo.peiwo.BuildConfig;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.RxBus;
import me.peiwo.peiwo.activity.AgoraCallInActivity;
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
public class AgoraCallInPresenter extends AgoraCallPresenter {
    private int caller_id;
    private int callee_id;
    private AgoraCalledMessageEvent calledMessageEvent;
    private Subscription timer_answer_subscription;
    private Subscription timer_callee_subscription;
    private Subscription timer_caller_subscription;
    private AgoraCallInActivity activity;

    private int call_stop_reason = DfineAction.REAL_STOP_CALL_NORMAL;

    public AgoraCallInPresenter(AgoraCallInActivity activity) {
        super(activity);
        this.activity = activity;
    }

    public void init() {
        setCalling(true, PeiwoApp.CALL_TYPE.CALL_REAL);
        DfineAction.CURRENT_CALL_STATUS = DfineAction.CURRENT_CALL_REAL;
        Intent intent = activity.getIntent();
        calledMessageEvent = intent.getParcelableExtra(AgoraCallInActivity.K_CALLED_EVENT);
        if (calledMessageEvent == null)
            activity.finish();
        caller_id = calledMessageEvent.data.user.uid;
        callee_id = UserManager.getUid(activity);
        if (BuildConfig.DEBUG) Log.i("agora", "call in caller_id==" + caller_id + "--callee_id==" + callee_id);
        timerAnswerTimeout();
        setUpNextEvent();
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
        } else if (event instanceof AgoraUserJoinedEvent) {
            setUpUserJoined((AgoraUserJoinedEvent) event);
        } else if (event instanceof AgoraConnectionLostEvent) {
            setUpConnectionLost((AgoraConnectionLostEvent) event);
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

    private void setUpOnJoinChannelSuccess(AgoraJoinChannelSuccessEvent event) {
        activity.setAudioViewEnable(true);
        call_stop_reason = DfineAction.REAL_STOP_CALL_CALLING_NORMAL;
        try {
            String message = new JSONObject().put("msg_type", DfineAction.MSG_CallBeginMessage).toString();
            TcpProxy.getInstance().sendTCPMessage(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setUpConnectionLost(AgoraConnectionLostEvent event) {
        //timer delay自己掉线
        timerCalleeTimeout();
    }

    private void setUpUserJoined(AgoraUserJoinedEvent event) {
        //对方重新加入，dissmiss timer
        if (timer_caller_subscription != null)
            mCompositeSubscription.remove(timer_caller_subscription);
    }

    private void setUpConnectionInterruped(AgoraConnectionInterruptedEvent event) {

    }

    private void timerCalleeTimeout() {
        if (timer_callee_subscription != null && !timer_callee_subscription.isUnsubscribed()) return;
        if (BuildConfig.DEBUG) Log.i("agora", "callee " + callee_id + "计时开始");
        timer_callee_subscription = Observable.timer(MAX_DELAY, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            //超时,退出
            hungUp(true);
        });
        mCompositeSubscription.add(timer_callee_subscription);
    }

    private void setUpReJoinChannelSuccess(AgoraReJoinChannelSuccessEvent event) {
        if (callee_id == event.uid) {
            //自己重新加入channel
            if (timer_callee_subscription != null)
                mCompositeSubscription.remove(timer_callee_subscription);
        }
    }

    private void setUpOnUserOffine(AgoraUserOffineEvent event) {
        if (caller_id == event.uid && Constants.USER_OFFLINE_QUIT == event.reason) {
            //对方主动退出
            hungUp(true);
        } else if (caller_id == event.uid && Constants.USER_OFFLINE_DROPPED == event.reason) {
            //对方掉线，设置超时时间
            timeCallerTimeout();
        }
    }

    private void timeCallerTimeout() {
        if (timer_caller_subscription != null && !timer_caller_subscription.isUnsubscribed()) return;
        if (BuildConfig.DEBUG) Log.i("agora", "caller " + caller_id + "计时开始");
        timer_caller_subscription = Observable.timer(MAX_DELAY, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            //超时，退出
            hungUp(true);
        });
        mCompositeSubscription.add(timer_caller_subscription);
    }

    private void setUpOnLeave(AgoraOnLeaveChannelEvent event) {

    }

    private void timerAnswerTimeout() {
        //设置应答超时时间
        if (timer_answer_subscription != null && !timer_answer_subscription.isUnsubscribed()) return;
        if (BuildConfig.DEBUG) Log.i("agora", "callee answer" + callee_id + "应答计时开始");
        activity.setCallerText("caller is " + caller_id);
        activity.setCalleeText("callee is " + callee_id);
        timer_answer_subscription = Observable.timer(MAX_DELAY, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            //应答超时，退出
            hungUp(true);
        });
        mCompositeSubscription.add(timer_answer_subscription);
    }

    public synchronized void hungUp(boolean send_stopcall_message) {
        activity.setHungUpViewEnable(false);
        unsubscribe();
        if (send_stopcall_message)
            sendStopCallMessage();
        leaveChannel();
        activity.finish();
    }

    private void sendStopCallMessage() {
        try {
            JSONObject o = new JSONObject();
            o.put("msg_type", DfineAction.MSG_STOPCALL_MESSAGE);
            o.put("call_id", calledMessageEvent.data.call_id);
            o.put("reason", call_stop_reason);
            TcpProxy.getInstance().sendTCPMessage(o.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    public void rejectCall() {
        if (timer_answer_subscription != null) mCompositeSubscription.remove(timer_answer_subscription);
        TcpProxy.getInstance().rejectCall("reject");
        //会收到服务端hungupbyservmessage
        hungUp(true);
    }

    public void answerCall() {
        if (timer_answer_subscription != null) mCompositeSubscription.remove(timer_answer_subscription);
        activity.changeViewWithAnswer();
        sendAnswerMessage();
        joinChannel(calledMessageEvent.data.channel_id, "i am " + callee_id, callee_id);
    }

    private void sendAnswerMessage() {
        try {
            JSONObject o = new JSONObject();
            o.put("msg_type", DfineAction.MSG_AnswerCall);
            o.put("action", DfineAction.INCOMING_CALL_ANSWER);
            TcpProxy.getInstance().sendTCPMessage(o.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onDestory() {
        setCalling(false, PeiwoApp.CALL_TYPE.CALL_NONE);
        DfineAction.CURRENT_CALL_STATUS = DfineAction.CURRENT_CALL_NOT;
    }
}
