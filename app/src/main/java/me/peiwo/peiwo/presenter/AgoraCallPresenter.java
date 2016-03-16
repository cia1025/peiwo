package me.peiwo.peiwo.presenter;

import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.activity.BaseActivity;
import me.peiwo.peiwo.constans.Constans;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by wallace on 16/3/15.
 */
public class AgoraCallPresenter extends BasePresenter {
    protected CompositeSubscription mCompositeSubscription;
    protected PeiwoApp application;
    protected static final long MAX_DELAY = 15;//second

    public AgoraCallPresenter(BaseActivity activity) {
        super(activity);
        application = (PeiwoApp) activity.getApplicationContext();
        mCompositeSubscription = new CompositeSubscription();
    }

    public void setCalling(boolean iscall, PeiwoApp.CALL_TYPE call_type) {
        if (application != null)
            application.setCalling(iscall, call_type);
    }

    public void setEnableSpeakerphone(boolean b) {
        application.getAgoraRtcEngine().setEnableSpeakerphone(b);
    }

    protected void unsubscribe() {
        if (mCompositeSubscription != null)
            mCompositeSubscription.unsubscribe();
    }

    protected void joinChannel(String channel_id, String who, int uid) {
        application.getAgoraRtcEngine().joinChannel(Constans.AGORA_VENDOR_KEY, channel_id, who, uid);
    }

    protected void leaveChannel() {
        application.getAgoraRtcEngine().leaveChannel();
    }

}
