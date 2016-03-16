package me.peiwo.peiwo.service;

import android.app.*;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.*;
import android.text.TextUtils;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import me.peiwo.peiwo.BuildConfig;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.RxBus;
import me.peiwo.peiwo.activity.AgoraCallInActivity;
import me.peiwo.peiwo.activity.RealCallActivity;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.eventbus.event.*;
import me.peiwo.peiwo.im.MessageUtil;
import me.peiwo.peiwo.model.agora.*;
import me.peiwo.peiwo.net.*;
import me.peiwo.peiwo.net.tcp.CoreServiceBinder;
import me.peiwo.peiwo.net.tcp.TcpConnection;
import me.peiwo.peiwo.receiver.HeartBeatTimeOutReceiver;
import me.peiwo.peiwo.receiver.TcpLoginBackReceiver;
import me.peiwo.peiwo.service.NetworkConnectivityListener.NetworkCallBack;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.SharedPreferencesUtil;
import me.peiwo.peiwo.util.UserManager;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.*;
import org.webrtc.MediaConstraints.KeyValuePair;
import org.webrtc.PeerConnection.IceConnectionState;
import org.webrtc.PeerConnection.IceGatheringState;
import org.webrtc.PeerConnection.Observer;
import org.webrtc.PeerConnection.SignalingState;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CoreService extends Service implements NetworkCallBack, Observer, SdpObserver {

    /**
     * 通话状态
     *
     * @author kevin
     */
    public enum RealCallState {
        IDEL,
        DIAL,        //收到callresponse到收到callbegin期间
        INCOMING,    //收到incomingcall到收到callready期间
        CALLREADY,   //收到服务器callready后到收到callbeginresponse期间
        CALLING      //通话中
    }

    /**
     * 限时聊状态
     *
     * @author kevin
     */
    public enum WildCatState {
        IDEL,
        MATCHING,
        CALLREADY,
        CALLING
    }

    public enum WebRTCState {
        SetLocalDescription,
        SetRemoteDescription
    }

    private WebRTCState webRTCState;

    private static final String TCP_SERVERS = "tcp_servers_adress";
    private static final String TCP_SERVERS_GET_TIME = "tcp_servers_adress_get_time";

    public static final int HANDLE_REAL_UI_CALL_RESPONSE = 3;
    public static final int HANDLE_REAL_UI_CALL_READY = 5;
    /**
     * 开始通话
     */
    public static final int CALL_BEGIN_RESPONSE = 9; // call begin

    public static final int STOP_CALL = 8; // 收到服务器stop call response

    public static final int WILDCAT_RESPONSE_MESSAGE = 10;

    /**
     * 限时聊被休息或关小黑屋
     */
    public static final int WILDCAT_BE_FORBIDDEN_ = 11;
    public static final int WILDCAT_REPUTATION_NICE_MESSAGE = 12;

    /**
     * 限时聊匹配成功,收到CallReady消息处理
     */
    public static final int HANDLE_WILDCAT_UI_CALL_READY = 13;

    /**
     * 限时聊匹配成功,等待用户接听或取消
     */
    public static final int HANDLE_WILDCAT_UI_MATCH_SUCCESS = 14;

    /**
     * 限时聊匹配成功,用户取消接听
     */
    public static final int HANDLE_WILDCAT_UI_CANCEL_ANSWER = 15;

    /**
     * 匿名聊点赞回执
     */
    public static final int WILDCAT_LIKE_RESPONSE_MESSAGE = 16;

    public static final int CALL_STATE_CHANGE = 100;

    /**
     * 本机角色未定
     */
    public static final int ROLE_VIRGIN = -1;
    /**
     * 本机为主叫
     */
    public static final int ROLE_MAINCALLER = 1;
    /**
     * 本机为被叫
     */
    public static final int ROLE_FIRSTLISTENER = 2;

/*	*//** 本机为主播 *//*
    public static final int ROLE_ROMMHOLDER = 10;
	*/
    /**
     * 本机为房间听众
     *//*
    public static final int ROLE_ROMMMATE = 11;*/


    public static final int WEBRTC_STATE_OFFLINE = -1;
    public static final int WEBRTC_STATE_ONLINE = 1;
    public static final int WEBRTC_STATE_CONNECTING = 2;
    public static final int WEBRTC_STATE_LOST = 102;
    public static final int WEBRTC_STATE_EXCHANGINGSD = 101;

    protected int m_nWebRTCState = WEBRTC_STATE_OFFLINE;
    protected int m_nRoleState = ROLE_VIRGIN;

    protected String webRTCConnectionState = "";

    /**
     * 记录当前电话状态
     */
    public static RealCallState callState = RealCallState.IDEL;

    /**
     * 记录当前限时聊状态
     */
    public static WildCatState wildcatState = WildCatState.IDEL;

    protected PeerConnection m_pcCurrent = null;
    private Context mContext = null;
    private CoreServiceBinder m_BinderThis = null;

    /**
     * 记录服务器心跳序列号
     */
    private long heart_seq = 0;
    /**
     * 0-未丢包，1-丢一个心跳包，2-丢两个心跳包，3-丢三个心跳包
     */
    private int heart_lost_count = 0;
    /**
     * 对端网络状况，-1--不在线，0--正常，1表示丢失掉1个心跳包  2表示丢失掉2个心跳包  3表示丢失掉3个心跳包
     */
    private int remote_user_state = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return m_BinderThis;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        EventBus.getDefault().register(this);
        CustomLog.i(DfineAction.TCP_TAG, "CoreService onCreate");
        m_BinderThis = new CoreServiceBinder(this, mSendMsgHandler);
        PeiwoApp.getApplication().addNetworkCallBack(this);
        lastType = PeiwoApp.getApplication().getNetType();
        initWebRtc();
        PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                boolean isDebugable = PeiwoApp.getApplication().isDebuggable();
                String tcp_servers = "";
                if (!isDebugable) {
                    tcp_servers = SharedPreferencesUtil.getStringExtra(mContext, TCP_SERVERS, "");
                }
                if (TextUtils.isEmpty(tcp_servers)) {
                    getTcpServers(true, false);
                } else {
                    getTcpServers(false, false);
                }
                CustomLog.i(DfineAction.TCP_TAG, "start coreservice tcp connect");
                tcpConnection(null, 0);
            }
        });
        requestClientIP();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CustomLog.i(DfineAction.TCP_TAG, "CoreService onStartCommand");
        return START_REDELIVER_INTENT;
    }


    @Override
    public void onDestroy() {
        PeiwoApp.getApplication().removeNetworkCallBack(this);
        EventBus.getDefault().unregister(this);
        CustomLog.i(DfineAction.TCP_TAG, "CoreService onDestroy");
        super.onDestroy();
    }

    private final Object syncObject = new Object();

    public void resetImServersAddress() {
        synchronized (syncObject) {
            if (imServiceAddress == null) {
                imServiceAddress = new ArrayList<String>();
            }
            String tcp_servers = SharedPreferencesUtil.getStringExtra(mContext, TCP_SERVERS, "");
            CustomLog.i(DfineAction.TCP_TAG, "SharedPreferences servers address = " + tcp_servers);
            imServiceAddress.clear();
            if (!TextUtils.isEmpty(tcp_servers)) {
                try {
                    JSONArray array = new JSONArray(tcp_servers);
                    if (array.length() > 0) {
                        for (int i = 0; i < array.length(); i++) {
                            String hostserver = array.getString(i);
                            if (hostserver.indexOf(":") > 0) {
                                imServiceAddress.add(hostserver);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (imServiceAddress.size() == 0) {
                String defaultHhost = PeiwoApp.getApplication().GetPWConfig().GetTCPSvr().m_strHostName;
                int defaultPort = PeiwoApp.getApplication().GetPWConfig().GetTCPSvr().m_nPort;
                String defaultServer = defaultHhost + ":" + defaultPort;
                imServiceAddress.add(defaultServer);
                for (int i = 0; i < DfineAction.DEFAULT_TCP_SERVERS.length; i++) {
                    imServiceAddress.add(DfineAction.DEFAULT_TCP_SERVERS[i]);
                }
            }

            pingHost(1000);
        }
    }

    private void requestClientIP() {
        ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
        ApiRequestWrapper.openAPIGET(mContext, param, AsynHttpClient.API_GET_CLIENT_IP, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
            }

            @Override
            public void onError(int error, Object ret) {
                if (!TextUtils.isEmpty(errorMessage)) {
                    String ip = errorMessage;
                    CustomLog.i("syf", "client_ip = " + ip);
                    SharedPreferencesUtil.putStringExtra(mContext, "client_ip", ip);
                }
            }
        });

    }

    private Handler mSendMsgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg == null)
                return;
            JSONObject messageObject = (JSONObject) msg.obj;
            if (messageObject == null)
                return;
            switch (messageObject.optInt("msg_type", 0)) {
                case DfineAction.MSG_Call:
                    m_nRoleState = ROLE_MAINCALLER;
                    callState = RealCallState.IDEL;
                    webRTCConnectionState = "";
                    m_jsonWebRTCInfo = null;
                    break;
                case DfineAction.MSG_STOPCALL_MESSAGE:
                    m_nRoleState = ROLE_VIRGIN;
                    m_jsonWebRTCInfo = null;
                    callState = RealCallState.IDEL;
                    stopWebRtcReconnectAlarm();
                    closeRTC();
                    break;
                case DfineAction.MSG_WILDCAT_MATCHING:
                    m_nRoleState = ROLE_VIRGIN;
                    wildcatState = WildCatState.IDEL;
                    webRTCConnectionState = "";
                    m_jsonWebRTCInfo = null;
                    break;
            }
            sendPacket(messageObject);
        }
    };

    private Handler mReceiveMsgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg == null)
                return;
            JSONObject messageObject = (JSONObject) msg.obj;
            if (messageObject == null)
                return;
            handleReceiveMsg(messageObject);
        }
    };


    public void onCallStateChange() {
        if (DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_REAL) {
            CallStateEvent event = new CallStateEvent();
            event.type = 0;
            event.nTCPState = isLoginStauts();
            event.nCallState = callState;
            event.nWebRTCState = m_nWebRTCState;
            sendRTCWildCallEvent(event);
            EventBus.getDefault().post(event);
        } else {
        }
    }

    private void sendRTCWildCallEvent(CallStateEvent event) {
        RxBus.provider().send(new RTCWildCallStateEvent(event.type, event.nTCPState, event.nWebRTCState, event.heart_lost_count, event.remote_user_state));
    }

    public void onTabMsgTitleChanged() {
        MsgTitleChangedEvent event = new MsgTitleChangedEvent();
        event.isLogin = isLoginStauts();
        event.netType = PeiwoApp.getApplication().getNetType();
        EventBus.getDefault().post(event);
    }


    public void onNetStateChange() {
        if (DfineAction.CURRENT_CALL_STATUS != DfineAction.CURRENT_CALL_REAL
                && DfineAction.CURRENT_CALL_STATUS != DfineAction.CURRENT_CALL_WILDCAT) {
            return;
        }
        if (wildcatState != WildCatState.CALLING
                && callState != RealCallState.CALLING) {
            return;
        }
        CallStateEvent event = new CallStateEvent();
        event.type = 1;
        event.heart_lost_count = heart_lost_count;
        event.remote_user_state = remote_user_state;
        sendRTCWildCallEvent(event);
        EventBus.getDefault().post(event);
    }

    public void onCallResponseForReal(int code, String msg, JSONObject data) {
        Intent intent = new Intent();
        intent.putExtra("type", HANDLE_REAL_UI_CALL_RESPONSE);
        intent.putExtra("code", code);
        intent.putExtra("msg", msg);
        intent.putExtra("data", data == null ? "" : data.toString());

        EventBus.getDefault().post(new RealCallMessageEvent(intent));
    }

    public void onCallBeginResponseForReal() {
        Intent intent = new Intent();
        intent.putExtra("type", CALL_BEGIN_RESPONSE);
        EventBus.getDefault().post(new RealCallMessageEvent(intent));
    }

    public void onCallBeginResponseForWildCat() {
        Intent intent = new Intent();
        intent.putExtra("type", CALL_BEGIN_RESPONSE);
        EventBus.getDefault().post(new WildCatMessageEvent(intent));
    }


    public void onIncomingCall(final JSONObject data) {
        CustomLog.i(DfineAction.TCP_TAG, "CoreService onIncomingCall");
        try {
            String push_str = SharedPreferencesUtil.getStringExtra(this, Constans.SP_KEY_PUSH_STR, "");
            CustomLog.i(DfineAction.TCP_TAG, "pust_str: " + push_str);
            if (!TextUtils.isEmpty(push_str)) {
                JSONObject o = new JSONObject(push_str);
                // 在应用之内始终能接打电话
                boolean nopush = o.getBoolean("nopush");
                boolean isforeground = true;//isOnForeground();
                if (nopush && !isforeground) {
                    nodisturbAnswer();
                    return;
                }
                boolean nodisturb = o.getBoolean("nodisturb");
                if (nodisturb) {
                    int ts = o.getInt("time_start");
                    int te = o.getInt("time_end");
                    if (needReturn(ts, te) && !isforeground) {
                        nodisturbAnswer();
                        return;
                    }
                }
                boolean vibrate = o.optBoolean("vibrate");
                if (vibrate) {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {100, 500, 100, 500, 100, 500, 100, 500,
                            100, 500};
                    vibrator.vibrate(pattern, -1);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        unLockScreen();
        Intent intent = new Intent(mContext, RealCallActivity.class);
        intent.putExtra("flag", DfineAction.INCOMMING_CALL);
        intent.putExtra("data", data.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                int count = 6;
//                while (true) {
//                    count--;
//                    //MainActivity.isStart ||
//                    if (count == 0) {
//                        Intent intent = new Intent(mContext, RealCallActivity.class);
//                        intent.putExtra("flag", DfineAction.INCOMMING_CALL);
//                        intent.putExtra("data", data.toString());
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                        startActivity(intent);
//                        break;
//                    } else {
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }).start();


    }

    protected void nodisturbAnswer() {
        m_BinderThis.rejectCall(DfineAction.NODISTURB_INCOMING_CALL_REJECT, null);
    }


    public void onStopWildcatResponseMessage(JSONObject data) {
        final int permission_type = data.optInt("permission_type", 0);
        final String tipMsg = data.optString("msg");
        String notify = data.optString("notify");
        final String end_time = data.optString("end_time");
        PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                int meUid = UserManager.getUid(PeiwoApp.getApplication());
                SharedPreferencesUtil.putStringExtra(CoreService.this, "wild_notify_msg" + meUid, tipMsg);
                SharedPreferencesUtil.putStringExtra(CoreService.this, "wild_notify_end_time" + meUid, end_time);
                SharedPreferencesUtil.putIntExtra(CoreService.this, "wild_notify_permission_type" + meUid, permission_type);
            }
        });
        if (permission_type != 0) {
            Intent intent = new Intent();
            intent.putExtra("type", WILDCAT_BE_FORBIDDEN_);
            intent.putExtra("notify", notify);
            intent.putExtra("msg", tipMsg);
            EventBus.getDefault().post(new WildCatMessageEvent(intent));
        }
    }

    public void onWildcatReputationNiceMessage() {
        Intent intent = new Intent();
        intent.putExtra("type", WILDCAT_REPUTATION_NICE_MESSAGE);
        EventBus.getDefault().post(new WildCatMessageEvent(intent));
    }

    public void onWildcatLikeResponseMessage(JSONObject jsonCMD) {
        Intent intent = new Intent();
        intent.putExtra("type", WILDCAT_LIKE_RESPONSE_MESSAGE);
        intent.putExtra("data", jsonCMD.toString());
        EventBus.getDefault().post(new WildCatMessageEvent(intent));
    }

    /**
     * 解锁屏幕
     */
    protected int unLockScreen() {
        // 如果屏幕被锁定，解锁
        if (isLockScreen()) {
            // 获取电源管理器对象
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
            // 点亮屏幕
            wl.acquire();
            // 释放
            wl.release();
            // 得到键盘锁管理器对象
            if (Build.VERSION.SDK_INT < 20) {
                KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                // 参数是LogCat里用的Tag
                KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
                // 解锁
                kl.disableKeyguard();
            }
        }
        return 0;
    }

    protected boolean needReturn(int ts, int te) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        if (ts < te) {
            if (hour >= ts && hour < te) {
                return true;
            }
        } else {
            if (hour >= ts || hour < te) {
                return true;
            }
        }
        return false;
    }

    public void onCallReadyForReal(int code, String msg, int call_id) {
        // 拒绝 code 4006
        Intent intent = new Intent();
        intent.putExtra("type", HANDLE_REAL_UI_CALL_READY);
        intent.putExtra("code", code);
        intent.putExtra("call_id", call_id);
        intent.putExtra("msg", msg);
        EventBus.getDefault().post(new RealCallMessageEvent(intent));
    }

    public void onCallReadyForWildcat(int code, String msg, JSONObject data, JSONObject userData) {
        Intent intent = new Intent();
        intent.putExtra("type", HANDLE_WILDCAT_UI_CALL_READY);
        intent.putExtra("code", code);
        intent.putExtra("msg", msg);
        intent.putExtra("data", data.toString());
        intent.putExtra("user", userData.toString());
        EventBus.getDefault().post(new WildCatMessageEvent(intent));
    }

    public void onWildCatMatchSuccess(JSONObject userData, String reports, int wildcat_wait_max_time) {
        Intent intent = new Intent();
        intent.putExtra("type", HANDLE_WILDCAT_UI_MATCH_SUCCESS);
        intent.putExtra("user", userData.toString());
        intent.putExtra("reports", reports);
        intent.putExtra("wait_time", wildcat_wait_max_time);
        EventBus.getDefault().post(new WildCatMessageEvent(intent));
    }

    public void onWildCatCancelAnswer() {
        Intent intent = new Intent();
        intent.putExtra("type", HANDLE_WILDCAT_UI_CANCEL_ANSWER);
        EventBus.getDefault().post(new WildCatMessageEvent(intent));
    }

    public void HangUpByRemoteForReal(String jsonCMD) {
        // 对方挂断电话
        m_nRoleState = ROLE_VIRGIN;
        callState = RealCallState.IDEL;
        Intent intent = new Intent();
        intent.putExtra("type", STOP_CALL);
        intent.putExtra("data", jsonCMD);
        EventBus.getDefault().post(new RealCallMessageEvent(intent));
        closeRTC();
    }

    public void HangUpByRemoteForWildcat(JSONObject jsonCMD) {
        // 对方挂断电话
        m_nRoleState = ROLE_VIRGIN;
        wildcatState = WildCatState.IDEL;
        try {
            CustomLog.d("HangUpByRemoteForWildcat. stop_match is : " + jsonCMD.getInt("stop_match"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent();
        intent.putExtra("type", STOP_CALL);
        intent.putExtra("data", jsonCMD.toString());
        EventBus.getDefault().post(new WildCatMessageEvent(intent));
        closeRTC();
    }

    private int lastType = NetUtil.NO_NETWORK;

    @Override
    public void getSelfNetworkType(final int networkType) {
        CustomLog.i(DfineAction.TCP_TAG, "##############################networkType = " + networkType);
        switch (networkType) {
            case NetUtil.NO_NETWORK:
                disconnect();
                break;
            case NetUtil.WIFI_NETWORK:
            case NetUtil.G2_NETWORK:
            case NetUtil.G3_NETWORK:
            case NetUtil.G4_NETWORK: {
                if (networkType != lastType) {
                    PeiwoApp.getApplication().mExecutorService.execute(() -> {
                        if (!isConnection() || currentNetState != networkType) {
                            CustomLog.i(DfineAction.TCP_TAG, "net change tcp connect");
                            tcpConnection(null, 0);
                        }
                    });
                }
            }
        }
        lastType = networkType;
    }


    private boolean isOnForeground() {
        List<ActivityManager.RunningTaskInfo> taskInfos = getTaskInfos(this, 2);
        if (taskInfos == null || taskInfos.isEmpty())
            return false;
        ActivityManager.RunningTaskInfo runningTaskInfo = taskInfos.get(0);
        ComponentName topActivity = runningTaskInfo.topActivity;
        // Trace.i("topActivity.getShortClassName() == " +
        // topActivity.getShortClassName());
        // 第三方登录这个判断会有问题
        // PPAlert.showToast(this, topActivity.getShortClassName());
        // 新浪登录不会有问题， qq有问题
        if ("com.tencent.open.agent.AuthorityActivity".equals(topActivity
                .getShortClassName())) {
            return true;
        }
        if (getPackageName().equals(topActivity.getPackageName())) {
            return true;
        }
        return false;
    }

    private List<ActivityManager.RunningTaskInfo> getTaskInfos(Context context,
                                                               int maxNum) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfos = activityManager
                .getRunningTasks(maxNum);
        if (taskInfos == null || taskInfos.isEmpty())
            return null;
        return taskInfos;
    }

    public boolean isLockScreen() {
        KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
            return true;
        } else {
            return false;
        }
    }


    public static class HubSrvConfig {
        public int delay; // 延迟时间
        public int lost; // 丢包率
        public String ip; // rtpp服务器地址

    }

    ;

    public void pingHost(final long totalTime) {
        if (imServiceAddress == null || imServiceAddress.size() <= 1)
            return;
        try {
            final ArrayList<HubSrvConfig> hubSrvList = new ArrayList<HubSrvConfig>();
            ThreadPoolExecutor executorPool = new ThreadPoolExecutor(15, 30,
                    60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(30),
                    new ThreadPoolExecutor.CallerRunsPolicy());

            for (int i = 0; i < imServiceAddress.size(); i++) {
                String httpName = "http://";
                final String ipString = imServiceAddress.get(i);
                String ipAndPort = ipString;
                if (ipAndPort.contains(httpName)) {
                    ipAndPort = ipString.substring(ipString.indexOf(httpName) + httpName.length());
                }
                int index = ipAndPort.lastIndexOf(":");
                if (index > 0) {
                    ipAndPort = ipAndPort.substring(0, index);
                } else {
                    ipAndPort = ipString;
                }
                final String ip = ipAndPort;

                executorPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        CustomLog.v("##############################pingHost : ip = " + ip);
                        int timeOut = 1000; // I recommend 3 seconds at least
                        int index = 0; // 索引 ping 的总次数
                        long pingTotalTime = 0; // ping成功的总时间
                        long startTime = System.currentTimeMillis(); // 起始时间
                        int lostCount = 0; // 丢包次数
                        double averageTime = 0; // 平均时间
                        int lost = 0; // 丢包率
                        try {
                            while (index < 5 && (System.currentTimeMillis() - startTime) < totalTime) {
                                index++;
                                long currentPingStartTime = System.currentTimeMillis();
                                try {
                                    boolean status = InetAddress.getByName(ip).isReachable(timeOut);
                                    if (status) {
                                        pingTotalTime += System.currentTimeMillis() - currentPingStartTime;
                                    } else {
                                        lostCount++;
                                    }
                                } catch (UnknownHostException e) {
                                    lostCount++;
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    lostCount++;
                                    e.printStackTrace();
                                }
                            }
                            // 平均耗时
                            if (lostCount == index || index == 0) {
                                // 全部丢包
                                averageTime = 5000;
                            } else {
                                // 计算未丢包数据平均耗时时间
                                averageTime = (double) pingTotalTime / (double) (index - lostCount);
                            }
                            // 丢包率
                            if (index == lostCount) {
                                lost = 100;
                            } else {
                                DecimalFormat format = new DecimalFormat("0");
                                lost = Integer.parseInt(format.format(((double) lostCount / index) * 100) + "");
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        } finally {
                            HubSrvConfig config = new HubSrvConfig();
                            config.lost = lost;
                            config.delay = (int) averageTime;
                            config.ip = ipString;
                            CustomLog.v("ADDRESS:" + config.ip + " : CURRENT_TIME:" + averageTime);
                            CustomLog.v("ADDRESS:" + config.ip + " : PING_COUNT:" + index);
                            CustomLog.v("ADDRESS:" + config.ip + " : LOST_COUNT:" + lostCount);
                            hubSrvList.add(config);
                        }
                    }
                });
            }
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (executorPool.getActiveCount() > 0);
            executorPool.shutdown();

            Collections.sort(hubSrvList, new Comparator<HubSrvConfig>() {
                public int compare(HubSrvConfig r1, HubSrvConfig r2) {
                    if (r1.lost == 100) {
                        return -1;
                    }
                    if (r2.lost == 100) {
                        return 1;
                    }
                    return (int) (r2.delay - r1.delay);
                }
            });
            imServiceAddress.clear();
            for (int i = 0; i < hubSrvList.size(); i++) {
                imServiceAddress.add(hubSrvList.get(i).ip);
            }
            uploadPingResult(hubSrvList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void uploadPingResult(final ArrayList<HubSrvConfig> hubSrvList) {
        ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
        ApiRequestWrapper.openAPIGET(mContext, param, AsynHttpClient.API_GET_CLIENT_IP, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {

            }

            @Override
            public void onError(int error, Object ret) {
                if (!TextUtils.isEmpty(errorMessage)) {
                    ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
                    String ip = errorMessage;
                    JSONArray array = new JSONArray();
                    for (int i = 0; i < hubSrvList.size(); i++) {
                        JSONObject item = new JSONObject();
                        try {
                            item.put("client_ip", ip);
                            item.put("server_addr", hubSrvList.get(i).ip);
                            item.put("server_type", 1);
                            item.put("speed", hubSrvList.get(i).delay);
                            item.put("extra", DfineAction.TCP_VERSION);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        array.put(item);
                    }
                    paramList.add(new BasicNameValuePair("data", array.toString()));
                    ApiRequestWrapper.openAPIPOST(mContext, paramList, AsynHttpClient.API_UPLOAD_NEW_STATE, new MsgStructure() {
                        @Override
                        public void onReceive(JSONObject data) {
                            System.out.println("uploadPingResult  onReceive data = " + data);
                        }

                        @Override
                        public void onError(int error, Object ret) {
                            System.out.println("uploadPingResult  onError ret = " + ret);
                        }
                    });
                }
            }
        });
    }

    //**************************************************************************************


    public ArrayList<String> imServiceAddress = new ArrayList<String>();    //IM服务器列表
    public String uid = "";

    private long reContentTime = 500;// 重连TCP时间，递增，连接成功后初始化
    private int reContentCount = 1;   //重连次数，每个接入地址尝试三次

    private long reGetTcpServerTime = 500;// 重新获取接入服务器地址时间
    private int reGetTcpServerCount = 1;   //尝试重新获取接入服务器次数

    private TcpConnection connection;

    private boolean isLogin = false;

    public long WEBRTC_RECONNECT_MAX_TIME = 40 * 1000;
    public final long PING_BACK = 20 * 1000 * 3;     //心跳超时监听

    /**
     * 等待SigninResponse时长, 从发送signin后开始计数
     */
    public final long TCP_LOGIN_BACK = 10 * 1000;    //TCP登录超时监听

    /**
     * 等待接收心跳包超时时长, 从收到任何一个数据包开始
     */
    public final long HEARTBEAT_TIMEOUT = 60 * 1000;    //TCP登录超时监听

    /**
     * 记录当前Tcp连接的网络
     */
    public static int currentNetState = NetUtil.NO_NETWORK;
    /**
     * 记录当前TCP连接的Host
     */
    public static String SOCKET_HOST = "";
    /**
     * 记录当前TCP连接的Port
     */
    public static int SOCKET_PORT = 0;

    public String last_wifi_ssid;

    public boolean isConnecting = false;
    public Object tcpObject = new Object();

    public void tcpConnection(String host, int port) {
        if (!UserManager.isLogin(this)) {
            return;
        }
        if (isConnecting)
            return;
        synchronized (tcpObject) {
            int networkType = PeiwoApp.getApplication().getNetType();
            if (networkType == NetUtil.NO_NETWORK) {
                return;
            }
            isConnecting = true;
            if (TextUtils.isEmpty(host) || port == 0) {
                resetImServersAddress();
                String hostserver = imServiceAddress.get(0);
                host = hostserver.substring(0, hostserver.indexOf(":"));
                port = Integer.valueOf(hostserver.substring(hostserver.indexOf(":") + 1, hostserver.length()));
            }
            if (connection == null) {
                connection = new TcpConnection(this, mReceiveMsgHandler);
            }
            disconnect();

            if (connection.connection(host, port)) {
                if (reConnectThread != null && reConnectThread.isAlive()) {
                    reConnectThread.interrupt();
                    reConnectThread = null;
                }
                reContentTime = 500;
                reContentCount = 1;
                currentNetState = networkType;
                SOCKET_HOST = host;
                SOCKET_PORT = port;

                heart_lost_count = 0;
                heart_seq = 0;
                if (currentNetState == NetUtil.WIFI_NETWORK) {
                    NetUtil.lockWifi(this);// 锁定WIFI
                }
                CustomLog.v(DfineAction.TCP_TAG, "TCP连接成功   HOST:" + host + "    PORT:" + port + "         currentNetState = " + currentNetState);
                //发送登录包，设置15秒超时
                /*startLoginBackTcp();*/
                m_BinderThis.sendSignInMessage();
                startLoginBackTcp();
            } else {
                reContentTime += (int) ((Math.pow((double) 2, (double) reContentCount) + Math.random() * 5) * 1000);//退火时间
                reContentCount++;
                if (reConnectThread != null && reConnectThread.isAlive()) {
                    reConnectThread.interrupt();
                    reConnectThread = null;
                }
                //此接入服务器是否已尝试连接三次，超过三次用其它接入服务器
                if (reContentCount <= 3) {
                    reConnection(host, port);
                } else {
                    reContentCount = 1;
                    reContentTime = 500;
                    int currentIndex = imServiceAddress.indexOf(host + ":" + port);
                    if (currentIndex < imServiceAddress.size() - 1) {
                        String hostserver = imServiceAddress.get(currentIndex + 1);
                        host = hostserver.substring(0, hostserver.indexOf(":"));
                        port = Integer.valueOf(hostserver.substring(hostserver.indexOf(":") + 1, hostserver.length()));
                        reConnection(host, port);
                    } else {
                        reGetTcpServerTime = 500;
                        reGetTcpServerCount = 1;
                        getTcpServers(true, true);
                    }
                }
            }
            isConnecting = false;
        }
    }

    private Thread reGetTcpServersThread = null;

    public void reGetTcpServers() {
        if (reGetTcpServersThread != null && reGetTcpServersThread.isAlive()) {
            reGetTcpServersThread.interrupt();
            reGetTcpServersThread = null;
        }
        int networkType = PeiwoApp.getApplication().getNetType();
        if (networkType == NetUtil.NO_NETWORK) {
            CustomLog.v(DfineAction.TCP_TAG, "Tcp reGetTcpServers  The network is not available");
            return;
        }

        reGetTcpServersThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_REAL
                            || DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_WILDCAT) {
                        Thread.sleep(1000);
                    } else {
                        Thread.sleep(reGetTcpServerTime);
                    }
                    getTcpServers(true, true);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        reGetTcpServersThread.start();
    }

    /**
     * 断线重连
     *
     * @param mContext
     * @param exception
     * @author: Kevin
     * @version: 2012-7-4 下午02:25:41
     */
    private Thread reConnectThread = null;

    public void reConnection(final String mHost, final int mPort) {
        CustomLog.v(DfineAction.TCP_TAG, "TcpUtil reConnection  reContentTime = " + reContentTime);
        disconnect();
        if (reConnectThread != null && reConnectThread.isAlive()) {
            reConnectThread.interrupt();
            reConnectThread = null;
        }
        int networkType = PeiwoApp.getApplication().getNetType();
        if (networkType == NetUtil.NO_NETWORK) {
            CustomLog.v(DfineAction.TCP_TAG, "Tcp reConnection  The network is not available");
            return;
        }

        reConnectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_REAL
                            || DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_WILDCAT) {
                        Thread.sleep(1000);
                    } else {
                        Thread.sleep(reContentTime);
                    }
                    CustomLog.v(DfineAction.TCP_TAG, "Tcp reConnection... host = " + mHost + ",  port = " + mPort);
                    tcpConnection(mHost, mPort);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        reConnectThread.start();
    }

    public void disconnect() {
        currentNetState = NetUtil.NO_NETWORK;
        setLoginStatus(false);
        stopLoginBackTcp();
        stopWaitHeartBeatTimeOut();

        NetUtil.unLockWifi();
        if (connection != null) {
            connection.shutdown();
        }
    }

    /**
     * TCP是否在线
     *
     * @return
     * @author: Kevin
     * @data:2013-11-13 下午2:12:02
     */
    public boolean isConnection() {
        return (connection != null) ? connection.isConnection() : false;
    }

    public boolean isLoginStauts() {
        return isLogin;
    }

    private void setLoginStatus(boolean isLogin) {
        this.isLogin = isLogin;
        onCallStateChange();
        onTabMsgTitleChanged();
    }

    public int sendPacket(JSONObject message) {
        if (connection != null) {
            connection.sendPacket(message);
            return 1;
        }
        return -1;
    }

    public void getWildcatTruthMessage(String title, String content) {
//        Intent intent = new Intent();
//        intent.putExtra("msg_type", DfineAction.MSG_WILDCAT_TRUTH_MESSAGE_RESPONSE);
//        intent.putExtra("title", title);
//        intent.putExtra("content", content);
//        EventBus.getDefault().post(new WildCatGameEvent(intent));
    }

    /**
     * 重新获取Tcp连接服务器列表
     *
     * @param getType     true--不判断时间直接调用，false--判断上次获取时间，当天调用一次
     * @param needConnect true--获取到服务器列表地址和立即进行连接，false--只用于获取地址保存下来
     */
    public void getTcpServers(boolean getType, final boolean needConnect) {
        CustomLog.i(DfineAction.TCP_TAG, "getTcpServers: getType = " + getType + ", needConnect = " + needConnect);
        SimpleDateFormat df = new SimpleDateFormat("yy:MM:dd");
        final String currentDate = df.format(new Date(System.currentTimeMillis()));
        if (!getType) {
            String tcp_servers_get_time = SharedPreferencesUtil.getStringExtra(mContext, TCP_SERVERS_GET_TIME, "");
            if (currentDate.equals(tcp_servers_get_time)) {
                return;
            }
        }
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tcp", DfineAction.TCP_VERSION));
        ApiRequestWrapper.openAPIGET(mContext, params,
                AsynHttpClient.API_GET_TCP_SERVERS, new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                        reGetTcpServerCount = 1;
                        reGetTcpServerTime = 500;
                        CustomLog.i(DfineAction.TCP_TAG, "getTcpServers success data = " + data);
                        if (data != null && data.has("servers")) {
                            String servers = data.optString("servers");
                            SharedPreferencesUtil.putStringExtra(mContext, TCP_SERVERS_GET_TIME, currentDate);
                            SharedPreferencesUtil.putStringExtra(mContext, TCP_SERVERS, servers);
                        }
                        if (needConnect && !isConnection()) {
                            PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    CustomLog.i(DfineAction.TCP_TAG, "get new tcpServers tcp connect");
                                    tcpConnection(null, 0);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(int error, Object ret) {
                        CustomLog.i(DfineAction.TCP_TAG, "getTcpServers faile error = " + error);
                        if (needConnect && !isConnection()) {
                            reGetTcpServerTime += (int) ((Math.pow((double) 2, (double) reGetTcpServerCount) + Math.random() * 5) * 1000);//退火时间
                            if (reGetTcpServerTime > 60000) {
                                reGetTcpServerTime = 60000;
                            }
                            reGetTcpServerCount++;
                            reGetTcpServers();
                        }
                    }
                });
    }

    private void clearSchedule(JSONObject jsonCMD) {
        String payload = jsonCMD.optString("payload");
        CustomLog.i("WILLS clear begin : " + jsonCMD);
        if (payload != null) {
            CustomLog.i("WILLS clear payload : " + payload);
            MessageUtil.removeResendListMessage(payload);
        }
    }

    public void handleReceiveMsg(final JSONObject jsonCMD) {
        if (jsonCMD == null)
            return;
        if (BuildConfig.DEBUG)
            Log.i("agora", "receice data == " + jsonCMD.toString());
        int msg_type = jsonCMD.optInt("msg_type", -1);
        startWaitHeartBeatTimeOut();
        JSONObject extraData = jsonCMD.optJSONObject("data");
        int channel = extraData != null ? extraData.optInt("channel", 0) : jsonCMD.optInt("channel", 0);
        AgoraCallEvent callEvent = null;
        if (msg_type != DfineAction.MSG_Heartbeat) {
            //CustomLog.i("handleReceiveMsg, jsonCMD is : " + jsonCMD);
        }
        switch (msg_type) {
            case DfineAction.Response_MSG_SignIn: {
                stopLoginBackTcp();
                int code = jsonCMD.optInt("code", -1);
                if (code == 0) {
                    setLoginStatus(true);
//				String call_state = jsonCMD.optString(DfineAction.SYNC_CALL_STATE_KEY);
//				if (!TextUtils.isEmpty(call_state)) {
//					syncCallStateByHeartBeat(call_state);
//				}
                    /***********dongfuhai add***************/
                    sendFetchMessageCommand();
                    /***********dongfuhai add***************/
                    if ((DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_REAL
                            && callState == RealCallState.CALLING)
                            || (DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_WILDCAT
                            && wildcatState == WildCatState.CALLING)) {
                        if (m_jsonWebRTCInfo != null) {
                            boolean needReconnect = false;
                            switch (currentNetState) {
                                case NetUtil.WIFI_NETWORK:
                                    String currentWifiSSID = getWifiInfo();
                                    CustomLog.e(DfineAction.WEBRTC_TAG, "currentWifiSSID = " + currentWifiSSID);
                                    if (TextUtils.isEmpty(last_wifi_ssid) || !last_wifi_ssid.equals(currentWifiSSID)) {
                                        needReconnect = true;
                                    }
                                    break;
                                default:
                                    needReconnect = true;
                                    break;
                            }
                            if (needReconnect) {
                                sendReConnectWebRTC();
                                CreateP2PCallPC(m_jsonWebRTCInfo);
                            }
                        }
                    }
                } else {
                    CustomLog.i(DfineAction.TCP_TAG, "MSG_SignInResponse, code != 0:  " + code);
                    disconnect();
                    //TCP不必要调用这个方法
                    PeiwoApp.getApplication().restartApp(AsynHttpClient.ERR_USER_AUTH, null);
                }
            }
            break;
            case DfineAction.MSG_Heartbeat: {
                if (webrtcReconnectTimer != null) {
                    webrtcReconnectTimer.webrtcReConnectTcpStatus = true;
                }
                handleHeartBeat(jsonCMD);
            }
            break;
            case DfineAction.Response_MSG_Call: {
                if (channel == DfineAction.CALL_CHANNEL_AGORA)
                    callEvent = JSON.parseObject(jsonCMD.toString(), AgoraCallResponseEvent.class);
                CustomLog.i(DfineAction.TCP_TAG, "handleReceiveMsg MSG Call Response");
                if (DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_REAL) {
                    int code = jsonCMD.optInt("code", -1);
                    if (code == 0) {
                        callState = RealCallState.DIAL;
                    }
                    onCallStateChange();
                    String msg = jsonCMD.optString("msg", null);
                    JSONObject data = jsonCMD.optJSONObject("data");
                    onCallResponseForReal(code, msg, data);
                } else {
                    CustomLog.i(DfineAction.TCP_TAG, "handleReceiveMsg MSG Call Response current state error");
                }
            }
            break;
            case DfineAction.MSG_IncomingCall: {
                boolean isCalling = PeiwoApp.getApplication().getIsCalling();
                CustomLog.i(DfineAction.WEBRTC_TAG, "MSG_IncomingCall ：" + jsonCMD.toString());
                if (isCalling) {
                    TcpProxy.getInstance().rejectCall("");
                    return;
                }
                if (channel == DfineAction.CALL_CHANNEL_AGORA) {
                    m_nRoleState = ROLE_FIRSTLISTENER;
                    callState = RealCallState.INCOMING;
                    webRTCConnectionState = "";
                    callEvent = JSON.parseObject(jsonCMD.toString(), AgoraCalledMessageEvent.class);
                    inAgoraCallinActivity((AgoraCalledMessageEvent) callEvent);
                    return;
                }
                int tuid = 0;
                JSONObject data = jsonCMD.optJSONObject("data");
                if (data != null) {
                    JSONObject userObject = data.optJSONObject("user");
                    if (userObject != null) {
                        tuid = userObject.optInt("uid", 0);
                    }
                }
                if (UserManager.getUid(this) != tuid) {
                    m_nRoleState = ROLE_FIRSTLISTENER;
                    callState = RealCallState.INCOMING;
                    webRTCConnectionState = "";
                    onIncomingCall(data);
                }
            }
            break;
            case DfineAction.MSG_WILDCAT_TRUTH_MESSAGE_RESPONSE: {
                CustomLog.i(DfineAction.WEBRTC_TAG, "MSG_Wildcat_Truth_Message_Response");
                if (DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_WILDCAT) {
                    final int code = jsonCMD.optInt("code", -1);
                    final JSONObject data = jsonCMD.optJSONObject("data");
                    if (code != 0) {
                        return;
                    }
                    String title = data.optString("title");
                    String content = data.optString("content");
                    getWildcatTruthMessage(title, content);
                }
            }
            break;
            case DfineAction.MSG_WILDCAT_MATCH_SUCCESS: {
                m_BinderThis.sendWildcatMatchSuccessResponse();
                JSONObject userData = jsonCMD.optJSONObject("user");
                String reports = jsonCMD.optString("reports");
                int wildcat_wait_max_time = jsonCMD.optInt("wildcat_wait_max_time");
                onWildCatMatchSuccess(userData, reports, wildcat_wait_max_time);
            }
            break;
            case DfineAction.MSG_WILDCAT_REQUEST_CANCEL_RESPONSE: {

            }
            break;
            case DfineAction.MSG_WILDCAT_OTHER_CANCEL: {
                onWildCatCancelAnswer();
            }
            break;
            case DfineAction.MSG_CallReady: {
                // 主叫和被叫同时收到CallReady消息
                int code = jsonCMD.optInt("code", -1);
                String msg = jsonCMD.optString("msg", null);
                JSONObject data = jsonCMD.optJSONObject("data");
                JSONObject userData = jsonCMD.optJSONObject("user");
                if (code != 0) {
                    return;
                }
                int call_id = data.optInt("call_id");
                m_BinderThis.setCallId(call_id);
                if (DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_REAL) {
                    onCallReadyForReal(code, msg, call_id);
                    if (RealCallActivity.WEBRTC_CODE_REJECT == code) {
                        callState = RealCallState.IDEL;
                    } else {
                        callState = RealCallState.CALLREADY;
                        CreateP2PCallPC(data);
                    }
                } else if (DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_WILDCAT) {
                    if (data.has("caller_id")) {
                        if (data.optInt("caller_id", 0) == UserManager.getUid(mContext)) {
                            // 主叫ID是自己
                            m_nRoleState = ROLE_MAINCALLER;
                        } else {
                            m_nRoleState = ROLE_FIRSTLISTENER;
                        }
                        wildcatState = WildCatState.CALLREADY;
                        //将自己的昵称添加进去，匿名聊要发送自己当前的昵称给对方申请好友
                        try {
                            userData.put("my_nickname", jsonCMD.optString("my_nickname"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (channel == DfineAction.CALL_CHANNEL_AGORA) {
                            callEvent = JSON.parseObject(jsonCMD.toString(), AgoraWildCallReadyEvent.class);
                        } else {
                            onCallReadyForWildcat(code, msg, data, userData);
                            CreateP2PCallPC(data);
                        }
                    }
                }
            }
            break;
            case DfineAction.MSG_ExchangeInfo: {
                JSONObject data = jsonCMD.optJSONObject("data");
                handleExchangeData(data);
            }
            break;
            case DfineAction.MSG_STOPCALL_MESSAGE: {

            }
            break;
            case DfineAction.MSG_HANGUP_BY_SVR:
                if (channel == DfineAction.CALL_CHANNEL_AGORA)
                    callEvent = new AgoraHungUpByServEvent();
            case DfineAction.MSG_STOPCALL_RESPONSE: {
                stopWebRtcReconnectAlarm();
                m_jsonWebRTCInfo = null;
                if (DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_REAL) {
                    HangUpByRemoteForReal(jsonCMD.toString());
                } else if (DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_WILDCAT) {
                    HangUpByRemoteForWildcat(jsonCMD);
                }
                if (channel == DfineAction.CALL_CHANNEL_AGORA) {
                    callEvent = JSON.parseObject(jsonCMD.toString(), AgoraStopCallResponseEvent.class);
                }
            }
            break;
            case DfineAction.MSG_CallBeginResponse: {
                CustomLog.i(DfineAction.WEBRTC_TAG, "MSG_CallBeginResponse");
                if (DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_REAL) {
                    callState = RealCallState.CALLING;
                    onCallBeginResponseForReal();
                } else {
                    wildcatState = WildCatState.CALLING;
                    onCallBeginResponseForWildCat();
                }
            }
            break;
            case DfineAction.MSG_WILDCAT_MATCHING_RESPONSE: {
                wildcatState = WildCatState.MATCHING;
                callEvent = JSON.parseObject(jsonCMD.toString(), AgoraWildCallResponseEvent.class);
            }
            break;
            case DfineAction.MSG_WILDCAT_EXIT_MATCHING_RESPONSE: {
                onStopWildcatResponseMessage(jsonCMD);
            }
            break;
            case DfineAction.MSG_WILDCAT_LIKE_RESPONSE: {
                CustomLog.i(DfineAction.WEBRTC_TAG, "MSG_LIKE_RESPONSE");
                clearSchedule(jsonCMD);
            }
            break;
            case DfineAction.MSG_WILDCAT_INFINITE_MODE: {
                CustomLog.i("hubserverthread receive WILDCATRE PUTATION NICE MESSAGE");
                onWildcatReputationNiceMessage();
            }
            break;
            case DfineAction.MSG_CallHeartbeatMessage: {
                // lastReceiveCallHeartBeatMessage = System.currentTimeMillis();
                CustomLog.i("socket receive call heart beat message");
            }
            break;
            case DfineAction.MSG_SendMessageFaileResponse: {
                CustomLog.i("socket receive MSG_SendMessageFaileResponse");
                String errorMsg = jsonCMD.optJSONObject("extra").optString("fail_msg");
                int fail_type = jsonCMD.optInt("fail_type");
                Intent intent = new Intent();
                intent.putExtra("fail_type", fail_type);
                intent.putExtra("errorMsg", errorMsg);
                if (!TextUtils.isEmpty(errorMsg)) {
                    EventBus.getDefault().post(new SendMsgErrorEvent(intent));
                }
                PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        MessageUtil.updateMessageFaile(mContext, jsonCMD);
                    }
                });
            }
            break;
            case DfineAction.MSG_SendMessageSuccessResponse: {
                CustomLog.i("socket receive MSG_SendMessageSuccessResponse");
                PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        MessageUtil.updateMessageSuccess(mContext, jsonCMD);
                    }
                });
                EventBus.getDefault().post(new SendMsgSuccessEvent());
            }
            break;
            case DfineAction.MSG_ReceiveMessage: {
                CustomLog.i("socket receive MSG_ReceiveMessage");
                PeiwoApp.getApplication().mExecutorService.execute(() -> {
                    try {
                        JSONArray responseArray = new JSONArray();
                        JSONArray array = jsonCMD.getJSONArray("data");
                        if (array != null && array.length() > 0) {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject messageObject = array.getJSONObject(i);
                                JSONArray d_array = messageObject.getJSONArray("dialogs");
                                for (int j = 0; j < d_array.length(); j++) {
                                    responseArray.put(d_array.getJSONObject(j).getInt("dialog_id"));
                                }
                            }
                            receiveMessageResponse(responseArray);
                            CustomLog.i(DfineAction.TCP_TAG, "[msg_type]:[" + jsonCMD.getInt("msg_type") + "]." + "receiveJsonCMD" + jsonCMD.toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    MessageUtil.receivesMessage(mContext, jsonCMD);
                });
            }
            break;
            case DfineAction.MSG_FOCUS_SUCCES: {
                clearSchedule(jsonCMD);
                EventBus.getDefault().post(new FocusEvent(FocusEvent.FOCUS_SUCCESS_EVENT));
            }
            break;
            case DfineAction.MSG_FOCUS_ERROR: {
                String err_msg = jsonCMD.optJSONObject("extra").optString("fail_msg");
                EventBus.getDefault().post(new FocusEvent(err_msg));
            }
            break;
            case DfineAction.MSG_UNFOCUS_SUCCES: {
                EventBus.getDefault().post(new FocusEvent(FocusEvent.UNFOCUS_SUCCESS_EVENT));
            }
            break;
            case DfineAction.MSG_UPDATE_USER_HOTVALUE: {
                //接收到热度改变
//                int hot_values = jsonCMD.optInt("hot_value");
//                Intent hotValueIntent = new Intent();
//                hotValueIntent.putExtra("hot_values", hot_values);
//                EventBus.getDefault().post(new HotValueChangeEvent(hotValueIntent));
            }
            break;
            case DfineAction.MSG_FEED_PUB_LIKE_RESPONSE:
            case DfineAction.MSG_FEED_PUB_UNLIKE_RESPONSE:
                //接收到信息流点赞成功   {"last_update_time":1.431142538458407E9,"status":1,"code":0,"uid":2098,"msg_type":226,"ssn":"c636a4ce","sid":36273,"seq":11}
//			String feed_id = jsonCMD.optString("feed_id");
//			FeedFlowActivity.likerHandleMap.remove(feed_id);
                break;
            case DfineAction.MSG_RECEIVE_PUB_LIKE_UNLIKE_NOTIFY:
        /*case DfineAction.MSG_RECEIVE_PUB_UNLIKE_NOTIFY : */
            {
                //有人对我的信息流点赞
//			if (DfineAction.MSG_RECEIVE_PUB_LIKE_NOTIFY == msg_type) {
//				TcpProxy.getInstance().answerPubLike(false);
//			} else {
                TcpProxy.getInstance().answerPubLike(true);
//			}

                JSONObject dataJson = jsonCMD.optJSONObject("data");
                if (dataJson != null) {
                    int likeCount = dataJson.optInt("count");
                    SharedPreferencesUtil.putIntExtra(mContext, "like_num_" + UserManager.getUid(mContext), likeCount);
                    EventBus.getDefault().post(new RedPonitVisibilityEvent(likeCount));
                }
            }
            break;
            case DfineAction.MSG_FEED_PUB_FRIEND_NOTIFY_RESPONSE: {
                System.out.println("CoreService.handleReceiveMsg(), MSG_FEED_PUB_FRIEND_NOTIFY_RESPONSE!");
                JSONObject dataJson = jsonCMD.optJSONObject("data");
                int count = dataJson.optInt("count");
                EventBus.getDefault().post(new RedPonitVisibilityEvent(count));
            }
            break;
            case DfineAction.IntentRewardResponseMessage:
                Intent data = new Intent();
                data.putExtra("data", jsonCMD.toString());
                data.putExtra("type", DfineAction.IntentRewardResponseMessage);
                EventBus.getDefault().post(new WildCatMessageEvent(data));
                EventBus.getDefault().post(new RealCallMessageEvent(data));
                break;
            case DfineAction.PayRewardResponseMessage:
                Intent pay_data = new Intent();
                pay_data.putExtra("data", jsonCMD.toString());
                pay_data.putExtra("type", DfineAction.PayRewardResponseMessage);
                EventBus.getDefault().post(new WildCatMessageEvent(pay_data));
                EventBus.getDefault().post(new RealCallMessageEvent(pay_data));
                break;
            case DfineAction.RewardedMessage:
                //收到对方打赏
                Intent reward_data = new Intent();
                reward_data.putExtra("data", jsonCMD.toString());
                reward_data.putExtra("type", DfineAction.RewardedMessage);
                EventBus.getDefault().post(new WildCatMessageEvent(reward_data));
                EventBus.getDefault().post(new RealCallMessageEvent(reward_data));
                break;
            default:
                break;
        }
        if (callEvent != null)
            RxBus.provider().send(callEvent);
    }


    private void inAgoraCallinActivity(AgoraCalledMessageEvent calledMessageEvent) {
        if (calledMessageEvent.data.channel == DfineAction.CALL_CHANNEL_AGORA) {
            Intent intent = new Intent(this, AgoraCallInActivity.class);
            intent.putExtra(AgoraCallInActivity.K_CALLED_EVENT, calledMessageEvent);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


    private void setWebRTCState(int state) {
        m_nWebRTCState = state;
        onCallStateChange();
    }

    private WebrtcReConnectTimer webrtcReconnectTimer = null;

    public void startWebRtcConnectAlarm() {
        if (webrtcReconnectTimer != null)
            return;
        CustomLog.i(DfineAction.TCP_TAG, "开始webrtc重连倒计时中");
        //setWebRTCState(WEBRTC_STATE_LOST);
        webrtcReconnectTimer = new WebrtcReConnectTimer();
        webrtcReconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //webrtc连接超时，如果此期间心跳正常，只断开电话，如果心跳也不正常，断开连接
                CallStateEvent event = new CallStateEvent();
                event.type = 2;
                sendRTCWildCallEvent(event);
                EventBus.getDefault().post(event);

                //2、如果Tcp连接正常，则不做任何处理，不正常重连tcp
                if (webrtcReconnectTimer != null && !webrtcReconnectTimer.webrtcReConnectTcpStatus) {
                    reConnection(null, 0);
                }
            }
        }, WEBRTC_RECONNECT_MAX_TIME);
    }

    public void stopWebRtcReconnectAlarm() {
        CustomLog.i(DfineAction.TCP_TAG, "结束webrtc重连倒计时");
        if (webrtcReconnectTimer != null) {
            webrtcReconnectTimer.cancel();
            webrtcReconnectTimer = null;
        }
    }

    protected int WaitWebRTCConnection() {
//		ToastMsgToMainThread("等待语音链路建立...");
        setWebRTCState(WEBRTC_STATE_CONNECTING);
//		m_eventHubThread.onCallStateChange();
//		m_bISWebRTCLost = true;
        return 0;
    }

    protected String getWifiInfo() {
        String wifi_ssid = "";
        try {
            WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (wifiMgr != null) {
                WifiInfo info = wifiMgr.getConnectionInfo();
                wifi_ssid = (info != null ? info.getSSID() : null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wifi_ssid;
    }

    public void startLoginBackTcp() {
        Intent intent = new Intent(this, TcpLoginBackReceiver.class);
        PendingIntent pendIntent = PendingIntent.getBroadcast(this, DfineAction.ALARM_RECEIVER_TCP_LOGIN_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // 设置一个PendingIntent对象，发送广播
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + TCP_LOGIN_BACK, pendIntent);
    }

    public void stopLoginBackTcp() {
        Intent intent = new Intent(this, TcpLoginBackReceiver.class);
        PendingIntent pendIntent = PendingIntent.getBroadcast(this, DfineAction.ALARM_RECEIVER_TCP_LOGIN_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // 与上面的intent匹配（filterEquals(intent)）的闹钟会被取消
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).cancel(pendIntent);
    }


    public void startWaitHeartBeatTimeOut() {
        Intent intent = new Intent(this, HeartBeatTimeOutReceiver.class);
        PendingIntent pendIntent = PendingIntent.getBroadcast(this, DfineAction.ALARM_RECEIVER_HEARTBEAT_TIMEOUT_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // 设置一个PendingIntent对象，发送广播
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + HEARTBEAT_TIMEOUT, pendIntent);
    }

    public void stopWaitHeartBeatTimeOut() {
        Intent intent = new Intent(this, HeartBeatTimeOutReceiver.class);
        PendingIntent pendIntent = PendingIntent.getBroadcast(this, DfineAction.ALARM_RECEIVER_HEARTBEAT_TIMEOUT_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // 与上面的intent匹配（filterEquals(intent)）的闹钟会被取消
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).cancel(pendIntent);
    }


    public void onEventMainThread(ServiceMessageEvent event) {
        Intent intent = event.intent;
        if (intent == null) {
            return;
        }
        String action = event.intent.getAction();
        if (action == null) {
            return;
        }
        if (action.equals(DfineAction.EVENT_ACTION_CONNECT_TCP)) {
            PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    CustomLog.i(DfineAction.TCP_TAG, "user operation tcp connect");
                    tcpConnection(null, 0);
                }
            });
        }
    }
    //********************************************************
    //********************************************************

//  protected DataChannel m_pwDC;

    protected PeerConnectionFactory m_factoryPC;
    protected PeerConnection m_pc2P_Call;
    protected ArrayList<PeerConnection> m_listPCOther = new ArrayList<PeerConnection>();
    protected MediaConstraints m_sdpMediaConstraints = new MediaConstraints();
    // 本地描述符
    protected SessionDescription m_localSD = null;
    // 可用通道列表
    protected AtomicBoolean inited_PeerConnectionFactory = new AtomicBoolean(false);

    protected void initWebRtc() {
        if (!inited_PeerConnectionFactory.get()) {
            inited_PeerConnectionFactory.set(true);
            PeerConnectionFactory.initializeAndroidGlobals(mContext, true, false, true, null);
        }
        m_sdpMediaConstraints.mandatory.add(new KeyValuePair(
                "OfferToReceiveAudio", "true"));
        m_listPCOther.clear();
    }

    // pw 创建PeerConnection
    protected PeerConnection CreatePC(LinkedList<PeerConnection.IceServer> listICE) {
        if (PeiwoApp.getApplication().getNetType() == NetUtil.WIFI_NETWORK) {
            last_wifi_ssid = getWifiInfo();
        } else {
            last_wifi_ssid = "";
        }
        CustomLog.e(DfineAction.WEBRTC_TAG, "last_wifi_ssid = " + last_wifi_ssid);
        m_factoryPC = new PeerConnectionFactory();
        MediaConstraints pcConstraints = new MediaConstraints();
        pcConstraints.mandatory.add(new KeyValuePair("OfferToReceiveAudio", "true"));
        pcConstraints.optional.add(new KeyValuePair("internalSctpDataChannels", "true"));
        pcConstraints.optional.add(new KeyValuePair("DtlsSrtpKeyAgreement", "false"));

        PeerConnection pc = m_factoryPC.createPeerConnection(listICE, pcConstraints, this);
        MediaStream lMS = m_factoryPC.createLocalMediaStream("ARDAMS");
        AudioTrack d = m_factoryPC.createAudioTrack("ARDAMSa0", m_factoryPC.createAudioSource(pcConstraints));
        lMS.addTrack(d);

        pc.addStream(lMS);
        pc.getStats(new StatsObserver() {
            @Override
            public void onComplete(StatsReport[] arg0) {
                CustomLog.i(DfineAction.WEBRTC_TAG, "StatsObserver::onComplete");
            }
        }, d);
        return pc;
    }

    public void closeRTC() {
        CustomLog.i(DfineAction.WEBRTC_TAG, "[begin] close RTC");
        if (m_pc2P_Call != null) {
            m_pc2P_Call.dispose();
            m_pc2P_Call = null;
        }
        for (PeerConnection pc : m_listPCOther) {
            pc.dispose();
            pc.close();
        }
        m_listPCOther.clear();
        stopWebRtcReconnectAlarm();
        CustomLog.i(DfineAction.WEBRTC_TAG, "[end] close RTC");
    }


    protected final Object WebRTCSync = new Object();
    protected JSONObject m_jsonWebRTCInfo = null;

    protected void CreateP2PCallPC(JSONObject dataRC) {
        synchronized (WebRTCSync) {
            CustomLog.i(DfineAction.WEBRTC_TAG, "CreateP2PCallPC dataRC = " + dataRC);
            if (m_pc2P_Call != null) {
                CustomLog.i(DfineAction.WEBRTC_TAG, "CreateP2PCallPC m_pc2P_Call != null");
                closeRTC();
            }
            m_jsonWebRTCInfo = dataRC;
            if (dataRC == null)
                return;
            // 解析ICEServer
            CustomLog.i(DfineAction.WEBRTC_TAG, "StatsObserver::onComplete");
            String password = dataRC.optString("password");
            JSONArray uris = dataRC.optJSONArray("uris");
            LinkedList<PeerConnection.IceServer> listIceServer = new LinkedList<PeerConnection.IceServer>();
            if (uris != null) {
                CustomLog.i(DfineAction.WEBRTC_TAG, "prepareCall : " + uris.length());
                for (int i = 0; i < uris.length(); i++) {
                    String url = uris.optString(i);
                    listIceServer.add(new PeerConnection.IceServer(url, String
                            .valueOf(UserManager.getUid(mContext)), password));
                    CustomLog.i(DfineAction.WEBRTC_TAG, "prepareCall : " + url);
                }
            } else {
                CustomLog.i(DfineAction.WEBRTC_TAG, "prepareCall : null");
            }

            m_pc2P_Call = CreatePC(listIceServer);

            if (m_nRoleState == ROLE_MAINCALLER) {
                // 缓存主叫的webRTC凭据信息

                CustomLog.i(DfineAction.WEBRTC_TAG, "主叫: createOffer");
                m_pc2P_Call.createOffer(this, m_sdpMediaConstraints);
            }
            setWebRTCState(WEBRTC_STATE_EXCHANGINGSD);
            startWebRtcConnectAlarm();
            setWebRTCState(WEBRTC_STATE_LOST);
        }
    }

    protected int CreateRoomHolderPCs(JSONObject dataRC, int nCount) {
        /*
         * {"call_id":768,"password":"4882baba26ddc86b7de467821bf4c3ce","video":0,
		 * "uris":["stun:120.24.232.62:9030","turn:120.24.232.62:9030?transport=udp"
		 * ]}
		 */
        // 解析ICEServer
        JSONObject data = dataRC;
        CustomLog.i(DfineAction.WEBRTC_TAG, "CreateRoomHolderPCs data = " + data);
        String password = data.optString("password");
        JSONArray uris = data.optJSONArray("uris");
        LinkedList<PeerConnection.IceServer> listIceServer = new LinkedList<PeerConnection.IceServer>();
        if (uris != null) {
            CustomLog.i(DfineAction.WEBRTC_TAG, "prepareCall : " + uris.length());
            for (int i = 0; i < uris.length(); i++) {
                String url = uris.optString(i);
                listIceServer.add(new PeerConnection.IceServer(url, String
                        .valueOf(UserManager.getUid(mContext)), password));
                CustomLog.i(DfineAction.WEBRTC_TAG, "prepareCall : " + url);
            }
        }

        // 增加一个受邀访问
        m_listPCOther.add(CreatePC(listIceServer));
        for (PeerConnection pc : m_listPCOther) {
            pc.createOffer(this, m_sdpMediaConstraints);
        }
        return 0;
    }

    // pw handleExchangeData
    protected int handleExchangeData(JSONObject data) {
        // 第一个接收到该消息的是被叫方
        // 被叫方需要在第一个该消息中处理主叫方发来的Offer信息
        CustomLog.i(DfineAction.WEBRTC_TAG, "handleExchangeData data = " + data);
        if (m_pc2P_Call == null) {
            CustomLog.i(DfineAction.WEBRTC_TAG, "handleExchangeData  but m_pc2P_Call is null");
            return 0;
        }
        try {
            String type = (String) data.get("type");
            CustomLog.i(DfineAction.WEBRTC_TAG, "handleExchangeData type is : " + type);
            if (type.equals("candidate")) {
                IceCandidate candidate = new IceCandidate(
                        (String) data.get("id"), data.getInt("label"),
                        (String) data.get("candidate"));
                if (m_pc2P_Call != null) {
                    m_pc2P_Call.addIceCandidate(candidate);
                }
            } else if (type.equals("answer") || type.equals("offer")) {
                if (type.equals("offer")) {
                    CustomLog.i(DfineAction.WEBRTC_TAG, "被叫: recieve offer,  setRemoteDescription");
                } else {
                    CustomLog.i(DfineAction.WEBRTC_TAG, "主叫: recieve answer,  setRemoteDescription");
                }

                SessionDescription sdRecieve = new SessionDescription(
                        SessionDescription.Type.fromCanonicalForm(type),
                        data.getString("sdp"));

                // 设置完成后，又会转到onSetSuccess
                webRTCState = WebRTCState.SetRemoteDescription;
                m_pc2P_Call.setRemoteDescription(this, sdRecieve);
            } else if (type.equals("bye")) {
                CustomLog.i(DfineAction.WEBRTC_TAG, "handleExchangeData bye");
                m_BinderThis.stopCallForMe(DfineAction.STOP_CALL_OTHER);
            } else if (type.equals("reconnect")) {
                CustomLog.i(DfineAction.WEBRTC_TAG, "handleExchangeData reconnect");
                if (m_jsonWebRTCInfo != null) {
                    CreateP2PCallPC(m_jsonWebRTCInfo);
                }
            } else {
                CustomLog.i(DfineAction.WEBRTC_TAG, "handleExchangeData " + type);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    private void sendHeartBeat(JSONObject json) {
        try {
            if (DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_REAL
                    || DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_WILDCAT) {
                if (!TextUtils.isEmpty(webRTCConnectionState)) {
                    json.put("webrtc_state", webRTCConnectionState);
                }
            }
            json.put("msg_type", DfineAction.MSG_Heartbeat);
            json.put("iscall", PeiwoApp.getApplication().getIsCalling() ? 1 : 0);
            sendPacket(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void handleHeartBeat(JSONObject json) {
        if (json == null) {
            json = new JSONObject();
        }
        long current_seq = -1;
        if (json.has("heart_seq")) {//计数自己心跳包丢失情况
            current_seq = json.optLong("heart_seq");
            if (heart_seq == current_seq) {//上个心跳包丢失
                heart_lost_count++;
                CustomLog.e(DfineAction.TCP_TAG, "***********new bad lost " + heart_lost_count + " heart beat***********");
                if (heart_lost_count >= 3) {
                    //已丢失三个心跳包，需要重连;
                    CustomLog.e(DfineAction.TCP_TAG, "***********lost 3 heart beat reConnection********");
                    reConnection(null, 0);
                }
            } else {
                heart_lost_count = 0;
            }
            heart_seq = current_seq;
        }
        if (json.has("remote_user_state")) {//通话中记录对方心跳包丢失情况
            remote_user_state = json.optInt("remote_user_state");
        }
        if (json.has("wildcat_state")) {//限时聊匹配进度，用于界面显示
            int wildcat_state = json.optInt("wildcat_state");
            if (DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_WILDCAT) {
                EventBus.getDefault().post(new WildCatMatchStateEvent(wildcat_state));
            }
        }
        if (current_seq > 0 && current_seq != 1) {
            if (json.has(DfineAction.SYNC_CALL_STATE_KEY)) {//服务器状态，与客户端状态保持一致
                String call_state = json.optString(DfineAction.SYNC_CALL_STATE_KEY);
                syncCallStateByHeartBeat(call_state);
            }
        }
        onNetStateChange();
        //回复心跳包
        sendHeartBeat(json);
    }

    /**
     * 用于同步服务器通话状态，保持状态一致
     *
     * @param call_state
     */
    private void syncCallStateByHeartBeat(String call_state) {
        if (TextUtils.isEmpty(call_state)) {
            return;
        }
        CustomLog.i(DfineAction.TCP_TAG, "****************************server call_state = " + call_state
                + ", client callstate = " + callState + ", wildcatState = " + wildcatState);
        if (call_state.equals(DfineAction.SYNC_STATE_OFFLINE)) {
            //服务器认定为离线状态，需要重新signin认证
            if (!isLoginStauts()) {
                m_BinderThis.sendSignInMessage();
            }
        } else if (call_state.equals(DfineAction.SYNC_STATE_IDEL)) {
            //服务器为闲置状态时
            if (DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_REAL) {
                //当前在通话界面，非Idel状态时，需要结束当前通话界面
                if (callState != RealCallState.IDEL) {
                    HangUpByRemoteForReal("");
                }
            } else if (DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_WILDCAT) {
                //当前在限时聊界面，非Idel状态时
                if (wildcatState == WildCatState.CALLING || wildcatState == WildCatState.CALLREADY) {
                    HangUpByRemoteForWildcat(new JSONObject());
                } else if (wildcatState == WildCatState.MATCHING) {
                    //这个地方拿不到wildcatactivity中的星座值，不传
                    m_BinderThis.sendWildcatMessage(UserManager.getGender(this) == 1 ? 2 : 1, 0, 0, "");
                }
            }
        } else if (call_state.equals(DfineAction.SYNC_STATE_CALLING_DIAL)) {
            if (DfineAction.CURRENT_CALL_STATUS != DfineAction.CURRENT_CALL_REAL
                    || (callState != RealCallState.DIAL
                    && callState != RealCallState.CALLREADY && callState != RealCallState.CALLING)) {
                CustomLog.i(DfineAction.TCP_TAG, "########################################server call_state = " + call_state
                        + ", client callstate = " + callState + ", wildcatState = " + wildcatState);
                m_BinderThis.stopCallForMe(DfineAction.STOP_CALL_OTHER);
            }
        } else if (call_state.equals(DfineAction.SYNC_STATE_CALLING_INCOMING)) {
            if (DfineAction.CURRENT_CALL_STATUS != DfineAction.CURRENT_CALL_REAL
                    || (callState != RealCallState.INCOMING
                    && callState != RealCallState.CALLREADY && callState != RealCallState.CALLING)) {
                m_BinderThis.stopCallForMe(DfineAction.STOP_CALL_OTHER);
            }
        } else if (call_state.equals(DfineAction.SYNC_STATE_CALLING_2PChat)) {
            if (DfineAction.CURRENT_CALL_STATUS != DfineAction.CURRENT_CALL_REAL
                    || (callState != RealCallState.CALLING && callState != RealCallState.CALLREADY)) {
                m_BinderThis.stopCallForMe(DfineAction.STOP_CALL_OTHER);
            }
        } else if (call_state.equals(DfineAction.SYNC_STATE_ANONYMOUSE_MATCHING)) {
            if (DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_WILDCAT) {
                if (wildcatState == WildCatState.CALLING) {
                    m_BinderThis.stopCallForMe(DfineAction.STOP_CALL_OTHER);
                }
            } else {
                m_BinderThis.sendStopWildcatMessage();
            }
        } else if (call_state.equals(DfineAction.SYNC_STATE_ANONYMOUSE_CHATING)) {
            if (DfineAction.CURRENT_CALL_STATUS == DfineAction.CURRENT_CALL_WILDCAT) {
                if (wildcatState == WildCatState.IDEL || wildcatState == WildCatState.MATCHING) {
                    m_BinderThis.stopCallForMe(DfineAction.STOP_CALL_OTHER);
                }
            } else {
                m_BinderThis.stopCallForMe(DfineAction.STOP_CALL_OTHER);
                m_BinderThis.sendStopWildcatMessage();
            }
        }
    }

    public void receiveMessageResponse(JSONArray dialogArray) {
        CustomLog.i("receiveMessageResponse dialogArray = " + dialogArray);
        JSONObject json = new JSONObject();
        try {
            json.put("msg_type", DfineAction.MSG_ReceiveMessageResponse);
            json.put("dialog_ids", dialogArray);
            sendPacket(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /****************dongfuhai add**********************/
    /**
     * 收到signin response之后发送这个消息
     */
    private void sendFetchMessageCommand() {
        JSONObject json = new JSONObject();
        try {
            json.put("msg_type", DfineAction.MSG_FETCHMESSAGE);
            sendPacket(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /****************
     * dongfuhai add
     **********************/

    protected void sendCallBeginCommand() {
        JSONObject json = new JSONObject();
        try {
            json.put("msg_type", DfineAction.MSG_CallBeginMessage);
            sendPacket(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void sendReConnectWebRTC() {
        JSONObject data = new JSONObject();
        try {
            data.put("type", "reconnect");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        exchangeInfo(data);
    }

    protected void exchangeInfo(JSONObject data) {
        JSONObject json = new JSONObject();
        try {
            json.put("msg_type", DfineAction.MSG_ExchangeInfo);
            json.put("data", data);
            sendPacket(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void sendLocalDescription() {
        JSONObject data = new JSONObject();
        try {
            data.put("type", m_localSD.type.canonicalForm());
            data.put("sdp", m_localSD.description);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        exchangeInfo(data);
    }

    // pw JHFkk_BroadCast
/*	protected void JHFkk_Broadcast(JSONObject dataToSend) {
        CustomLog.i(DfineAction.WEBRTC_TAG, "JHFkk_Broadcast");
		// 仅由主叫方发起
		// 主叫方状态变为房主
		try {
			JSONObject data = new JSONObject();
			data.put("content", dataToSend);
			JSONObject json = new JSONObject();
			json.put("msg_type", DfineAction.MSG_JHFkk);
			json.put("data", data);
			sendPacket(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}*/

/*	protected int BroadCastLocalDescription() {
        m_log.Debug("BroadCastLocalDescription");
		JSONObject data = new JSONObject();
		if (m_nRoleState == ROLE_ROMMHOLDER) {
			try {
				data.put("type", "RoomHolderInfo");
				// 找到一个没有被占用的发出去
				for (PeerConnection pc : m_listPCOther) {
					if ((pc.getRemoteDescription() == null)
							&& (pc.getLocalDescription() != null)) {
						data.put("sd_type", pc.getLocalDescription().type);
						data.put("sd", pc.getLocalDescription().description);
						break;
					}
				}
			} catch (JSONException e) {
			}
		} else if (m_nRoleState == ROLE_ROMMMATE) {
			try {
				data.put("type", "RoomMateInfo");
				data.put("sd_type", m_localSD.type.canonicalForm());
				data.put("sd", m_localSD.description);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		JHFkk_Broadcast(data);
		return 0;
	}*/

    //WebRtc的Observer和SdpObserver回调接口
    @Override
    public void onCreateFailure(String arg0) {
        CustomLog.i(DfineAction.WEBRTC_TAG, "onCreateFailure arg0 = " + arg0);
    }

    @Override
    public void onCreateSuccess(SessionDescription sdORIG) {
        CustomLog.i(DfineAction.WEBRTC_TAG, "onCreateSuccess sdORIG = " + sdORIG);
        if ((m_nRoleState == ROLE_MAINCALLER)
                || (m_nRoleState == ROLE_FIRSTLISTENER)
                /*|| (m_nRoleState == ROLE_ROMMMATE)*/) {
            // 双人通话
            m_localSD = sdORIG;
            // 根据Offer创建本地SDP
            // set成功后会导致OnSetSuccess方法
            if (m_nRoleState == ROLE_MAINCALLER) {
                CustomLog.i(DfineAction.WEBRTC_TAG, "主叫: createOffer success,   setLocalDescription");
            } else {
                CustomLog.i(DfineAction.WEBRTC_TAG, "被叫: createAnswer success,  setLocalDescription");
            }
            webRTCState = WebRTCState.SetLocalDescription;
            m_pc2P_Call.setLocalDescription(this, sdORIG);
        }/* else if (m_nRoleState == ROLE_ROMMHOLDER) {
            // 多人通话
			// 从没有LocalDescription的PC中分配一个

			for (PeerConnection pc : m_listPCOther) {
				SessionDescription sd = pc.getLocalDescription();
				if (sd != null)
					continue;
				m_log.Debug("pc.setLocalDescription(HubServerThread.this, sdORIG);");
				// 得到PC
				pc.setLocalDescription(this, sdORIG);
			}
		} else {

		}*/
    }

    @Override
    public void onSetFailure(String arg0) {
        CustomLog.i(DfineAction.WEBRTC_TAG, "onSetFailure arg0 = " + arg0);
    }

    @Override
    public void onSetSuccess() {
        CustomLog.i(DfineAction.WEBRTC_TAG, "onSetSuccess");
        switch (m_nRoleState) {
/*		case ROLE_ROMMHOLDER:
            // 房主,检查是不是已经配对完成
			// if (m_pcCurrent != null)
			// // 配对完成，主叫开始消费缓存的服务器列表
			// drainRemoteCandidates();
			// else
			// // 刚开始配对，主叫广播自己的sd
			// BroadCastLocalDescription();
			break;
		case ROLE_ROMMMATE:
			// 房客,等同于p2p模式
			if (m_pc2P_Call.getLocalDescription() == null) {
				// 如果本地SD还没有创建，则调用createAnswer来创建本地sd信息
				// 创建成功后会转入onCreateSuccess方法
				m_log.Debug("被叫: start createAnswer");
				m_pc2P_Call.createAnswer(this, m_sdpMediaConstraints);
			} else {
				m_log.Debug("被叫: start BroadCastLocalDescription");
				BroadCastLocalDescription();
				m_log.Debug("被叫: start drainRemoteCandidates");
				drainRemoteCandidates();
			}
			break;*/
            case ROLE_MAINCALLER:
                if (webRTCState == WebRTCState.SetLocalDescription) {
                    CustomLog.i(DfineAction.WEBRTC_TAG, "主叫: setLocalDescription success, send offer");
                    sendLocalDescription();
                } else {
                    CustomLog.i(DfineAction.WEBRTC_TAG, "主叫: setRemoteDescription success");
                }
                break;
            case ROLE_FIRSTLISTENER:
                // 被叫方获得Offer并调用m_pc.setRemoteDescription(HubServerThread.this, sdRecieve)成功后会进入 onSetSuccess
                if (webRTCState == WebRTCState.SetLocalDescription) {
                    CustomLog.i(DfineAction.WEBRTC_TAG, "被叫: setLocalDescription success, send answer");
                    sendLocalDescription();
                } else {
                    // 如果本地SD还没有创建，则调用createAnswer来创建本地sd信息
                    // 创建成功后会转入onCreateSuccess方法
                    CustomLog.i(DfineAction.WEBRTC_TAG, "被叫: setRemoteDescription success,  begin createAnswer");
                    m_pc2P_Call.createAnswer(this, m_sdpMediaConstraints);
                }
                break;
        }
    }

    @Override
    public void onAddStream(MediaStream mStream) {
        int size = mStream.audioTracks.size();
        CustomLog.i(DfineAction.WEBRTC_TAG, "onAddStream mStream.audioTracks.size() = " + size);
        if (size == 1) {
            sendCallBeginCommand();
        } else {
            CustomLog.i("onAddStream mStream.audioTracks.size() != 1");
        }
    }

    @Override
    public void onDataChannel(DataChannel arg0) {
        CustomLog.i(DfineAction.WEBRTC_TAG, "onDataChannel arg0 = " + arg0.state());
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {
        if ((m_nRoleState == ROLE_MAINCALLER)
                || (m_nRoleState == ROLE_FIRSTLISTENER)) {
            CustomLog.i(DfineAction.WEBRTC_TAG, "onIceCandidate 1");
            JSONObject data = new JSONObject();
            try {
                data.put("type", "candidate");
                data.put("label", candidate.sdpMLineIndex);
                data.put("id", candidate.sdpMid);
                data.put("candidate", candidate.sdp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            exchangeInfo(data);
        }/* else if ((m_nRoleState == ROLE_ROMMMATE)
                || (m_nRoleState == ROLE_ROMMHOLDER)) {
			m_log.Debug("onIceCandidate 2");
			JSONObject data = new JSONObject();
			try {
				data.put("type", "candidate");
				data.put("label", candidate.sdpMLineIndex);
				data.put("id", candidate.sdpMid);
				data.put("candidate", candidate.sdp);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			JHFkk_Broadcast(data);
		}*/ else {
            CustomLog.i(DfineAction.WEBRTC_TAG, "onIceCandidate 3");
        }
    }

    @Override
    public void onIceConnectionChange(IceConnectionState state) {
        CustomLog.e(DfineAction.WEBRTC_TAG, "onIceConnectionChange state = " + state);
        webRTCConnectionState = state + "";
        if (state == IceConnectionState.DISCONNECTED) {
            // 开始计数，如果40s内无法恢复通话，则需要挂断电话，如果此期间没有正常心跳，则需要重连Tcp
            startWebRtcConnectAlarm();
            setWebRTCState(WEBRTC_STATE_LOST);
        } else if (state == IceConnectionState.CLOSED) {
            // 停止心跳计步器
            startWebRtcConnectAlarm();
        } else if (state == IceConnectionState.COMPLETED) {
        } else if (state == IceConnectionState.CONNECTED) {
            // 停止心跳计步器
            // ToastMsgToMainThread("语音线路已连接");
            setWebRTCState(WEBRTC_STATE_ONLINE);
            stopWebRtcReconnectAlarm();
        } else if (state == IceConnectionState.NEW) {
        } else if (state == IceConnectionState.FAILED) {
            // 开始心跳计步器
            startWebRtcConnectAlarm();
            setWebRTCState(WEBRTC_STATE_LOST);
        }
    }

    @Override
    public void onIceGatheringChange(IceGatheringState iceGatheringStateb) {
        if (iceGatheringStateb == IceGatheringState.COMPLETE) {
            CustomLog.i(DfineAction.WEBRTC_TAG, "onIceGatheringChange  IceGatheringState.COMPLETE");
        } else if (iceGatheringStateb == IceGatheringState.NEW) {
            CustomLog.i(DfineAction.WEBRTC_TAG, "onIceGatheringChange  IceGatheringState.NEW");
        } else {
            CustomLog.i(DfineAction.WEBRTC_TAG, "onIceGatheringChange : " + iceGatheringStateb);
        }
    }

    @Override
    public void onRemoveStream(MediaStream arg0) {
        CustomLog.e(DfineAction.WEBRTC_TAG, "onRemoveStream");
    }

    @Override
    public void onRenegotiationNeeded() {
        CustomLog.e(DfineAction.WEBRTC_TAG, "onRenegotiationNeeded");
    }

    @Override
    public void onSignalingChange(SignalingState arg0) {
        if (arg0 == SignalingState.STABLE) {
            CustomLog.e(DfineAction.WEBRTC_TAG, "onSignalingChange :SignalingState.STABLE");
            WaitWebRTCConnection();
        } else if (arg0 == SignalingState.CLOSED) {
            CustomLog.e(DfineAction.WEBRTC_TAG, "onSignalingChange :SignalingState.CLOSED");
            stopWebRtcReconnectAlarm();
        } else if (arg0 == SignalingState.HAVE_LOCAL_OFFER) {
            CustomLog.e(DfineAction.WEBRTC_TAG, "onSignalingChange :SignalingState.HAVE_LOCAL_OFFER");
        } else if (arg0 == SignalingState.HAVE_LOCAL_PRANSWER) {
            CustomLog.e(DfineAction.WEBRTC_TAG, "onSignalingChange :SignalingState.HAVE_LOCAL_PRANSWER");
        } else if (arg0 == SignalingState.HAVE_REMOTE_OFFER) {
            CustomLog.e(DfineAction.WEBRTC_TAG, "onSignalingChange :SignalingState.HAVE_REMOTE_OFFER");
        } else if (arg0 == SignalingState.HAVE_REMOTE_PRANSWER) {
            CustomLog.e(DfineAction.WEBRTC_TAG, "onSignalingChange :SignalingState.HAVE_REMOTE_PRANSWER");
        } else {
            CustomLog.e(DfineAction.WEBRTC_TAG, "onSignalingChange :Other");
        }
    }
}
