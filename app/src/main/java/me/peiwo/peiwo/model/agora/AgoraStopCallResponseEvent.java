package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/8.
 */
public class AgoraStopCallResponseEvent extends AgoraCallEvent {
    public double last_update_time;
    public int csn;
    public int uid;
    public int msg_type;
    public String ssn;
    public String sid;
    public int seq;
    public String payload;
    public int channel;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.last_update_time);
        dest.writeInt(this.csn);
        dest.writeInt(this.uid);
        dest.writeInt(this.msg_type);
        dest.writeString(this.ssn);
        dest.writeString(this.sid);
        dest.writeInt(this.seq);
        dest.writeString(this.payload);
        dest.writeInt(this.channel);
    }

    public AgoraStopCallResponseEvent() {
    }

    protected AgoraStopCallResponseEvent(Parcel in) {
        super(in);
        this.last_update_time = in.readDouble();
        this.csn = in.readInt();
        this.uid = in.readInt();
        this.msg_type = in.readInt();
        this.ssn = in.readString();
        this.sid = in.readString();
        this.seq = in.readInt();
        this.payload = in.readString();
        this.channel = in.readInt();
    }

    public static final Creator<AgoraStopCallResponseEvent> CREATOR = new Creator<AgoraStopCallResponseEvent>() {
        public AgoraStopCallResponseEvent createFromParcel(Parcel source) {
            return new AgoraStopCallResponseEvent(source);
        }

        public AgoraStopCallResponseEvent[] newArray(int size) {
            return new AgoraStopCallResponseEvent[size];
        }
    };
}
