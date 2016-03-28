package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/18.
 */
public class AgoraPayRewardResponseEvent extends AgoraCallEvent {
    public double last_update_time;
    public String ssn;
    public int code;
    public int uid;
    public int msg_type;
    public int money;
    public int transaction;
    public int tuid;
    public String sid;
    public int balance;
    //public Object payload;
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
        dest.writeInt(this.money);
        dest.writeInt(this.transaction);
        dest.writeInt(this.tuid);
        dest.writeString(this.sid);
        dest.writeInt(this.balance);
        dest.writeInt(this.seq);
    }

    public AgoraPayRewardResponseEvent() {
    }

    protected AgoraPayRewardResponseEvent(Parcel in) {
        super(in);
        this.last_update_time = in.readDouble();
        this.ssn = in.readString();
        this.code = in.readInt();
        this.uid = in.readInt();
        this.msg_type = in.readInt();
        this.money = in.readInt();
        this.transaction = in.readInt();
        this.tuid = in.readInt();
        this.sid = in.readString();
        this.balance = in.readInt();
        this.seq = in.readInt();
    }

    public static final Creator<AgoraPayRewardResponseEvent> CREATOR = new Creator<AgoraPayRewardResponseEvent>() {
        @Override
        public AgoraPayRewardResponseEvent createFromParcel(Parcel source) {
            return new AgoraPayRewardResponseEvent(source);
        }

        @Override
        public AgoraPayRewardResponseEvent[] newArray(int size) {
            return new AgoraPayRewardResponseEvent[size];
        }
    };
}
