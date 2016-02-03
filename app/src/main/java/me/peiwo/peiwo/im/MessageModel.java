package me.peiwo.peiwo.im;

import java.util.HashSet;

/**
 * IM消息对象
 * @author kevin
 *
 */
public class MessageModel {
	public long id = -1;
	public String content = "";
	public String msg_id = "0";
	public int uid = 0;
	public String details = "";
	public String update_time = "";
	public long dialog_id = 0;
	public int dialog_type = DIALOG_TYPE_DEFAULT;
	public int send_status = MessageModel.SEND_STATUS_DEFAULT;//0--发送中     1--发送失败    2--发送成功
	public int type = 0; // 0--发送信息    1--接收消息
	public String feed_id;
	public static final int SEND_STATUS_DEFAULT = 0;
	public static final int SEND_STATUS_FAIL = 1;
	public static final int SEND_STATUS_SUCCESS = 2;

	public static final int DIALOG_TYPE_DEFAULT = 0;
    public static final int DIALOG_TYPE_CALL_HISTORY = 1;
    public static final int DIALOG_TYPE_IMAGE = 2;
    public static final int DIALOG_TYPE_WEBPAGE = 3;
    public static final int DIALOG_TYPE_IM = 4;
    public static final int DIALOG_TYPE_PACKAGE = 5;
    public static final int DIALOG_TYPE_TIP = 6;
    public static final int DIALOG_TYPE_FOCUS = 7;
    public static final int DIALOG_TYPE_HOTVALUE = 8;
    public static final int DIALOG_TYPE_ATTENTION = 10;
    public static final int DIALOG_TYPE_IMAGE_MESSAGE = 11;
	/**
	 * 从2.2版本开始，招呼盒子里边只能放申请消息，所以根据dialog_type判断聊天消息是否放进招呼盒子里边，
	 * 当dialog_type等于7或12时，放到招呼盒子里边。
	 */
	public static final int DIALOG_TYPE_VOICE_MESSAGE = 12;
	public int is_hide = 0;

	public static HashSet<Integer> dialogTypeSet = new HashSet<Integer>();
	static {
		dialogTypeSet.add(DIALOG_TYPE_DEFAULT);
		dialogTypeSet.add(DIALOG_TYPE_CALL_HISTORY);
		dialogTypeSet.add(DIALOG_TYPE_IMAGE);
		dialogTypeSet.add(DIALOG_TYPE_WEBPAGE);
		dialogTypeSet.add(DIALOG_TYPE_IM);
		dialogTypeSet.add(DIALOG_TYPE_PACKAGE);
		dialogTypeSet.add(DIALOG_TYPE_TIP);
		dialogTypeSet.add(DIALOG_TYPE_FOCUS);
		dialogTypeSet.add(DIALOG_TYPE_HOTVALUE);
		dialogTypeSet.add(DIALOG_TYPE_ATTENTION);
		dialogTypeSet.add(DIALOG_TYPE_IMAGE_MESSAGE);
		dialogTypeSet.add(DIALOG_TYPE_VOICE_MESSAGE);
	}
}
