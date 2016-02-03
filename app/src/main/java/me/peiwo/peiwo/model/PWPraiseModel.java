package me.peiwo.peiwo.model;

import org.json.JSONObject;

public class PWPraiseModel extends PPBaseModel {
	private int mUid;
	private String mName;
	private Long mPraiseTime;
	private String mBirthday;
	private String mAvatarThumbnail;
	private String mSlogan;
	private int mGender;

	public PWPraiseModel() {
	}

	public PWPraiseModel(JSONObject o) {
		mUid = getJsonInt(o, "uid");
		mName = getJsonValue(o, "name");
		mPraiseTime =o.optLong("like_time")*1000;
		mBirthday = getJsonValue(o, "birthday");
		mAvatarThumbnail = getJsonValue(o, "avatar");
		mSlogan = getJsonValue(o, "slogan");
		mGender = getJsonInt(o, "gender");
	}

	public int getUid() {
		return mUid;
	}

	public void setUid(int uid) {
		mUid = uid;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public Long getPraiseTime() {
		return mPraiseTime;
	}

	public void setPraiseTime(Long praiseTime) {
		mPraiseTime = praiseTime;
	}

	public String getBirthday() {
		return mBirthday;
	}

	public void setBirthday(String birthday) {
		mBirthday = birthday;
	}

	public String getAvatarThumbnail() {
		return mAvatarThumbnail;
	}

	public void setAvatarThumbnail(String avatarThumbnail) {
		mAvatarThumbnail = avatarThumbnail;
	}

	public String getSlogan() {
		return mSlogan;
	}

	public void setSlogan(String slogan) {
		mSlogan = slogan;
	}

	public int getGender() {
		return mGender;
	}

	public void setmGender(int gender) {
		mGender = gender;
	}

}
