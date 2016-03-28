package me.peiwo.peiwo.callback;

import android.os.Looper;
import android.util.Log;
import io.agora.rtc.IRtcEngineEventHandler;
import me.peiwo.peiwo.BuildConfig;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.RxBus;
import me.peiwo.peiwo.model.agora.*;

import java.util.Arrays;

/**
 * Created by wallace on 16/3/7.
 */
public class AgoraEngineEventHandler extends IRtcEngineEventHandler {
    private PeiwoApp application;

    private void log(String msg) {
        msg += "--is main thread==" + (Looper.myLooper() == Looper.getMainLooper());
        Log.i("agora", msg);
    }

    public AgoraEngineEventHandler(PeiwoApp application) {
        this.application = application;
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        if (BuildConfig.DEBUG)
            log("onJoinChannelSuccess()--channel==" + channel + "--uid==" + uid + "--elapsed==" + elapsed);
        RxBus.provider().send(new AgoraJoinChannelSuccessEvent(channel, uid, elapsed));
    }

    @Override
    public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
        if (BuildConfig.DEBUG)
            log("onRejoinChannelSuccess()--channel==" + channel + "--uid==" + uid + "--elapsed==" + elapsed);
        RxBus.provider().send(new AgoraReJoinChannelSuccessEvent(channel, uid, elapsed));
    }

    @Override
    public void onWarning(int warn) {
        if (BuildConfig.DEBUG) log("onWarning()--warn==" + warn);
    }

    @Override
    public void onError(int err) {
        if (BuildConfig.DEBUG) log("onError()--err==" + err);
    }

    @Override
    public void onAudioQuality(int uid, int quality, short delay, short lost) {
        if (BuildConfig.DEBUG)
            log("onAudioQuality()--uid==" + uid + "--quality==" + quality + "--delay==" + delay + "--lost==" + lost);
<<<<<<< HEAD
        RxBus.provider().send(new AgoraAudioQualityEvent(uid, quality, delay, lost));
=======
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
    }

    @Override
    public void onLeaveChannel(RtcStats stats) {
        if (BuildConfig.DEBUG) log("onLeaveChannel()--stats==" + stats.toString());
        RxBus.provider().send(new AgoraOnLeaveChannelEvent(stats.totalDuration, stats.txBytes, stats.rxBytes, stats.txKBitRate, stats.rxKBitRate, stats.lastmileQuality, stats.cpuTotalUsage, stats.cpuAppUsage));
    }

    @Override
    public void onRtcStats(RtcStats stats) {
        if (BuildConfig.DEBUG) log("onRtcStats()--stats==" + stats.toString());
    }

    @Override
    public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
        if (BuildConfig.DEBUG)
            log("onAudioVolumeIndication()--speakers==" + speakers.length + "--totalVolume" + totalVolume);
    }

    @Override
    public void onNetworkQuality(int quality) {
        if (BuildConfig.DEBUG) log("onNetworkQuality()--quality==" + quality);
<<<<<<< HEAD
        RxBus.provider().send(new AgoraNetworkQualityEvent(quality));
=======
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        if (BuildConfig.DEBUG) log("onUserJoined()--uid==" + uid + "--elapsed==" + elapsed);
        //对方
        RxBus.provider().send(new AgoraUserJoinedEvent(uid, elapsed));
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        if (BuildConfig.DEBUG) log("onUserOffline()--uid==" + uid + "--reason==" + reason);
        //对方
        RxBus.provider().send(new AgoraUserOffineEvent(uid, reason));
    }

    @Override
    public void onUserMuteAudio(int uid, boolean muted) {
        if (BuildConfig.DEBUG) log("onUserMuteAudio()--uid==" + uid + "--muted==" + muted);
        RxBus.provider().send(new AgoraUserMuteAudioEvent(uid, muted));
    }

    @Override
    public void onConnectionLost() {
        if (BuildConfig.DEBUG) log("onConnectionLost()");
        RxBus.provider().send(new AgoraConnectionLostEvent());
    }

    @Override
    public void onConnectionInterrupted() {
        if (BuildConfig.DEBUG) log("onConnectionInterrupted()");
        RxBus.provider().send(new AgoraConnectionInterruptedEvent());
    }

    @Override
    public void onMediaEngineEvent(int code) {
        if (BuildConfig.DEBUG) log("onMediaEngineEvent()--code==" + code);
    }

    @Override
    public void onVendorMessage(int uid, byte[] data) {
        if (BuildConfig.DEBUG) log("onVendorMessage()--uid==" + uid + "--data==" + Arrays.toString(data));
    }
}
