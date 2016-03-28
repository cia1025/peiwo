package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/18.
 */
public class AgoraRewardedEvent extends AgoraCallEvent {
    public double last_update_time;
    public int uid;
    public int msg_type;
    public int money;
    public String ssn;
    public int fuid;
    public String sid;
    public String msg;
    public int balance;
    public int seq;


    //local
    public String remote_avatar;
    public String nick_name;
    public String money_format;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.last_update_time);
        dest.writeInt(this.uid);
        dest.writeInt(this.msg_type);
        dest.writeInt(this.money);
        dest.writeString(this.ssn);
        dest.writeInt(this.fuid);
        dest.writeString(this.sid);
        dest.writeString(this.msg);
        dest.writeInt(this.balance);
        dest.writeInt(this.seq);
        dest.writeString(this.remote_avatar);
        dest.writeString(this.nick_name);
        dest.writeString(this.money_format);
    }

    public AgoraRewardedEvent() {
    }

    protected AgoraRewardedEvent(Parcel in) {
        super(in);
        this.last_update_time = in.readDouble();
        this.uid = in.readInt();
        this.msg_type = in.readInt();
        this.money = in.readInt();
        this.ssn = in.readString();
        this.fuid = in.readInt();
        this.sid = in.readString();
        this.msg = in.readString();
        this.balance = in.readInt();
        this.seq = in.readInt();
        this.remote_avatar = in.readString();
        this.nick_name = in.readString();
        this.money_format = in.readString();
    }

    public static final Creator<AgoraRewardedEvent> CREATOR = new Creator<AgoraRewardedEvent>() {
        @Override
        public AgoraRewardedEvent createFromParcel(Parcel source) {
            return new AgoraRewardedEvent(source);
        }

        @Override
        public AgoraRewardedEvent[] newArray(int size) {
            return new AgoraRewardedEvent[size];
        }
    };
}
