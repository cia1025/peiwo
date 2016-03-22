
package me.peiwo.peiwo.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.db.PWConfig;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.SharedPreferencesUtil;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AsynHttpClient {
    // test git
    public static final int ERROR_MSG_NETWORK_NOT_AVAILABLE = 100;
    //请求数据不存在
    public static final int DATA_NOT_EXISTS = 20005;

    public static final int PW_RESPONSE_OPERATE_ERROR = 20002;
    public static final int PW_RESPONSE_DATA_NOT_AVAILABLE = 20003;
    public static final int PW_RESPONSE_DATA_ALREADY_EXIST = 20004;
    public static final int APP_NEED_UPDATE = 20007;

    public static final int METHOD_GET = 1;
    public static final int METHOD_POST = 2;

    public static final String DEVICE_ANDROID = "3";

    public static final String KEY_VERSION = "version";
    public static final String KEY_APP = "app"; //渠道号key

    public static final String KEY_DEVICE_TOKEN = "device_token"; //信鸽token或者小米的regId
    public static final String KEY_DEVICE_TYPE = "device_type"; //设备类型
    public static final String KEY_PLATFORM_TYPE = "platform_type"; //根据platform_type使用不用的推送，0：信鸽推送，1：小米推送

    public static final int ERR_USER_AUTH = 10002;
    public static final String KEY_CC_CODE = "code";
    public static final String API_FANS_LIST = "v1.0/focuses";
    public static final String API_TAG_TOPIC = "v1.0/feed/topic";
    public static final String API_TAG_TOPIC_LIST = "v1.0/feed/pub/list";
    public static final String API_TAG_MAIN_TOPIC_LIST = "v2.0/feed/pub/list";
    public static final String API_FEED_FLOW_LIKERS = "v1.0/feed/pub/like/dynamic";
    public static final String API_TOPIC_PUB = "v1.0/feed/pub";
    public static final String API_FEED_PUB_HISTORY = "v1.0/feed/pub/list/history";
    public static final String API_FEED_PUB_FRIEND = "v2.0/feed/pub/friend";
    public static final String API_USERS_FILTER_SETTING = "v1.0/usersetting";
    public static final String API_MATCHZODIAC = "v1.0/userinfo/matchzodiac";

    public static final String API_PRAISE_LIST = "v1.0/feed/pub/likers";
    public static final String API_RELATION = "v1.0/userinfo/relation";
    public static final String API_FREECALL = "v1.0/usersetting/freecall";
    public static final String API_RONG_CLOUD_TOKEN = "v1.0/group_chat/user/token";
    public static final String API_GROUPS_RECRUIT = "v1.0/group_chat/groups/recruit";
    public static final String API_GROUPCHAT_JOIN = "v2.0/group_chat/join";
    public static final String API_GROUPCHAT_JOIN_ORDER = "v2.0/group_chat/join_order";
    public static final String API_GROUPCHAT_PAY_JOIN_ORDER = "v2.0/group_chat/pay_join_order";
    public static final String API_MY_GROUPS = "v2.0/group_chat/mygroups";
    public static final String API_GROUP_FEED_FLOW = "v1.0/group_chat/group/feed";
    public static final String API_GROUP_PERSONAL_PACKET_SEND = "v1.0/group_packet/personal_packet/send";
    public static final String API_GROUP_MONEY_PACKET_SEND = "v1.0/group_packet/money_packet/send";
    public static final String API_GROUP_PACKET = "v1.0/group_packet";//抢红包
    public static final String API_GROUP_SCORE_PACKET_RECEIVE = "v1.0/group_packet/score_packet/receive";//抢声望红包
    public static final String API_GROUP_PRESTIGE = "v1.0/group_chat/group/prestige";
    public static final String API_GROUP_BALANCE = "v1.0/group_chat/group/balance";
    public static final String API_GROUP_SCORE_PACKET_SEND = "v1.0/group_packet/score_packet/send";
    public static final String API_GROUP_MEMBER_MERGE = "v1.0/group_chat/group/group_and_member";
    public static final String API_CREATE_WEIXIN_ORDER = "v1.0/payment/weixin/order";
    public static final String API_GET_WEIXIN_PAYMENT_VERIFY = "v1.0/payment/weixin/verify";

    public static final String KEY_CC_DATA = "data";
    public static final String KEY_CC_CURRENT_TIME = "current_time";

    public static final String KEY_SOCIAL_TYPE = "social_type";
    public static final String KEY_SOCIAL_UID = "social_uid";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_IMAGE = "image_url";
    public static final String KEY_IMAGES = "images";

    public static final String KEY_EMOTION = "emotion";
    public static final String KEY_PROVINCE = "province";
    public static final String KEY_SLOGAN = "slogan";
    public static final String KEY_PROFESSION = "profession";
    public static final String KEY_TAGS = "tags";
    public static final String KEY_MONEY = "money";
    public static final String KEY_PRICE = "price";
    public static final String KEY_CITY = "city";
    public static final String KEY_SESSION_DATA = "session_data";
    public static final String KEY_CHECK = "check";
    public static final String KEY_UID = "uid";
    public static final String KEY_RELOAD = "reload";
    public static final String KEY_INDEX = "index";
    public static final String KEY_CURSOR = "cursor";
    public static final String KEY_GENDER_MASK = "mask";
    public static final String KEY_TUID = "tuid";
    public static final String KEY_REASON = "reason";
    public static final String KEY_REPORT_WILDCAT = "wildcat";
    public static final String KEY_TUIDS = "tuids";
    public static final String KEY_ITEMS = "items";
    public static final String KEY_PAYMENT_ID = "payment_id";
    public static final String KEY_ITEM_ID = "item_id";
    public static final String KEY_CHANNEL = "channel";
    public static final String KEY_ORDER = "order";
    public static final String KEY_ALIPAY_ACCOUNT = "alipay_account";

    public static final String KEY_TOPIC_ID = "topic_id";
    public static final String KEY_TOPIC_CONTENT = "content";
    public static final String KEY_TOPIC_LOCATION = "location";
    public static final String KEY_TOPIC_EXTRA = "extra";
    public static final String KEY_TOPIC_CUSTOM_CONTENT = "feed_content";
    public static final String KEY_NODISTURB = "nodisturb";
    public static final String KEY_NOPUSH = "nopush";
    public static final String KEY_INTERVAL = "interval";
    public static final int GENDER_MASK_MALE = 1;
    public static final int GENDER_MASK_FEMALE = 2;
    public static final int GENDER_MASK_ALL = 3;
    public static final String API_PERSONAL_PACKET_SEND = "v1.0/person_packet/send";
    public static final String API_PERSONAL_PACKET_GRAB = "v1.0/person_packet/receive";

    public static final String KEY_NAME = "name";
    public static final String KEY_DESC = "desc";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_GENDER_FILTER = "s_gender";
    public static final String KEY_AGE_FROM_FILTER = "s_age_from";
    public static final String KEY_AGE_TO_FILTER = "s_age_to";
    public static final String KEY_USER_LOCATION_FILTER = "use_location";
    public static final String KEY_BIRTHDAY = "birthday";
    public static final String KEY_SIGN = "sign";
    public static final String KEY_FANS_NUMBER = "key_fans_number";
    public static final String KEY_FOLLOWS_NUMBER = "key_follows_number";

    public static final String KEY_INFO_COMPLEMENT = "key_info_complement";
    public static final String KEY_INFO_SHOW_DYNAMIC = "key_info_show_dynamic";
    public static final String KEY_MSG_ID = "msg_id";
    public static final String KEY_MSG_IDS = "msg_ids";
    public static final String KEY_MSG_TYPE = "msg_type";
    public static final String KEY_CONTENT = "content";

    public static final String API_ACCOUNT_SIGNIN = "v1.0/account/signin";
    public static final String API_ACCOUNT_BIND_SOCIAL = "v1.0/account/bindsocial";
    public static final String API_ACCOUNT_SIGNOUT = "account/signout";
    public static final String API_ACCOUNT_CAPTCHA = "account/captcha"; //提交手机号
    public static final String API_GET_CAPTCHA = "v1.0/account/captcha";
    //account/forgetpassword 参数为phone，captcha，新的password
    public static final String API_ACCOUNT_FORGETPWD = "account/forgetpassword";
    public static final String API_ACCOUNT_SET_USERINFO = "v1.0/account/setuserinfo";

    //account/resetpassword 参数为old旧密码，new新密码
    public static final String API_ACCOUNT_RESETPASSWORD = "account/resetpassword";

    //account/usercaptcha 应用内使用的验证码 参数为phone和captcha_type, 如果需要传密码，参数为password
    public static final String API_ACCOUNT_USERCAPTCHA = "account/usercaptcha";

    //account/bindphone 参数为phone，captcha，password
    public static final String API_ACCOUNT_BINDPHONE = "account/bindphone";
    //account/resetphone 参数为phone, captcha
    public static final String API_ACCOUNT_RESETPHONE = "account/resetphone";

    public static final String API_ACCOUNT_SIGNUP = "v1.0/account/signup";

    public static final String API_USERINFO_PUSHBIND = "v1.0/userinfo/pushbind"; //上传token

    public static final String API_USERIMAGE_UPLOAD = "userimage/upload";
    public static final String API_USERINFO_GETINFO = "v2.0/userinfo/getinfo";
    public static final String API_USERINFO_GETFINANCE = "userinfo/getfinance";
    public static final String API_ACCOUNT_AUTOSIGNIN = "v1.0/account/autosignin";
    public static final String API_USERINFO_UPDATE = "userinfo/update";
    public static final String API_ACCOUNT_GET_PACKAGE = "v1.0/account/packages";
    public static final String API_GET_USERINFO_COMPLEMENT = "v1.0/userinfo/testcomplement";

    public static final String API_USERINFO_PERMISSION = "v2.0/userinfo/permission";// 参数为tuid，就是对方的uid

    public static final String API_USERINFO_MESSAGEID = "v1.0/userinfo/messageid";// 参数为tuid，就是对方的uid
    public static final String API_USERINFO_LAZY_VOICE = "v1.0/userinfo/voice";
    public static final String API_USERINFO_WILDCAT = "v1.0/userinfo/wildcat";
    public static final String API_USERINFO_WILDCAT_HINT = "userinfo/getWildcatHint";

    public static final String API_UPLOAD_AVATAR = "upload/avatar";
    public static final String API_USERLIST_RECOMMENDLIST = "userlist/recommendlist";
    public static final String API_USERLIST_MAINLIST = "v4.0/userlist/mainlist";
    public static final String API_USERLIST_SEARCHBYTAG = "userlist/searchbytag";

    public static final String API_REPORT_SEND = "report/send";
    public static final String API_REPORT_DOBLOCK = "v1.0/report/send";
    public static final String API_FRIEND_DELETE = "contact/delete";
    public static final String API_PAYMENT_ITEMS = "payment/items";
    public static final String API_PAYMENT_ORDER = "payment/order";
    public static final String API_PAYMENT_HISTORY = "payment/history";
    public static final String API_PAYMENT_WITHDRAW = "payment/withdraw";
    public static final String API_SETTING_PRICE = "setting/price";
    public static final String API_SETTING_SYSTEM = "v1.0/setting/system";
    public static final String API_SETTING_NODISTURB = "setting/nodisturb";
    public static final String API_SETTING_SEARCHABLETAGS = "setting/searchabletags";
    public static final String API_REPORT_CRASH_LOG = "v1.0/report/crashlog";

    public static final String API_GET_TCP_SERVERS = "v1.0/setting/tcpservers";
    public static final String API_GET_SYSTEM_TAGS = "v1.0/setting/tag";
    public static final String API_GET_WILDCAT_RECORD = "userinfo/wildcatRecord";
    public static final String API_GET_SYSTEM_TOPIC = "v1.0/feed/topic/system";
    public static final String API_PAYMENT_WITHDRAW_MONTH = "v1.0/payment/withdraw/month_summary";

    public static final int MSG_TYPE_CALL_APPLY = 1;
    public static final String API_MESSAGE_SEND = "contact/request/send";
    public static final String API_MESSAGE_LIST = "message/list";
    public static final String API_MESSAGE_DELETE = "message/delete";
    //    public static final String API_MESSAGE_DIALOG = "message/dialog";
    public static final String API_MESSAGE_BADGE = "message/badge";

    public static final String API_MESSAGE_DIALOGS = "v1.0/im/messages"; //?msg_id=x&init=x&dialog_id=x&prev=x新的api消息中心本地化
    public static final String API_CALLAPPLY_CHECK = "contact/request/accept"; //?request_id
    public static final String API_CALLAPPLY_LIST = "contact/request/list"; //这个有更改 ?max_id=

    public static final String API_CONTACT_BLOCK = "contact/block";
    public static final String API_CALLAPPLY_CANCLE = "contact/request/delete"; //?request_ids=1,2,3,4

    public static final String API_CALLHISTORY_LIST = "callhistory/list";
    public static final String API_CALLHISTORY_DELETE = "callhistory/delete";

    //获取领取奖励
    public static final String API_REWARD_SOCIAL = "reward/social";

    public static final String API_CONTACT_LIST = "contact/list"; //联系人列表?max_id=
    public static final String API_CONTACT_ORDER = "contact/order"; //联系人排序?order_type=1
    public static final String API_CONTACT_DELETE = "contact/delete"; //?tuids=1,2,3,4
    public static final String API_CONTACT_BLOCKLIST = "contact/blocklist"; //黑名单列表
    public static final String API_CONTACT_NOTE_ADD = "contact/note/add"; //?tuid=xxx&note=xxxx
    public static final String API_CONTACT_NOTE_LIST = "contact/note/list";
    public static final String API_CONTACT_NOTE_DEL = "contact/note/del"; //?tuid

    public static final String API_CONTACT_UNBLOCK = "contact/unblock";

    public static final String API_CONTACT_TIPS = "contact/tips"; //?tuid=xxxxx   {'code': xx, 'data': {'show_tips': true or false, 'tips': xxxxxx}}
    public static final String API_REPORT_WARNING = "report/warning";
    public static final String API_QINIU_TOKEN = "v1.0/qiniu/token";
    public static final String API_QINIU_TOKENS = "v1.0/qiniu/tokens";//一次获取多个七牛token

    public static final String API_GET_CLIENT_IP = "util/myip";
    public static final String API_UPLOAD_NEW_STATE = "v1.0/report/serverspeed";

    public static final String API_AGREEMENT_INFORMATION = "agreement/information";
    public static final String API_AGREEMENT_AGREE = "agreement/agree";
    public static final String API_AGREEMENT_DISAGREE = "agreement/disagree";
    public static final String API_RECEIVE = "v1.0/package/receive";
    public static final String API_RECEIVEBONUS = "v1.0/package/receivebonus";
    public static final String API_UPDATE = "v1.0/setting/updateinfo";

    public static final String API_GETUPLOADER = "userupload/getuploader";
    public static final String API_POSTUPLOADER = "userupload/uploadfile";
    public static final String API_SETTING_HTTPSERVERS = "v1.0/setting/httpservers";
    public static final String API_REPORT_FEED_FLOW = "v1.0/feed/pub/report";
    public static final String API_DELETE_FEED_FLOW = "v1.0/feed/pub/delete";
    public static final String API_CREATE_GROUP_CHAT = "v1.0/group_chat/group";
    public static final String API_FETCH_GROUP_INFO = "v1.0/group_chat/group/group_setting";
    public static final String API_QUIT_GROUP_CHAT = "v1.0/group_chat/quit";
    public static final String API_UPDATE_GROUP_SETTING = "v2.0/group_chat/group/update";
    public static final String API_GROUP_MEMBERS = "v1.0/group_chat/members";
    public static final String API_CHECK_GROUP_PERMISSION = "v1.0/group_chat/group/permission";
    public static final String API_GET_GROUP_MEMBER = "v1.0/group_chat/member";
    public static final String API_GET_GROUP_MEMBER_EXTRA = "v1.0/group_chat/member_extra";
    public static final String API_KICK_OUT_PEOPLE_IN_GROUP = "v1.0/group_chat/kickout";
    public static final String API_GROUP_CHAT_REPORT = "v1.0/group_chat/group/report";
    public static final String API_GROUPCHAT_REDBAG_INFO = "v1.0/group_packet/money_packet/info";
    public static final String API_GROUPCHAT_PACKET_ICONS = "v1.0/setting/packet_icons";
    public static final String API_GETUPLOADCONFIG = "v1.0/hourglass/getuploadconfig";
    public static final String API_HOURGLASS_UPLOADDATA = "v1.0/hourglass/uploaddata";
    public static final String API_CALL_CHANNEL = "v1.0/userinfo/channel";

    public static final String API_PRISE_COUNT = "v1.0/feed/pub/like/count";
    public static final String ENCODEING = "UTF-8";

    public static final int ERROR_LOCAL = 1001;
    public static final int ERROR_LOCAL_PARAM = ERROR_LOCAL + 1;
    public static final int ERROR_SERVER = ERROR_LOCAL + 2;
    public static final int ERROR_UNKNOWN = ERROR_LOCAL + 3;
    public static final int ERROR_SERVER_DOWN = 500;


    private HttpClient sHttpclient;
    private static AsynHttpClient sAsynHttpClient;

    private MsgSender mPostSender;

    private AsynHttpClient() {
        sHttpclient = getNewHttpClient();
        HandlerThread senderThread = new HandlerThread("AsyncHttpClient");
        senderThread.start();
        Looper looper = senderThread.getLooper();
        mPostSender = new MsgSender(looper);
        assignmentServers();
    }

    public static AsynHttpClient getInstance() {
        if (sAsynHttpClient == null) {
            sAsynHttpClient = new AsynHttpClient();
        }
        return sAsynHttpClient;
    }

    public void setGlobalErrHandler(MsgStructure msg) {
        mPostSender.setGlobalErrHandler(msg);
    }

    public void execHttpPost(MsgStructure absMsg) {
        if (!isNetWorkAvailable()) {
            absMsg.onError(ERROR_MSG_NETWORK_NOT_AVAILABLE, null);
            CustomLog.e(DfineAction.HTTP_TAG, "net not available : url = " + absMsg.requestUrl);
            return;
        }

        Message msg = mPostSender.obtainMessage();
        msg.obj = absMsg;
        msg.arg1 = METHOD_POST;
        msg.sendToTarget();
    }

    public boolean isNetWorkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) PeiwoApp.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return (info != null && info.isAvailable());
    }


    public void execHttpGet(MsgStructure absMsg) {
        if (!isNetWorkAvailable()) {
            absMsg.onError(ERROR_MSG_NETWORK_NOT_AVAILABLE, null);
            CustomLog.e(DfineAction.HTTP_TAG, "net not available : url = " + ((absMsg.requestUrl.length() > 40) ? absMsg.requestUrl.substring(0, 40) : absMsg.requestUrl));
            return;
        }

        Message msg = mPostSender.obtainMessage();
        msg.obj = absMsg;
        msg.arg1 = METHOD_GET;
        msg.sendToTarget();
    }

    class MsgSender extends Handler {
        private MsgStructure mmGlobalErr;

        public MsgSender(Looper looper) {
            super(looper);
        }

        public void setGlobalErrHandler(MsgStructure msg) {
            mmGlobalErr = msg;
        }

        @Override
        public void handleMessage(Message m) {
            MsgStructure msg = (MsgStructure) m.obj;
            int requestMethod = m.arg1;
            handleConnection(requestMethod, msg, mmGlobalErr);
        }
    }

    private void handleConnection(int requestMethod, MsgStructure msg, MsgStructure mmGlobalErr) {
        HttpResponse resp = null;
        String requestUrl = (msg.requestUrl.length() > 50) ? msg.requestUrl.substring(0, 50) : msg.requestUrl;
        CustomLog.e(DfineAction.HTTP_TAG, "begin request url = " + requestUrl);
        try {
            if (requestMethod == METHOD_POST) {
                HttpPost httpPost = new HttpPost();

                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                for (int i = 0; i < msg.paramList.size(); i++) {
                    NameValuePair np = msg.paramList.get(i);
                    CustomLog.i("np.name is : " + np.getName() + "\t - np.value is : " + np.getValue());
                    if (!KEY_IMAGE.equals(np.getName())) {
                        builder.addTextBody(np.getName(), np.getValue(), ContentType.create("text/plain", "UTF-8"));
                    } else {
                        builder.addBinaryBody(np.getName(), new File(np.getValue()), ContentType.create("image/jpeg", "UTF-8"), np.getValue());
                    }
                }

                httpPost.setEntity(builder.build());
                httpPost.setURI(new URI(msg.requestUrl));
                // httpPost.setEntity(new
                // UrlEncodedFormEntity(msg.paramList, HTTP.UTF_8));

                resp = sHttpclient.execute(httpPost);
            } else {
                HttpGet httpGet = new HttpGet();
                httpGet.setURI(new URI(msg.requestUrl));
                resp = sHttpclient.execute(httpGet);

            }
        } catch (ClientProtocolException e) {
            CustomLog.e(DfineAction.HTTP_TAG, "request error = " + e.toString());
        } catch (UnsupportedEncodingException e) {
            CustomLog.e(DfineAction.HTTP_TAG, "request error = " + e.toString());
        } catch (ConnectionPoolTimeoutException e) {
            CustomLog.e(DfineAction.HTTP_TAG, "request error = " + e.toString());
            /*从ConnectionManager管理的连接池中取出连接的超时时间*/
            if (onConnectionError(requestMethod, msg, mmGlobalErr)) {
                return;
            }
        } catch (ConnectTimeoutException e) {
            CustomLog.e(DfineAction.HTTP_TAG, "request error = " + e.toString());
            /*通过网络与服务器建立连接的超时时间*/
            if (onConnectionError(requestMethod, msg, mmGlobalErr)) {
                return;
            }
        } catch (SocketTimeoutException e) {
            CustomLog.e(DfineAction.HTTP_TAG, "request error = " + e.toString());
            /*Socket读数据的超时时间，即从服务器获取响应数据需要等待的时间*/
            if (onConnectionError(requestMethod, msg, mmGlobalErr)) {
                return;
            }
        } catch (IOException e) {
            CustomLog.e(DfineAction.HTTP_TAG, "request error = " + e.toString());
            if (onConnectionError(requestMethod, msg, mmGlobalErr)) {
                return;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            CustomLog.e(DfineAction.HTTP_TAG, "request error = " + e.toString());
            if (onConnectionError(requestMethod, msg, mmGlobalErr)) {
                return;
            }
        }

        if (resp == null) {
            CustomLog.e(DfineAction.HTTP_TAG, "request HttpResponse resp is null, maybe no connection");
            msg.onError(ERROR_UNKNOWN, null);
            return;
        }

        //Trace.i("state code == " + resp.getStatusLine().getStatusCode());
        int scode = resp.getStatusLine().getStatusCode();
        CustomLog.e(DfineAction.HTTP_TAG, "request status code = " + scode);
        //APP_NEED_UPDATE 20007
        if ((scode != 504 && scode / 500 == 1) || scode == APP_NEED_UPDATE) {
            msg.onError(ERROR_SERVER_DOWN, null);
            mmGlobalErr.onError(ERROR_SERVER_DOWN, null);
            return;
        }

        if (hostList != null) {
            for (int i = 0; i < hostList.size(); i++) {
                if (msg.requestUrl.contains(hostList.get(i))) {
                    PeiwoApp.getApplication().setAvailable_http_host(hostList.get(i));
                    break;
                }
            }
        }

        boolean isRespOk = false;
        JSONObject respJson = null;
        String resString = null;
        int errorNum = ERROR_SERVER;
        try {
            resString = EntityUtils.toString(resp.getEntity(), HTTP.UTF_8);
            CustomLog.d("handleConnection resString == " + resString);
            respJson = new JSONObject(resString);
            JSONObject update = respJson.optJSONObject("update");
            if (update != null && !msg.requestUrl.contains(AsynHttpClient.API_UPDATE)) {
                mmGlobalErr.onReceive(update);
            }
            errorNum = respJson.optInt(KEY_CC_CODE, ERROR_SERVER_DOWN);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK
                    && errorNum == 0) {
                isRespOk = true;
                if (msg.onInterceptRawData(resString)) {
                    return;
                }

                double current_time = respJson.optDouble(KEY_CC_CURRENT_TIME, 0);
                long server_time = (long) current_time;
                respJson = respJson.optJSONObject(KEY_CC_DATA);
                //记录本机与服务器时间差
                if (server_time > 0) {
                    server_time -= (System.currentTimeMillis() / 1000);
                    SharedPreferencesUtil.putLongExtra(PeiwoApp.getApplication(), KEY_CC_CURRENT_TIME, server_time * 1000);
                }
            }
        } catch (ParseException e) {
            CustomLog.e(DfineAction.HTTP_TAG, "request error2 = " + e.toString());
        } catch (JSONException e) {
            CustomLog.e(DfineAction.HTTP_TAG, "request error2 = " + e.toString());
        } catch (IOException e) {
            CustomLog.e(DfineAction.HTTP_TAG, "request error2 = " + e.toString());
        }

        if (isRespOk) {
            msg.onReceive(respJson);
            CustomLog.d("handleConnection3 response is : " + respJson);
        } else {
            if (errorNum == ERR_USER_AUTH && mmGlobalErr != null) {
                mmGlobalErr.onError(errorNum, respJson);
            } else {
                msg.errorMessage = resString;
                msg.onError(errorNum, respJson);
            }
        }
    }

    public boolean onConnectionError(int requestMethod, MsgStructure msg, MsgStructure mmGlobalErr) {
        if (msg == null || TextUtils.isEmpty(msg.requestUrl)) {
            return false;
        }
        assignmentServers();
        if (hostList == null) {
            return false;
        }
        int hostTotal = hostList.size();
        if (hostTotal == 0) {
            return false;
        }
        String newHost = "";
        String oldHost = "";

        for (int i = 0; i < hostTotal; i++) {
            if (msg.requestUrl.contains(hostList.get(i)) && i < (hostTotal - 1)) {
                oldHost = hostList.get(i);
                newHost = hostList.get(i + 1);
                break;
            }
        }
        if (TextUtils.isEmpty(newHost)) {
            return false;
        }

        msg.requestUrl = msg.requestUrl.replace(oldHost, newHost);
        ;
        handleConnection(requestMethod, msg, mmGlobalErr);
        return true;
    }


    public List<String> hostList = new ArrayList<String>();
    public static final String HTTP_SERVERS_GET_TIME = "HTTP_SERVERS_GET_TIME";
    public static final String HTTP_SERVERS = "HTTP_SERVERS";

    public void getHttpServers(final Context mContext) {
        SimpleDateFormat df = new SimpleDateFormat("yy:MM:dd");
        final String currentDate = df.format(new Date(System.currentTimeMillis()));

        String http_servers_get_time = SharedPreferencesUtil.getStringExtra(mContext, HTTP_SERVERS_GET_TIME, "");
        if (currentDate.equals(http_servers_get_time)) {
            //return;
        }
        tryGetHttpServers(mContext, currentDate);
    }

    private void tryGetHttpServers(final Context mContext, final String currentDate) {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        ApiRequestWrapper.openAPIGET(mContext, params, AsynHttpClient.API_SETTING_HTTPSERVERS, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("onReceive. data is :" + data);
                if (data != null) {
                    try {
                        String httpServers = data.getString("servers");
                        CustomLog.i(httpServers);
                        SharedPreferencesUtil.putStringExtra(mContext, HTTP_SERVERS_GET_TIME, currentDate);
                        SharedPreferencesUtil.putStringExtra(mContext, HTTP_SERVERS, httpServers);
                        hostList.clear();
                        assignmentServers();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("onError, ret is : " + ret);
                if (!PeiwoApp.getApplication().isOnLineEnv()) {
                    return;
                }
                List<PWConfig.UsedDomain> domains = PeiwoApp.getApplication().GetPWConfig().getDomains();
                String retryUrl = null;
                String lastUrl = PeiwoApp.getApplication().GetPWConfig().GetHTTPSvr().m_strHostName;
                Log.i("tag", "lasturl == " + lastUrl);
                for (PWConfig.UsedDomain domain : domains) {
                    if (!domain.isused) {
                        retryUrl = domain.domain;
                        domain.isused = true;
                        break;
                    }
                }
                if (retryUrl != null) {
                    Log.i("tag", "retry url == " + retryUrl);
                    PeiwoApp.getApplication().GetPWConfig().setHostName(retryUrl);
                    tryGetHttpServers(mContext, currentDate);
                }
            }
        });
    }

    private void assignmentServers() {
        if (hostList != null && hostList.size() > 0) {
            return;
        }
        String httpServers = SharedPreferencesUtil.getStringExtra(PeiwoApp.getApplication(), HTTP_SERVERS, "");
        String strHTTPType = PeiwoApp.getApplication().GetPWConfig().GetHTTPSvr().m_strHTTPType;
        String strHTTPSvr = PeiwoApp.getApplication().GetPWConfig().GetHTTPSvr().m_strHostName;
        int nPort = PeiwoApp.getApplication().GetPWConfig().GetHTTPSvr().m_nPort;
        String defaultHost = strHTTPType + "://" + strHTTPSvr + ":" + nPort;
        hostList.add(defaultHost);

        if (TextUtils.isEmpty(httpServers)) {
            for (int i = 0; i < DfineAction.DEFAULT_HTTP_SERVERS.length; i++) {
                if (!hostList.contains(DfineAction.DEFAULT_HTTP_SERVERS[i])) {
                    hostList.add(DfineAction.DEFAULT_HTTP_SERVERS[i]);
                }
            }
            return;
        }
        try {
            JSONArray array = new JSONArray(httpServers);
            for (int i = 0; i < array.length(); i++) {
                if (!hostList.contains(array.getString(i))) {
                    hostList.add(array.getString(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * **********************************
     */
    public HttpClient getNewHttpClient() {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 10000);
        HttpConnectionParams.setSoTimeout(params, 100000);
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));
            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient(params);
        }
    }

    public void clearAllRequest() {
        if (mPostSender != null)
            mPostSender.removeCallbacksAndMessages(null);
    }
}
