package me.peiwo.peiwo.model;

import org.json.JSONObject;
public class PWFansModel extends PPBaseModel {
	public int sync_id;
	public String signin_time;
	public String focus_time;
	public int contact_id;
	public int uid;
	public String birthday;
	public String avatar_thumbnail;
	public String slogan;
	public String price;
	public String name;
	public String province;
	public int gender;
	public String avatar;
	public String city;

	public int contact_state;

	public String word;
	public String search_key;

	public String remark;

	public PWFansModel() {
	}

	public PWFansModel(JSONObject o) {
		signin_time = getJsonValue(o, "signin_time");
		signin_time = "null".equalsIgnoreCase(signin_time) ? "1970-01-01 00:00:00": signin_time;
		
		focus_time = getJsonValue(o, "focus_time");
		focus_time = "null".equalsIgnoreCase(focus_time) ? "1970-01-01 00:00:00": focus_time;
		uid = getJsonInt(o, "uid");
		birthday = getJsonValue(o, "birthday");
		avatar_thumbnail = getJsonValue(o, "avatar_thumbnail");
		name = getJsonValue(o, "name");
		gender = getJsonInt(o, "gender");
		avatar = getJsonValue(o, "avatar");
		slogan = getJsonValue(o, "slogan");
	}
}
