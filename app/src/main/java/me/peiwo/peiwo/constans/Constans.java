package me.peiwo.peiwo.constans;

public interface Constans {
    String SP_NAME = "pw";
    String SP_USER = "pwuser";
    String SP_KEY_PUSH_STR = "push_str";
    String SP_KEY_CURRGENDER = "curr_gender";
    String SP_KEY_XGTOKEN = "xgtoken";
    String SP_KEY_XIAOMIREGID = "xmregid";
    String SP_KEY_HWTOKEN = "hwtoken";
    String APP_PACKEGE_NAME = "me.peiwo.peiwo";

    String SP_KEY_OPENID = "openid"; // 存第三方的id
    String SP_KEY_OPENTOKEN = "opentoken"; // 存第三方的token
    String SP_KEY_SOCIALTYPE = "socialtype"; // 存第三方的登录类型
    String SP_KEY_LOGINTYPE = "logintype";
    String SP_KEY_PCODE = "phone_code";

    String SP_KEY_CPU_INFO_ARM = "cpu_arm"; // cpu info
    String SP_KEY_WILD_GUIDE = "wild_guide";
    String SP_KEY_ALIPAY_ACCOUNT = "ali_account";
    String SP_KEY_ALIPAY_ACCOUNT_NAME = "ali_account_name";
    String SP_KEY_UID = "uid";

    //public static final String SP_KEY_UPDATE_FORCED = "forced";
    String SP_KEY_SERVER_APPVER = "appversioncode";
    String SP_KEY_UPGRADE_DATA = "upgrade_data_information";
    String SP_KEY_ALEART_UPDATE = "aleart_update";
    String SP_KEY_ONLINE_ENVIROMENT = "online_enviroment";

    String SP_KEY_CIVTIPS = "civtips";

    String SP_KEY_WELCOME_PERCENT_PREFIX = "welc_per_";
    String SP_KEY_CHARGE_CALL_GUIDE = "charge_call_guide";

    String WX_APP_ID = "wx160cbd973d4137ac";
    // wx_secret:965135bc687538105075211ee6c18e41
    // 分享文案
    String WX_SHARE_TITLE = "用\"陪我\"app免费打电话,下载就能领红包!";
    String WX_SHARE_DES = "陪我是一款情感热线app,简单快捷的找到帅哥美女和你电话聊天!";
    String WX_SHARE_CONTENT = "我在“陪我”一共拯救了%s位文艺小青年，强大如你，不服来战咯?";

    String SHARE_APP_URL = "https://h5.peiwoapi.com/";
    String SHARE_APP_NEW_URL = "https://h5.peiwoapi.com/";
    String WEIBO_REDIRECT_URL = "http://www.peiwo.me"; // 应用的回调页
    String DEBUG_LOVE_URL = "http://li.peiwo.cn/h5/";
    String RELEASE_LOVE_URL = "http://love.peiwo.cn/h5/";
    String CLAUSE_LINK = "https://h5.peiwoapi.com/agreement.html";
    String PEIWO_SCHOOL_URL = "https://h5.peiwoapi.com/helptext/index.html";
    String scheme_host = "peiwo_web://cn.peiwo.action_viewweb/";

    String QQ_APP_ID = "1101716237";
    // public static final String QQ_APP_ID = "222222"; //test
    // public static final String QQ_APP_KEY =
    // 1506032652
    // "815568ce9d905a2df231225064606e56";
    String QQ_SHARE_TITLE = WX_SHARE_TITLE;
    String QQ_SHARE_DESC = WX_SHARE_DES;
    String QQ_SCOPE = "all";

    int NOTIFY_ID_MESSAGE = 1;
    String WEIBO_APP_KEY = "1506032652"; // 应用的APP_KEY
    String WEIBO_SCOPE = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog,invitation_write";

    // id
    int NOTIFY_ID_CALL_BACKGROUND = 2; // 后台电话的notify id
    int NOTIFY_ID_WILDCAT_BACKGROUND = 3; // 后台限时聊电话的notify
    int NOTIFY_ID_IM_MESSAGE = 0x100;

    // id
    int ACTION_FLAG_MESSAGE = 1; // 通知栏传递参数到mainactivity
    int ACTION_FLAG_WILDCAT = 2; // 通知栏传递参数到mainactivity
    String ACTION_CLOSE_USERINFO = "me.peiwo.peiwo.ACTION_CLOSE_USERINFO"; // 关闭userinfo

    String ACTION_SEND_IMG_PERMISSION = "me.peiwo.peiwo.ACTION_SEND_IMG_PERMISSION";//通话40秒后开启发送图片权限
//	// 限时聊举报理由
//	public static final int REPORT_REASON_DEFAULT = 0; // 其他
//	public static final int REPORT_REASON_PRON = 1; // 色情狂
//	public static final int REPORT_REASON_CHEAT = 2; // 欺诈
//	public static final int REPORT_REASON_HARASS = 3; // 骚扰
//	public static final int REPORT_REASON_INFRINGE = 4; // 侵权
//	public static final int REPORT_REASON_ABUSIVE = 5; // 乱骂人
//	public static final int REPORT_REASON_UNREASONABLE = 6; // 神经病


    String XML_NMAE = "statistic.xml";
    int REQUEST_CALL = 1;
    int INCOMING_CALL = 2;
    int ANONYMOUS_CHAT = 3;
    int PLATFORM_XG = 0;
    int PLATFORM_XIAOMI = 1;
    int PLATFORM_HUAWEI = 2;


    String XIAOMI_APP_ID = "2882303761517247702";
    String XIAOMI_APP_KEY = "5201724793702";
    String XIAOMI_APP_TAG = "me.peiwo.peiwo";

    int PW_MESSAGE_FROM_DEFAULT = 0x00;
    int PW_MESSAGE_FROM_FIND = 0x01;
    int PW_MESSAGE_FROM_SEARCH = 0x02;
    int PW_MESSAGE_FROM_FEED = 0x03;
    int PW_MESSAGE_FROM_WILDCAT_LOG = 0x04;


    String AGORA_VENDOR_KEY = "6dd92d6dee704a6fa278a7314f37a22b";

    // 市场渠道
    // M360
    // xiaomi
    // wdj
    // myapp
    // baidu
    // other

}
