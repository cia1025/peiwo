package me.peiwo.peiwo.model;

import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.im.MessageUtil;

import me.peiwo.peiwo.util.CustomLog;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.text.TextUtils;

public class TabMsgModel extends PPBaseModel {
    private static final long serialVersionUID = 1L;

    public String content;
    public String dialogs;
    public String update_time;
    public int msg_type;
    public String msg_id;
    public String uid;
    public int message_count = 0;
    public int unread_count = 0;
    public int inside;


    public PWUserModel userModel;
	public TabMsgModel() {

	}
    
    public TabMsgModel(Cursor c) {
        try {
            update_time = c.getString(c.getColumnIndex(PWDBConfig.MessagesTable.UPDATE_TIME));
            content = MessageUtil.resetContent(c.getString(c.getColumnIndex(PWDBConfig.MessagesTable.CONTENT)),
            		c.getInt(c.getColumnIndex(PWDBConfig.MessagesTable.TYPE)));
            msg_type = c.getInt(c.getColumnIndex(PWDBConfig.MessagesTable.MSG_TYPE));
            msg_id = c.getString(c.getColumnIndex(PWDBConfig.MessagesTable.MSG_ID));
            unread_count = c.getInt(c.getColumnIndex(PWDBConfig.MessagesTable.UNREAD_COUNT));
            message_count = c.getInt(c.getColumnIndex(PWDBConfig.MessagesTable.MESSGAE_COUNT));
            uid = c.getString(c.getColumnIndex(PWDBConfig.MessagesTable.UID));
            inside = c.getInt(c.getColumnIndex(PWDBConfig.MessagesTable.INSIDE));
            String suser = c.getString(c.getColumnIndex(PWDBConfig.MessagesTable.USER));
            if (!TextUtils.isEmpty(suser)) {
            	userModel = new PWUserModel(new JSONObject(suser));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        return "update_time is : "+update_time+
                "\n content is : "+content+
                "\n msg_type is : "+msg_type+
                "\n msg_id is : "+msg_id+
                "\n message_count is : "+message_count+
                "\n uid is : "+uid+
                "\n inside is : "+inside;
    }
}
