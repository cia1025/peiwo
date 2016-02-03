package me.peiwo.peiwo.model;

import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.im.MessageUtil;

import me.peiwo.peiwo.util.CustomLog;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.text.TextUtils;

public class SayHelloModel extends PPBaseModel {
    private static final long serialVersionUID = 1L;

    public String content;
    public String dialogs;
    public String update_time;
    public int msg_type;
    public int msg_id;
    public int uid;
    public int message_count = 0;
    public int unread_count = 0;
    public String diaglogDetails;
    public int dialogBadge = 0;
    public int from = 0;
    public PWUserModel userModel;
    public VoiceModel voice;

	public SayHelloModel() {
	}
    
    public SayHelloModel(Cursor c) {
        try {
            update_time = c.getString(c.getColumnIndex(PWDBConfig.MessagesTable.UPDATE_TIME));
            content = MessageUtil.resetContent(c.getString(c.getColumnIndex(PWDBConfig.MessagesTable.CONTENT)),
                    c.getInt(c.getColumnIndex(PWDBConfig.MessagesTable.TYPE)));
            msg_type = c.getInt(c.getColumnIndex(PWDBConfig.MessagesTable.MSG_TYPE));
            msg_id = c.getInt(c.getColumnIndex(PWDBConfig.MessagesTable.MSG_ID));
            unread_count = c.getInt(c.getColumnIndex(PWDBConfig.MessagesTable.UNREAD_COUNT));
            message_count = c.getInt(c.getColumnIndex(PWDBConfig.MessagesTable.MESSGAE_COUNT));
            uid = c.getInt(c.getColumnIndex(PWDBConfig.MessagesTable.UID));
            String suser = c.getString(c.getColumnIndex(PWDBConfig.MessagesTable.USER));
            if (!TextUtils.isEmpty(suser)) {
            	userModel = new PWUserModel(new JSONObject(suser));
                uid = userModel.uid;
            }
            fillDialogData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private void fillDialogData()throws JSONException {
    	MsgDBCenterService dbCenterService = MsgDBCenterService.getInstance();
    	String dialogContent = dbCenterService.getDetailsByUidDesc(uid);
        CustomLog.d("dialogContent == "+dialogContent);
    	if (!TextUtils.isEmpty(dialogContent)) {
        	JSONObject o = new JSONObject(dialogContent);
            voice = new VoiceModel(o);
        	from = getJsonInt(o, "from");
    	}
    }
    
}
