package me.peiwo.peiwo.presenter;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import me.peiwo.peiwo.BuildConfig;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.RxBus;
import me.peiwo.peiwo.activity.AgoraWildCallActivity;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.model.agora.*;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.CallSmothDragView;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by wallace on 16/3/14.
 */
public class AgoraWildCallPresenter extends AgoraCallPresenter {
    private static final int TIME_STAMP = (int) (System.currentTimeMillis() / 1000);
    private AgoraWildCallActivity activity;
    private PWUserModel selfUser;
    private int remote_uid;

    public AgoraWildCallPresenter(AgoraWildCallActivity activity) {
        super(activity);
        this.activity = activity;
    }


    public void pauseMatchingAnimator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.pauseAnimator();
        } else {
            activity.stopMatchingAnimator();
        }
    }

    public void resumeMatchingAnimator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.resumeAnimator();
        } else {
            activity.startMatchingAnim();
        }
    }


    public void stopAllAnimator() {
        activity.stopMatchingAnimator();
    }

    public void init() {
        setCalling(true, PeiwoApp.CALL_TYPE.CALL_WILD);
        DfineAction.CURRENT_CALL_STATUS = DfineAction.CURRENT_CALL_WILDCAT;
        selfUser = UserManager.getPWUser(activity);
        setUpNextEvent();
        sendWildcatMessage();
    }

    private void sendWildcatMessage() {
        TcpProxy.getInstance().sendWildcatMessage(selfUser.gender, 1, TIME_STAMP, "");
    }

    private void setUpNextEvent() {
        Subscription subscription = RxBus.provider().toObserverable().filter(o -> o instanceof AgoraCallEvent).map(o -> (AgoraCallEvent) o).observeOn(AndroidSchedulers.mainThread()).subscribe(this::dispatchWildCallEvent);
        mCompositeSubscription.add(subscription);
    }

    private void dispatchWildCallEvent(AgoraCallEvent event) {
        if (event instanceof AgoraWildCallResponseEvent) {
            if (BuildConfig.DEBUG) log("call response");
        } else if (event instanceof AgoraWildCallReadyEvent) {
            setUpWildCallReady((AgoraWildCallReadyEvent) event);
        } else if (event instanceof RTCWildCallStateEvent) {

        } else if (event instanceof AgoraJoinChannelSuccessEvent) {
            setUpOnJoinChannelSuccess((AgoraJoinChannelSuccessEvent) event);
        } else if (event instanceof AgoraUserJoinedEvent) {
            setUpUserJoined((AgoraUserJoinedEvent) event);
        } else if (event instanceof AgoraOnLeaveChannelEvent) {
            setUpOnLeaveChannel((AgoraOnLeaveChannelEvent) event);
        } else if (event instanceof AgoraUserOffineEvent) {
            //setUpOnUserOffine((AgoraUserOffineEvent) event);
        } else if (event instanceof AgoraReJoinChannelSuccessEvent) {
            //setUpReJoinChannelSuccess((AgoraReJoinChannelSuccessEvent) event);
        } else if (event instanceof AgoraConnectionInterruptedEvent) {
            //setUpConnectionInterruped((AgoraConnectionInterruptedEvent) event);
        } else if (event instanceof AgoraConnectionLostEvent) {
            //lost connection
            //setUpConnectLost((AgoraConnectionLostEvent) event);
        } else if (event instanceof AgoraUserMuteAudioEvent) {
            //setUpUserMute((AgoraUserMuteAudioEvent) event);
        }
    }

    private void setUpOnLeaveChannel(AgoraOnLeaveChannelEvent event) {
        if (BuildConfig.DEBUG) log("leaved channel join channel");
        //leave channel
    }

    private void setUpUserJoined(AgoraUserJoinedEvent event) {
        //对方加入成功，变换界面，开始通话
        if (remote_uid == event.uid) {
            //同一个人重复加入
        } else {
            //换人之后
        }
        remote_uid = event.uid;
        if (BuildConfig.DEBUG) log("user joined start voice");
    }

    private void setUpOnJoinChannelSuccess(AgoraJoinChannelSuccessEvent event) {
        //自己加入channel成功
        if (BuildConfig.DEBUG) log("join channel success send call begin message");
        sendCallBeginMessage();
    }

    private void setUpWildCallReady(AgoraWildCallReadyEvent event) {
        if (BuildConfig.DEBUG) log("setUpWildCallReady");
        //先leave
        String channel_id = event.data.channel_id;
        //leaveChannel();直接join
        if (!TextUtils.isEmpty(channel_id)) {
            joinChannel(channel_id, "i am " + selfUser.uid, selfUser.uid);
            setEnableSpeakerphone(true);
        }
    }


    private void sendCallBeginMessage() {
        try {
            String message = new JSONObject().put("msg_type", DfineAction.MSG_CallBeginMessage).toString();
            TcpProxy.getInstance().sendTCPMessage(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public synchronized void quit() {
        unsubscribe();
        leaveChannel();
        stopAllAnimator();
        activity.finish();
        //etc...
    }

    private void log(String msg) {
        Log.i("wild", msg);
    }

    public void onDestory() {
        DfineAction.CURRENT_CALL_STATUS = DfineAction.CURRENT_CALL_NOT;
    }

    public void handleDragState(int state) {
        if (state == CallSmothDragView.OUTSIDE) {
            quit();
        } else {

        }
    }
}
