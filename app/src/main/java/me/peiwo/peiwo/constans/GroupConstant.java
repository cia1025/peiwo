package me.peiwo.peiwo.constans;

/**
 * Created by fuhaidong on 15/12/10.
 */
public final class GroupConstant {

    public static final class MessageType {
        //        PW_GROUP_MESSAGE = 0,        	  /* 文本&小表情*/
//        PW_GROUP_IM_IMAGE = 1,   		     /* 发送图片 */
//        PW_GROUP_IM_GIF = 2,   		     /* 发送GIF */
//        PW_GROUP_REDBAG = 3,       		   /* 红包 */
//        PW_GROUP_REPUTATION_REDBAG = 4,       		   /* 声望红包 */
//        PW_GROUP_REDBAG_TIP = 5,			/*红包系统提醒消息*/
//        PW_GROUP_TIP = 6,				/* 普通系统提醒消息*/
        public static final int TYPE_TEXT = 0;
        public static final int TYPE_IMAGE = 1;
        public static final int TYPE_GIF = 2;
        public static final int TYPE_REDBAG = 3;
        public static final int TYPE_REPUTATION_REDBAG = 4;
        public static final int TYPE_REDBAG_TIP = 5;
        public static final int TYPE_DECORATION = 6;
        public static final int TYPE_REPUREDBAG_TIP = 7;

        //local extra
        public static final int TYPE_HEADER = -2;
        public static final int TYPE_UNKNOWN = -1;
    }

    public static final class ViewType {
        //self
        public static final int TYPE_TEXT_SELF = 0;
        public static final int TYPE_IMAGE_SELF = 1;
        public static final int TYPE_GIF_SELF = 2;
        public static final int TYPE_REDBAG_SELF = 3;
        public static final int TYPE_REPUTATION_REDBAG_SELF = 4;

        //other
        public static final int TYPE_TEXT_OTHER = 5;
        public static final int TYPE_IMAGE_OTHER = 6;
        public static final int TYPE_GIF_OTHER = 7;
        public static final int TYPE_REDBAG_OTHER = 8;
        public static final int TYPE_REPUTATION_REDBAG_OTHER = 9;
        //gloable
        public static final int TYPE_REDBAG_TIP = 10;
        public static final int TYPE_DECORATION = 11;
        public static final int TYPE_UNKNOWN = 12;
        public static final int TYPE_HEADER = 13;
        public static final int TYPE_REPUREDBAG_TIP = 14;
    }

    public static final class Direction {
        public static final int SELF = 0;
        public static final int OTHER = 1;
    }

    public static final class SendStatus {
        public static final int SENDING = 0;
        public static final int SUCCESS = 1;
        public static final int ERROR = 2;
    }

    public static final class MemberType {
        public static final String ADMIN = "ADMIN";
        public static final String MEMBER = "MEMBER";
        //不在群组中
        public static final String ALIEN = "ALIEN";
        //新人
        public static final String NEWBIE = "NEWBIE";
    }

    public static final class CMD_NAME {
        public static final String REGULAR = "order_regular";
        public static final String UPDATE = "order_update";
    }

}
