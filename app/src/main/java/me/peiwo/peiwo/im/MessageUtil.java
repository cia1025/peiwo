package me.peiwo.peiwo.im;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import me.peiwo.peiwo.constans.PWActionConfig;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.util.CustomLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;

public class MessageUtil {
//    PW_DIALOG_NORMAL = 0,           /* 普通富文本 */
//    PW_DIALOG_CALL_HISTORY = 1,     /* 通话记录 */
//    PW_DIALOG_IMAGE = 2,            /* 图片 */
//    PW_DIALOG_WEBPAGE = 3,          /* 网页 */
//    PW_DIALOG_MESSAGE = 4,          /* 短消息 */
//    PW_DIALOG_PACKAGE = 5,          /* 红包 */
//    PW_DIALOG_TIP = 6               /* 系统提醒: 例, 敏感词汇/扣费提醒... */
//    PW_DIALOG_FOCUS = 7              /* 关注 */

    public static HashMap<Timer, Long> sendMessageMap = new HashMap<Timer, Long>();
    public static HashMap<Timer, String> resendMessageMap = new HashMap<Timer, String>();

    public static long insertMessage(Context mContext, MessageModel model, PWUserModel userModel) {
        return MsgDBCenterService.getInstance().insertDialogsWithMessages(mContext, model, userModel);
    }

    public static int updateMessage(Context mContext, long id, ContentValues valuse) {
        int count = 0;
        if (valuse == null) return count;
        try {
            String where = PWDBConfig.DialogsTable.ID + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(id)};
            count = mContext.getContentResolver().update(PWDBConfig.DialogsTable.CONTENT_URI, valuse, where, selectionArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public static void updateMessageSuccess(Context mContext, JSONObject command) {
//	    {
//        "msg_id": 172127,
//        "payload": 15,
//        "sid": 155680,
//        "tuid": 1003,
//        "update_time": "2015-01-20 15:47:31",
//        "dialog_id": 325631,
//        "msg_type": 195
//      }
        try {
            long dbId = command.getLong("payload");
            removeSendListMessage(dbId);
            updateMessageSuccessToDb(mContext, (int) dbId, command.getString("msg_id"), command.getInt("dialog_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void updateMessageSuccessToDb(Context mContext, int dbId, String msgId, int dialogId) {
        ContentValues values = new ContentValues();
        values.put(PWDBConfig.DialogsTable.MSG_ID, msgId);
        values.put(PWDBConfig.DialogsTable.DIALOG_ID, dialogId);
        values.put(PWDBConfig.DialogsTable.SEND_STATUS, MessageModel.SEND_STATUS_DEFAULT);
        String where = PWDBConfig.DialogsTable.ID + " = ?";
        String selectionArgs[] = new String[]{String.valueOf(dbId)};
        mContext.getContentResolver().update(PWDBConfig.DialogsTable.CONTENT_URI, values, where, selectionArgs);
    }

    public static void updateMessageFaile(Context mContext, JSONObject command) {
//        TIMEOUT = -1
//        OK = 0
//        INSUFFICIENT_BALANCE = 1   // 余额不足
//        NO_SUCH_USER = 2           // 不存在用户
//        NOT_CHATTED_YET = 3        // 未通话，无权限
//        NO_SUCH_CONTACT = 4        // 不是好友
//        USER_BLOCKED = 5           // 拉黑
//        UNKNOWN_ERROR = 6

		/*
		 * IMSendFailMessage = {
		 *      "msg_type" = 0x0c2;
		 *      "tuid" = xxx;
		 *      "fail_type" =
		 *      "payload" = xxx 客户端自定义id;
		 *      "extra": {
		 *          im_price:"单价"
		 *          fail_msg:"失败消息"
		 *          icon_url:"必要的其他信息"
		 *          icon_name:"必要的其他信息"
		 *      }
		 * }
		 */

        try {
            int errorCode = 0;
            if (command.has("fail_type")) {
                errorCode = command.getInt("fail_type");
            }
            long dbId = command.getLong("payload");
            removeSendListMessage(dbId);
            updateMessageFaileToDb(mContext, errorCode, dbId);
//        	if (errorCode == 1) {
//        		//余额不足，需要界面提示
//        		UserManager.updateMoney(mContext, "0");
//        	}
            Intent intent = new Intent(PWActionConfig.ACTION_SEND_MSG_FAILE);
            intent.putExtra("error_code", errorCode);
            mContext.sendBroadcast(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void updateMessageFaileToDb(Context mContext, int errorCode, long dbId) {
        ContentValues values = new ContentValues();
        values.put(PWDBConfig.DialogsTable.SEND_STATUS, MessageModel.SEND_STATUS_FAIL);
        values.put(PWDBConfig.DialogsTable.ERROR_CODE, errorCode);
        String where = PWDBConfig.DialogsTable.ID + " = ?";
        String selectionArgs[] = new String[]{String.valueOf(dbId)};
        mContext.getContentResolver().update(PWDBConfig.DialogsTable.CONTENT_URI, values, where, selectionArgs);
    }

    private static void removeSendListMessage(long dbID) {
        for (Timer timer : sendMessageMap.keySet()) {
            long sn = sendMessageMap.get(timer);
            if (sn == dbID) {
                sendMessageMap.remove(timer);
                timer.cancel();
                break;
            }
        }
    }

    public static void removeResendListMessage(String payload) {
        for (Timer timer : resendMessageMap.keySet()) {
            String sn = resendMessageMap.get(timer);
            CustomLog.i("WILLS clear payload sn: " + sn + ":" + payload);
            if (sn.equals(payload)) {
                CustomLog.i("WILLS clear payload sn cleared: " + sn + ":" + payload);
                resendMessageMap.remove(timer);
                timer.cancel();
                break;
            }
        }
    }

    public static void receivesMessage(Context mContext, JSONObject command) {
        try {
            JSONArray array = command.getJSONArray("data");
            if (array != null && array.length() > 0) {
                MsgDBCenterService.getInstance().insertDialogsWithMessages(array, false, -1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void updateRedBagStatus(int id) {
        MsgDBCenterService.getInstance().updateRedBagStatus(id);
    }

    public static void updateMessageContent(int uid, String redbag_extra) {
        MsgDBCenterService.getInstance().updateMessageContent(uid, redbag_extra);
    }

    public static void updateDialogDetails(long dbId, String details) {
        MsgDBCenterService.getInstance().updateDialogDetails(dbId, details);
    }

    public static String resetContent(String content, int type) {
        String newContent = content;
        if (!TextUtils.isEmpty(content) && content.contains("]")) {
            int patIndex = content.lastIndexOf("]") + 1;
            String pattern = content.substring(0, patIndex);
            if ("[取消呼叫]".equals(pattern)) {
                newContent = "[语音通话]" + (type == 0 ? "已取消" : "未接听");
            } else if ("[通话]".equals(pattern)) {
                newContent = "[语音通话]" + content.substring(patIndex, content.length());
            } else if ("[对方忙]".equals(pattern)) {
                newContent = "[语音通话]" + (type == 0 ? "对方忙" : "未接听");
            } else if ("[无人接听]".equals(pattern)) {
                newContent = "[语音通话]" + (type == 0 ? "已取消" : "未接听");
            } else if ("[拒绝通话]".equals(pattern)) {
                newContent = "[语音通话]" + (type == 0 ? "对方已拒绝" : "未接听");
            }
        } else if (!TextUtils.isEmpty(content) && content.startsWith("{")
                && content.endsWith("}")) {
            newContent = "[动态表情]";
//    		for (String key : ExpressionUtil.getInstance().gifFaceMap.keySet()) {
//        		if (content.contains(key)) {
//        			newContent = "[动态表情]";
//        			break;
//        		}
//			}
        }
        return newContent;
    }
}
