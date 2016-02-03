package me.peiwo.peiwo.model;

import org.json.JSONObject;

public class TrendNoticeModel extends PPBaseModel {

	public String image_url;
	public String name;
	public String time;
	public String pub_image_url;
	public int uid;
	public String id;

	public TrendNoticeModel(JSONObject dataJson) {
		if (dataJson == null)
			return;
		pub_image_url = dataJson.optJSONObject("pub").optString("image");
		JSONObject likerObject = dataJson.optJSONObject("liker");
		uid = likerObject.optInt("uid");
		name = likerObject.optString("name");
		image_url = likerObject.optString("avatar_thumbnail");
		time = likerObject.optString("like_time");
		id = dataJson.optJSONObject("pub").optString("id");
	}
}
