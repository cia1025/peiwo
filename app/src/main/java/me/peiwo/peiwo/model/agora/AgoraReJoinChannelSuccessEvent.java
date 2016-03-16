package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/8.
 */
public class AgoraReJoinChannelSuccessEvent extends AgoraCallEvent {
    public String channel;
    public int uid;
    public int elapsed;

    public AgoraReJoinChannelSuccessEvent(String channel, int uid, int elapsed) {
        this.channel = channel;
        this.uid = uid;
        this.elapsed = elapsed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.channel);
        dest.writeInt(this.uid);
        dest.writeInt(this.elapsed);
    }

    public AgoraReJoinChannelSuccessEvent() {
    }

    protected AgoraReJoinChannelSuccessEvent(Parcel in) {
        super(in);
        this.channel = in.readString();
        this.uid = in.readInt();
        this.elapsed = in.readInt();
    }

    public static final Creator<AgoraReJoinChannelSuccessEvent> CREATOR = new Creator<AgoraReJoinChannelSuccessEvent>() {
        public AgoraReJoinChannelSuccessEvent createFromParcel(Parcel source) {
            return new AgoraReJoinChannelSuccessEvent(source);
        }

        public AgoraReJoinChannelSuccessEvent[] newArray(int size) {
            return new AgoraReJoinChannelSuccessEvent[size];
        }
    };
}
