package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/11.
 */
public class RTCWildCallStateEvent extends AgoraCallEvent {

    public int type;//0--显示自己网络状态，WebRTC状态， //1--显示自己和对方的网络好坏  //2--异常情况需要挂断电话
    public boolean nTCPState;//true连接中，false断开中
    public int nWebRTCState;

    public int heart_lost_count;
    public int remote_user_state;

    public RTCWildCallStateEvent(int type, boolean nTCPState, int nWebRTCState, int heart_lost_count, int remote_user_state) {
        this.type = type;
        this.nTCPState = nTCPState;
        this.nWebRTCState = nWebRTCState;
        this.heart_lost_count = heart_lost_count;
        this.remote_user_state = remote_user_state;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.type);
        dest.writeByte(nTCPState ? (byte) 1 : (byte) 0);
        dest.writeInt(this.nWebRTCState);
        dest.writeInt(this.heart_lost_count);
        dest.writeInt(this.remote_user_state);
    }

    protected RTCWildCallStateEvent(Parcel in) {
        super(in);
        this.type = in.readInt();
        this.nTCPState = in.readByte() != 0;
        this.nWebRTCState = in.readInt();
        this.heart_lost_count = in.readInt();
        this.remote_user_state = in.readInt();
    }

    public static final Creator<RTCWildCallStateEvent> CREATOR = new Creator<RTCWildCallStateEvent>() {
        public RTCWildCallStateEvent createFromParcel(Parcel source) {
            return new RTCWildCallStateEvent(source);
        }

        public RTCWildCallStateEvent[] newArray(int size) {
            return new RTCWildCallStateEvent[size];
        }
    };
}
