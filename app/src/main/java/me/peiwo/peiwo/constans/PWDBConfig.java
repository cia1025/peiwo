package me.peiwo.peiwo.constans;

import android.net.Uri;


/**
 * Created by Dong Fuhai on 2014-07-24 17:12.
 *
 * @modify:
 */
public interface PWDBConfig {

    public static final String AUTOHORITY = "me.peiwo.message";


    public static final String DB_NAME_USER = "muser";

    public static final String TB_NAME_USER = "mprofile";

    /**
     * 联系人数据库表
     */
    public static final String TB_NAME_PW_CONTACTS = "pw_contacts";

    public static class ContactsTable {
        public static final String ID = "_id";
        public static final String UID = "uid";
        public static final String SYNC_ID = "sync_id";
        public static final String SIGNIN_TIME = "signin_time";
        public static final String CONTACT_ID = "contact_id";
        public static final String BIRTHDAY = "birthday";
        public static final String AVATAR_THUMBNAIL = "avatar_thumbnail";
        public static final String SLOGAN = "slogan";
        public static final String PRICE = "price";
        public static final String NAME = "name";
        public static final String PROVINCE = "province";
        public static final String GENDER = "gender";
        public static final String AVATAR = "avatar";
        public static final String CITY = "city";
        public static final String CONTACT_STATE = "contact_state";
        public static final String WORD = "word";
        public static final String CALL_DURATION = "call_duration";

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTOHORITY + "/" + TB_NAME_PW_CONTACTS);
    }


    /**
     * 消息详细记录数据库表
     */
    public static final String TB_NAME_PW_DIALOGS = "pw_dialogs";

    public static class DialogsTable {
        public static final String ID = "_id";
        public static final String CONTENT = "content";        //兼容低版本用
        public static final String DIALOG_ID = "dialog_id";        //每条短信的ID
        public static final String UPDATE_TIME = "update_time";    //更新时间
        public static final String MSG_ID = "msg_id";            //同步pw_message表
        public static final String UID = "uid";                    //用户ID
        public static final String TYPE = "type";                // 0 自己发送的消息	1 对方发送的消息
        public static final String DIALOG_TYPE = "dialog_type";    //	普通文本/网页/。。。
        public static final String DETAILS = "details";            //消息
        public static final String SEND_STATUS = "send_status";    // 0 默认	1 发送失败	2 发送成功
        public static final String READ_STATUS = "read_status";    // 0 未读     1 已读
        public static final String FILE_LENGTH = "file_length";    //无
        public static final String FILE_PATH = "file_path";        //无
        public static final String ERROR_CODE = "error_code";    //发送失败返回码
        public static final String DATA1 = "data1";                //无
        public static final String DATA2 = "data2";                //无
        public static final String DATA3 = "data3";                //无
        public static final String DATA4 = "data4";                //无

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTOHORITY + "/" + TB_NAME_PW_DIALOGS);
    }


    /**
     * 消息一级列表数据库表
     */
    String TB_NAME_PW_MESSAGES = "pw_messages";

    class MessagesTable {
        public static final String ID = "_id";
        public static final String MSG_ID = "msg_id";
        public static final String UID = "uid";        //用户ID
        public static final String USER = "user";
        public static final String TYPE = "type";
        public static final String MSG_TYPE = "msg_type";    //区分是申请通话还是用户还是系统消息
        public static final String UPDATE_TIME = "update_time";        //更新时间
        public static final String CONTENT = "content";        //低版本时会显示该内容
        public static final String INPUT_STATUS = "input_status";    //无
        public static final String DRAFT_MSG = "draft_msg";            //无
        public static final String MESSGAE_COUNT = "messgae_count";    //无
        public static final String UNREAD_COUNT = "unread_count";    //未读条数
        public static final String INSIDE = "inside";            // 1在打招呼盒子里面   0在消息列表
        public static final String INSIDE_NEW = "inside_new";            // 是否新的打招呼消息  0--已看，1--新
        public static final String IS_HIDE = "is_hide";            // 是否隐藏此消息

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTOHORITY + "/" + TB_NAME_PW_MESSAGES);
    }

    /**
     * 向你申请通话的人 数据库表
     */
    public static final String TB_NAME_PW_REQUESTS = "pw_requests";

    public static class RequestsTable {
        public static final String ID = "_id";
        public static final String REQUEST_ID = "request_id";
        public static final String SYNC_ID = "sync_id";
        public static final String STATE = "state";
        public static final String CONTENT = "content";
        public static final String UID = "uid";
        public static final String BIRTHDAY = "birthday";
        public static final String AVATAR_THUMBNAIL = "avatar_thumbnail";
        public static final String SLOGAN = "slogan";
        public static final String PRICE = "price";
        public static final String NAME = "name";
        public static final String PROVINCE = "province";
        public static final String GENDER = "gender";
        public static final String AVATAR = "avatar";
        public static final String CITY = "city";
        public static final String UPDATE_TIME = "update_time";

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTOHORITY + "/" + TB_NAME_PW_REQUESTS);
    }

    public static final String TB_NAME_UID_MSGID = "uid_msgid";

    public static class UidMsgId {
        public static final String UID = "uid";
        public static final String MSG_ID = "msg_id";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTOHORITY + "/" + TB_NAME_UID_MSGID);
    }

    public static final int TB_PW_MESSAGE_URI_CODE = 1;
    public static final int TB_PW_DIALOGS_URI_CODE = 2;
    public static final int TB_PW_CONTACTS_URI_CODE = 3;
    public static final int TB_PW_REQUESTS_URI_CODE = 4;
    public static final int TB_PW_UID_MSGID_URI_CODE = 5;

    //联系人备注
    public static final String DB_NAME_MK_PREFIX = "rmk";
    public static final String TB_NAME_PW_REMARK = "pw_remark";

    /**
     * 匿名聊记录被举报的用户映射表
     */
    String TB_PW_WILDRLOG_REPORT_MAP = "report_map";
    String TB_PW_CONTACTS = "pw_contacts_brite";
    String TB_PW_NO_DISTURB = "pw_no_disturb_brite";
    String TB_PW_AT_USER = "pw_at_user_brite";

}
