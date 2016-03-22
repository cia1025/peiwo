package me.peiwo.peiwo.net.tcp;

import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.db.PWConfig;
import me.peiwo.peiwo.im.MessageModel;
import me.peiwo.peiwo.im.MessageUtil;
import me.peiwo.peiwo.service.CoreService;
import me.peiwo.peiwo.util.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class CoreServiceBinder extends Binder {
    private Context mContext = null;
    private Handler mHandle = null;
    private int call_id = 0;

    public CoreServiceBinder(Context mContext, Handler mHandle) {
        this.mContext = mContext;
        this.mHandle = mHandle;
    }

    private String getPayload() {
        return Md5Util.getMd5code(String.valueOf(System.currentTimeMillis()));
    }

    public int getCallId() {
        return call_id;
    }

    public void setCallId(int call_id) {
        this.call_id = call_id;
    }

    public void sendSignInMessage() {
        PeiwoApp app = PeiwoApp.getApplication();
        PWConfig config = app.GetPWConfig();
        CustomLog.d("CoreServiceBinder, config is : " + config);
        int mUid = UserManager.getUid(mContext);
        String mSession_data = UserManager.getSessionData(mContext);
        boolean isOnlineEnv = app.isOnLineEnv();
        String envStr = isOnlineEnv ? "online" : "offline";
        CustomLog.d("CoreServiceBinder, " + envStr + ", \t uid is : " + mUid + ", \t session data is : " + mSession_data);
        JSONObject json = new JSONObject();
        String sign = Md5Util.MD5("33" + mUid + mSession_data);
        String ip = SharedPreferencesUtil.getStringExtra(mContext, "client_ip", "");
        try {
            json.put("msg_type", DfineAction.MSG_SignIn);
            json.put("uid", mUid);
            json.put("sign", sign);
            json.put("device_type", Build.MODEL + "," + Build.VERSION.SDK_INT);
            json.put("version", DfineAction.TCP_VERSION);
            json.put("app_version", PWUtils.getVersionCode(mContext));
            // 渠道号
            json.put("app", PWUtils.getChannel(mContext));
            if (!TextUtils.isEmpty(ip)) {
                json.put("client_host", ip);
            }
            //网络类型运营商(cm, cu, ct, other四种)
            json.put("network", PWDeviceInfo.getNetworkType(mContext));
            //运营商网络信息(3g, 4g, wifi, other四种)
            json.put("provider", PWDeviceInfo.getOperators(mContext));
            CustomLog.d("CoreServiceBinder, send signin json msg: " + json.toString());
            sendMessage(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean m_bShouldStopCall = false;

    public boolean callUser(int tuid, String payload, boolean recall) {
        JSONObject json = new JSONObject();
        try {
            json.put("msg_type", DfineAction.MSG_Call);
            json.put("tuid", tuid);
            json.put("payload", payload);
            if (recall) {
                json.put("resend", 1);
            }
            sendMessage(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }


    public void JHFkk_Broadcast() {
/*		if (m_threadTCP != null && m_threadTCP.isSocketConnect())
        {
			m_threadTCP.BroadCastOffer();
			nStep++;
		}
		else {
			if (m_HandlerActivity != null)
				m_HandlerActivity.sendEmptyMessage(ANSWER_CALL_FAILED);
		}*/
    }

    public boolean isConnection() {
        return ((CoreService) mContext).isConnection();
    }

    public boolean isLoginStauts() {
        return ((CoreService) mContext).isLoginStauts();
    }

    public void rejectCall(int action, String message) {
        JSONObject json = new JSONObject();
        try {
            json.put("msg_type", DfineAction.MSG_AnswerCall);
            json.put("action", action);
            if (!TextUtils.isEmpty(message)) {
                json.put("message", message);
            }
            sendMessage(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void answerCall() {
        JSONObject json = new JSONObject();
        try {
            json.put("msg_type", DfineAction.MSG_AnswerCall);
            json.put("action", DfineAction.INCOMING_CALL_ANSWER);
            sendMessage(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void substitutionUser(int reason) {
        if (DfineAction.CURRENT_CALL_STATUS != DfineAction.CURRENT_CALL_WILDCAT) {
            return;
        }
        JSONObject json = new JSONObject();
        try {
            json.put("msg_type", DfineAction.MSG_STOPCALL_MESSAGE);
            json.put("call_id", call_id);
            json.put("reason", reason);
            sendMessage(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void stopCallForMe(int reason) {
        JSONObject json = new JSONObject();
        try {
            json.put("msg_type", DfineAction.MSG_STOPCALL_MESSAGE);
            json.put("call_id", call_id);
            if (reason != DfineAction.STOP_CALL_OTHER) {
                json.put("reason", reason);
            }
            sendMessage(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendWildcatMessage(int gender, int isTimer, int timeStamp, String match_constellation) {
        if (DfineAction.CURRENT_CALL_STATUS != DfineAction.CURRENT_CALL_WILDCAT) {
            return;
        }
        JSONObject json = new JSONObject();
        try {
            json.put("msg_type", DfineAction.MSG_WILDCAT_MATCHING);
            json.put("gender", gender);
            json.put("isTimer", isTimer);
            if (timeStamp != 0)
                json.put("wildcat_timestamp", timeStamp);
            if (!TextUtils.isEmpty(match_constellation)) {
                json.put("zodiac", match_constellation);
            }
            sendMessage(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendWildcatTruthMessage() {
        if (DfineAction.CURRENT_CALL_STATUS != DfineAction.CURRENT_CALL_WILDCAT) {
            return;
        }
        JSONObject json = new JSONObject();
        try {
            json.put("msg_type", DfineAction.MSG_WILDCAT_TRUTH_MESSAGE);
            sendMessage(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendStopWildcatMessage() {
        JSONObject json = new JSONObject();
        try {
            json.put("msg_type", DfineAction.MSG_WILDCAT_EXIT_MATCHING);
            sendMessage(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendWildcatReputationMessage() {
        JSONObject json = new JSONObject();
        try {
            String payload = getPayload();

            json.put("msg_type", DfineAction.MSG_WILDCAT_LIKE);
            json.put("payload", payload);
            sendMessage(json);

            addMsgSchedule(payload, json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendWildcatMatchSuccessResponse() {
        JSONObject json = new JSONObject();
        try {
            json.put("msg_type", DfineAction.MSG_WILDCAT_MATCH_SUCCESS_RESPONSE);
            sendMessage(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendWildcatRequestCallReady() {
        JSONObject json = new JSONObject();
        try {
            json.put("msg_type", DfineAction.MSG_WILDCAT_REQUEST_CALLREADY);
            sendMessage(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendWildcatRequestCancel() {
        JSONObject json = new JSONObject();
        try {
            json.put("msg_type", DfineAction.MSG_WILDCAT_REQUEST_CANCEL);
            sendMessage(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 定时自动重发
     *
     * @param json
     */
    public void addMsgSchedule(String payload, final JSONObject json) {
        final Timer messageTimer = new Timer();
        messageTimer.schedule(new TimerTask() {
            private int count = 0;

            @Override
            public void run() {
                // 如果消息定时器5秒之后没有发送成功
                if (MessageUtil.resendMessageMap.containsKey(messageTimer)) {
                    if (count == 0) {
                        try {
                            json.put("resend", 1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (count < 3) {
                        sendMessage(json);
                    } else {
                        messageTimer.cancel();
                        MessageUtil.resendMessageMap.remove(messageTimer);
                    }
                }
                count++;
            }
        }, 5000, 5000);
        MessageUtil.resendMessageMap.put(messageTimer, payload);
    }

    /**
     * 定时自动重发
     *
     * @param dbId
     * @param json
     */
    public void addResendMessageSchedule(final long dbId, final JSONObject json) {
        final Timer callMessageTimer = new Timer();
        callMessageTimer.schedule(new TimerTask() {
            private int count = 0;

            @Override
            public void run() {
                // 如果消息定时器5秒之后没有发送成功
                if (MessageUtil.sendMessageMap.containsKey(callMessageTimer)) {
                    if (count == 0) {
                        try {
                            json.put("resend", 1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (count < 60) {
                        sendMessage(json);
                    } else {
                        callMessageTimer.cancel();
                        MessageUtil.sendMessageMap.remove(callMessageTimer);
                        MessageUtil.updateMessageFaileToDb(mContext, -1, dbId);
                    }
                }
                count++;
            }
        }, 5000, 5000);
        MessageUtil.sendMessageMap.put(callMessageTimer, dbId);
    }

    /**
     * TCP发送消息
     *
     * @param tUid  对方UID
     * @param model 发送消息model
     * @return
     */
    public boolean sendImTextMessage(int tUid, MessageModel model, int what_message_from) {
        if (isLoginStauts()) {
            JSONObject json = new JSONObject();
            try {
                json.put("msg_type", DfineAction.MSG_SendTextMessage);
                json.put("tuid", tUid);
                json.put("msg", model.content);
                json.put("payload", model.id);
                json.put("dialog_type", model.dialog_type);

                JSONObject jo = new JSONObject();
                if (what_message_from != 0) {
                    jo.put("from", what_message_from);
                    if (!TextUtils.isEmpty(model.feed_id)) {
                        jo.put("feed_id", model.feed_id);
                    }
                }
                if (model.dialog_type == MessageModel.DIALOG_TYPE_IMAGE_MESSAGE) {
                    JSONObject detailsJson = new JSONObject(model.details);
                    JSONObject imageJson = detailsJson.optJSONObject("im_image");
                    imageJson.put("local_path", "");
                    jo.put("im_image", imageJson);
                } else if (model.dialog_type == MessageModel.DIALOG_TYPE_IM_PACKET) {
                    JSONObject detailsJson = new JSONObject(model.details);
                    JSONObject packetJson = detailsJson.optJSONObject("im_packet");
                    jo.put("im_packet", packetJson);
                }
                json.put("extra", jo);

                addResendMessageSchedule(model.id, json);

                sendMessage(json);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean focusUser(int tUid, JSONObject jobj) {
        if (isLoginStauts()) {
            JSONObject json = new JSONObject();
            try {
                String payload = getPayload();
                json.put("msg_type", DfineAction.MSG_FOCUS_USER);
                json.put("tuid", tUid);
//	            jo.put("from", msgFromWhat);
                json.put("extra", jobj);
                json.put("dialog_type", MessageModel.DIALOG_TYPE_VOICE_MESSAGE);
                json.put("payload", payload);
                CustomLog.d("focus user " + tUid + ". jsonObj is : " + jobj.toString());
                addMsgSchedule(payload, json);

                sendMessage(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean wildcatRequestFriend(int tUid, JSONObject jobj) {
        if (isLoginStauts()) {
            JSONObject json = new JSONObject();
            try {
                String payload = getPayload();
                json.put("msg_type", DfineAction.MSG_FOCUS_USER);
                json.put("tuid", tUid);
//	            jo.put("from", msgFromWhat);
                json.put("extra", jobj);
                json.put("dialog_type", MessageModel.DIALOG_TYPE_FOCUS);
                json.put("payload", payload);
                CustomLog.d("tuid is : " + tUid);
                CustomLog.d("focus user. jsonObj is : " + jobj.toString());
                addMsgSchedule(payload, json);

                sendMessage(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    public void answerPubLike(boolean isCancel) {
        if (isLoginStauts()) {
            JSONObject json = new JSONObject();
            try {
                json.put("code", 0);
//	        	if (isCancel) {
//	        		json.put("msg_type", DfineAction.MSG_RECEIVE_PUB_UNLIKE_NOTIFY_RESPONSE);
//	        	} else {
//	        		json.put("msg_type", DfineAction.MSG_RECEIVE_PUB_LIKE_NOTIFY_RESPONSE);
//	        	}
                json.put("msg_type", DfineAction.MSG_RECEIVE_PUB_LIKE_NOTIFY_RESPONSE);
                sendMessage(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void requestFriendFlow() {
        if (isLoginStauts()) {
            JSONObject json = new JSONObject();
            try {
                json.put("msg_type", DfineAction.MSG_FEED_PUB_FRIEND_NOTIFY_REQUEST);
                sendMessage(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean unfocusUser(int tUid) {
        if (isLoginStauts()) {
            JSONObject json = new JSONObject();
            try {
                json.put("msg_type", DfineAction.MSG_UNFOCUS_USER);
                json.put("tuid", tUid);
                sendMessage(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean feedFlowLikeHandle(HashMap<String, Boolean> likerHandleMap) {
        if (likerHandleMap == null)
            return false;
        if (isLoginStauts()) {
            try {
                JSONArray likeArray = new JSONArray();
                StringBuilder unLikeArray = new StringBuilder();
                for (String key : likerHandleMap.keySet()) {
                    if (likerHandleMap.get(key)) {
                        JSONObject objectJson = new JSONObject();
                        objectJson.put("pub_id", key);
                        objectJson.put("like_time", System.currentTimeMillis() / 1000);
                        likeArray.put(objectJson);
                    } else {
                        if (unLikeArray.length() > 0) {
                            unLikeArray.append(",");
                        }
                        unLikeArray.append(key);
                    }
                }

                if (likeArray.length() > 0) {
                    JSONObject json = new JSONObject();
                    json.put("msg_type", DfineAction.MSG_FEED_PUB_LIKE_REQUEST);
                    json.put("data", likeArray);
                    sendMessage(json);
                }
                if (unLikeArray.length() > 0) {
                    JSONObject json = new JSONObject();
                    json.put("msg_type", DfineAction.MSG_FEED_PUB_UNLIKE_REQUEST);
                    JSONObject jsonData = new JSONObject();
                    jsonData.put("pub_ids", unLikeArray.toString());
                    json.put("data", jsonData);
                    sendMessage(json);
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void feedFlowRead() {
        if (isLoginStauts()) {
            JSONObject json = new JSONObject();
            try {
                json.put("msg_type", DfineAction.MSG_FEED_PUB_READ_REQUEST);
                sendMessage(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void receiveMessageResponse(JSONArray dialogArray) {
        JSONObject json = new JSONObject();
        try {
            json.put("msg_type", DfineAction.MSG_ReceiveMessageResponse);
            json.put("message_ids", dialogArray);
            sendMessage(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void closeRTC() {
        ((CoreService) mContext).closeRTC();
    }

    public void disconnect() {
        ((CoreService) mContext).disconnect();
    }

    private void sendMessage(JSONObject messageObject) {
        Message message = mHandle.obtainMessage();
        message.obj = messageObject;
        mHandle.sendMessage(message);
    }

    public void sendTCPMessage(String message) {
        try {
            sendMessage(new JSONObject(message));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}