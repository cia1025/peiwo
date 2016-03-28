package me.peiwo.peiwo.presenter;

<<<<<<< HEAD
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.v4.widget.ViewDragHelper;
import android.text.TextUtils;
import android.util.Log;
import io.agora.rtc.Constants;
import me.peiwo.peiwo.*;
import me.peiwo.peiwo.activity.AgoraWildCallActivity;
import me.peiwo.peiwo.activity.ChargeActivity;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.model.agora.*;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.CallSmothDragView;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

=======
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

>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
/**
 * Created by wallace on 16/3/14.
 */
public class AgoraWildCallPresenter extends AgoraCallPresenter {
<<<<<<< HEAD
    private AgoraWildCallActivity activity;
    private PWUserModel selfUser;
    private AgoraWildCallReadyEvent callReadyEvent;
    private int remote_uid;
    private boolean canInvalidate = true;
    private boolean canRequestFriend = false;
    private boolean first_match = true;
    private Subscription subscriptionTiming;//计时，3分钟&无限计时
    private Subscription subscriptionShowAds;
    private String[] wild_ads;
    private SoundPool mSoundPool;
    private int soundIdMetched;
    private int soundIdPostLike;
=======
    private static final int TIME_STAMP = (int) (System.currentTimeMillis() / 1000);
    private AgoraWildCallActivity activity;
    private PWUserModel selfUser;
    private int remote_uid;
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198

    public AgoraWildCallPresenter(AgoraWildCallActivity activity) {
        super(activity);
        this.activity = activity;
<<<<<<< HEAD
        mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        loadSounds();
        getWildCallAds();
    }

    private void loadSounds() {
        soundIdMetched = mSoundPool.load(activity, R.raw.ppdl, 1);
        soundIdPostLike = mSoundPool.load(activity, R.raw.dz, 1);
    }

    private void getWildCallAds() {
        Subscription subscription = ApiRequestWrapper.apiGetJson(activity, new ArrayList<>(), AsynHttpClient.API_WILDCAT_ADS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<JSONObject>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(JSONObject object) {
                JSONArray array = object.optJSONArray("ads");
                if (array != null && array.length() > 0) {
                    wild_ads = new String[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        wild_ads[i] = array.optString(i);
                    }
                    timerDelayShowAds();
                }
            }
        });
        mCompositeSubscription.add(subscription);
    }

    private void timerDelayShowAds() {
        if (subscriptionShowAds != null) mCompositeSubscription.remove(subscriptionShowAds);
        subscriptionShowAds = Observable.timer(3, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            activity.showWildCallAd(wild_ads[new Random().nextInt(wild_ads.length)]);
        });
        mCompositeSubscription.add(subscriptionShowAds);
    }
=======
    }

>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198

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
<<<<<<< HEAD
        showPayPhoneIfNeed();
        setUpNextEvent();
        sendWildcatMessage();
        timerSendWildcatMessage();
    }

    private void showPayPhoneIfNeed() {
        if (selfUser.gender == AsynHttpClient.GENDER_MASK_MALE)
            activity.showPayPhoneView();
        else activity.hidePayPhoneView();
    }

    /**
     * 没2分钟发射一次wildcat message
     */
    private void timerSendWildcatMessage() {
        Subscription subscription = Observable.interval(20, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            if (BuildConfig.DEBUG) log("send wild message interval 20s");
            if (dialing())
                TcpProxy.getInstance().sendWildcatMessage(selfUser.gender, 1, (int) (System.currentTimeMillis() / 1000), "");
        });
        mCompositeSubscription.add(subscription);
    }

    private void sendWildcatMessage() {
        TcpProxy.getInstance().sendWildcatMessage(selfUser.gender, 0, (int) (System.currentTimeMillis() / 1000), "");
=======
        setUpNextEvent();
        sendWildcatMessage();
    }

    private void sendWildcatMessage() {
        TcpProxy.getInstance().sendWildcatMessage(selfUser.gender, 1, TIME_STAMP, "");
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
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
<<<<<<< HEAD
            setUpRTCCallStateEvent((RTCWildCallStateEvent) event);
=======

>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
        } else if (event instanceof AgoraJoinChannelSuccessEvent) {
            setUpOnJoinChannelSuccess((AgoraJoinChannelSuccessEvent) event);
        } else if (event instanceof AgoraUserJoinedEvent) {
            setUpUserJoined((AgoraUserJoinedEvent) event);
        } else if (event instanceof AgoraOnLeaveChannelEvent) {
            setUpOnLeaveChannel((AgoraOnLeaveChannelEvent) event);
<<<<<<< HEAD
        } else if (event instanceof AgoraHungUpByServEvent) {
            if (BuildConfig.DEBUG) log("receive AgoraHungUpByServEvent");
            setUpHungUpByServer((AgoraHungUpByServEvent) event);
        } else if (event instanceof AgoraWildCallLikeEvent) {
            setUpReceiveLikeEvent((AgoraWildCallLikeEvent) event);
        } else if (event instanceof AgoraStopCallResponseEvent) {
            setUpStopCallResponse((AgoraStopCallResponseEvent) event);
        } else if (event instanceof AgoraIntentRewardResponseEvent) {
            setUpIntentRewardResponse((AgoraIntentRewardResponseEvent) event);
        } else if (event instanceof AgoraPayRewardResponseEvent) {
            setUpPayRewardResponse((AgoraPayRewardResponseEvent) event);
        } else if (event instanceof AgoraRewardedEvent) {
            setUpAgoraRewarded((AgoraRewardedEvent) event);
        } else if (event instanceof AgoraCallBeginResponseEvent) {
            setUpAgoraCallBeginResponse((AgoraCallBeginResponseEvent) event);
        } else if (event instanceof AgoraNetworkQualityEvent) {
            setUpAgoraNetworkQuality((AgoraNetworkQualityEvent) event);
        } else if (event instanceof AgoraAudioQualityEvent) {
            setUpAgoraAudioQuality((AgoraAudioQualityEvent) event);
        } else if (event instanceof AgoraLikeResponseEvent) {
            setUpLikeResponse((AgoraLikeResponseEvent) event);
        } else if (event instanceof AgoraRemoteLikeEvent) {
            setUpRemoteLike((AgoraRemoteLikeEvent) event);
=======
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
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

<<<<<<< HEAD
    /**
     * 收到对方的点赞
     *
     * @param event
     */
    private void setUpRemoteLike(AgoraRemoteLikeEvent event) {
        activity.toast("对方已赞");
        mSoundPool.play(soundIdPostLike, 1.0f, 1.0f, 0, 0, 1.0f);
    }

    /**
     * 收到自己点赞的应答
     *
     * @param event
     */
    private void setUpLikeResponse(AgoraLikeResponseEvent event) {

    }

    /*******/
    /*****
     * 质量检测
     ****/
    private void setUpAgoraAudioQuality(AgoraAudioQualityEvent event) {
        if (event.quality >= Constants.QUALITY_POOR) {
            activity.showNetWorkTips("当前音频质量差");
        }
    }

    private void setUpAgoraNetworkQuality(AgoraNetworkQualityEvent event) {
        if (event.quality >= Constants.QUALITY_POOR) {
            activity.showNetWorkTips("当前网络质量差");
        }
    }

    private void setUpRTCCallStateEvent(RTCWildCallStateEvent event) {
        if (channel == DfineAction.CALL_CHANNEL_AGORA) return;
        if (event.type == 1) {
//            if (!event.nTCPState) {
//                activity.showNetWorkTips("当前网络不稳定");
//            }
            if (event.heart_lost_count > 0) {
                activity.showNetWorkTips("当前网络连接不稳定");
            } else if (event.remote_user_state > 0) {
                activity.showNetWorkTips("对方当前网络连接不稳定");
            }
        } else if (event.type == 2) {
            activity.toast("连接超时");
            substitutionOfUser();
        }
    }

    /*****
     * 质量检测
     ****/
    /*******/
    private void setUpAgoraCallBeginResponse(AgoraCallBeginResponseEvent event) {
        //webrtc enent
        Subscription subscription = Observable.timer(2, TimeUnit.SECONDS).subscribe(aLong -> {
            //只有在通话过程中才设置声道
            if (onPhone())
                if (first_match) handsFree();
        });
        mCompositeSubscription.add(subscription);
    }


    /**
     * 收到打赏
     *
     * @param event
     */
    private void setUpAgoraRewarded(AgoraRewardedEvent event) {
        if (event.balance != 0)
            UserManager.updateMoney(activity, String.valueOf(event.balance / 100.00f));
        activity.removeRewardView();
        event.remote_avatar = callReadyEvent.user.pic;
        event.nick_name = callReadyEvent.user.nickname;
        event.money_format = String.format("￥%d.%02d", event.money / 100, event.money % 100);
        if (activity.hasRewardedView())
            activity.reAssignRewardedViewValue(event);
        else
            activity.showRewardedView(event);
    }

    /**
     * 收到 发送打赏事件的回复
     *
     * @param event
     */
    private void setUpPayRewardResponse(AgoraPayRewardResponseEvent event) {
        if (event.balance != 0)
            UserManager.updateMoney(activity, String.valueOf(event.balance / 100.00f));
        if (60002 == event.code)
            activity.toast("余额不足");
        else if (event.code == 0)
            activity.toast("打赏成功");
    }

    /**
     * 收到服务器返回打赏指令
     *
     * @param event message
     */
    private void setUpIntentRewardResponse(AgoraIntentRewardResponseEvent event) {
        event.money_format = String.format("￥%d.%02d", event.money / 100, event.money % 100);
        if (activity.hasRewardView())
            activity.reAssignRewardViewValue(event);
        else
            activity.showRewardView(event);
    }

    public void charge() {
        Intent intent = new Intent(activity, ChargeActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivity(intent);
    }

    private void setUpStopCallResponse(AgoraStopCallResponseEvent event) {
        //发送WildcatMessage??
        sendWildcatMessage();
    }

    private void setUpHungUpByServer(AgoraHungUpByServEvent event) {
        //收到服务器挂断指令
        substitutionOfUser();
    }

    /**
     * 双方点赞，无限时模式
     *
     * @param event
     */
    private void setUpReceiveLikeEvent(AgoraWildCallLikeEvent event) {
        removeTimingSubscription();
        timeingInfinite();
        canRequestFriend = true;
    }

    private void timeingInfinite() {
        subscriptionTiming = Observable.interval(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Long>() {
            @Override
            public void onStart() {
                activity.setTimerText("00:00");
                activity.setTimeIndiText("（无限时）");
            }

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Long l) {
                if (l == 0) l = 1l;
                if (canInvalidate) {
                    if (l < 3600) {//1 hour
                        activity.setTimerText(String.format("%02d:%02d", l / 60, l % 60));
                    } else {
                        activity.setTimerText(String.format("%02d:%02d:%02d", l / 3600, l % 3600 / 60, l % 60));
                    }
                }
            }
        });
        mCompositeSubscription.add(subscriptionTiming);
    }


=======
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
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
<<<<<<< HEAD
            remote_uid = event.uid;
            setUpMatchedViews();
        }
        if (BuildConfig.DEBUG) log("user joined start voice");
    }

    private void setUpMatchedViews() {
        pauseMatchingAnimator();
        setOnPhone();
        activity.changeViewWithMatched(callReadyEvent.user.pic);
        activity.hidePayPhoneView();
        countDown3M();
        activity.setRemoteNickName(callReadyEvent.user.nickname);
        if (callReadyEvent.user.tags != null && callReadyEvent.user.tags.length > 0)
            activity.setRemoteTags("#" + callReadyEvent.user.tags[0]);
        if (callReadyEvent.user.hint != null && callReadyEvent.user.hint.size() > 0)
            activity.setWildStyle(callReadyEvent.user.hint.get(0));
        mSoundPool.play(soundIdMetched, 1.0f, 1.0f, 0, 0, 1.0f);
    }

    private void countDown3M() {
        subscriptionTiming = Observable.interval(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Long>() {
            @Override
            public void onStart() {
                activity.setTimerText("03:00");
                activity.setTimeIndiText("（倒计时）");
            }

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Long l) {
                long m = 3 * 60 - l - 1;
                if (m >= 0) {
                    if (canInvalidate)
                        activity.setTimerText(String.format("%02d:%02d", m / 60, m % 60));
                    if (m == 60) vibrator();
                } else {
                    //换人
                    if (!isUnsubscribed())
                        unsubscribe();
                    substitutionOfUser();
                }
            }
        });
        mCompositeSubscription.add(subscriptionTiming);
    }

=======
        }
        remote_uid = event.uid;
        if (BuildConfig.DEBUG) log("user joined start voice");
    }

>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
    private void setUpOnJoinChannelSuccess(AgoraJoinChannelSuccessEvent event) {
        //自己加入channel成功
        if (BuildConfig.DEBUG) log("join channel success send call begin message");
        sendCallBeginMessage();
    }

    private void setUpWildCallReady(AgoraWildCallReadyEvent event) {
        if (BuildConfig.DEBUG) log("setUpWildCallReady");
<<<<<<< HEAD
        channel = event.data.channel;
        callReadyEvent = event;
        if (channel == DfineAction.CALL_CHANNEL_AGORA) {
            activity.showMuteControl();
            //先leave
            String channel_id = event.data.channel_id;
            //leaveChannel();直接join
            if (!TextUtils.isEmpty(channel_id)) {
                joinChannel(channel_id, "i am " + selfUser.uid, selfUser.uid);
                if (first_match) handsFree();
            }
        } else {
            remote_uid = event.user.uid;
            //log("remote uid ==  " + remote_uid);
            setUpMatchedViews();
            activity.hideMuteControl();
        }
        first_match = false;
=======
        //先leave
        String channel_id = event.data.channel_id;
        //leaveChannel();直接join
        if (!TextUtils.isEmpty(channel_id)) {
            joinChannel(channel_id, "i am " + selfUser.uid, selfUser.uid);
            setEnableSpeakerphone(true);
        }
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
    }


    private void sendCallBeginMessage() {
        try {
            String message = new JSONObject().put("msg_type", DfineAction.MSG_CallBeginMessage).toString();
            TcpProxy.getInstance().sendTCPMessage(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

<<<<<<< HEAD
    @Override
    public synchronized void quit() {
        if (BuildConfig.DEBUG) log(getClass().getSimpleName() + " quit()...");
        unsubscribe();
        leaveChannel();
        stopAllAnimator();
        sendStopCallMessage();
        sendExitWildCallMessage();
        handsOff();
=======
    public synchronized void quit() {
        unsubscribe();
        leaveChannel();
        stopAllAnimator();
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
        activity.finish();
        //etc...
    }

<<<<<<< HEAD
    //    public synchronized void quit() {
//        unsubscribe();
//        leaveChannel();
//        stopAllAnimator();
//        sendStopCallMessage();
//        sendExitWildCallMessage();
//        handsOff();
//        activity.finish();
//        //etc...
//    }

    private void sendExitWildCallMessage() {
        //MSG_WILDCAT_EXIT_MATCHING
        TcpProxy.getInstance().sendStopWildcatMessage();
    }

    private void sendStopCallMessage() {
        //MSG_STOPCALL_MESSAGE//DfineAction.WILDCAT_STOP_CALL_EXIT
        //期间有电话打进来被动挂断发WILDCAT_STOP_CALL_SYSTEM_PHONE
        TcpProxy.getInstance().stopCallForMe(DfineAction.WILDCAT_STOP_CALL_EXIT);
    }

=======
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
    private void log(String msg) {
        Log.i("wild", msg);
    }

    public void onDestory() {
<<<<<<< HEAD
        super.onDestory();
        if (mSoundPool != null) mSoundPool.release();
        mSoundPool = null;
        cancelNotification();
=======
        DfineAction.CURRENT_CALL_STATUS = DfineAction.CURRENT_CALL_NOT;
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
    }

    public void handleDragState(int state) {
        if (state == CallSmothDragView.OUTSIDE) {
<<<<<<< HEAD
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
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, new Intent(activity, AgoraWildCallActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        showNotification(activity, pendingIntent, "正在匿名聊");
        activity.moveTaskToBack(true);
        activity.overridePendingTransition(0, 0);
    }


    private void resumeAllViewInvalidate() {
        canInvalidate = true;
    }

    private void pauseAllViewInvalidate() {
        canInvalidate = false;
    }

    public void sendLikeMessage() {
        TcpProxy.getInstance().sendWildcatReputationMessage();
    }

    public void requestFriend() {
        //if(无限时)request
        //else toast
        if (canRequestFriend) {
            TcpProxy.getInstance().wildcatRequestFriend(remote_uid, new JSONObject());
            activity.toast("申请成功");
        } else activity.toast("需互赞后申请好友");
    }

    public void substitutionOfUser() {
        remote_uid = 0;
        leaveChannel();
        removeTimingSubscription();
        unmute();
        //handsFree();
        setDialing();
        activity.hideWildCallAds();
        showPayPhoneIfNeed();
        activity.changeViewWithMatching();
        resumeMatchingAnimator();
        TcpProxy.getInstance().substitutionUser(DfineAction.WILDCAT_STOP_CALL_NORMAL);
        timerDelayShowAds();
    }

    private void removeTimingSubscription() {
        if (subscriptionTiming != null) mCompositeSubscription.remove(subscriptionTiming);
        subscriptionTiming = null;
    }

    /**
     * 举报用户
     */
    public void accusationUser() {
        activity.toast("此人已被花式吊打");
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(AsynHttpClient.KEY_REASON, "1"));
        params.add(new BasicNameValuePair(AsynHttpClient.KEY_REPORT_WILDCAT, "1"));
        params.add(new BasicNameValuePair(AsynHttpClient.KEY_TUID, String.valueOf(remote_uid)));
        params.add(new BasicNameValuePair("type", "wildcat"));
        Subscription subscription = ApiRequestWrapper.apiGetJson(activity, params, AsynHttpClient.API_REPORT_SEND).subscribe(new Subscriber<JSONObject>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(JSONObject object) {

            }
        });
        mCompositeSubscription.add(subscription);
    }

    public void sendWildCallIntentRewardMessage() {
        sendIntentRewardMessage(selfUser.uid, remote_uid, REWARD_TYPE_WILD_CALL);
    }

    public void sendPayRewardMessage(int transaction) {
        try {
            JSONObject o = new JSONObject();
            o.put("msg_type", DfineAction.PayRewardMessage);
            o.put("uid", selfUser.uid);
            o.put("tuid", remote_uid);
            o.put("call_id", callReadyEvent.data.call_id);
            o.put("transaction", transaction);
            TcpProxy.getInstance().sendTCPMessage(o.toString());
        } catch (JSONException e) {
            e.printStackTrace();
=======
            quit();
        } else {

>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
        }
    }
}
