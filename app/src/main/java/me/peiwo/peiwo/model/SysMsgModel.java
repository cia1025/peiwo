package me.peiwo.peiwo.model;

import me.peiwo.peiwo.constans.PWDBConfig;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.text.TextUtils;

/**
 * Created by Dong Fuhai on 2014-07-21 14:40.
 * 
 * @modify:
 */
public class SysMsgModel extends PPBaseModel {
	// content: "欢迎加入陪我，在这里，您可以快速约到美女陪您电话聊天娱乐！现在赠送您1元红包，赶紧去找个人陪你吧！",
	// dialog_id: 205,
	// update_time: "2014-07-21 14:09:54",
	// user: {
	// province: null,
	// city: null,
	// slogan: null,
	// name: "system",
	// gender: 0,
	// price: 0.2,
	// birthday: null,
	// avatar: null,
	// images: [ ],
	// uid: 1
	public String content;
	public int dialog_id;
	public String update_time;
	public User user;
	public int dialog_type;
	public String redbag_title;
	public String redbag_content;
	public String redbag_extra;
	public String icon_url;
	public String url;
	public int readStatus;
	public int id;
	public String icon_name;

	public SysMsgModel(JSONObject o) {
		try {
			content = getJsonValue(o, "content");
			dialog_id = getJsonInt(o, "dialog_id");
			update_time = getJsonValue(o, "update_time");
			user = new User(o.getJSONObject("user"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public SysMsgModel(Cursor c) {
		content = c.getString(c.getColumnIndex(PWDBConfig.DialogsTable.CONTENT));
		dialog_id = c.getInt(c.getColumnIndex(PWDBConfig.DialogsTable.DIALOG_ID));
		update_time = c.getString(c.getColumnIndex(PWDBConfig.DialogsTable.UPDATE_TIME));
		dialog_type = c.getInt(c.getColumnIndex(PWDBConfig.DialogsTable.DIALOG_TYPE));
		readStatus = c.getInt(c.getColumnIndex(PWDBConfig.DialogsTable.READ_STATUS));
		id = c.getInt(c.getColumnIndex(PWDBConfig.DialogsTable.ID));
		String details = c.getString(c.getColumnIndex(PWDBConfig.DialogsTable.DETAILS));
		if(!TextUtils.isEmpty(details)){
			setDetails(details);
		}
	}

	private void setDetails(String details) {
		try {
			JSONObject obj = new JSONObject(details);
			redbag_title = getJsonValue(obj, "title");
			icon_url = getJsonValue(obj, "icon_url");
			url = getJsonValue(obj, "url");
			redbag_content = getJsonValue(obj, "msg");
			redbag_extra = getJsonValue(obj, "extra");
			icon_name = getJsonValue(obj, "icon_name");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static class User extends PPBaseModel {
		public String name;

		public User(JSONObject o) {
			name = getJsonValue(o, "name");
		}
	}
}
