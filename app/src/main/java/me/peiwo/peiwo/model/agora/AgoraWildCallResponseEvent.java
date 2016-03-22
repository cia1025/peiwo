package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/11.
 */
public class AgoraWildCallResponseEvent extends AgoraCallEvent {

    public double last_update_time;
    public String ssn;
    public int code;
    public int uid;
    public int msg_type;
    public int csn;
    public String sid;
    public int seq;


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
        dest.writeString(this.sid);
        dest.writeInt(this.seq);
    }

    public AgoraWildCallResponseEvent() {
    }

    protected AgoraWildCallResponseEvent(Parcel in) {
        super(in);
        this.last_update_time = in.readDouble();
        this.ssn = in.readString();
        this.code = in.readInt();
        this.uid = in.readInt();
        this.msg_type = in.readInt();
        this.csn = in.readInt();
        this.sid = in.readString();
        this.seq = in.readInt();
    }

    public static final Creator<AgoraWildCallResponseEvent> CREATOR = new Creator<AgoraWildCallResponseEvent>() {
        public AgoraWildCallResponseEvent createFromParcel(Parcel source) {
            return new AgoraWildCallResponseEvent(source);
        }

        public AgoraWildCallResponseEvent[] newArray(int size) {
            return new AgoraWildCallResponseEvent[size];
        }
    };
}
