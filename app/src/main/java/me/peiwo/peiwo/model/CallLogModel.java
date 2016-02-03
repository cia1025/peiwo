package me.peiwo.peiwo.model;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

public class CallLogModel extends PPBaseModel implements Parcelable {
	private static final long serialVersionUID = 1L;
//	 state: 0, 
//     uid: 425910363, 
//     history_type: 1, 
//     duration: 8, 
//     update_time: "2014-07-07 19:16:39", 
//     user: {
//           avatar: "http://static.peiwo.me/image/31/2233789479835504664.jpg", 
//           avatar_thumbnail: "http://static.peiwo.me/image/31/2233789479835504665.jpg", 
//           name: "AlwaysOnline"
//     }, 
//     history_id: 1136
	
	public int state;
	public int uid;
	public int history_type;
	public int duration;
	public String update_time;
	public int history_id;
	public User user;
	
	public CallLogModel(JSONObject o) {
		try {
			state = getJsonInt(o, "state");
			uid = getJsonInt(o, "uid");
			history_type = getJsonInt(o, "history_type");
			duration = getJsonInt(o, "duration");
			history_id = getJsonInt(o, "history_id");
			update_time = getJsonValue(o, "update_time");
			user = new User(o.getJSONObject("user"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public static class User extends PPBaseModel implements Parcelable {
		private static final long serialVersionUID = 1L;
		public String avatar;
		public String avatar_thumbnail;
		public String name;
        public int uid;
		public User(JSONObject oo){
			avatar = getJsonValue(oo, "avatar");
			avatar_thumbnail = getJsonValue(oo, "avatar_thumbnail");
			name = getJsonValue(oo, "name");
            uid = getJsonInt(oo, "uid");
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(this.avatar);
			dest.writeString(this.avatar_thumbnail);
			dest.writeString(this.name);
			dest.writeInt(this.uid);
		}

		protected User(Parcel in) {
			this.avatar = in.readString();
			this.avatar_thumbnail = in.readString();
			this.name = in.readString();
			this.uid = in.readInt();
		}

		public static final Creator<User> CREATOR = new Creator<User>() {
			public User createFromParcel(Parcel source) {
				return new User(source);
			}

			public User[] newArray(int size) {
				return new User[size];
			}
		};
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.state);
		dest.writeInt(this.uid);
		dest.writeInt(this.history_type);
		dest.writeInt(this.duration);
		dest.writeString(this.update_time);
		dest.writeInt(this.history_id);
		dest.writeParcelable(this.user, flags);
	}

	protected CallLogModel(Parcel in) {
		this.state = in.readInt();
		this.uid = in.readInt();
		this.history_type = in.readInt();
		this.duration = in.readInt();
		this.update_time = in.readString();
		this.history_id = in.readInt();
		this.user = in.readParcelable(User.class.getClassLoader());
	}

	public static final Parcelable.Creator<CallLogModel> CREATOR = new Parcelable.Creator<CallLogModel>() {
		public CallLogModel createFromParcel(Parcel source) {
			return new CallLogModel(source);
		}

		public CallLogModel[] newArray(int size) {
			return new CallLogModel[size];
		}
	};
}
