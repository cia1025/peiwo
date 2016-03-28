package me.peiwo.peiwo.presenter;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.media.AudioManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import me.peiwo.peiwo.*;
import me.peiwo.peiwo.activity.AgoraCallActivity;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.net.TcpProxy;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import java.util.concurrent.TimeUnit;

/**
 * Created by wallace on 16/3/15.
 */
public class AgoraCallPresenter extends BasePresenter {
    protected CompositeSubscription mCompositeSubscription;
    protected PeiwoApp application;
    protected int channel;
    private boolean headset_on = false;
    protected static final long MAX_DELAY = 15;//second
    protected static final int REWARD_TYPE_WILD_CALL = 1;
    protected static final int REWARD_TYPE_CALL = 2;
    private AudioManager audioManager = null;

    private NotificationManager notificationManager;
    private int notifyId;
    private Activity activity;

    protected static final int CALL_STATE_DIALING = 0;//正在拨打或者正在匹配
    protected static final int CALL_STATE_ONPHONE = 1;//通话中
    private int call_state = CALL_STATE_DIALING;

    public AgoraCallPresenter(AgoraCallActivity activity) {
        super(activity);
        this.activity = activity;
        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        headset_on = audioManager.isWiredHeadsetOn();
        notifyId = hashCode();
        mCompositeSubscription = new CompositeSubscription();
        notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        application = (PeiwoApp) activity.getApplicationContext();
        diliverRangeSensorEvent(activity);
        diliverHeadSetEvent(activity);
        diliverTelephonyEvent(activity);
    }

    private void diliverTelephonyEvent(AgoraCallActivity activity) {
        Subscription subscription = RxTelephonyManager.postEvent(activity).subscribe(hasPhone -> {
            if (hasPhone) quit();
        });
        mCompositeSubscription.add(subscription);
    }

    private void diliverHeadSetEvent(AgoraCallActivity activity) {
        Subscription subscription = RxHeadSetReceiver.postEvent(activity).subscribe(headSet -> {
            headset_on = headSet;
            if (headSet) {
                //耳机插入
                audioOutputCommunication();
            } else {
                //耳机拔出 保持原来输出通道
                //audioOutoutMegapphone();
            }
        });
        mCompositeSubscription.add(subscription);
    }

    private void diliverRangeSensorEvent(AgoraCallActivity activity) {
        Subscription subscription = RxRangeSensor.postEvent(activity).debounce(500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(near -> {
            if (near) {
                activity.showRangeBlack();
                activity.enableDrag(false);
            } else {
                activity.hideRangeBlack();
                activity.enableDrag(onPhone());
            }
        });
        mCompositeSubscription.add(subscription);
    }

    public void setCalling(boolean iscall, PeiwoApp.CALL_TYPE call_type) {
        if (application != null)
            application.setCalling(iscall, call_type);
    }

    protected void setEnableSpeakerphone(boolean b) {
        application.getAgoraRtcEngine().setEnableSpeakerphone(b);
    }

    protected void unsubscribe() {
        if (mCompositeSubscription != null)
            mCompositeSubscription.unsubscribe();
        cancelNotification();
    }

    public void cancelNotification() {
        notificationManager.cancel(notifyId);
    }

    protected void joinChannel(String channel_id, String who, int uid) {
        application.getAgoraRtcEngine().joinChannel(Constans.AGORA_VENDOR_KEY, channel_id, who, uid);
    }

    protected void leaveChannel() {
        if (channel == DfineAction.CALL_CHANNEL_AGORA)
            application.getAgoraRtcEngine().leaveChannel();
        else TcpProxy.getInstance().closeRTC();
    }

    public void addSubscription(Subscription s) {
        mCompositeSubscription.add(s);
    }

    public void mute() {
        if (channel == DfineAction.CALL_CHANNEL_AGORA)
            application.getAgoraRtcEngine().muteLocalAudioStream(true);
    }

    public void unmute() {
        if (channel == DfineAction.CALL_CHANNEL_AGORA)
            application.getAgoraRtcEngine().muteLocalAudioStream(false);
    }

    public void handsFree() {
        if (this.headset_on) {
            audioOutputCommunication();
        } else {
            audioOutoutMegapphone();
        }
    }

    private void audioOutoutMegapphone() {
        if (channel == DfineAction.CALL_CHANNEL_AGORA)
            setEnableSpeakerphone(true);
        else setAudioModeMegaphone();
    }

    private void audioOutputCommunication() {
        if (channel == DfineAction.CALL_CHANNEL_AGORA)
            setEnableSpeakerphone(false);
        else setAudioModeCommunication();
    }

    public void handsOff() {
        if (this.headset_on) return; //etc.
        audioOutputCommunication();
    }

    /**
     * 听筒
     */
    private void setAudioModeCommunication() {
        if (audioManager.getMode() != AudioManager.MODE_IN_COMMUNICATION) {
            if (audioManager.isSpeakerphoneOn()) {
                audioManager.setSpeakerphoneOn(false);
            }
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        }
    }

    /**
     * 外放
     */
    protected void setAudioModeMegaphone() {
        if (audioManager.getMode() != AudioManager.MODE_NORMAL) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            if (!audioManager.isSpeakerphoneOn()) {
                audioManager.setSpeakerphoneOn(true);
            }
        }
    }

    protected void showNotification(Context context, PendingIntent intent, String title) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setContentTitle(title).setSmallIcon(R.mipmap.ic_launcher).setOngoing(true).setContentIntent(intent);
        notificationManager.notify(notifyId, builder.build());
    }

    /**
     * 对服务器发起--发送打赏指令
     *
     * @param remote_uid  对方uid
     * @param reward_type 1: 匿名聊， 2: 普通电话
     */
    protected void sendIntentRewardMessage(int self_uid, int remote_uid, int reward_type) {
        try {
            JSONObject o = new JSONObject();
            o.put("msg_type", DfineAction.IntentRewardMessage);
            o.put("uid", self_uid);
            o.put("tuid", remote_uid);
            o.put("type", reward_type);
            TcpProxy.getInstance().sendTCPMessage(o.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public synchronized void quit() {

    }

    protected void vibrator() {
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100, 500, 100, 500};
        vibrator.vibrate(pattern, -1);
    }

    public void onDestory() {
        setCalling(false, PeiwoApp.CALL_TYPE.CALL_NONE);
        DfineAction.CURRENT_CALL_STATUS = DfineAction.CURRENT_CALL_NOT;
    }

    protected void setOnPhone() {
        call_state = CALL_STATE_ONPHONE;
    }

    protected void setDialing() {
        call_state = CALL_STATE_DIALING;
    }

    protected boolean onPhone() {
        return call_state == CALL_STATE_ONPHONE;
    }

    protected boolean dialing() {
        return call_state == CALL_STATE_DIALING;
    }
}
