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
import me.peiwo.peiwo.activity.AgoraCallInActivity;
import me.peiwo.peiwo.activity.ChargeActivity;
import me.peiwo.peiwo.activity.UserInfoActivity;
import me.peiwo.peiwo.model.agora.AgoraCallEvent;
import me.peiwo.peiwo.model.agora.AgoraCalledMessageEvent;
import me.peiwo.peiwo.model.agora.AgoraConnectionInterruptedEvent;
import me.peiwo.peiwo.model.agora.AgoraConnectionLostEvent;
import me.peiwo.peiwo.model.agora.AgoraHungUpByServEvent;
import me.peiwo.peiwo.model.agora.AgoraIntentRewardResponseEvent;
import me.peiwo.peiwo.model.agora.AgoraJoinChannelSuccessEvent;
import me.peiwo.peiwo.model.agora.AgoraOnLeaveChannelEvent;
import me.peiwo.peiwo.model.agora.AgoraReJoinChannelSuccessEvent;
import me.peiwo.peiwo.model.agora.AgoraRewardedEvent;
import me.peiwo.peiwo.model.agora.AgoraStopCallResponseEvent;
import me.peiwo.peiwo.model.agora.AgoraUser;
import me.peiwo.peiwo.model.agora.AgoraUserJoinedEvent;
import me.peiwo.peiwo.model.agora.AgoraUserMuteAudioEvent;
import me.peiwo.peiwo.model.agora.AgoraUserOffineEvent;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.CallSmothDragView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
=======
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
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198

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
<<<<<<< HEAD
    private boolean canInvalidate = true;
    private boolean joined = false;
    private AgoraUser user;
    private AgoraCalledMessageEvent.Data data;
=======
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198

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
<<<<<<< HEAD
        if (BuildConfig.DEBUG)
            Log.i("agora", "call in caller_id==" + caller_id + "--callee_id==" + callee_id);
        initView();
=======
        if (BuildConfig.DEBUG) Log.i("agora", "call in caller_id==" + caller_id + "--callee_id==" + callee_id);
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
        timerAnswerTimeout();
        setUpNextEvent();
    }

<<<<<<< HEAD
    private void initView() {
        activity.initConnectingView();
        initCalleeMsg();
    }

    private void initCalleeMsg() {
        data = calledMessageEvent.data;
        if (data == null)
            activity.finish();
        user = data.user;
        if (user == null)
            activity.finish();
        String[] tags = user.tags;
        activity.setTags(tags);
        String face_url = user.avatar_thumbnail;
        activity.setCalleeImg(face_url);
        String name = user.name;
        activity.setNickName(callee_id, name);
    }

=======
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
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
<<<<<<< HEAD
//            hungUp(true);
            setUpStopCallResponse();
        } else if (event instanceof AgoraHungUpByServEvent) {
            setUpHungUpByServer();
//            hungUp(false);
=======
            hungUp(true);
        } else if (event instanceof AgoraHungUpByServEvent) {
            hungUp(false);
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
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
<<<<<<< HEAD
        } else if (event instanceof AgoraIntentRewardResponseEvent) {
            setUpIntentRewardResponse((AgoraIntentRewardResponseEvent) event);
        } else if (event instanceof AgoraRewardedEvent) {
            setUpAgoraRewarded((AgoraRewardedEvent) event);
        }
    }

    private void setUpAgoraRewarded(AgoraRewardedEvent event) {
        if (event.balance != 0)
            UserManager.updateMoney(activity, String.valueOf(event.balance / 100.00f));
        activity.removeRewardView();
        event.remote_avatar = user.avatar_thumbnail;
        event.nick_name = getNickName();
        event.money_format = String.format("￥%d.%02d", event.money / 100, event.money % 100);
        if (activity.hasRewardedView())
            activity.reAssignRewardedViewValue(event);
        else
            activity.showRewardedView(event);
    }

    private String getNickName() {
        return UserManager.getRealName(callee_id, user.name, activity);
    }

    private void setUpIntentRewardResponse(AgoraIntentRewardResponseEvent event) {
        event.money_format = String.format("￥%d.%02d", event.money / 100, event.money % 100);
        if (activity.hasRewardView())
            activity.reAssignRewardViewValue(event);
        else
            activity.showRewardView(event);
    }

    private void setUpStopCallResponse() {
        hungUp(true);
    }

    private void setUpHungUpByServer() {
        hungUp(false);
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

    private void setUpOnJoinChannelSuccess(AgoraJoinChannelSuccessEvent event) {
<<<<<<< HEAD
=======
        activity.setAudioViewEnable(true);
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
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
<<<<<<< HEAD
//        if (!joined) {
//            joined = true;
//            activity.initJoinedView();
//            startChargeGuideAnim();
//            countTime();
//        }
    }

    private void startChargeGuideAnim() {
        Subscription animSubscription = Observable.timer(5, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    activity.animChargeGuide();
                });
        mCompositeSubscription.add(animSubscription);
    }

    private void countTime() {
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
=======
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
    }

    private void setUpConnectionInterruped(AgoraConnectionInterruptedEvent event) {

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
<<<<<<< HEAD
        if (timer_caller_subscription != null && !timer_caller_subscription.isUnsubscribed())
            return;
=======
        if (timer_caller_subscription != null && !timer_caller_subscription.isUnsubscribed()) return;
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
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
<<<<<<< HEAD
        if (timer_answer_subscription != null && !timer_answer_subscription.isUnsubscribed())
            return;
        if (BuildConfig.DEBUG) Log.i("agora", "callee answer" + callee_id + "应答计时开始");
//        activity.setCallerText("caller is " + caller_id);
//        activity.setCalleeText("callee is " + callee_id);
=======
        if (timer_answer_subscription != null && !timer_answer_subscription.isUnsubscribed()) return;
        if (BuildConfig.DEBUG) Log.i("agora", "callee answer" + callee_id + "应答计时开始");
        activity.setCallerText("caller is " + caller_id);
        activity.setCalleeText("callee is " + callee_id);
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
        timer_answer_subscription = Observable.timer(MAX_DELAY, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            //应答超时，退出
            hungUp(true);
        });
        mCompositeSubscription.add(timer_answer_subscription);
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

<<<<<<< HEAD

    public void rejectCall() {
        if (timer_answer_subscription != null)
            mCompositeSubscription.remove(timer_answer_subscription);
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
        }
    }

    public void rejectCall() {
        if (timer_answer_subscription != null) mCompositeSubscription.remove(timer_answer_subscription);
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
        TcpProxy.getInstance().rejectCall("reject");
        //会收到服务端hungupbyservmessage
        hungUp(true);
    }

    public void answerCall() {
<<<<<<< HEAD
        if (timer_answer_subscription != null)
            mCompositeSubscription.remove(timer_answer_subscription);
//        activity.changeViewWithAnswer();
        if (!joined) {
            joined = true;
            activity.initJoinedView();
            startChargeGuideAnim();
            countTime();
        }
=======
        if (timer_answer_subscription != null) mCompositeSubscription.remove(timer_answer_subscription);
        activity.changeViewWithAnswer();
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
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
<<<<<<< HEAD

    public void handleDragState(int state) {
        if (state == CallSmothDragView.OUTSIDE) {
//            quit();
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
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, new Intent(activity, AgoraCallInActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        //需要根据是否正在打电话显示不同的Notification
        showNotification(activity, pendingIntent, activity.getString(R.string.calling_in_background));
        activity.moveTaskToBack(true);
        activity.overridePendingTransition(0, 0);
    }

    private void resumeAllViewInvalidate() {
        canInvalidate = true;

    }

    private void pauseAllViewInvalidate() {
        canInvalidate = false;
    }

    public void startUserInfoActivity() {
        Intent userinfoIntent = new Intent(activity, UserInfoActivity.class);
        userinfoIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        userinfoIntent.putExtra(UserInfoActivity.TARGET_UID, callee_id);
        activity.startActivity(userinfoIntent);
    }

    public void sendCallInIntentRewardMessage() {
        sendIntentRewardMessage(caller_id, callee_id, REWARD_TYPE_CALL);
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
            o.put("call_id", data.call_id);
            o.put("transaction", transaction);
            TcpProxy.getInstance().sendTCPMessage(o.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void quit() {
        super.quit();
        unsubscribe();
        hungUp(true);
        handsOff();
        activity.finish();
    }

=======
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
}
