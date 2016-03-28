package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/18.
 */
public class AgoraIntentRewardResponseEvent extends AgoraCallEvent {
    public double last_update_time;
    public String ssn;
    public int code;
    public int uid;
    public int msg_type;
    public int money;
    public int transaction;
    public String sid;
    public String msg;
    //public Object payload;
    public int seq;

    public String money_format;


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
        dest.writeInt(this.money);
        dest.writeInt(this.transaction);
        dest.writeString(this.sid);
        dest.writeString(this.msg);
        dest.writeInt(this.seq);
        dest.writeString(this.money_format);
    }

    public AgoraIntentRewardResponseEvent() {
    }

    protected AgoraIntentRewardResponseEvent(Parcel in) {
        super(in);
        this.last_update_time = in.readDouble();
        this.ssn = in.readString();
        this.code = in.readInt();
        this.uid = in.readInt();
        this.msg_type = in.readInt();
        this.money = in.readInt();
        this.transaction = in.readInt();
        this.sid = in.readString();
        this.msg = in.readString();
        this.seq = in.readInt();
        this.money_format = in.readString();
    }

    public static final Creator<AgoraIntentRewardResponseEvent> CREATOR = new Creator<AgoraIntentRewardResponseEvent>() {
        @Override
        public AgoraIntentRewardResponseEvent createFromParcel(Parcel source) {
            return new AgoraIntentRewardResponseEvent(source);
        }

        @Override
        public AgoraIntentRewardResponseEvent[] newArray(int size) {
            return new AgoraIntentRewardResponseEvent[size];
        }
    };
}