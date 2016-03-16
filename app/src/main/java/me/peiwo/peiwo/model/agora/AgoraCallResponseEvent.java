package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/7.
 */
public class AgoraCallResponseEvent extends AgoraCallEvent {
    public double last_update_time;
    public String ssn;
    public int code;
    public int uid;
    public int msg_type;
    public int csn;
    public AgoraUser user;
    public String sid;
    public String msg;
    public String payload;
    public int seq;
    public int call_id;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.last_update_time);
        dest.writeString(this.ssn);
        dest.writeInt(this.code);
        dest.writeInt(this.uid);
        dest.writeInt(this.msg_type);
        dest.writeInt(this.csn);
        dest.writeParcelable(this.user, 0);
        dest.writeString(this.sid);
        dest.writeString(this.msg);
        dest.writeString(this.payload);
        dest.writeInt(this.seq);
        dest.writeInt(this.call_id);
    }

    public AgoraCallResponseEvent() {
    }

    protected AgoraCallResponseEvent(Parcel in) {
        super(in);
        this.last_update_time = in.readDouble();
        this.ssn = in.readString();
        this.code = in.readInt();
        this.uid = in.readInt();
        this.msg_type = in.readInt();
        this.csn = in.readInt();
        this.user = in.readParcelable(AgoraUser.class.getClassLoader());
        this.sid = in.readString();
        this.msg = in.readString();
        this.payload = in.readString();
        this.seq = in.readInt();
        this.call_id = in.readInt();
    }

    public static final Creator<AgoraCallResponseEvent> CREATOR = new Creator<AgoraCallResponseEvent>() {
        public AgoraCallResponseEvent createFromParcel(Parcel source) {
            return new AgoraCallResponseEvent(source);
        }

        public AgoraCallResponseEvent[] newArray(int size) {
            return new AgoraCallResponseEvent[size];
        }
    };
}
