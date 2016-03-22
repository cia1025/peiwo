package me.peiwo.peiwo;

import me.peiwo.peiwo.util.PWTimer;

import java.util.HashMap;

public class DfineAction {
    /*************服务器定义的message key********************/
    /**
     *  # network 0x0001 ~ 0x0020
     0x0001: 'ConnectionMadeMessage',
     0x0002: 'ConnectionLostMessage',
     0x0003: 'HeartbeatMessage',
     # common message 0x0021 ~ 0x0030
     0x0021: 'SignInMessage',
     0x0022: 'SignInResponseMessage',
     0x0023: 'SignOutMessage',
     0x0024: 'SignOutResponseMessage',
     0x0025: 'UserActiveMessage',
     0x0026: 'NotificationMessage',
     # base call 0x0041 ~ 0x0060
     0x0041: 'CallMessage',
     0x0042: 'CallResponseMessage',
     0x0043: 'CalledMessage',
     0x0044: 'AnswerCallMessage',
     0x0045: 'CallReadyMessage',
     0x0046: 'ExchangeInfoMessage',
     0x0047: 'CallBeginMessage',
     0x0048: 'CallBeginResponseMessage',
     0x0049: 'StopCallMessage',
     0x004A: 'StopCallResponseMessage',
     0x004B: 'CallHeartbeatMessage',
     0x004C: 'HangUpBySvrMessage',
     0x004D: 'HangUpBySvrResponseMessage',
     # 内部消息，定时器相关
     0x004E: 'CalleeNoAnswerMessage',
     0x004F: 'CallingHeartbeatTimeoutMessage',
     0x0050: 'DeductMoneyMessage',

     # wildcat - random call 0x0061 ~ 0x0080
     0x0061: 'WildcatMessage',
     0x0062: 'WildcatResponseMessage',
     0x0063: 'StopWildcatMessage',
     0x0064: 'StopWildcatResponseMessage',
     0x0065: 'WildcatLikeMessage',
     0x0066: 'WildcatLikeBothMessage',
     0x0067: 'WildcatTruthMessage',
     0x0068: 'WildcatTruthResponseMessage',
     0x0069: 'WildcatLikeResponseMessage',
     # 内部消息，定时器相关
     0x006A: 'WildcatLikeTimeoutMessage',

     # after v1.8
     0x006B: 'FoundWildcatMatcherMessage',
     0x006C: 'FoundWildcatMatcherRespMessage',
     0x006D: 'AcceptWildcatMatcherMessage',
     # 0x006E: 'AcceptWildcatMatcherRespMessage',
     0x006F: 'ChangeWildcatUserMessage',
     0x0070: 'ChangeWildcatUserRespMessage',
     0x0071: "ChangeByUserMessage",

     # client side 0x0081 ~ 0x00A0
     # only used in client side
     0x0081: 'PeerConnectionMessage',

     # IM messages
     0x00C1: 'IMSendMessage',
     0x00C2: 'IMSendFailMessage',
     0x00C3: 'IMSendSuccessMessage',
     0x00C4: 'IMSentMessage',
     0x00C5: 'IMReceivedMessage',
     0x00C6: 'FocusMessage',
     0x00C7: 'FocusSuccessMessage',
     0x00C8: 'UnFocusMessage',
     0x00C9: 'UnFocusSuccessMessage',
     0x00CA: 'FocusFailMessage',
     0x00CB: 'FetchMessage',

     0x00D1: 'UpdateUserHotValueMessage',

     # Feed messages
     0x00E1: 'FeedPubLikeRequestMessage',
     0x00E2: 'FeedPubLikeResponseMessage',
     0x00E3: 'FeedPubDynamicNotifyMessage',
     0x00E4: 'FeedPubDynamicAnswerMessage',
     0x00E5: 'FeedPubLikeReadRequestMessage',
     0x00E6: 'FeedPubLikeReadResponseMessage',
     0x00E7: 'FeedPubUnlikeRequestMessage',
     0x00E8: 'FeedPubUnlikeResponseMessage',
     0x00E9: 'FeedPubFriendRequestMessage',
     0x00EA: 'FeedPubFriendResponseMessage',

     # Reward message
     0x00F1: 'IntentRewardMessage',
     0x00F2: 'IntentRewardResponseMessage',
     0x00F3: 'PayRewardMessage',
     0x00F4: 'PayRewardResponseMessage',
     0x00F5: 'RewardedMessage',
     */
    /*************
     * 服务器定义的message key
     ********************/
    public static final String TCP_TAG = "pw_tcp";
    public static final String WEBRTC_TAG = "pw_webrtc";
    public static final String HTTP_TAG = "pw_http";

    public static final String TCP_VERSION = "v2.7";

    /**
     * 未通话状态
     */
    public static final int CURRENT_CALL_NOT = 0;
    /**
     * 直拨通话中
     */
    public static final int CURRENT_CALL_REAL = 1;
    /**
     * 限时聊通话中
     */
    public static final int CURRENT_CALL_WILDCAT = 2;

    /**
     * 客户端当前通话状态
     */
    public static int CURRENT_CALL_STATUS = CURRENT_CALL_NOT;


    public static final int INCOMING_CALL_ANSWER = 1;
    public static final int INCOMING_CALL_REJECT = 2;
    public static final int NODISTURB_INCOMING_CALL_REJECT = 3;

    /**
     * 心跳包
     */
    public static final int MSG_Heartbeat = 0x3;
    /**
     * 认证信息，客户端发服务器发送用户认证
     */
    public static final int MSG_SignIn = 0x21;       //33
    /**
     * 认证信息应答
     */
    public static final int Response_MSG_SignIn = 0x22;        //34
    /**
     * 拨打直拨电话 消息类型
     */
    public static final int MSG_Call = 0x41;        //	65
    /**
     * 拨打直拨电话 应答消息类型
     */
    public static final int Response_MSG_Call = 0x42;        //	66
    /**
     * 来电
     */
    public static final int MSG_IncomingCall = 0x43;        //	67
    public static final int MSG_AnswerCall = 0x44;           // 68
    /**
     * 接通电话时收到消息类型
     */
    public static final int MSG_CallReady = 0x45;        //	69
    public static final int MSG_ExchangeInfo = 0x46;        //	70
    // protected static final int MSG_CallBeginResponse = 0x4a;
    public static final int MSG_CallBeginMessage = 0x47;    //	71
    public static final int MSG_CallBeginResponse = 0x48; //72
    // protected static final int MSG_STOPCALLMESSAGE = 0x4b;
    /**
     * 结束通话消息
     */
    public static final int MSG_STOPCALL_MESSAGE = 0x49;    //73
    public static final int MSG_STOPCALL_RESPONSE = 0x4a;   //74

    public static final int MSG_HANGUP_BY_SVR = 0x4C;       //76

    //0x004B: 'CallHeartbeatMessage'
    public static final int MSG_CallHeartbeatMessage = 0x4b;          //75

    /**
     * 申请限时聊匹配消息
     */
    public static final int MSG_WILDCAT_MATCHING = 0x61;        //	97
    /**
     * 申请限时聊匹配消息应答
     */
    public static final int MSG_WILDCAT_MATCHING_RESPONSE = 0x62;    //	98
    /**
     * 退出限时聊匹配
     */
    public static final int MSG_WILDCAT_EXIT_MATCHING = 0x63;    //	99
    /**
     * 退出限时聊匹配应答
     */
    public static final int MSG_WILDCAT_EXIT_MATCHING_RESPONSE = 0x64; //100
    /**
     * 限时聊点赞
     */
    public static final int MSG_WILDCAT_LIKE = 0x65;        // 101
    /**
     * 限时聊点赞应答
     */
    public static final int MSG_WILDCAT_LIKE_RESPONSE = 0x69;        //	105

    /**
     * 双方点赞，无限模式
     */
    public static final int MSG_WILDCAT_INFINITE_MODE = 0x66;    //102
    /**
     * 切换真心话
     */
    public static final int MSG_WILDCAT_TRUTH_MESSAGE = 0x67;         //103
    /**
     * 接收真心话
     */
    public static final int MSG_WILDCAT_TRUTH_MESSAGE_RESPONSE = 0x68;   //104

    public static final int MSG_WILDCAT_MATCH_SUCCESS = 0x6B;
    public static final int MSG_WILDCAT_MATCH_SUCCESS_RESPONSE = 0x6C;

    public static final int MSG_WILDCAT_REQUEST_CALLREADY = 0x6D;
    public static final int MSG_WILDCAT_REQUEST_CANCEL = 0x6F;
    public static final int MSG_WILDCAT_REQUEST_CANCEL_RESPONSE = 0x70;
    public static final int MSG_WILDCAT_OTHER_CANCEL = 0x71;

    /**
     * 发送文本信息
     */
    public static final int MSG_SendTextMessage = 0xC1;                //193
    /**
     * 发送文本信息失败Server应答
     */
    public static final int MSG_SendMessageFaileResponse = 0xC2;       //194
    /**
     * 发送文本信息成功Server应答
     */
    public static final int MSG_SendMessageSuccessResponse = 0xC3;     //195
    /**
     * 消息接收
     */
    public static final int MSG_ReceiveMessage = 0xC4;            //196
    /**
     * 接收到消息后给服务器回执
     */
    public static final int MSG_ReceiveMessageResponse = 0xC5;        //197


    /**
     * 点击关注
     */
    public static final int MSG_FOCUS_USER = 0x00C6;           //198
    /**
     * 关注成功
     */
    public static final int MSG_FOCUS_SUCCES = 0x00C7;              //199
    /**
     * 关注失败
     */
    public static final int MSG_FOCUS_ERROR = 0x00CA;                //202
    /**
     * 取消关注
     */
    public static final int MSG_UNFOCUS_USER = 0x00C8;              //200
    /**
     * 取消关注成功
     */
    public static final int MSG_UNFOCUS_SUCCES = 0x00C9;            //201
    /**
     * 热度发生改变,收到Server应答
     */
    public static final int MSG_UPDATE_USER_HOTVALUE = 0x00D1;      //209

    public static final int MSG_EnterRoom = 0x101;                  //257
    public static final int MSG_RoomInfoChange = 0x102;             //258

    public static final int MSG_FEED_PUB_LIKE_REQUEST = 0xE1;        //225
    public static final int MSG_FEED_PUB_LIKE_RESPONSE = 0xE2;    //226

    public static final int MSG_RECEIVE_PUB_LIKE_UNLIKE_NOTIFY = 0xE3;    //227
    public static final int MSG_RECEIVE_PUB_LIKE_NOTIFY_RESPONSE = 0xE4;    //228

    public static final int MSG_FEED_PUB_READ_REQUEST = 0xE5;    //229
    public static final int MSG_FEED_PUB_READ_RESPONSE = 0xE6;    //230

    public static final int MSG_FEED_PUB_UNLIKE_REQUEST = 0xE7;    //231
    public static final int MSG_FEED_PUB_UNLIKE_RESPONSE = 0xE8;     //232

    public static final int MSG_FEED_PUB_FRIEND_NOTIFY_REQUEST = 0xE9;     //233
    public static final int MSG_FEED_PUB_FRIEND_NOTIFY_RESPONSE = 0xEA;    //234

    public static final String EVENT_ACTION_CONNECT_TCP = "event_action_connect_tcp";

    /**
     * 来电
     */
    public static final int INCOMMING_CALL = 6;
    /**
     * 去电
     */
    public static final int OUTGOING_CALL = 7;


    public static final int ALARM_RECEIVER_TCP_LOGIN_CODE = 0x1001;

    public static final int ALARM_RECEIVER_HEARTBEAT_TIMEOUT_CODE = 0x1002;


    public static final int REAL_STOP_CALL_NORMAL = 200;
    public static final int REAL_STOP_CALL_CALLING_NORMAL = 201;
    public static final int REAL_STOP_CALL_WEBRTC_TIMEOUT = 202;
    public static final int REAL_STOP_CALL_SYSTEM_PHONE = 203;

    public static final int WILDCAT_STOP_CALL_EXIT = 100;
    public static final int WILDCAT_STOP_CALL_NORMAL = 101;
    public static final int WILDCAT_STOP_CALL_LIKE_TIMEOUT = 102;
    public static final int WILDCAT_STOP_CALL_REPORT = 103;
    public static final int WILDCAT_STOP_CALL_WEBRTC_TIMEOUT = 104;
    public static final int WILDCAT_STOP_CALL_SYSTEM_PHONE = 105;


    public static final int IntentRewardMessage = 0x00F1;
    public static final int IntentRewardResponseMessage = 0x00F2;
    public static final int PayRewardMessage = 0x00F3;
    public static final int PayRewardResponseMessage = 0x00F4; //244
    public static final int RewardedMessage = 0x00F5;//245


    /**
     * dongfuhai add
     * 收到signin response之后发送这个消息
     */
    public static final int MSG_FETCHMESSAGE = 0x00CB;    //	0x00CB


    public static final int STOP_CALL_OTHER = -1;

    public static final String MSG_ID_SAYHELLO = "-2000";

    public static final String SYNC_CALL_STATE_KEY = "call_state";
    public static final String SYNC_STATE_OFFLINE = "Offline";
    public static final String SYNC_STATE_IDEL = "Idle";
    public static final String SYNC_STATE_CALLING_DIAL = "Calling_Dial";
    public static final String SYNC_STATE_CALLING_INCOMING = "Calling_Incoming";
    public static final String SYNC_STATE_CALLING_2PChat = "Calling_2PChat";
    public static final String SYNC_STATE_ANONYMOUSE_MATCHING = "Anonymouse_Matching";
    public static final String SYNC_STATE_ANONYMOUSE_CHATING = "Anonymouse_Chating";

//	public static final String SOHU_THUMB_BASE_URL_DEBUG = "http://peiwo-test.bjcnc.img.sohucs.com/s_thumbnail/";
//	public static final String SOHU_BASE_URL_DEBUG = "http://peiwo-test.bjcnc.scs.sohucs.com/";

    public static final String SOHU_THUMB_BASE_URL = "http://22387d6775553.cdn.sohucs.com/s_thumbnail/";
    public static final String SOHU_BASE_URL = "http://5acc299c37fcc.cdn.sohucs.com/";

    public static final int SYSTEM_UID = 1;
    public static final String[] DEFAULT_HTTP_SERVERS = new String[]{/*
            "http://proxy.peiwoapi.com:80", "http://115.28.7.177:80" */};

    public static final String[] DEFAULT_TCP_SERVERS = new String[]{/*
            "115.28.47.66:8900", "115.28.57.184:8900", "115.28.236.9:8900" */};

    public static HashMap<String, PWTimer> verificationCodeMap = new HashMap<String, PWTimer>();


    public static int CALL_CHANNEL_AGORA = 1;
    public static int CALL_CHANNEL_NORMAL = 0;

}
