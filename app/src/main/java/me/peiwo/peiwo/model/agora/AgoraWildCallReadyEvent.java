package me.peiwo.peiwo.model.agora;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wallace on 16/3/11.
 */
public class AgoraWildCallReadyEvent extends AgoraCallEvent {
    public double last_update_time;
    public int code;
    public int uid;
    public int msg_type;
    public String my_nickname;
    public String ssn;
    public User user;
    public String sid;
    public Data data;
    public int seq;

    public static class Data implements Parcelable {
        public double welcome_percent;
        public int caller_id;
        public int call_id;
        public String channel_id;
        public int video;
        public int tuid;
        public String password;
        public String[] uris;
        public int channel;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeDouble(this.welcome_percent);
            dest.writeInt(this.caller_id);
            dest.writeInt(this.call_id);
            dest.writeString(this.channel_id);
            dest.writeInt(this.video);
            dest.writeInt(this.tuid);
            dest.writeString(this.password);
            dest.writeStringArray(this.uris);
            dest.writeInt(this.channel);
        }

        public Data() {
        }

        protected Data(Parcel in) {
            this.welcome_percent = in.readDouble();
            this.caller_id = in.readInt();
            this.call_id = in.readInt();
            this.channel_id = in.readString();
            this.video = in.readInt();
            this.tuid = in.readInt();
            this.password = in.readString();
            this.uris = in.createStringArray();
            this.channel = in.readInt();
        }

        public static final Parcelable.Creator<Data> CREATOR = new Parcelable.Creator<Data>() {
            public Data createFromParcel(Parcel source) {
                return new Data(source);
            }

            public Data[] newArray(int size) {
                return new Data[size];
            }
        };
    }

    public static class User implements Parcelable {
        public int uid;
        public String[] tags;
        public List<Hint> hint = new ArrayList<>();
        public String pic;
        public String birthday;
        public String nickname;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.uid);
            dest.writeStringArray(this.tags);
            dest.writeTypedList(hint);
            dest.writeString(this.pic);
            dest.writeString(this.birthday);
            dest.writeString(this.nickname);
        }

        public User() {
        }

        protected User(Parcel in) {
            this.uid = in.readInt();
            this.tags = in.createStringArray();
            this.hint = in.createTypedArrayList(Hint.CREATOR);
            this.pic = in.readString();
            this.birthday = in.readString();
            this.nickname = in.readString();
        }

        public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
            public User createFromParcel(Parcel source) {
                return new User(source);
            }

            public User[] newArray(int size) {
                return new User[size];
            }
        };
    }

    public static class Hint implements Parcelable {
        public String msg;
        public int style;
        public String group_name;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.msg);
            dest.writeInt(this.style);
            dest.writeString(this.group_name);
        }

        public Hint() {
        }

        protected Hint(Parcel in) {
            this.msg = in.readString();
            this.style = in.readInt();
            this.group_name = in.readString();
        }

        public static final Parcelable.Creator<Hint> CREATOR = new Parcelable.Creator<Hint>() {
            public Hint createFromParcel(Parcel source) {
                return new Hint(source);
            }

            public Hint[] newArray(int size) {
                return new Hint[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.last_update_time);
        dest.writeInt(this.code);
        dest.writeInt(this.uid);
        dest.writeInt(this.msg_type);
        dest.writeString(this.my_nickname);
        dest.writeString(this.ssn);
        dest.writeParcelable(this.user, 0);
        dest.writeString(this.sid);
        dest.writeParcelable(this.data, 0);
        dest.writeInt(this.seq);
    }

    public AgoraWildCallReadyEvent() {
    }

    protected AgoraWildCallReadyEvent(Parcel in) {
        super(in);
        this.last_update_time = in.readDouble();
        this.code = in.readInt();
        this.uid = in.readInt();
        this.msg_type = in.readInt();
        this.my_nickname = in.readString();
        this.ssn = in.readString();
        this.user = in.readParcelable(User.class.getClassLoader());
        this.sid = in.readString();
        this.data = in.readParcelable(Data.class.getClassLoader());
        this.seq = in.readInt();
    }

    public static final Creator<AgoraWildCallReadyEvent> CREATOR = new Creator<AgoraWildCallReadyEvent>() {
        public AgoraWildCallReadyEvent createFromParcel(Parcel source) {
            return new AgoraWildCallReadyEvent(source);
        }

        public AgoraWildCallReadyEvent[] newArray(int size) {
            return new AgoraWildCallReadyEvent[size];
        }
    };
}
