package me.peiwo.peiwo.net;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.eventbus.event.ServiceMessageEvent;
import me.peiwo.peiwo.im.MessageModel;
import me.peiwo.peiwo.net.tcp.CoreServiceBinder;
import me.peiwo.peiwo.service.CoreService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class TcpProxy {
    protected CoreServiceBinder m_BinderCoreService;
    private static TcpProxy instance;

    public static TcpProxy getInstance() {
        if (instance == null) {
            instance = new TcpProxy();
        }
        return instance;
    }

    public void bindCorService(Context mContext) {
        Intent serviceIntent = new Intent(mContext, CoreService.class);
        mContext.bindService(serviceIntent, mCorService, Context.BIND_AUTO_CREATE);
    }

    public void unbindCorService(Context mContext) {
        if (mCorService != null && m_BinderCoreService != null) {
            mContext.unbindService(mCorService);
        }
    }

    private ServiceConnection mCorService = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            m_BinderCoreService = (CoreServiceBinder) service;
        }

        public void onServiceDisconnected(ComponentName name) {
            m_BinderCoreService = null;
        }
    };

    public boolean isConnection() {
        if (m_BinderCoreService != null) {
            return m_BinderCoreService.isConnection();
        }
        return false;
    }

    public boolean isLoginStauts() {
        if (m_BinderCoreService != null) {
            return m_BinderCoreService.isLoginStauts();
        }
        return false;
    }

    public boolean sendImTextMessage(int tUid, MessageModel model, int what_message_from) {
        if (m_BinderCoreService != null) {
            return m_BinderCoreService.sendImTextMessage(tUid, model, what_message_from);
        }
        return false;
    }

    public boolean focusUser(int tUid, JSONObject jobj) {
        if (m_BinderCoreService != null) {
            return m_BinderCoreService.focusUser(tUid, jobj);
        }
        return false;
    }

    public boolean wildcatRequestFriend(int tUid, JSONObject jobj) {
        if (m_BinderCoreService != null) {
            return m_BinderCoreService.wildcatRequestFriend(tUid, jobj);
        }
        return false;
    }

    public void answerPubLike(boolean isCancel) {
        if (m_BinderCoreService != null) {
            m_BinderCoreService.answerPubLike(isCancel);
        }
    }

    public void requestFriendPubFlow() {
        if (m_BinderCoreService != null) {
            m_BinderCoreService.requestFriendFlow();
        }
    }


    public boolean unFocusUser(int tUid) {
        if (m_BinderCoreService != null) {
            return m_BinderCoreService.unfocusUser(tUid);
        }
        return false;
    }

    public boolean feedFlowLikeHandle(HashMap<String, Boolean> likerHandleMap) {
        if (m_BinderCoreService != null) {
            return m_BinderCoreService.feedFlowLikeHandle(likerHandleMap);
        }
        return false;
    }

    public void feedFlowRead() {
        if (m_BinderCoreService != null) {
            m_BinderCoreService.feedFlowRead();
        }
    }

//    public void releaseMediaPlayer() {
//        if (resId != R.raw.call_ring && resId != R.raw.call_ring)
//            return;
//        if (m_BinderCoreService != null) {
//            m_BinderCoreService.releaseMediaPlayer();
//        }
//    }

    public void rejectCall(String message) {
        if (m_BinderCoreService != null) {
            m_BinderCoreService.rejectCall(DfineAction.INCOMING_CALL_REJECT, message);
        }
    }

    public void answerCall() {
        if (m_BinderCoreService != null) {
            m_BinderCoreService.answerCall();
        }
    }

    public int resId = 0;

//    public void playMusic(int resId, boolean loop) {
//        this.resId = resId;
//        if (m_BinderCoreService != null) {
//            m_BinderCoreService.playMusic(resId, loop);
//        }
//    }

    public void stopCallForMe(int reason) {
        if (m_BinderCoreService != null) {
            m_BinderCoreService.stopCallForMe(reason);
        }
    }

    public void callUser(int tid, String payload, boolean recall) {
        if (m_BinderCoreService != null) {
            m_BinderCoreService.callUser(tid, payload, recall);
        }
    }

    public void closeRTC() {
        if (m_BinderCoreService != null) {
            m_BinderCoreService.closeRTC();
        }
    }

    public void connectionTcp() {
        Intent intent = new Intent();
        intent.setAction(DfineAction.EVENT_ACTION_CONNECT_TCP);
        EventBus.getDefault().post(new ServiceMessageEvent(intent));
    }

    public int disconnectionTcp() {
        if (m_BinderCoreService != null) {
            m_BinderCoreService.disconnect();
        }
        return 0;
    }

    public void sendWildcatReputationMessage() {
        if (m_BinderCoreService != null) {
            m_BinderCoreService.sendWildcatReputationMessage();
        }
    }

    public void sendWildcatRequestCallReady() {
        if (m_BinderCoreService != null) {
            m_BinderCoreService.sendWildcatRequestCallReady();
        }
    }

    public void sendWildcatRequestCancel() {
        if (m_BinderCoreService != null) {
            m_BinderCoreService.sendWildcatRequestCancel();
        }
    }

    public void sendWildcatTruthMessage() {
        if (m_BinderCoreService != null) {
            m_BinderCoreService.sendWildcatTruthMessage();
        }
    }

    public void sendStopWildcatMessage() {
        if (m_BinderCoreService != null) {
            m_BinderCoreService.sendStopWildcatMessage();
        }
    }

    public void sendWildcatMessage(int gender, int isTimer, int timeStamp, String match_constellation) {
        if (m_BinderCoreService != null) {
            m_BinderCoreService.sendWildcatMessage(gender, isTimer, timeStamp, match_constellation);
        }
    }

    public void substitutionUser(int reason) {
        if (m_BinderCoreService != null) {
            m_BinderCoreService.substitutionUser(reason);
        }
    }

    public void receiveMessageResponse(JSONArray dialogArray) {
        if (m_BinderCoreService != null) {
            m_BinderCoreService.receiveMessageResponse(dialogArray);
        }
    }

    public void sendTCPMessage(String message) {
        if (m_BinderCoreService != null) {
            m_BinderCoreService.sendTCPMessage(message);
        }
    }
}
